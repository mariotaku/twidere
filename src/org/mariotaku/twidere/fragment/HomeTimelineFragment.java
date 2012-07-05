package org.mariotaku.twidere.fragment;

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

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
			} else if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
				onRefreshComplete();
			} else if (BROADCAST_HOME_TIMELINE_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
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
	public boolean mustShowLastAsGap() {
		return false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mShouldRestorePositoin = true;
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		super.onLoadFinished(loader, data);
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, false);
		if (mShouldRestorePositoin && remember_position) {
			final ListView list = getListView();
			final long status_id = mPreferences.getLong(PREFERENCE_KEY_SAVED_HOME_TIMELINE_ID, -1);
			final int position = getListAdapter().findItemPositionByStatusId(status_id);
			if (position > -1 && position < list.getCount()) {
				list.setSelection(position);
			}
			mShouldRestorePositoin = false;
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
