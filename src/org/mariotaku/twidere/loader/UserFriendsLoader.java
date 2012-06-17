package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.util.ParcelableUser;

import twitter4j.IDs;
import twitter4j.TwitterException;
import android.content.Context;

public class UserFriendsLoader extends IDsUsersLoader {

	private final long mUserId;
	private final String mScreenName;

	public UserFriendsLoader(Context context, long account_id, long user_id, String screen_name, long max_id,
			List<ParcelableUser> users_list) {
		super(context, account_id, max_id, users_list);
		mUserId = user_id;
		mScreenName = screen_name;
	}

	@Override
	public IDs getIDs() throws TwitterException {
		if (mUserId > 0)
			return getTwitter().getFriendsIDs(mUserId, -1);
		else if (mScreenName != null) return getTwitter().getFriendsIDs(mScreenName, -1);
		return null;
	}

}