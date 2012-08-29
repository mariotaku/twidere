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
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		return new TweetSearchLoader(getActivity(), account_id, screen_name.startsWith("@") ? screen_name : "@"
				+ screen_name, max_id, getData());
	}
}
