package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.UserFriendsLoader;
import org.mariotaku.twidere.util.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserFriendsFragment extends BaseUsersListFragment {

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			return new UserFriendsLoader(getActivity(), account_id, user_id, screen_name, max_id, getData());
		}
		return null;
	}

}
