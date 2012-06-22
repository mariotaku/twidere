package org.mariotaku.twidere.loader;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ParcelableUser;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class IDsUsersLoader extends ParcelableUsersLoader {

	private final long mMaxId, mAccountId;
	private IDs mIDs;

	public IDsUsersLoader(Context context, long account_id, long max_id, List<ParcelableUser> users_list) {
		super(context, account_id, users_list);
		mAccountId = account_id;
		mMaxId = max_id;
	}

	public abstract IDs getIDs() throws TwitterException;

	@Override
	public List<ParcelableUser> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		final SharedPreferences prefs = getContext()
				.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int prefs_load_item_limit = prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
				PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		final int load_item_limit = prefs_load_item_limit > 100 ? 100 : prefs_load_item_limit;
		if (mIDs == null) {
			mIDs = getIDs();
			if (mIDs == null) return null;
		}
		final long[] ids = mIDs.getIDs();
		final int max_id_idx = mMaxId > 0 ? ArrayUtils.indexOf(ids, mMaxId) : 0;
		final int count = max_id_idx + load_item_limit < ids.length ? max_id_idx + load_item_limit : ids.length
				- max_id_idx;

		final long[] ids_to_load = new long[count];
		int temp_idx = max_id_idx;
		for (int i = 0; i < ids_to_load.length; i++) {
			ids_to_load[i] = ids[temp_idx];
			temp_idx++;
		}
		final ResponseList<User> users = twitter.lookupUsers(ids_to_load);
		final List<ParcelableUser> result = new ArrayList<ParcelableUser>();
		for (final User user : users) {
			final int position = ArrayUtils.indexOf(mIDs.getIDs(), user.getId());
			result.add(new ParcelableUser(user, mAccountId, position));
		}
		return result;
	}

}