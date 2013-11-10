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
