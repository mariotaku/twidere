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

package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.adapter.CursorStatusesAdapter;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListView;

public class HomeTimelineFragment extends CursorStatusesListFragment {

	private SharedPreferences mPreferences;
	private long mMinIdToRefresh;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			final Bundle extras = intent.getExtras();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
				}
			} else if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
				onRefreshComplete();
				if (extras != null) {
					mMinIdToRefresh = extras.getBoolean(INTENT_KEY_SUCCEED) ? extras.getLong(INTENT_KEY_MIN_ID, -1)
							: -1;
				} else {
					mMinIdToRefresh = -1;
				}
			} else if (BROADCAST_HOME_TIMELINE_DATABASE_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
				}
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (getServiceInterface().isHomeTimelineRefreshing()) {
					setRefreshing(false);
				}
			} else if ((HomeTimelineFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP).equals(action))
				if (getListView() != null) {
					getListView().setSelection(0);
				}
		}
	};

	private boolean mShouldRestorePositoin = false;

	@Override
	public Uri getContentUri() {
		return Statuses.CONTENT_URI;
	}

	@Override
	public int getStatuses(long[] account_ids, long[] max_ids) {
		return getServiceInterface().getHomeTimeline(account_ids, max_ids);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mShouldRestorePositoin = true;
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		final ListView list = getListView();
		final CursorStatusesAdapter adapter = getListAdapter();
		long last_viewed_id = -1;
		{
			final int position = list.getFirstVisiblePosition();
			if (position > 0) {
				last_viewed_id = adapter.findItemIdByPosition(position);
			}
		}
		super.onLoadFinished(loader, data);
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, false);
		if (mShouldRestorePositoin && remember_position) {

			final long status_id = mPreferences.getLong(PREFERENCE_KEY_SAVED_HOME_TIMELINE_ID, -1);
			final int position = adapter.findItemPositionByStatusId(status_id);
			if (position > -1 && position < list.getCount()) {
				list.setSelection(position);
			}
			mShouldRestorePositoin = false;
			return;
		}
		if (mMinIdToRefresh > 0) {
			final int position = adapter.findItemPositionByStatusId(last_viewed_id > 0 ? last_viewed_id
					: mMinIdToRefresh);
			if (position >= 0 && position < list.getCount()) {
				list.setSelection(position);
			}
			mMinIdToRefresh = -1;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_HOME_TIMELINE_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED);
		filter.addAction(BROADCAST_REFRESHSTATE_CHANGED);
		filter.addAction(getClass().getName() + SHUFFIX_SCROLL_TO_TOP);
		registerReceiver(mStatusReceiver, filter);
		if (getServiceInterface().isHomeTimelineRefreshing()) {
			setRefreshing(false);
		} else {
			onRefreshComplete();
		}
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		final int first_visible_position = getListView().getFirstVisiblePosition();
		final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
		mPreferences.edit().putLong(PREFERENCE_KEY_SAVED_HOME_TIMELINE_ID, status_id).commit();
		super.onStop();
	}

}
