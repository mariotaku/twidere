package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.util.ParcelableUser;

import twitter4j.IDs;
import twitter4j.TwitterException;
import android.content.Context;

public class UserBlocksLoader extends IDsUsersLoader {

	public UserBlocksLoader(Context context, long account_id, long max_id, List<ParcelableUser> users_list) {
		super(context, account_id, max_id, users_list);
	}

	@Override
	public IDs getIDs() throws TwitterException {
		return getTwitter().getBlockingUsersIDs();
	}

}