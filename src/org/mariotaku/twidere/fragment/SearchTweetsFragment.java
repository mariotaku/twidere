package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.TweetSearchLoader;
import org.mariotaku.twidere.util.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class SearchTweetsFragment extends ParcelableStatusesListFragment {

	@Override
	public boolean mustShowLastAsGap() {
		return false;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			final String query = args.getString(INTENT_KEY_QUERY);
			if (query != null) return new TweetSearchLoader(getActivity(), account_id, query, max_id, getData());
		}
		return null;
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {
		
	}

}
