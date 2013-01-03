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

import static org.mariotaku.twidere.util.Utils.buildActivatedStatsWhereClause;
import static org.mariotaku.twidere.util.Utils.buildStatusFilterWhereClause;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getOldestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getTableNameByUri;

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.CursorStatusesAdapter;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

public abstract class CursorStatusesListFragment extends BaseStatusesListFragment<Cursor> implements
		View.OnTouchListener {

	protected CursorStatusesAdapter mAdapter;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action) || BROADCAST_FILTERS_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, CursorStatusesListFragment.this);
				}
			}
		}
	};

	public HomeActivity getHomeActivity() {
		final FragmentActivity activity = getActivity();
		if (activity instanceof HomeActivity) return (HomeActivity) activity;
		return null;
	}

	@Override
	public CursorStatusesAdapter getListAdapter() {
		return mAdapter;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mAdapter = new CursorStatusesAdapter(getActivity());
		super.onActivityCreated(savedInstanceState);
		mListView.setOnTouchListener(this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final String[] cols = new String[] { Statuses._ID, Statuses.ACCOUNT_ID, Statuses.STATUS_ID, Statuses.USER_ID,
				Statuses.STATUS_TIMESTAMP, Statuses.TEXT, Statuses.NAME, Statuses.SCREEN_NAME,
				Statuses.PROFILE_IMAGE_URL, Statuses.IN_REPLY_TO_SCREEN_NAME, Statuses.IN_REPLY_TO_STATUS_ID,
				Statuses.LOCATION, Statuses.IS_RETWEET, Statuses.RETWEET_COUNT, Statuses.RETWEET_ID,
				Statuses.RETWEETED_BY_NAME, Statuses.RETWEETED_BY_SCREEN_NAME, Statuses.IS_FAVORITE,
				Statuses.IS_PROTECTED, Statuses.IS_VERIFIED, Statuses.IS_GAP, Statuses.IS_POSSIBLY_SENSITIVE };
		final Uri uri = getContentUri();
		final String sort_by = getSharedPreferences().getBoolean(PREFERENCE_KEY_SORT_TIMELINE_BY_TIME, false) ? Statuses.SORT_ORDER_TIMESTAMP_DESC
				: Statuses.SORT_ORDER_STATUS_ID_DESC;
		final String where = buildActivatedStatsWhereClause(getActivity(), null);
		final String table = getTableNameByUri(uri);
		return new CursorLoader(getActivity(), uri, cols, buildStatusFilterWhereClause(table, where), null, sort_by);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		super.onLoaderReset(loader);
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		final int first_visible_position = mListView.getFirstVisiblePosition();
		final long last_viewed_id = mAdapter.findItemIdByPosition(first_visible_position);
		super.onLoadFinished(loader, data);
		mAdapter.swapCursor(data);
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		final int curr_first_visible_position = mListView.getFirstVisiblePosition();
		final long curr_viewed_id = mAdapter.findItemIdByPosition(curr_first_visible_position);
		final long status_id;
		if (last_viewed_id <= 0) {
			if (!remember_position) return;
			status_id = mPreferences.getLong(getSavedTimelinePreferenceKey(), -1);
		} else if ((first_visible_position > 0 || remember_position) && curr_viewed_id > 0 && last_viewed_id != curr_viewed_id) {
			status_id = last_viewed_id;
		} else
			return;
		final int position = mAdapter.findItemPositionByStatusId(status_id);
		if (position > -1 && position < mListView.getCount()) {
			mListView.setSelection(position);
		}
	}

	@Override
	public void onPostStart() {
		if (!isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onPullDownToRefresh() {
		saveReadPosition();
		new AsyncTask<Void, Void, long[][]>() {

			@Override
			protected long[][] doInBackground(final Void... params) {
				final long[][] result = new long[3][];
				result[0] = getActivatedAccountIds(getActivity());
				result[2] = getNewestStatusIds();
				return result;
			}

			@Override
			protected void onPostExecute(final long[][] result) {
				getStatuses(result[0], result[1], result[2]);
			}

		}.execute();
	}

	@Override
	public void onPullUpToRefresh() {
		saveReadPosition();
		new AsyncTask<Void, Void, long[][]>() {

			@Override
			protected long[][] doInBackground(final Void... params) {
				final long[][] result = new long[3][];
				result[0] = getActivatedAccountIds(getActivity());
				result[1] = getOldestStatusIds();
				return result;
			}

			@Override
			protected void onPostExecute(final long[][] result) {
				getStatuses(result[0], result[1], result[2]);
			}

		}.execute();
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		switch (scrollState) {
			case SCROLL_STATE_IDLE:
				saveReadPosition();
				break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_FILTERS_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		saveReadPosition();
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean onTouch(final View view, final MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mTwitterWrapper.clearNotification(getNotificationIdToClear());
				break;
			}
		}
		return false;
	}

	abstract Uri getContentUri();

	@Override
	long[] getNewestStatusIds() {
		return getNewestStatusIdsFromDatabase(getActivity(), getContentUri());
	}

	abstract int getNotificationIdToClear();

	@Override
	long[] getOldestStatusIds() {
		return getOldestStatusIdsFromDatabase(getActivity(), getContentUri());
	}

	abstract String getSavedTimelinePreferenceKey();

	void saveReadPosition() {
		final int first_visible_position = mListView.getFirstVisiblePosition();
		final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
		mPreferences.edit().putLong(getSavedTimelinePreferenceKey(), status_id).commit();
	}

}
