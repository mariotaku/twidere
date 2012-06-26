package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.UserFavoritesLoader;
import org.mariotaku.twidere.util.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserFavoritesFragment extends ParcelableStatusesListFragment {

	private boolean isAllItemsLoaded = false;

	@Override
	public boolean mustShowLastAsGap() {
		return !isAllItemsLoaded;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			if (user_id != -1)
				return new UserFavoritesLoader(getActivity(), account_id, user_id, max_id, getData());
			else if (screen_name != null)
				return new UserFavoritesLoader(getActivity(), account_id, screen_name, max_id, getData());

		}
		return null;
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {
		if (loader instanceof UserFavoritesLoader) {
			final int total = ((UserFavoritesLoader) loader).getTotalItemsCount();
			isAllItemsLoaded = total != -1 && total == adapter.getCount();
		}
	}

}
