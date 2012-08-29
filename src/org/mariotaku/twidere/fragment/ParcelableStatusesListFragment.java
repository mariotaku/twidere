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

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;

public abstract class ParcelableStatusesListFragment extends BaseStatusesListFragment<List<ParcelableStatus>> {

	private ParcelableStatusesAdapter mAdapter;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_STATUS_DESTROYED.equals(action)) {
				final long status_id = intent.getLongExtra(INTENT_KEY_STATUS_ID, -1);
				final boolean succeed = intent.getBooleanExtra(INTENT_KEY_SUCCEED, false);
				if (status_id > 0 && succeed) {
					deleteStatus(status_id);
				}
			} else if (BROADCAST_RETWEET_CHANGED.equals(action)) {
				final long status_id = intent.getLongExtra(INTENT_KEY_STATUS_ID, -1);
				final boolean retweeted = intent.getBooleanExtra(INTENT_KEY_RETWEETED, false);
				if (status_id > 0 && !retweeted) {
					deleteStatus(status_id);
				}
			}

		}

	};

	public final void deleteStatus(long status_id) {
		if (status_id <= 0) return;
		final List<ParcelableStatus> data = getData();
		final ArrayList<ParcelableStatus> data_to_remove = new ArrayList<ParcelableStatus>();
		for (final ParcelableStatus status : data) {
			if (status.status_id == status_id || status.retweet_id == status_id) {
				data_to_remove.add(status);
			}
		}
		data.removeAll(data_to_remove);
		mAdapter.setData(data, true);
	}

	@Override
	public final long[] getLastStatusIds() {
		final int last_idx = mAdapter.getCount() - 1;
		final long last_id = last_idx >= 0 ? mAdapter.getItem(last_idx).status_id : -1;
		return last_id > 0 ? new long[] { last_id } : null;
	}

	@Override
	public final ParcelableStatusesAdapter getListAdapter() {
		return mAdapter;
	}

	@Override
	public final int getStatuses(long[] account_ids, long[] max_ids) {
		final long max_id = max_ids != null && max_ids.length == 1 ? max_ids[0] : -1;
		final Bundle args = getArguments();
		args.putLong(INTENT_KEY_MAX_ID, max_id);
		getLoaderManager().restartLoader(0, args, this);
		return -1;
	}

	public boolean isLoaderUsed() {
		return true;
	}

	public abstract Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args);

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			final ArrayList<ParcelableStatus> data = savedInstanceState.getParcelableArrayList(INTENT_KEY_DATA);
			if (data != null && getData() != null) {
				getData().clear();
				getData().addAll(data);
			}
		}
		final TwidereApplication app = getApplication();
		mAdapter = new ParcelableStatusesAdapter(getActivity(), app != null ? app.getProfileImageLoader() : null,
				app != null ? app.getPreviewImageLoader() : null);
		mAdapter.setData(getData());
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public final Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
		if (isLoaderUsed()) {
			setProgressBarIndeterminateVisibility(true);
		}
		return newLoaderInstance(args);
	}

	public abstract void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter);

	@Override
	public void onDestroyView() {
		if (getData() != null) {
			getData().clear();
		}
		super.onDestroyView();
	}

	@Override
	public final void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
		super.onLoaderReset(loader);
		if (!isLoaderUsed()) return;
		onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public final void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
		super.onLoadFinished(loader, data);
		if (!isLoaderUsed()) return;
		mAdapter.setData(data);
		onDataLoaded(loader, mAdapter);
		onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public final void onPostStart() {
		if (isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, getArguments(), this);
		}
	}

	@Override
	public void onPullDownToRefresh() {
		getStatuses(null, null);
	}

	@Override
	public void onPullUpToRefresh() {
		final int count = mAdapter.getCount();
		final ParcelableStatus status = count > 0 ? mAdapter.getItem(count - 1) : null;
		if (status != null) {
			getStatuses(new long[] { status.account_id }, new long[] { status.status_id });
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (getData() instanceof ArrayList) {
			outState.putParcelableArrayList(INTENT_KEY_DATA, (ArrayList<? extends Parcelable>) getData());
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_STATUS_DESTROYED);
		filter.addAction(BROADCAST_RETWEET_CHANGED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}
}
