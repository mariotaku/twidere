package org.mariotaku.twidere.loader;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

public class StatusRepliesLoader extends UserMentionsLoader {

	private final long mInReplyToStatusId;

	public StatusRepliesLoader(final Context context, final long accountId, final String screenName,
			final long statusId, final long maxId, final long sinceId, final List<ParcelableStatus> data,
			final String[] savedStatusesArgs, final int tabPosition) {
		super(context, accountId, screenName, maxId, sinceId, data, savedStatusesArgs, tabPosition);
		mInReplyToStatusId = statusId;
	}

	@Override
	public List<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		final List<Status> statuses = super.getStatuses(twitter, paging);
		final List<Status> result = new ArrayList<Status>();
		for (final Status status : statuses) {
			if (status.getInReplyToStatusId() == mInReplyToStatusId) {
				result.add(status);
			}
		}
		return result;
	}

}
