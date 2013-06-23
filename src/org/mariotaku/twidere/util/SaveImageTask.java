/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getImageMimeType;

import java.io.File;
import java.io.IOException;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

public class SaveImageTask extends AsyncTask<Void, Void, File> implements Constants {

	private static final String PROGRESS_FRAGMENT_TAG = "progress";

	private final File src;
	private final FragmentActivity activity;

	public SaveImageTask(final FragmentActivity activity, final File src) {
		this.activity = activity;
		this.src = src;
	}

	@Override
	protected File doInBackground(final Void... args) {
		if (src == null) return null;
		return saveImage(activity, src);
	}

	@Override
	protected void onCancelled() {
		final FragmentManager fm = activity.getSupportFragmentManager();
		final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
		if (fragment != null && fragment.isVisible()) {
			fragment.dismiss();
		}
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(final File result) {
		final FragmentManager fm = activity.getSupportFragmentManager();
		final DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(PROGRESS_FRAGMENT_TAG);
		if (fragment != null) {
			fragment.dismiss();
		}
		super.onPostExecute(result);
		if (result != null && result.exists()) {
			Crouton.showText(activity, activity.getString(R.string.file_saved_to, result.getPath()),
					CroutonStyle.CONFIRM);
		} else {
			Crouton.showText(activity, R.string.error_occurred, CroutonStyle.ALERT);
		}
	}

	@Override
	protected void onPreExecute() {
		final DialogFragment fragment = new ProgressDialogFragment();
		fragment.setCancelable(false);
		fragment.show(activity.getSupportFragmentManager(), PROGRESS_FRAGMENT_TAG);
		super.onPreExecute();
	}

	public static File saveImage(final Context context, final File image_file) {
		if (context == null && image_file == null) return null;
		try {
			final String name = image_file.getName();
			if (isEmpty(name)) return null;
			final String mime_type = getImageMimeType(image_file);
			final MimeTypeMap map = MimeTypeMap.getSingleton();
			final String extension = map.getExtensionFromMimeType(mime_type);
			if (extension == null) return null;
			final String name_to_save = name.indexOf(".") != -1 ? name : name + "." + extension;
			final File pub_dir = EnvironmentAccessor
					.getExternalStoragePublicDirectory(EnvironmentAccessor.DIRECTORY_PICTURES);
			if (pub_dir == null) return null;
			final File save_dir = new File(pub_dir, "Twidere");
			if (!save_dir.isDirectory() && !save_dir.mkdirs()) return null;
			final File save_file = new File(save_dir, name_to_save);
			FileUtils.copyFile(image_file, save_file);
			if (save_file != null && mime_type != null) {
				MediaScannerConnection.scanFile(context, new String[] { save_file.getPath() },
						new String[] { mime_type }, null);
			}
			return save_file;
		} catch (final IOException e) {
			Log.w(LOGTAG, "Failed to save file", e);
			return null;
		}
	}

}
