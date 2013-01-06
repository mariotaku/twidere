package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getImageMimeType;

import java.io.File;
import java.io.IOException;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

public class SaveImageTask extends AsyncTask<Void, Void, File> {

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
			Toast.makeText(activity, activity.getString(R.string.file_saved_to, result.getPath()), Toast.LENGTH_SHORT)
					.show();
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
		final String mime_type;
		try {
			final String name = image_file.getName();
			if (isEmpty(name)) return null;
			mime_type = getImageMimeType(image_file);
			final MimeTypeMap map = MimeTypeMap.getSingleton();
			final String extension = map.getExtensionFromMimeType(mime_type);
			if (extension == null) return null;
			final String name_to_save = name.indexOf(".") != -1 ? name : name + "." + extension;
			final File pub_dir = EnvironmentAccessor
					.getExternalStoragePublicDirectory(EnvironmentAccessor.DIRECTORY_PICTURES);
			if (pub_dir == null) return null;
			final File save_dir = new File(pub_dir, "Twidere");
			if (!save_dir.mkdirs() || !save_dir.isDirectory() && !save_dir.mkdirs()) return null;
			final File save_file = new File(save_dir, name_to_save);
			FileUtils.copyFileToDirectory(image_file, save_file);
			if (save_file != null && mime_type != null) {
				MediaScannerConnection.scanFile(context, new String[] { save_file.getPath() },
						new String[] { mime_type }, null);
			}
			return save_file;
		} catch (final IOException e) {
			return null;
		}
	}

}