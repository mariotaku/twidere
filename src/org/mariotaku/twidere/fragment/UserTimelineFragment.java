package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.UserTimelineLoader;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.content.Loader;

public class UserTimelineFragment extends BaseStatusesListFragment<List<ParcelableStatus>> {

	private ParcelableStatusesAdapter mAdapter;
	private final List<ParcelableStatus> mData = new ArrayList<ParcelableStatus>();

	@Override
	public long[] getLastStatusIds() {
		int last_idx = mAdapter.getCount() - 1;
		long last_id = last_idx >= 0 ? mAdapter.getItem(last_idx).status_id : -1;
		return last_id > 0 ? new long[] { last_id } : null;
	}

	@Override
	public ParcelableStatusesAdapter getListAdapter() {
		return mAdapter;
	}

	@Override
	public int getStatuses(long[] account_ids, long[] max_ids) {
		long max_id = max_ids != null && max_ids.length == 1 ? max_ids[0] : -1;
		Bundle args = getArguments();
		args.putLong(INTENT_KEY_MAX_ID, max_id);
		getLoaderManager().restartLoader(0, args, this);
		return -1;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LazyImageLoader imageloader = ((TwidereApplication) getActivity().getApplication()).getProfileImageLoader();
		mAdapter = new ParcelableStatusesAdapter(getActivity(), imageloader);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			if (user_id != -1)
				return new UserTimelineLoader(getActivity(), account_id, user_id, max_id, mData);
			else if (screen_name != null)
				return new UserTimelineLoader(getActivity(), account_id, screen_name, max_id, mData);

		}
		return null;
	}

	@Override
	public void onDestroyView() {
		mData.clear();
		super.onDestroyView();
	}

	@Override
	public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
		getListView().onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
		mAdapter.clear();
		if (data != null) {
			for (ParcelableStatus status : data) {
				mAdapter.add(status);
			}
		}
		getListView().onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onPostStart() {

	}

	@Override
	public void onRefresh() {
		getStatuses(null, null);
	}

}
