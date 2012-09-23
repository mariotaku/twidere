package org.mariotaku.twidere.preference;

import static android.os.Environment.getExternalStorageDirectory;

import java.io.File;
import java.io.FileFilter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;

public class ClearCachePreference extends Preference implements Constants, OnPreferenceClickListener {

	private ClearCacheTask mClearCacheTask;

	public ClearCachePreference(final Context context) {
		this(context, null);
	}

	public ClearCachePreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ClearCachePreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		if (mClearCacheTask == null || mClearCacheTask.getStatus() != Status.RUNNING) {
			mClearCacheTask = new ClearCacheTask(getContext());
			mClearCacheTask.execute();
		}
		return true;
	}

	static class ClearCacheTask extends AsyncTask<Void, Void, Void> {

		private final Context context;
		private final ProgressDialog mProgress;

		public ClearCacheTask(final Context context) {
			this.context = context;
			mProgress = new ProgressDialog(context);
		}

		@Override
		protected Void doInBackground(final Void... args) {
			if (context == null) return null;
			final File external_cache_dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor
					.getExternalCacheDir(context) : getExternalStorageDirectory() != null ? new File(
					getExternalStorageDirectory().getPath() + "/Android/data/" + context.getPackageName() + "/cache/")
					: null;

			if (external_cache_dir != null) {
				for (final File file : external_cache_dir.listFiles((FileFilter) null)) {
					deleteRecursive(file);
				}
			}
			final File internal_cache_dir = context.getCacheDir();
			if (internal_cache_dir != null) {
				for (final File file : internal_cache_dir.listFiles((FileFilter) null)) {
					deleteRecursive(file);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			super.onPostExecute(result);
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mProgress.setMessage(context.getString(R.string.please_wait));
			mProgress.setCancelable(false);
			mProgress.show();
			super.onPreExecute();
		}

		private void deleteRecursive(final File f) {
			if (f.isDirectory()) {
				for (final File c : f.listFiles()) {
					deleteRecursive(c);
				}
			}
			f.delete();
		}

	}

}
