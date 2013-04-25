package org.mariotaku.twidere.loader;

import android.content.Context;
import java.util.Collections;
import java.util.List;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUser;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

public abstract class Twitter4JUsersLoader extends ParcelableUsersLoader {

	private final long mAccountId;
	private final boolean mHiResProfileImage;

	private final Context mContext;

	public Twitter4JUsersLoader(final Context context, final long account_id, final List<ParcelableUser> data) {
		super(context, data);
		mContext = context;
		mAccountId = account_id;
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public List<ParcelableUser> loadInBackground() {
		final List<ParcelableUser> data = getData();
		final List<User> users;
		try {
			users = getUsers(getTwitter());
		} catch (final TwitterException e) {
			e.printStackTrace();
			return data;
		}
		final int size = users.size();
		for (int i = 0; i < size; i++) {
			final User user = users.get(i);
			if (!hasId(user.getId())) {
				data.add(new ParcelableUser(user, mAccountId, getUserPosition(user, i), mHiResProfileImage));
			}
		}
		Collections.sort(data);
		return data;
	}
	
	protected abstract long getUserPosition(final User user, final int index);

	protected final Twitter getTwitter() {
		return getTwitterInstance(mContext, mAccountId, true);
	}

	protected abstract List<User> getUsers(Twitter twitter) throws TwitterException;
}
