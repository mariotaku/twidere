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

import java.io.File;
import java.util.ArrayList;

import org.mariotaku.gallery3d.app.IGalleryApplication;
import org.mariotaku.gallery3d.data.DataManager;
import org.mariotaku.gallery3d.data.DownloadCache;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.service.RefreshService;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.NoDuplicatesLinkedList;
import org.mariotaku.twidere.util.TwidereHostAddressResolver;
import org.mariotaku.twidere.util.imageloader.ImageLoaderUtils;

import twitter4j.http.HostAddressResolver;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.webkit.WebView;
import edu.ucdavis.earlybird.UCDService;

public class TwidereApplication extends Application implements Constants, OnSharedPreferenceChangeListener,
		IGalleryApplication {

	private LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;

	private boolean mMultiSelectActive = false;

	private final ItemsList mSelectedItems = new ItemsList();

	private final ArrayList<Long> mSelectedStatusIds = new ArrayList<Long>();
	private final ArrayList<Long> mSelectedUserIds = new ArrayList<Long>();

	private HostAddressResolver mResolver;

	private static final String DOWNLOAD_FOLDER = "download";

	private static final long DOWNLOAD_CAPACITY = 64 * 1024 * 1024; // 64M

	private DataManager mDataManager;

	private ThreadPool mThreadPool;

	private DownloadCache mDownloadCache;
	private Handler mHandler;

	private String mBrowserUserAgent;

	@Override
	public Context getAndroidContext() {
		return this;
	}

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager != null) return mAsyncTaskManager;
		return mAsyncTaskManager = AsyncTaskManager.getInstance();
	}

	public String getBrowserUserAgent() {
		return mBrowserUserAgent;
	}

	@Override
	public synchronized DataManager getDataManager() {
		if (mDataManager == null) {
			mDataManager = new DataManager(this);
		}
		return mDataManager;
	}

	@Override
	public synchronized DownloadCache getDownloadCache() {
		if (mDownloadCache == null) {
			final File cacheDir = new File(getExternalCacheDir(), DOWNLOAD_FOLDER);

			if (!cacheDir.isDirectory()) {
				cacheDir.mkdirs();
			}

			if (!cacheDir.isDirectory()) throw new RuntimeException("fail to create: " + cacheDir.getAbsolutePath());
			mDownloadCache = new DownloadCache(this, cacheDir, DOWNLOAD_CAPACITY);
		}
		return mDownloadCache;
	}

	public Handler getHandler() {
		return mHandler;
	}

	public HostAddressResolver getHostAddressResolver() {
		if (mResolver != null) return mResolver;
		return mResolver = new TwidereHostAddressResolver(this);
	}

	public LazyImageLoader getPreviewImageLoader() {
		if (mPreviewImageLoader != null) return mPreviewImageLoader;
		final int preview_image_size = getResources().getDimensionPixelSize(R.dimen.image_preview_preferred_width);
		final int max_mem_size = ImageLoaderUtils.getMemoryClass(this) * 1024 * 1024 / 12;
		return mPreviewImageLoader = new LazyImageLoader(this, DIR_NAME_CACHED_THUMBNAILS, 0, preview_image_size,
				preview_image_size, max_mem_size);
	}

	public LazyImageLoader getProfileImageLoader() {
		if (mProfileImageLoader != null) return mProfileImageLoader;
		final int profile_image_size = getResources().getDimensionPixelSize(R.dimen.profile_image_size);
		final int max_mem_size = ImageLoaderUtils.getMemoryClass(this) * 1024 * 1024 / 6;
		return mProfileImageLoader = new LazyImageLoader(this, DIR_NAME_PROFILE_IMAGES,
				R.drawable.ic_profile_image_default, profile_image_size, profile_image_size, max_mem_size);
	}

	public ItemsList getSelectedItems() {
		return mSelectedItems;
	}

	public ArrayList<Long> getSelectedStatusIds() {
		return mSelectedStatusIds;
	}

	public ArrayList<Long> getSelectedUserIds() {
		return mSelectedUserIds;
	}

	@Override
	public synchronized ThreadPool getThreadPool() {
		if (mThreadPool == null) {
			mThreadPool = new ThreadPool();
		}
		return mThreadPool;
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return mTwitterWrapper;
	}

	public boolean isDebugBuild() {
		return DEBUG;
	}

	public boolean isMultiSelectActive() {
		return mMultiSelectActive;
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

	public void startMultiSelect() {
		mMultiSelectActive = true;
		final Intent intent = new Intent(BROADCAST_MULTI_SELECT_STATE_CHANGED);
		intent.setPackage(getPackageName());
		sendBroadcast(intent);
	}

	public void stopMultiSelect() {
		mSelectedItems.clear();
		mMultiSelectActive = false;
		final Intent intent = new Intent(BROADCAST_MULTI_SELECT_STATE_CHANGED);
		intent.setPackage(getPackageName());
		sendBroadcast(intent);
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

	@SuppressWarnings("serial")
	public class ItemsList extends NoDuplicatesLinkedList<Object> {

		@Override
		public boolean add(final Object object) {
			final boolean ret = super.add(object);
			if (object instanceof ParcelableStatus) {
				mSelectedStatusIds.add(((ParcelableStatus) object).status_id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.add(((ParcelableUser) object).user_id);
			}
			final Intent intent = new Intent(BROADCAST_MULTI_SELECT_ITEM_CHANGED);
			intent.setPackage(getPackageName());
			sendBroadcast(intent);
			return ret;
		}

		@Override
		public void clear() {
			super.clear();
			mSelectedStatusIds.clear();
			mSelectedUserIds.clear();
			final Intent intent = new Intent(BROADCAST_MULTI_SELECT_ITEM_CHANGED);
			intent.setPackage(getPackageName());
			sendBroadcast(intent);
		}

		@Override
		public boolean remove(final Object object) {
			final boolean ret = super.remove(object);
			if (object instanceof ParcelableStatus) {
				mSelectedStatusIds.remove(((ParcelableStatus) object).status_id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.remove(((ParcelableUser) object).user_id);
			}
			final Intent intent = new Intent(BROADCAST_MULTI_SELECT_ITEM_CHANGED);
			intent.setPackage(getPackageName());
			sendBroadcast(intent);
			return ret;
		}

	}

}
