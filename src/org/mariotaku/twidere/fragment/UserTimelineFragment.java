package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.UserTimelineLoader;
import org.mariotaku.twidere.util.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserTimelineFragment extends ParcelableStatusesListFragment {

	private boolean isAllItemsLoaded = false;

	@Override
	public boolean mustShowLastAsGap() {
		return !isAllItemsLoaded;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			return new UserTimelineLoader(getActivity(), account_id, user_id, screen_name, max_id, getData());
		}
		return null;
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {
		if (loader instanceof UserTimelineLoader) {
			final int total = ((UserTimelineLoader) loader).getTotalItemsCount();
			isAllItemsLoaded = total != -1 && total == adapter.getCount();
		}

	}

}
