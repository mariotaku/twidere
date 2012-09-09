package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserMentionsFragment extends SearchTweetsFragment {

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		long account_id = -1, max_id = -1;
		String screen_name = null;
		boolean is_home_tab = false;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			is_home_tab = args.getBoolean(INTENT_KEY_IS_HOME_TAB);
		}
		return new TweetSearchLoader(getActivity(), account_id, screen_name.startsWith("@") ? screen_name : "@"
				+ screen_name, max_id, getData(), getClass().getSimpleName(), is_home_tab);
	}

	@Override
	public void onDestroy() {
		TweetSearchLoader.writeSerializableStatuses(this, getActivity(), getData(), getArguments());
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		TweetSearchLoader.writeSerializableStatuses(this, getActivity(), getData(), getArguments());
		super.onDestroyView();
	}
}
