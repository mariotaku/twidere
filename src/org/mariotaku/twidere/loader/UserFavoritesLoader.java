package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.util.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;

public class UserFavoritesLoader extends Twitter4JStatusLoader {

	private final long mUserId;
	private final String mUserScreenName;
	private int mTotalItemsCount;

	public UserFavoritesLoader(Context context, long account_id, long user_id, long max_id, List<ParcelableStatus> data) {
		this(context, account_id, user_id, null, max_id, data);
	}

	public UserFavoritesLoader(Context context, long account_id, String user_screenname, long max_id,
			List<ParcelableStatus> data) {
		this(context, account_id, -1, user_screenname, max_id, data);
	}

	private UserFavoritesLoader(Context context, long account_id, long user_id, String user_screenname, long max_id,
			List<ParcelableStatus> data) {
		super(context, account_id, max_id, data);
		mUserId = user_id;
		mUserScreenName = user_screenname;
	}

	@Override
	public ResponseList<Status> getStatuses(Paging paging) throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mUserId != -1) {
			if (mTotalItemsCount == -1) {
				try {
					mTotalItemsCount = twitter.showUser(mUserId).getFavouritesCount();
				} catch (final TwitterException e) {
					mTotalItemsCount = -1;
				}
			}
			return twitter.getFavorites(String.valueOf(mUserId), paging);
		} else if (mUserScreenName != null) return twitter.getFavorites(mUserScreenName, paging);
		return null;
	}

	public int getTotalItemsCount() {
		return mTotalItemsCount;
	}

}
