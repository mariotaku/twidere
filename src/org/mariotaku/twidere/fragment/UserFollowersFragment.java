package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.loader.UserSearchLoader;

import twitter4j.ResponseList;
import twitter4j.User;
import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserFollowersFragment extends BaseUsersListFragment {

	@Override
	public Loader<ResponseList<User>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			int page = args.getInt(INTENT_KEY_PAGE);
			String query = args.getString(INTENT_KEY_QUERY);
			return new UserSearchLoader(getActivity(), account_id, query, page);
		}
		return null;
	}

}
