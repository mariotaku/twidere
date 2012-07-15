package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;

public class UserListSubscriptionsLoader extends BaseUserListLoader {

	private final String mScreenName;

	public UserListSubscriptionsLoader(Context context, long account_id, String screen_name, long cursor,
			List<ParcelableUserList> data) {
		super(context, account_id, cursor, data);
		mScreenName = screen_name;
	}

	@Override
	public PagableResponseList<UserList> getUserLists() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mScreenName != null) return twitter.getUserListSubscriptions(mScreenName, getCursor());
		return null;
	}

}
