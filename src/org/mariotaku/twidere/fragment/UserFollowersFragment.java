package org.mariotaku.twidere.fragment;

import java.util.List;

import twitter4j.User;
import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserFollowersFragment extends BaseUsersListFragment {

	@Override
	public Loader<List<User>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			// return new UserSearchLoader(getActivity(), account_id, query,
			// page);
		}
		return null;
	}

}
