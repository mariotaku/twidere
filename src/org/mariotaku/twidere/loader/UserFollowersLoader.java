package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.util.Utils;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;

public class UserFollowersLoader extends UsersLoader {

	private final long mUserId;
	private final long mMaxId;
	private IDs mIDs;

	public UserFollowersLoader(Context context, long account_id, long user_id, long max_id, List<User> users_list) {
		super(context, account_id, users_list);
		mUserId = user_id;
		mMaxId = max_id;
	}

	@Override
	public ResponseList<User> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		final SharedPreferences prefs = getContext()
				.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int load_item_limit = prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		if (mIDs == null) {
			mIDs = twitter.getFollowersIDs(mUserId, -1);
			if (mIDs == null) return null;
		}
		final long[] ids = mIDs.getIDs();
		final int max_id_idx = Utils.indexOfArray(ids, mMaxId);
		final int count = max_id_idx + load_item_limit < ids.length ? max_id_idx + load_item_limit : ids.length
				- max_id_idx;

		final long[] ids_to_load = new long[count];
		int temp_idx = max_id_idx;
		for (int i = 0; i < ids_to_load.length; i++) {
			ids_to_load[i] = ids[temp_idx];
			temp_idx++;
		}
		return twitter.lookupUsers(ids_to_load);
	}

}