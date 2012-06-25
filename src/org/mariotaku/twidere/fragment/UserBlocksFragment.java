package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.UserBlocksLoader;
import org.mariotaku.twidere.util.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserBlocksFragment extends BaseUsersListFragment {

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			return new UserBlocksLoader(getActivity(), account_id, max_id, getData());
		}
		return null;
	}

}
