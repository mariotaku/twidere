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
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.util.ServiceInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ListView;

public class MentionsFragment extends CursorStatusesListFragment implements OnTouchListener {

	private SharedPreferences mPreferences;
	private ListView mListView;
	private ServiceInterface mService;

	private long mMinIdToRefresh;
	private boolean mShouldRestorePosition = false;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			final Bundle extras = intent.getExtras();
			if (BROADCAST_MENTIONS_REFRESHED.equals(action)) {
				onRefreshComplete();
				if (extras != null) {
					mMinIdToRefresh = extras.getBoolean(INTENT_KEY_SUCCEED) ? extras.getLong(INTENT_KEY_MIN_ID, -1)
							: -1;
				} else {
					mMinIdToRefresh = -1;
				}
			} else if (BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, MentionsFragment.this);
				}
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (mService != null && mService.isMentionsRefreshing()) {
					setRefreshing(false);
				}
			}
		}
	};
	private CursorStatusesAdapter mAdapter;

	@Override
	public Uri getContentUri() {
		return Mentions.CONTENT_URI;
	}

	@Override
	public int getStatuses(final long[] account_ids, final long[] max_ids) {
		return mService.getMentions(account_ids, max_ids);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getServiceInterface();
		mShouldRestorePosition = true;
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		mListView.setOnTouchListener(this);
		mAdapter = getListAdapter();
		mAdapter.setMentionsHightlightDisabled(true);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		long last_viewed_id = -1;
		{
			final int position = mListView.getFirstVisiblePosition();
			if (position > 0) {
				last_viewed_id = mAdapter.findItemIdByPosition(position);
			}
		}
		super.onLoadFinished(loader, data);
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		if (mShouldRestorePosition && remember_position) {
			final long status_id = mPreferences.getLong(PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID, -1);
			final int position = mAdapter.findItemPositionByStatusId(status_id);
			if (position > -1 && position < mListView.getCount()) {
				mListView.setSelection(position);
			}
			mShouldRestorePosition = false;
			return;
		}
		if (mMinIdToRefresh > 0 && remember_position) {
			final int position = mAdapter.findItemPositionByStatusId(last_viewed_id > 0 ? last_viewed_id
					: mMinIdToRefresh);
			if (position >= 0 && position < mListView.getCount()) {
				mListView.setSelection(position);
			}
			mMinIdToRefresh = -1;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_MENTIONS_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_MENTIONS_DATABASE_UPDATED);
		filter.addAction(BROADCAST_REFRESHSTATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
		if (getServiceInterface().isMentionsRefreshing()) {
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
		mPreferences.edit().putLong(PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID, status_id).commit();
		super.onStop();
	}

	@Override
	public boolean onTouch(final View view, final MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mService.clearNotification(NOTIFICATION_ID_MENTIONS);
				break;
			}
		}
		return false;
	}

}
