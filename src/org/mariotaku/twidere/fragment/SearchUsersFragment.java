package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.UserSearchLoader;
import org.mariotaku.twidere.util.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class SearchUsersFragment extends BaseUsersListFragment {

	private int mPage = 1;

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			final String query = args.getString(INTENT_KEY_QUERY);
			return new UserSearchLoader(getActivity(), account_id, query, mPage, getData());
		}
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mPage = savedInstanceState.getInt(INTENT_KEY_PAGE, 1);
		}
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		mPage = 1;
		super.onDestroyView();
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
		if (data != null) {
			mPage++;
		}
		super.onLoadFinished(loader, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(INTENT_KEY_PAGE, mPage);
		super.onSaveInstanceState(outState);
	}

}
