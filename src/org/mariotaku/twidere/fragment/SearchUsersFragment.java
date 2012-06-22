package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.loader.UserSearchLoader;
import org.mariotaku.twidere.util.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class SearchUsersFragment extends BaseUsersListFragment {

	private final List<ParcelableUser> mUserResultList = new ArrayList<ParcelableUser>();

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			final int page = args.getInt(INTENT_KEY_PAGE);
			final String query = args.getString(INTENT_KEY_QUERY);
			return new UserSearchLoader(getActivity(), account_id, query, page, mUserResultList);
		}
		return null;
	}

}
