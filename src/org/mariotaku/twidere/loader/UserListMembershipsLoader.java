package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;

public class UserListMembershipsLoader extends BaseUserListsLoader {

	private final long mUserId;
	private final String mScreenName;

	public UserListMembershipsLoader(final Context context, final long account_id, final long user_id,
			final String screen_name, final long cursor, final List<ParcelableUserList> data) {
		super(context, account_id, cursor, data);
		mUserId = user_id;
		mScreenName = screen_name;
	}

	@Override
	public PagableResponseList<UserList> getUserLists() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mUserId > 0)
			return twitter.getUserListMemberships(mUserId, getCursor());
		else if (mScreenName != null) return twitter.getUserListMemberships(mScreenName, getCursor());
		return null;
	}

}
