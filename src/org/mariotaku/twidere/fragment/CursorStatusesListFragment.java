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

public abstract class CursorStatusesListFragment extends BaseStatusesListFragment<Cursor> {

	private CursorStatusesAdapter mAdapter;

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

	public abstract Uri getContentUri();

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
		super.onLoadFinished(loader, data);
		mAdapter.swapCursor(data);
	}

	@Override
	public void onPostStart() {
		if (!isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onPullDownToRefresh() {
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
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_FILTERS_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	long[] getNewestStatusIds() {
		return getNewestStatusIdsFromDatabase(getActivity(), getContentUri());
	}

	@Override
	long[] getOldestStatusIds() {
		return getOldestStatusIdsFromDatabase(getActivity(), getContentUri());
	}

}
