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

import static org.mariotaku.twidere.Constants.CRASH_REPORT_FORM_KEY;

import java.util.ArrayList;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.NoDuplicatesList;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

@ReportsCrashes(formKey = CRASH_REPORT_FORM_KEY)
public class TwidereApplication extends Application implements Constants, OnSharedPreferenceChangeListener {

	private LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private ServiceInterface mServiceInterface;

	private boolean mMultiSelectActive = false;

	private final ItemsList mSelectedItems = new ItemsList();

	private final ArrayList<Long> mSelectedStatusIds = new ArrayList<Long>();
	private final ArrayList<Long> mSelectedUserIds = new ArrayList<Long>();

	public AsyncTaskManager getAsyncTaskManager() {
		if (mAsyncTaskManager == null) {
			mAsyncTaskManager = AsyncTaskManager.getInstance();
		}
		return mAsyncTaskManager;
	}

	public LazyImageLoader getPreviewImageLoader() {
		if (mPreviewImageLoader == null) {
			final int preview_image_size = getResources().getDimensionPixelSize(R.dimen.preview_image_size);
			mPreviewImageLoader = new LazyImageLoader(this, DIR_NAME_CACHED_THUMBNAILS,
					R.drawable.image_preview_fallback, preview_image_size, preview_image_size, 30);
		}
		return mPreviewImageLoader;
	}

	public LazyImageLoader getProfileImageLoader() {
		if (mProfileImageLoader == null) {
			final int profile_image_size = getResources().getDimensionPixelSize(R.dimen.profile_image_size);
			mProfileImageLoader = new LazyImageLoader(this, DIR_NAME_PROFILE_IMAGES,
					R.drawable.ic_profile_image_default, profile_image_size, profile_image_size, 60);
		}
		return mProfileImageLoader;
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

	public ServiceInterface getServiceInterface() {
		if (mServiceInterface != null) return mServiceInterface;
		return mServiceInterface = ServiceInterface.getInstance(this);
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
		if (!isDebugBuild() && mPreferences.getBoolean(PREFERENCE_KEY_REPORT_ERRORS_AUTOMATICALLY, true)) {
			try {
				ACRA.init(this);
			} catch (final Exception e) {
				// Ignore.
			}
		}
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		super.onCreate();
		mServiceInterface = ServiceInterface.getInstance(this);
	}

	@Override
	public void onLowMemory() {
		if (mProfileImageLoader != null) {
			mProfileImageLoader.clearMemoryCache();
		}
		super.onLowMemory();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		if (mServiceInterface != null
				&& (PREFERENCE_KEY_AUTO_REFRESH.equals(key) || PREFERENCE_KEY_REFRESH_INTERVAL.equals(key))) {
			mServiceInterface.stopAutoRefresh();
			if (preferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
				mServiceInterface.startAutoRefresh();
			}
		} else if (PREFERENCE_KEY_ENABLE_PROXY.equals(key)) {
			reloadConnectivitySettings();
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

	@SuppressWarnings("serial")
	public class ItemsList extends NoDuplicatesList<Object> {

		@Override
		public boolean add(Object object) {
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
		public boolean remove(Object object) {
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
