package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.DummyParcelableStatusesLoader;
import org.mariotaku.twidere.loader.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserMentionsFragment extends SearchTweetsFragment {

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		if (args == null) return new DummyParcelableStatusesLoader(getActivity(), account_id, getData());
		final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final boolean is_home_tab = args.getBoolean(INTENT_KEY_IS_HOME_TAB);
		if (screen_name == null) return new DummyParcelableStatusesLoader(getActivity(), account_id, getData());
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
