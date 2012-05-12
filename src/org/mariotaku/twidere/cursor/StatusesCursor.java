package org.mariotaku.twidere.cursor;

import static org.mariotaku.twidere.util.Utils.formatGeoLocationToString;
import static org.mariotaku.twidere.util.Utils.getSpannedStatusText;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.provider.TweetStore.Statuses;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.database.AbstractCursor;
import android.os.Bundle;

public abstract class StatusesCursor extends AbstractCursor {

	private static List<Status> statuses = new ArrayList<Status>();

	private static String[] mColumns = new String[0];

	private static long mAccountId;

	/**
	 * Perform alphabetical comparison of application entry objects.
	 */
	public static final Comparator<Status> TIMESTAMP_COMPARATOR = new Comparator<Status>() {

		@Override
		public int compare(Status object1, Status object2) {
			long diff = object1.getCreatedAt().getTime() - object2.getCreatedAt().getTime();
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) (object1.getCreatedAt().getTime() - object2.getCreatedAt().getTime());
		}
	};

	public StatusesCursor(Twitter twitter, long id, Paging paging, String[] cols) {
		try {
			init(twitter, cols);
			statuses = getStatuses(twitter, id, paging);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	public StatusesCursor(Twitter twitter, String screen_name, Paging paging, String[] cols) {
		try {
			init(twitter, cols);
			statuses = getStatuses(twitter, screen_name, paging);
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

	public abstract List<Status> getStatuses(Twitter twitter, long id, Paging paging) throws TwitterException;

	public abstract List<Status> getStatuses(Twitter twitter, String screen_name, Paging paging)
			throws TwitterException;

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
		Status status = statuses.get(mPos);
		if (Statuses._ID.equals(col)) return status.getId();
		if (Statuses.ACCOUNT_ID.equals(col)) return mAccountId;
		if (Statuses.HAS_MEDIA.equals(col)) return status.getMediaEntities() == null ? 0 : 1;
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
		if (Statuses.LOCATION.equals(col)) return formatGeoLocationToString(status.getGeoLocation());
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
		if (Statuses.TEXT.equals(col)) return getSpannedStatusText(status, mAccountId);
		if (Statuses.USER_ID.equals(col)) return status.getUser().getId();

		return null;
	}

	private void init(Twitter twitter, String[] cols) throws TwitterException {
		if (cols == null) {
			mColumns = Statuses.COLUMNS;
		} else {
			mColumns = cols;
		}
		mAccountId = twitter.getId();
	}
}
