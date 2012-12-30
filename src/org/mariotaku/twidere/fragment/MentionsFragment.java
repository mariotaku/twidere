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
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

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
import android.widget.AbsListView;
import android.widget.ListView;

public class MentionsFragment extends CursorStatusesListFragment {
	
	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MENTIONS_REFRESHED.equals(action)) {
				onRefreshComplete();
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, MentionsFragment.this);
				}
			} else if (BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, MentionsFragment.this);
				}
			} else if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				if (mTwitterWrapper != null && mTwitterWrapper.isMentionsRefreshing()) {
					setRefreshing(false);
				}
			}
		}
	};

	@Override
	public int getStatuses(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getMentions(account_ids, max_ids, since_ids);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter.setMentionsHightlightDisabled(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_MENTIONS_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_MENTIONS_DATABASE_UPDATED);
		filter.addAction(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
		if (getTwitterWrapper().isMentionsRefreshing()) {
			setRefreshing(false);
		} else {
			onRefreshComplete();
		}
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	Uri getContentUri() {
		return Mentions.CONTENT_URI;
	}
	
	@Override
	int getNotificationIdToClear() {
		return NOTIFICATION_ID_MENTIONS;
	}

	@Override
	String getSavedTimelinePreferenceKey() {
		return PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID;
	}
}
