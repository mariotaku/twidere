package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.loader.UserSearchLoader;
import org.mariotaku.twidere.util.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class SearchUsersFragment extends BaseUsersListFragment {

	private final List<ParcelableUser> mUserResultList = new ArrayList<ParcelableUser>();
	private int mPage = 1;

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			final String query = args.getString(INTENT_KEY_QUERY);
			return new UserSearchLoader(getActivity(), account_id, query, mPage, mUserResultList);
		}
		return null;
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

}
