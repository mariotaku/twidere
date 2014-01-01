/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.task;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ImportSettingsTask extends AsyncTask<Void, Void, Boolean> {

	private final Context mContext;
	private final File mFile;

	ImportSettingsTask(final Context context, final File file) {
		mContext = context;
		mFile = file;
	}

	@Override
	protected Boolean doInBackground(final Void... params) {
		if (mFile == null || !mFile.exists()) return false;
		try {
			final ZipFile zip = new ZipFile(mFile);
			zip.close();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

}
