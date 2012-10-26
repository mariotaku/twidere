package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class UserListsLoader extends AsyncTaskLoader<UserListsLoader.UserListsData> {

	private final Twitter mTwitter;

	private final long mAccountId;
	private final long mUserId;
	private final String mScreenName;

	public UserListsLoader(final Context context, final long account_id, final long user_id, final String screen_name) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mUserId = user_id;
		mScreenName = screen_name;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	@Override
	public UserListsData loadInBackground() {
		try {
			final List<UserList> user_lists;
			final List<UserList> user_list_memberships;
			if (mUserId > 0) {
				user_lists = mTwitter.getUserLists(mUserId);
				user_list_memberships = mTwitter.getUserListMemberships(mUserId, -1);
			} else if (mScreenName != null) {
				user_lists = mTwitter.getUserLists(mScreenName);
				user_list_memberships = mTwitter.getUserListMemberships(mScreenName, -1);
			} else
				return null;
			final int user_lists_size = user_lists.size(), user_list_memberships_size = user_list_memberships.size();
			final UserListsData data = new UserListsData();
			for (int i = 0; i < user_lists_size; i++) {
				data.lists.add(new ParcelableUserList(user_lists.get(i), mAccountId, i));
			}
			for (int i = 0; i < user_list_memberships_size; i++) {
				data.memberships.add(new ParcelableUserList(user_list_memberships.get(i), mAccountId, i));
			}
			return data;
		} catch (final TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	public static final class UserListsData {
		private final List<ParcelableUserList> lists = new NoDuplicatesArrayList<ParcelableUserList>();
		private final List<ParcelableUserList> memberships = new NoDuplicatesArrayList<ParcelableUserList>();

		public List<ParcelableUserList> getLists() {
			return lists;
		}

		public List<ParcelableUserList> getMemberships() {
			return memberships;
		}

	}
}
