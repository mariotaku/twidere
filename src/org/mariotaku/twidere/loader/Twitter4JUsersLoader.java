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
			users = getUsers(getTwitterInstance(mContext, mAccountId, true));
			if (users == null) return data;
		} catch (final TwitterException e) {
			e.printStackTrace();
			return data;
		}
		int pos = data.size();
		for (final User user : users) {
			if (hasId(user.getId())) continue;
			data.add(new ParcelableUser(user, mAccountId, pos, mHiResProfileImage));
			pos++;
		}
		Collections.sort(data);
		return data;
	}

	protected abstract List<User> getUsers(Twitter twitter) throws TwitterException;
}
