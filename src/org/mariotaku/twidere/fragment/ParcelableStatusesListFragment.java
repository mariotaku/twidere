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

import static org.mariotaku.twidere.util.Utils.encodeQueryParams;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.loader.DummyParcelableStatusesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public abstract class ParcelableStatusesListFragment extends BaseStatusesListFragment<List<ParcelableStatus>> {

	protected SharedPreferences mPreferences;

	private boolean mIsStatusesSaved;

	private boolean mStatusesRestored;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_STATUS_DESTROYED.equals(action)) {
				final long status_id = intent.getLongExtra(EXTRA_STATUS_ID, -1);
				final boolean succeed = intent.getBooleanExtra(EXTRA_SUCCEED, false);
				if (status_id > 0 && succeed) {
					deleteStatus(status_id);
				}
			} else if (BROADCAST_RETWEET_CHANGED.equals(action)) {
				final long status_id = intent.getLongExtra(EXTRA_STATUS_ID, -1);
				final boolean retweeted = intent.getBooleanExtra(EXTRA_RETWEETED, false);
				if (status_id > 0 && !retweeted) {
					deleteStatus(status_id);
				}
			} else if (BROADCAST_MULTI_MUTESTATE_CHANGED.equals(action)) {
				final Bundle args = getArguments();
				final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
				if (account_id <= 0) return;
				getStatuses(new long[] { account_id }, null, null);
			}

		}

	};

	public final void deleteStatus(final long status_id) {
		final List<ParcelableStatus> data = getData();
		if (status_id <= 0 || data == null) return;
		final ArrayList<ParcelableStatus> data_to_remove = new ArrayList<ParcelableStatus>();
		for (final ParcelableStatus status : data) {
			if (status.id == status_id || status.retweet_id > 0 && status.retweet_id == status_id) {
				data_to_remove.add(status);
			}
		}
		data.removeAll(data_to_remove);
		getListAdapter().setData(data);
	}

	@Override
	public final int getStatuses(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mStatusesRestored = true;
		final long max_id = max_ids != null && max_ids.length == 1 ? max_ids[0] : -1;
		final long since_id = since_ids != null && since_ids.length == 1 ? since_ids[0] : -1;
		final Bundle args = new Bundle(getArguments());
		args.putLong(EXTRA_MAX_ID, max_id);
		args.putLong(EXTRA_SINCE_ID, since_id);
		getLoaderManager().restartLoader(0, args, this);
		return -1;
	}

	public boolean isLoaderUsed() {
		return true;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mStatusesRestored = false;
		if (savedInstanceState != null) {
			final List<ParcelableStatus> saved = savedInstanceState.getParcelableArrayList(EXTRA_DATA);
			if (saved != null) {
				setData(saved);
			}
		}
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences();
	}

	@Override
	public final Loader<List<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
		if (isLoaderUsed()) {
			setProgressBarIndeterminateVisibility(true);
		}
		final List<ParcelableStatus> data = getData();
		if (isInstanceStateSaved() && data != null && !mStatusesRestored)
			return new DummyParcelableStatusesLoader(getActivity(), data);
		final Loader<List<ParcelableStatus>> loader = newLoaderInstance(getActivity(), args);
		return loader != null ? loader : new DummyParcelableStatusesLoader(getActivity());
	}

	@Override
	public void onDestroy() {
		saveStatuses();
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		saveStatuses();
		super.onDestroyView();
	}

	@Override
	public final void onPostStart() {
		if (isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, getArguments(), this);
		}
	}

	@Override
	public void onRefreshStarted() {
		super.onRefreshStarted();
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		final int count = adapter.getCount();
		final ParcelableStatus status = count > 0 ? adapter.getStatus(0) : null;
		if (status != null) {
			getStatuses(new long[] { status.account_id }, null, new long[] { status.id });
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		final List<ParcelableStatus> data = getData();
		if (data != null) {
			outState.putParcelableArrayList(EXTRA_DATA, new ArrayList<ParcelableStatus>(data));
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

	@Override
	protected final long[] getNewestStatusIds() {
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		final long last_id = adapter.getCount() > 0 ? adapter.getStatus(0).id : -1;
		return last_id > 0 ? new long[] { last_id } : null;
	}

	@Override
	protected final long[] getOldestStatusIds() {
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		final ParcelableStatus status = adapter.getLastStatus();
		final long last_id = status != null ? status.id : -1;
		return last_id > 0 ? new long[] { last_id } : null;
	}

	@Override
	protected final String getPositionKey() {
		final Object[] args = getSavedStatusesFileArgs();
		if (args == null || args.length <= 0) return null;
		try {
			return encodeQueryParams(ArrayUtils.toString(args, '.', false) + "." + getTabPosition());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract Object[] getSavedStatusesFileArgs();

	@Override
	protected void loadMoreStatuses() {
		if (isRefreshing()) return;
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		final ParcelableStatus status = adapter.getLastStatus();
		if (status != null) {
			getStatuses(new long[] { status.account_id }, new long[] { status.id }, null);
		}
	}

	@Override
	protected ParcelableStatusesAdapter newAdapterInstance() {
		return new ParcelableStatusesAdapter(getActivity());
	}

	protected abstract Loader<List<ParcelableStatus>> newLoaderInstance(Context context, Bundle args);

	protected void saveStatuses() {
		if (getActivity() == null || getView() == null || mIsStatusesSaved) return;
		if (saveStatusesInternal()) {
			mIsStatusesSaved = true;
		}
	}

	protected final boolean saveStatusesInternal() {
		if (mIsStatusesSaved) return true;
		try {
			final List<ParcelableStatus> data = getData();
			if (data == null) return false;
			final int items_limit = mPreferences.getInt(PREFERENCE_KEY_DATABASE_ITEM_LIMIT,
					PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);
			final List<ParcelableStatus> statuses = data.subList(0, Math.min(items_limit, data.size()));
			final File file = JSONSerializer.getSerializationFile(getActivity(), getSavedStatusesFileArgs());
			JSONSerializer.toFile(file, statuses.toArray(new ParcelableStatus[statuses.size()]));
		} catch (final IOException e) {
			return false;
		} catch (final ConcurrentModificationException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
