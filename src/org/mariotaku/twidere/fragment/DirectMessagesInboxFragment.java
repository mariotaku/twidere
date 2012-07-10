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

import org.mariotaku.twidere.provider.TweetStore.DirectMessages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

public class DirectMessagesInboxFragment extends BaseDirectMessagesListFragment {

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED.equals(action)) {
				onRefreshComplete();
			} else if (BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, getArguments(), DirectMessagesInboxFragment.this);
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (getServiceInterface().isReceivedDirectMessagesRefreshing()) {
					setProgressBarIndeterminateVisibility(true);
					setRefreshing(false);
				} else {
					setProgressBarIndeterminateVisibility(false);
				}
			}
		}
	};
	
	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED);
		filter.addAction(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED);
		filter.addAction(BROADCAST_REFRESHSTATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
		if (getServiceInterface().isReceivedDirectMessagesRefreshing()) {
			setRefreshing(false);
		} else {
			onRefreshComplete();
		}
	}
	
	@Override
	public Uri getContentUri() {
		return DirectMessages.Inbox.CONTENT_URI;
	}

	@Override
	public int getDirectMessages(long account_id, long max_id) {
		return getServiceInterface().getReceivedDirectMessages(account_id, max_id);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setProgressBarIndeterminateVisibility(getServiceInterface().isReceivedDirectMessagesRefreshing());
	}

	@Override
	public void onPostStart() {

	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}
}
