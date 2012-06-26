package org.mariotaku.twidere.app;

import java.io.File;
import java.io.FileFilter;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.util.ManagedAsyncTask;
import org.mariotaku.twidere.util.ProfileImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Environment;

public class TwidereApplication extends Application implements Constants {

	private ProfileImageLoader mProfileImageLoader;
	private AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager();
	private ClearCacheTask mClearCacheTask;

	public void clearCache() {
		if (mClearCacheTask == null || mClearCacheTask.getStatus() == Status.FINISHED) {
			mClearCacheTask = new ClearCacheTask(this, getAsyncTaskManager());
			mClearCacheTask.execute();
		}
	}

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public ProfileImageLoader getProfileImageLoader() {
		return mProfileImageLoader;
	}

	public ServiceInterface getServiceInterface() {
		return ServiceInterface.getInstance(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mProfileImageLoader = new ProfileImageLoader(this, R.drawable.ic_profile_image_default, getResources()
				.getDimensionPixelSize(R.dimen.profile_image_size));
	}

	@Override
	public void onLowMemory() {
		mProfileImageLoader.clearMemoryCache();
		super.onLowMemory();
	}

	private static class ClearCacheTask extends ManagedAsyncTask<Void, Void, Void> {

		private final Context context;

		public ClearCacheTask(Context context, AsyncTaskManager manager) {
			super(context, manager);
			this.context = context;
		}

		@Override
		protected Void doInBackground(Void... args) {
			final File external_cache_dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor
					.getExternalCacheDir(context) : Environment.getExternalStorageDirectory() != null ? new File(
					Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + context.getPackageName()
							+ "/cache/") : null;

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

		private void deleteRecursive(File f) {
			if (f.isDirectory()) {
				for (final File c : f.listFiles()) {
					deleteRecursive(c);
				}
			}
			f.delete();
		}

	}
}
