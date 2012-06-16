package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.loader.UserSearchLoader;

import twitter4j.User;
import android.os.Bundle;
import android.support.v4.content.Loader;

public class SearchUsersFragment extends BaseUsersListFragment {

	private final List<User> mUserResultList = new ArrayList<User>();

	@Override
	public Loader<List<User>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			int page = args.getInt(INTENT_KEY_PAGE);
			String query = args.getString(INTENT_KEY_QUERY);
			return new UserSearchLoader(getActivity(), account_id, query, page, mUserResultList);
		}
		return null;
	}

}
