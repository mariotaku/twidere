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

import static org.mariotaku.twidere.util.Utils.hasActiveConnection;

import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.DatabaseHelper;
import org.mariotaku.twidere.util.ImageLoaderUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.TwidereHostAddressResolver;

import twitter4j.http.HostAddressResolver;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.webkit.WebView;
import edu.ucdavis.earlybird.UCDService;

public class TwidereApplication extends Application implements Constants, OnSharedPreferenceChangeListener {

	private LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private MultiSelectManager mMultiSelectManager;

	private HostAddressResolver mResolver;
	private SQLiteDatabase mDatabase;

	private Handler mHandler;

	private String mBrowserUserAgent;

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager != null) return mAsyncTaskManager;
		return mAsyncTaskManager = AsyncTaskManager.getInstance();
	}

	public String getBrowserUserAgent() {
		return mBrowserUserAgent;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public HostAddressResolver getHostAddressResolver() {
		if (mResolver != null) return mResolver;
		return mResolver = new TwidereHostAddressResolver(this);
	}

	public MultiSelectManager getMultiSelectManager() {
		if (mMultiSelectManager != null) return mMultiSelectManager;
		return mMultiSelectManager = new MultiSelectManager();
	}

	public LazyImageLoader getPreviewImageLoader() {
		if (mPreviewImageLoader != null) return mPreviewImageLoader;
		final int mem = ImageLoaderUtils.getMemoryClass(this);
		final int preview_image_size = getResources().getDimensionPixelSize(R.dimen.image_preview_preferred_width);
		return mPreviewImageLoader = new LazyImageLoader(this, DIR_NAME_CACHED_THUMBNAILS, 0, preview_image_size,
				preview_image_size, mem / 4);
	}

	public LazyImageLoader getProfileImageLoader() {
		if (mProfileImageLoader != null) return mProfileImageLoader;
		final int mem = ImageLoaderUtils.getMemoryClass(this);
		final int profile_image_size = getResources().getDimensionPixelSize(R.dimen.profile_image_size);
		return mProfileImageLoader = new LazyImageLoader(this, DIR_NAME_PROFILE_IMAGES,
				R.drawable.ic_profile_image_default, profile_image_size, profile_image_size, mem);
	}

	public SQLiteDatabase getSQLiteDatabase() {
		if (mDatabase != null) return mDatabase;
		return mDatabase = new DatabaseHelper(this, DATABASES_NAME, DATABASES_VERSION).getWritableDatabase();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return mTwitterWrapper;
	}

	public boolean isDebugBuild() {
		return DEBUG;
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
		mTwitterWrapper = AsyncTwitterWrapper.getInstance(this);
		mBrowserUserAgent = new WebView(this).getSettings().getUserAgentString();
		mMultiSelectManager = new MultiSelectManager();
		if (mPreferences.getBoolean(PREFERENCE_KEY_UCD_DATA_PROFILING, false)) {
			startService(new Intent(this, UCDService.class));
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			startService(new Intent(this, RefreshService.class));
		}
	}

	@Override
	public void onLowMemory() {
		if (mProfileImageLoader != null) {
			mProfileImageLoader.clearMemoryCache();
		}
		if (mPreviewImageLoader != null) {
			mPreviewImageLoader.clearMemoryCache();
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
		} else if (PREFERENCE_KEY_ENABLE_PROXY.equals(key) || PREFERENCE_KEY_CONNECTION_TIMEOUT.equals(key)) {
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
		if (mPreviewImageLoader != null) {
			mPreviewImageLoader.reloadConnectivitySettings();
		}
		if (mProfileImageLoader != null) {
			mProfileImageLoader.reloadConnectivitySettings();
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
		return context != null ? (TwidereApplication) context.getApplicationContext() : null;
	}

}
