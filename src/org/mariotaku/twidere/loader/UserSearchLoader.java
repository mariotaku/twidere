package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.util.ParcelableUser;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;

public class UserSearchLoader extends ParcelableUsersLoader {

	private final Twitter mTwitter;
	private final String mQuery;
	private final int mPage;
	private final long mAccountId;

	public UserSearchLoader(Context context, long account_id, String query, int page, List<ParcelableUser> users_list) {
		super(context, account_id, users_list);
		mTwitter = getTwitterInstance(context, account_id, true);
		mQuery = query;
		mPage = page;
		mAccountId = account_id;
	}

	@Override
	public List<ParcelableUser> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		final ResponseList<User> users = twitter.searchUsers(mQuery, mPage);
		final List<ParcelableUser> result = new ArrayList<ParcelableUser>();
		for (int i = 0; i < users.size(); i++) {
			result.add(new ParcelableUser(users.get(i), mAccountId, mPage * 20 + i));
		}
		return result;
	}

}