package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.util.ParcelableStatus;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class UserTimelineLoader extends AsyncTaskLoader<List<ParcelableStatus>> {

	private final Twitter mTwitter;
	private final long mUserId, mAccountId;
	private final String mUserScreenName;

	public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(ParcelableStatus object1, ParcelableStatus object2) {
			long diff = object2.status_timestamp - object1.status_timestamp;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public UserTimelineLoader(Context context, long account_id, long user_id) {
		this(context, account_id, user_id, null);
	}

	public UserTimelineLoader(Context context, long account_id, String user_screenname) {
		this(context, account_id, -1, user_screenname);
	}

	private UserTimelineLoader(Context context, long account_id, long user_id, String user_screenname) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mUserId = user_id;
		mUserScreenName = user_screenname;
	}

	@Override
	public void deliverResult(List<ParcelableStatus> data) {
		if (data != null) {
			Collections.sort(data, TIMESTAMP_COMPARATOR);
		}
		super.deliverResult(data);
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		ResponseList<Status> statuses = null;
		try {
			if (mUserId != -1) {
				statuses = mTwitter.getUserTimeline(mUserId);
			} else if (mUserScreenName != null) {
				statuses = mTwitter.getUserTimeline(mUserScreenName);
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		List<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
		if (statuses != null) {
			for (Status status : statuses) {
				result.add(new ParcelableStatus(status, mAccountId));
			}
		}
		return result;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}