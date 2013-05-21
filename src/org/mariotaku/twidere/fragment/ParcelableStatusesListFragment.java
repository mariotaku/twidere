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

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.DummyParcelableStatusesLoader;
import org.mariotaku.twidere.loader.ParcelableStatusesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ArrayUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.widget.ListView;
import org.mariotaku.jsonserializer.JSONSerializer;
import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;

public abstract class ParcelableStatusesListFragment extends BaseStatusesListFragment<List<ParcelableStatus>> {

	protected SharedPreferences mPreferences;

	protected ParcelableStatusesAdapter mAdapter;
	private ListView mListView;
	private boolean mIsStatusesSaved;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
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
			} else if (BROADCAST_MULTI_MUTESTATE_CHANGED.equals(action)) {
				final Bundle args = getArguments();
				final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
				if (account_id <= 0) return;
				getStatuses(new long[] { account_id }, null, null);
			}

		}

	};

	public final void deleteStatus(final long status_id) {
		if (status_id <= 0 || mData == null) return;
		final ArrayList<ParcelableStatus> data_to_remove = new ArrayList<ParcelableStatus>();
		for (final ParcelableStatus status : mData) {
			if (status.status_id == status_id || status.retweet_id > 0 && status.retweet_id == status_id) {
				data_to_remove.add(status);
			}
		}
		mData.removeAll(data_to_remove);
		mAdapter.setData(mData);
	}

	@Override
	public final ParcelableStatusesAdapter getListAdapter() {
		return mAdapter;
	}

	@Override
	public final int getStatuses(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		final long max_id = max_ids != null && max_ids.length == 1 ? max_ids[0] : -1;
		final long since_id = since_ids != null && since_ids.length == 1 ? since_ids[0] : -1;
		final Bundle args = new Bundle(getArguments());
		args.putLong(INTENT_KEY_MAX_ID, max_id);
		args.putLong(INTENT_KEY_SINCE_ID, since_id);
		getLoaderManager().restartLoader(0, args, this);
		return -1;
	}

	public boolean isLoaderUsed() {
		return true;
	}

	protected abstract Loader<List<ParcelableStatus>> newLoaderInstance(Context context, Bundle args);

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			final List<ParcelableStatus> saved = savedInstanceState.getParcelableArrayList(INTENT_KEY_DATA);
			if (saved != null) {
				mData = saved;
			}
		}
		mAdapter = new ParcelableStatusesAdapter(getActivity());
		mAdapter.setData(mData);
		super.onActivityCreated(savedInstanceState);
		mListView = getListView();
		mPreferences = getSharedPreferences();
	}

	@Override
	public final Loader<List<ParcelableStatus>> onCreateLoader(final int id,
			final Bundle args) {
		if (isLoaderUsed()) {
			setProgressBarIndeterminateVisibility(true);
		}
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
	public final void onLoaderReset(final Loader<List<ParcelableStatus>> loader) {
		super.onLoaderReset(loader);
		if (!isLoaderUsed()) return;
		onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
	}

//	@Override
//	public final void onLoadFinished(final Loader<List<ParcelableStatus>> loader, final List<ParcelableStatus> data) {
//		super.onLoadFinished(loader, data);
//		if (!isLoaderUsed()) return;
//		mAdapter.setData(data);
//		onRefreshComplete();
//		setProgressBarIndeterminateVisibility(false);
//	}

	@Override
	public final void onPostStart() {
		if (isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, getArguments(), this);
		}
	}

	@Override
	public void onPullDownToRefresh() {
		final int count = mAdapter.getCount();
		final ParcelableStatus status = count > 0 ? mAdapter.getItem(0) : null;
		if (status != null) {
			getStatuses(new long[] { status.account_id }, null, new long[] { status.status_id });
		}
	}

	@Override
	public void onPullUpToRefresh() {
		final int count = mAdapter.getCount();
		final ParcelableStatus status = count > 0 ? mAdapter.getItem(count - 1) : null;
		if (status != null) {
			getStatuses(new long[] { status.account_id }, new long[] { status.status_id }, null);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		if (mData != null) {
			outState.putParcelableArrayList(INTENT_KEY_DATA, new ArrayList<ParcelableStatus>(mData));
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

	protected void saveStatuses() {
		if (getActivity() == null || getView() == null || mIsStatusesSaved) return;
		if (saveStatusesInternal()) {
			mIsStatusesSaved = true;
		}
	}

	@Override
	protected final long[] getNewestStatusIds() {
		final long last_id = mAdapter.getCount() > 0 ? mAdapter.getItem(0).status_id : -1;
		return last_id > 0 ? new long[] { last_id } : null;
	}

	@Override
	protected final long[] getOldestStatusIds() {
		final int last_idx = mAdapter.getCount() - 1;
		final long last_id = last_idx >= 0 ? mAdapter.getItem(last_idx).status_id : -1;
		return last_id > 0 ? new long[] { last_id } : null;
	}

	protected final boolean saveStatusesInternal() {
		if (mIsStatusesSaved) return true;
		try {
			final List<ParcelableStatus> data = getData();
			if (data == null) return false;
			final int items_limit = mPreferences.getInt(PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);
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
	
	protected abstract String[] getSavedStatusesFileArgs();

	protected final String getPositionKey() {
		final String[] args = getSavedStatusesFileArgs();
		if (args == null || args.length <= 0) return null;
		try {
			return encodeQueryParams(ArrayUtils.toString(args, '.', false) + "." + getTabPosition());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
