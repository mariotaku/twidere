package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.UserListMembershipsLoader;
import org.mariotaku.twidere.model.ParcelableUserList;

import android.support.v4.content.Loader;

public class UserListMembershipsFragment extends BaseUserListsFragment {

	@Override
	public Loader<List<ParcelableUserList>> newLoaderInstance(long account_id, long user_id, String screen_name) {
		return new UserListMembershipsLoader(getActivity(), account_id, user_id, screen_name, getCursor(), getData());
	}

}
