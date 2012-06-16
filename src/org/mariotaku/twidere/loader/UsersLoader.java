package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.List;

import org.mariotaku.twidere.Constants;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class UsersLoader extends AsyncTaskLoader<List<User>> implements Constants {

	private final Twitter mTwitter;
	private final List<User> mUsersList;

	public UsersLoader(Context context, long account_id, List<User> users_list) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mUsersList = users_list;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	public abstract List<User> getUsers() throws TwitterException;

	@Override
	public List<User> loadInBackground() {
		List<User> list_loaded = null;
		try {
			list_loaded = getUsers();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if (list_loaded != null) {
			for (User user : list_loaded) {
				mUsersList.remove(user);
				mUsersList.add(user);
			}
		}
		return mUsersList;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}