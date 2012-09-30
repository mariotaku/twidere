package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import android.content.Context;

public class UserListSubscriptionsLoader extends BaseUserListsLoader {

	private final String mScreenName;
	private final long mUserId;

	public UserListSubscriptionsLoader(final Context context, final long account_id, final long user_id,
			final String screen_name, final long cursor, final List<ParcelableUserList> data) {
		super(context, account_id, cursor, data);
		mScreenName = screen_name;
		mUserId = user_id;
	}

	@Override
	public PagableResponseList<UserList> getUserLists() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mScreenName != null)
			return twitter.getUserListSubscriptions(mScreenName, getCursor());
		else if (mUserId > 0) {
			final User user = twitter.showUser(mUserId);
			if (user != null && user.getId() > 0)
				return twitter.getUserListSubscriptions(user.getScreenName(), getCursor());
		}
		return null;
	}

}
