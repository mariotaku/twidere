package org.mariotaku.twidere.loader;

import java.util.ArrayList;
import java.util.List;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;

public class UserFollowersLoader extends UsersLoader {

	private final long mUserId;
	private final long mMaxId;
	private IDs mFollowerIDs;
	private final List<Long> mIDsList = new ArrayList<Long>();

	public UserFollowersLoader(Context context, long account_id, long user_id, long max_id) {
		super(context, account_id);
		mUserId = user_id;
		mMaxId = max_id;
	}

	@Override
	public ResponseList<User> getUsers() throws TwitterException {
		if (mFollowerIDs == null) {
			mFollowerIDs = getTwitter().getFollowersIDs(mUserId, -1);
			if (mFollowerIDs == null) return null;
			mIDsList.clear();
			for (long id : mFollowerIDs.getIDs()) {
				mIDsList.add(id);
			}
		}
		final SharedPreferences prefs = getContext()
				.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int load_item_limit = prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);

		return getTwitter().lookupUsers(mFollowerIDs.getIDs());
	}

}