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

package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.Utils.buildStatusFilterWhereClause;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getOldestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getTableNameByUri;
import static org.mariotaku.twidere.util.Utils.shouldEnableFiltersForRTs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MotionEvent;
import android.widget.AbsListView;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.CursorStatusesAdapter;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

public abstract class CursorStatusesListFragment extends BaseStatusesListFragment<Cursor> {

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action) || BROADCAST_FILTERS_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, CursorStatusesListFragment.this);
			}
		}
	};

	public HomeActivity getHomeActivity() {
		final Activity activity = getActivity();
		if (activity instanceof HomeActivity) return (HomeActivity) activity;
		return null;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListAdapter().setFiltersEnabled(isFiltersEnabled());
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = getContentUri();
		final String table = getTableNameByUri(uri);
		final String sort_by = Statuses.SORT_ORDER_STATUS_ID_DESC;
		final long account_id = getAccountId();
		final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(getActivity());
		final boolean no_account_selected = account_ids.length == 0;
		setEmptyText(no_account_selected ? getString(R.string.no_account_selected) : null);
		if (!no_account_selected) {
			getListView().setEmptyView(null);
		}
		final Where account_where = Where.in(new Column(Statuses.ACCOUNT_ID), new RawItemArray(account_ids));
		if (isFiltersEnabled()) {
			account_where.and(new Where(buildStatusFilterWhereClause(table, null,
					shouldEnableFiltersForRTs(getActivity()))));
		}
		return new CursorLoader(getActivity(), uri, CursorStatusesAdapter.CURSOR_COLS, account_where.getSQL(), null,
				sort_by);
	}

	@Override
	public boolean onDown(final MotionEvent e) {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		if (twitter != null) {
			twitter.clearNotification(getNotificationIdToClear());
		}
		return super.onDown(e);
	}

	@Override
	public void onPostStart() {
		if (!isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onRefreshStarted() {
		super.onRefreshStarted();
		savePosition();
		new AsyncTask<Void, Void, long[][]>() {

			@Override
			protected long[][] doInBackground(final Void... params) {
				final long[][] result = new long[3][];
				final long account_id = getAccountId();
				result[0] = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(getActivity());
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
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		switch (scrollState) {
			case SCROLL_STATE_FLING:
			case SCROLL_STATE_TOUCH_SCROLL: {
				break;
			}
			case SCROLL_STATE_IDLE:
				savePosition();
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
		savePosition();
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	protected long getAccountId() {
		final Bundle args = getArguments();
		return args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
	}

	protected abstract Uri getContentUri();

	@Override
	protected long[] getNewestStatusIds() {
		final long account_id = getAccountId();
		final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(getActivity());
		return getNewestStatusIdsFromDatabase(getActivity(), getContentUri(), account_ids);
	}

	protected abstract int getNotificationIdToClear();

	@Override
	protected long[] getOldestStatusIds() {
		final long account_id = getAccountId();
		final long[] account_ids = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(getActivity());
		return getOldestStatusIdsFromDatabase(getActivity(), getContentUri(), account_ids);
	}

	protected abstract boolean isFiltersEnabled();

	@Override
	protected void loadMoreStatuses() {
		if (isRefreshing()) return;
		savePosition();
		new AsyncTask<Void, Void, long[][]>() {

			@Override
			protected long[][] doInBackground(final Void... params) {
				final long[][] result = new long[3][];
				final long account_id = getAccountId();
				result[0] = account_id > 0 ? new long[] { account_id } : getActivatedAccountIds(getActivity());
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
	protected CursorStatusesAdapter newAdapterInstance() {
		return new CursorStatusesAdapter(getActivity());
	}

	@Override
	protected boolean shouldShowAccountColor() {
		return getAccountId() <= 0 && getActivatedAccountIds(getActivity()).length > 1;
	}

}
