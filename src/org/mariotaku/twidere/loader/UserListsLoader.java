package org.mariotaku.twidere.loader;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;

public class UserListsLoader extends BaseUserListsLoader {

	private final long mUserId;
	private final String mScreenName;

	public UserListsLoader(final Context context, final long account_id, final long user_id, final String screen_name,
			final long cursor, final List<ParcelableUserList> data) {
		super(context, account_id, cursor, data);
		mUserId = user_id;
		mScreenName = screen_name;
	}

	@Override
	public List<UserList> getUserLists() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		final ArrayList<UserList> lists = new ArrayList<UserList>();
		if (mUserId > 0) {
			for (final UserList list : twitter.getUserLists(mUserId)) {
				if (list.getUser() != null && list.getUser().getId() == mUserId) {
					lists.add(list);
				}
			}
			return lists;
		} else if (mScreenName != null) {
			for (final UserList list : twitter.getUserLists(mScreenName)) {
				if (list.getUser() != null && mScreenName.equalsIgnoreCase(list.getUser().getScreenName())) {
					lists.add(list);
				}
			}
			return lists;
		}
		return null;
	}

}
