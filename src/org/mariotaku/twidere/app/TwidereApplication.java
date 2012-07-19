/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.app;

import static android.os.Environment.getExternalStorageDirectory;

import java.io.File;
import java.io.FileFilter;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ManagedAsyncTask;
import org.mariotaku.twidere.util.MemCache;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask.Status;
import android.os.Build;

@ReportsCrashes(formKey = "dEcwVVBoTDE0RXZaczFiMUxuek43WGc6MQ")
public class TwidereApplication extends Application implements Constants {

	private LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private ClearCacheTask mClearCacheTask;
	private MemCache mMemCache;
	private SharedPreferences mPreferences;

	public void clearCache() {
		if (mClearCacheTask == null || mClearCacheTask.getStatus() == Status.FINISHED) {
			mClearCacheTask = new ClearCacheTask(this, getAsyncTaskManager());
			mClearCacheTask.execute();
		}
	}

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager == null) {
			mAsyncTaskManager = new AsyncTaskManager();
		}
		return mAsyncTaskManager;
	}

	public MemCache getMemCache() {
		if (mMemCache == null) {
			mMemCache = MemCache.getInstance();
		}
		return mMemCache;
	}

	public LazyImageLoader getPreviewImageLoader() {
		if (mPreviewImageLoader == null) {
			final int preview_image_width = getResources().getDimensionPixelSize(R.dimen.image_preview_width);
			final int preview_image_height = getResources().getDimensionPixelSize(R.dimen.image_preview_height);
			mPreviewImageLoader = new LazyImageLoader(this, DIR_NAME_CACHED_THUMBNAILS,
					R.drawable.image_preview_fallback, preview_image_width, preview_image_height);
		}
		return mPreviewImageLoader;
	}

	public LazyImageLoader getProfileImageLoader() {
		if (mProfileImageLoader == null) {
			final int profile_image_size = getResources().getDimensionPixelSize(R.dimen.profile_image_size);
			mProfileImageLoader = new LazyImageLoader(this, DIR_NAME_PROFILE_IMAGES,
					R.drawable.ic_profile_image_default, profile_image_size, profile_image_size);
		}
		return mProfileImageLoader;
	}

	public ServiceInterface getServiceInterface() {
		return ServiceInterface.getInstance(this);
	}

	@Override
	public void onCreate() {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		if (mPreferences.getBoolean(PREFERENCE_KEY_REPORT_ERRORS_AUTOMATICALLY, true)) {
			ACRA.init(this);
		}
		super.onCreate();
	}

	@Override
	public void onLowMemory() {
		if (mProfileImageLoader != null) {
			mProfileImageLoader.clearMemoryCache();
		}
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
