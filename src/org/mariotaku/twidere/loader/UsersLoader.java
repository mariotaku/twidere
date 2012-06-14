package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import org.mariotaku.twidere.Constants;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class UsersLoader extends AsyncTaskLoader<ResponseList<User>> implements Constants {

	private final Twitter mTwitter;

	public UsersLoader(Context context, long account_id) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
	}

	@Override
	public void deliverResult(ResponseList<User> data) {
		super.deliverResult(data);
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	public abstract ResponseList<User> getUsers() throws TwitterException;

	@Override
	public ResponseList<User> loadInBackground() {
		try {
			return getUsers();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}