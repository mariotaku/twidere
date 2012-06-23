package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ParcelableUser;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ParcelableUsersLoader extends AsyncTaskLoader<List<ParcelableUser>> implements Constants {

	private final Twitter mTwitter;
	private final List<ParcelableUser> mUsersList;
	private final long mAccountId;

	public ParcelableUsersLoader(Context context, long account_id, List<ParcelableUser> users_list) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mUsersList = users_list;
		mAccountId = account_id;
	}

	public long getAccountId() {
		return mAccountId;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	public abstract List<ParcelableUser> getUsers() throws TwitterException;

	@Override
	public List<ParcelableUser> loadInBackground() {
		List<ParcelableUser> list_loaded = null;
		try {
			list_loaded = getUsers();
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (list_loaded != null) {
			for (final ParcelableUser user : list_loaded) {
				if (!hasId(user.user_id)) {
					mUsersList.add(user);
				}
			}
		} else {
			return null;
		}
		Collections.sort(mUsersList, ParcelableUser.POSITION_COMPARATOR);
		return mUsersList;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	private boolean hasId(long id) {
		for (final ParcelableUser user : mUsersList) {
			if (user.user_id == id) return true;
		}
		return false;
	}

}