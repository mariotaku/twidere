package org.mariotaku.twidere.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.provider.TweetStore.Statuses;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.database.AbstractCursor;
import android.os.Bundle;

public class UserTimelineCursor extends AbstractCursor {

	private List<Status> statuses = new ArrayList<Status>();
	private String[] mColumns = new String[0];
	private long mAccountId;

	public UserTimelineCursor(Twitter twitter, long id, String[] cols) {
		mColumns = cols;
		try {
			mAccountId = twitter.getId();
			statuses = twitter.getUserTimeline(id);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public UserTimelineCursor(Twitter twitter, long id, Paging paging, String[] cols) {
		mColumns = cols;
		try {
			mAccountId = twitter.getId();
			statuses = twitter.getUserTimeline(id, paging);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public UserTimelineCursor(Twitter twitter, String screen_name, String[] cols) {
		mColumns = cols;
		try {
			mAccountId = twitter.getId();
			statuses = twitter.getUserTimeline(screen_name);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
	
	public UserTimelineCursor(Twitter twitter, String screen_name, Paging paging, String[] cols) {
		mColumns = cols;
		try {
			mAccountId = twitter.getId();
			statuses = twitter.getUserTimeline(screen_name, paging);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String[] getColumnNames() {
		return mColumns;
	}

	@Override
	public int getCount() {
		return statuses.size();
	}

	@Override
	public double getDouble(int index) {
		return (Double) getObject(index);
	}

	@Override
	public Bundle getExtras() {
		Bundle bundle = new Bundle();
		return bundle;
	}

	@Override
	public float getFloat(int index) {
		return (Float) getObject(index);
	}

	@Override
	public int getInt(int index) {
		return (Integer) getObject(index);
	}

	@Override
	public long getLong(int index) {
		return (Long) getObject(index);
	}

	@Override
	public short getShort(int index) {
		return (Short) getObject(index);
	}

	@Override
	public String getString(int index) {
		Object obj = getObject(index);
		return obj == null ? null : obj.toString();
	}

	@Override
	public boolean isNull(int index) {
		return getObject(index) == null;
	}

	private Object getObject(int index) {
		String col = mColumns[index];
		Status status = statuses.get(getPosition());
		if (Statuses.ACCOUNT_ID.equals(col)) return mAccountId;
		if (Statuses.HAS_MEDIA.equals(col)) return status.getMediaEntities() == null;
		if (Statuses.IN_REPLY_TO_SCREEN_NAME.equals(col)) return status.getInReplyToScreenName();
		if (Statuses.IN_REPLY_TO_STATUS_ID.equals(col)) return status.getInReplyToStatusId();
		if (Statuses.IN_REPLY_TO_USER_ID.equals(col)) return status.getInReplyToUserId();
		if (Statuses.IS_FAVORITE.equals(col)) return status.isFavorited() ? 1 : 0;
		if (Statuses.IS_GAP.equals(col)) return 0;
		if (Statuses.IS_PROTECTED.equals(col)) return status.getUser().isProtected() ? 1 : 0;
		if (Statuses.IS_RETWEET.equals(col)) {
			int retweet_status = Math.abs(status.isRetweet() ? 1 : 0);
			retweet_status = status.isRetweetedByMe() ? -retweet_status : retweet_status;
			return retweet_status;
		}
		if (Statuses.LOCATION.equals(col))
			return CommonUtils.formatGeoLocationToString(status.getGeoLocation());
		if (Statuses.NAME.equals(col)) return status.getUser().getName();
		if (Statuses.PROFILE_IMAGE_URL.equals(col)) {
			URL url = status.getUser().getProfileImageURL();
			return url == null ? null : url.toString();
		}
		if (Statuses.RETWEET_COUNT.equals(col)) return status.getRetweetCount();
		if (Statuses.SCREEN_NAME.equals(col)) return status.getUser().getScreenName();
		if (Statuses.SOURCE.equals(col)) return status.getSource();
		if (Statuses.STATUS_ID.equals(col)) return status.getId();
		if (Statuses.STATUS_TIMESTAMP.equals(col)) return status.getCreatedAt().getTime();
		if (Statuses.TEXT.equals(col)) return CommonUtils.formatStatusString(status);
		if (Statuses.USER_ID.equals(col)) return status.getUser().getId();

		return null;
	}
}
