package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ProfileImageLoader;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.Loader;

public abstract class ParcelableStatusesListFragment extends BaseStatusesListFragment<List<ParcelableStatus>> {

	private ParcelableStatusesAdapter mAdapter;

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

	public abstract Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args);

	@Override
	public final void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			final ArrayList<ParcelableStatus> data = savedInstanceState.getParcelableArrayList(INTENT_KEY_DATA);
			if (data != null && getData() != null) {
				getData().clear();
				getData().addAll(data);
			}
		}
		final ProfileImageLoader imageloader = ((TwidereApplication) getActivity().getApplication())
				.getProfileImageLoader();
		mAdapter = new ParcelableStatusesAdapter(getActivity(), imageloader);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public final Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return newLoaderInstance(args);
	}

	public abstract void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter);

	@Override
	public final void onDestroyView() {
		if (getData() != null) {
			getData().clear();
		}
		super.onDestroyView();
	}

	@Override
	public final void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
		super.onLoaderReset(loader);
		onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public final void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
		super.onLoadFinished(loader, data);
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
	public final void onRefresh() {
		getStatuses(null, null);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (getData() instanceof ArrayList) {
			outState.putParcelableArrayList(INTENT_KEY_DATA, (ArrayList<? extends Parcelable>) getData());
		}
		super.onSaveInstanceState(outState);
	}

}
