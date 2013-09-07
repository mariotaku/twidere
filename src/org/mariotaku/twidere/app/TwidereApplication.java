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

import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.hasActiveConnection;
import static org.mariotaku.twidere.util.Utils.initAccountColor;
import static org.mariotaku.twidere.util.Utils.initUserColor;

import java.io.File;

import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.DatabaseHelper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageMemoryCache;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.TwidereHostAddressResolver;
import org.mariotaku.twidere.util.TwidereImageDownloader;
import org.mariotaku.twidere.util.URLFileNameGenerator;

import twitter4j.http.HostAddressResolver;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import edu.ucdavis.earlybird.UCDService;

public class TwidereApplication extends Application implements Constants, OnSharedPreferenceChangeListener {

	private ImageLoaderWrapper mImageLoaderWrapper;
	private ImageLoader mImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private MultiSelectManager mMultiSelectManager;
	private TwidereImageDownloader mImageDownloader;
	private MessagesManager mCroutonsManager;

	private HostAddressResolver mResolver;
	private SQLiteDatabase mDatabase;

	private Handler mHandler;

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager != null) return mAsyncTaskManager;
		return mAsyncTaskManager = AsyncTaskManager.getInstance();
	}

	public Handler getHandler() {
		return mHandler;
	}

	public HostAddressResolver getHostAddressResolver() {
		if (mResolver != null) return mResolver;
		return mResolver = new TwidereHostAddressResolver(this);
	}

	public ImageDownloader getImageDownloader() {
		if (mImageDownloader != null) return mImageDownloader;
		return mImageDownloader = new TwidereImageDownloader(this);
	}

	public ImageLoader getImageLoader() {
		if (mImageLoader != null) return mImageLoader;
		final File cache_dir = getBestCacheDir(this, DIR_NAME_IMAGE_CACHE);
		final ImageLoader loader = ImageLoader.getInstance();
		final ImageLoaderConfiguration.Builder cb = new ImageLoaderConfiguration.Builder(this);
		cb.threadPoolSize(8);
		cb.memoryCache(new ImageMemoryCache(40));
		cb.discCache(new UnlimitedDiscCache(cache_dir, new URLFileNameGenerator()));
		cb.imageDownloader(getImageDownloader());
		loader.init(cb.build());
		return mImageLoader = loader;
	}

	public ImageLoaderWrapper getImageLoaderWrapper() {
		if (mImageLoaderWrapper != null) return mImageLoaderWrapper;
		return mImageLoaderWrapper = new ImageLoaderWrapper(getImageLoader());
	}

	public MessagesManager getMessagesManager() {
		if (mCroutonsManager != null) return mCroutonsManager;
		return mCroutonsManager = new MessagesManager(this);
	}

	public MultiSelectManager getMultiSelectManager() {
		if (mMultiSelectManager != null) return mMultiSelectManager;
		return mMultiSelectManager = new MultiSelectManager();
	}

	public SQLiteDatabase getSQLiteDatabase() {
		if (mDatabase != null) return mDatabase;
		return mDatabase = new DatabaseHelper(this, DATABASES_NAME, DATABASES_VERSION).getWritableDatabase();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		if (mTwitterWrapper != null) return mTwitterWrapper;
		return mTwitterWrapper = AsyncTwitterWrapper.getInstance(this);
	}

	public boolean isMultiSelectActive() {
		return getMultiSelectManager().isActive();
	}

	@Override
	public void onCreate() {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mHandler = new Handler();
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		super.onCreate();
		initializeAsyncTask();
		GalleryUtils.initialize(this);
		if (mPreferences.getBoolean(PREFERENCE_KEY_UCD_DATA_PROFILING, false)) {
			startService(new Intent(this, UCDService.class));
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			startService(new Intent(this, RefreshService.class));
		}
		initAccountColor(this);
		initUserColor(this);
	}

	@Override
	public void onLowMemory() {
		if (mImageLoaderWrapper != null) {
			mImageLoaderWrapper.clearMemoryCache();
		}
		super.onLowMemory();
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (PREFERENCE_KEY_AUTO_REFRESH.equals(key) || PREFERENCE_KEY_REFRESH_INTERVAL.equals(key)) {
			final Intent intent = new Intent(this, RefreshService.class);
			stopService(intent);
			if (preferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false) && hasActiveConnection(this)) {
				startService(intent);
			}
		} else if (PREFERENCE_KEY_ENABLE_PROXY.equals(key) || PREFERENCE_KEY_CONNECTION_TIMEOUT.equals(key)
				|| PREFERENCE_KEY_PROXY_HOST.equals(key) || PREFERENCE_KEY_PROXY_PORT.equals(key)
				|| PREFERENCE_KEY_FAST_IMAGE_LOADING.equals(key)) {
			reloadConnectivitySettings();
		} else if (PREFERENCE_KEY_UCD_DATA_PROFILING.equals(key)) {
			final Intent intent = new Intent(this, UCDService.class);
			if (preferences.getBoolean(PREFERENCE_KEY_UCD_DATA_PROFILING, false)) {
				startService(intent);
			} else {
				stopService(intent);
			}
		}
	}

	public void reloadConnectivitySettings() {
		if (mImageDownloader != null) {
			mImageDownloader.reloadConnectivitySettings();
		}
	}

	private void initializeAsyncTask() {
		// AsyncTask class needs to be loaded in UI thread.
		// So we load it here to comply the rule.
		try {
			Class.forName(AsyncTask.class.getName());
		} catch (final ClassNotFoundException e) {
		}
	}

	public static TwidereApplication getInstance(final Context context) {
		if (context == null) return null;
		final Context app = context.getApplicationContext();
		return app instanceof TwidereApplication ? (TwidereApplication) app : null;
	}

}
