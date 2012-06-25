package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.provider.TweetStore.Mentions;

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

public class MentionsFragment extends CursorStatusesListFragment {

	private SharedPreferences mPreferences;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, MentionsFragment.this);
			} else if (BROADCAST_MENTIONS_REFRESHED.equals(action)) {
			} else if (BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, MentionsFragment.this);
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (!getServiceInterface().isMentionsRefreshing()) {
					getListView().onRefreshComplete();
				}
			} else if ((MentionsFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP).equals(action))
				if (getListView() != null) {
					getListView().getRefreshableView().setSelection(0);
				}
		}
	};

	@Override
	public Uri getContentUri() {
		return Mentions.CONTENT_URI;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);
		if (isActivityFirstCreated()) {
			final ListView list = getListView().getRefreshableView();
			final long status_id = mPreferences.getLong(PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID, -1);
			final int position = getListAdapter().findItemPositionByStatusId(status_id);
			if (position > -1 && position < list.getCount()) {
				list.setSelection(position);
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_MENTIONS_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_MENTIONS_DATABASE_UPDATED);
		filter.addAction(BROADCAST_REFRESHSTATE_CHANGED);
		filter.addAction(getClass().getName() + SHUFFIX_SCROLL_TO_TOP);
		registerReceiver(mStatusReceiver, filter);
		if (!getServiceInterface().isMentionsRefreshing()) {
			getListView().onRefreshComplete();
		}
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		final int first_visible_position = getListView().getRefreshableView().getFirstVisiblePosition();
		final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
		mPreferences.edit().putLong(PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID, status_id).commit();
		super.onStop();
	}

	@Override
	public boolean mustShowLastAsGap() {
		return false;
	}
}
