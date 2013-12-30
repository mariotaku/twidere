package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.isOfficialConsumerKeySecret;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.auth.XAuthAuthorization;
import twitter4j.conf.Configuration;

import java.util.Collections;
import java.util.List;

public class StatusConversationLoader extends UserMentionsLoader {

	private final long mInReplyToStatusId;

	public StatusConversationLoader(final Context context, final long accountId, final String screenName,
			final long statusId, final long maxId, final long sinceId, final List<ParcelableStatus> data,
			final String[] savedStatusesArgs, final int tabPosition) {
		super(context, accountId, screenName, maxId, sinceId, data, savedStatusesArgs, tabPosition);
		mInReplyToStatusId = statusId;
	}

	@Override
	public List<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		final Configuration conf = twitter.getConfiguration();
		final Authorization auth = twitter.getAuthorization();
		final boolean isOAuth = auth instanceof OAuthAuthorization || auth instanceof XAuthAuthorization;
		final String consumerKey = conf.getOAuthConsumerKey(), consumerSecret = conf.getOAuthConsumerSecret();
		if (isOAuth && isOfficialConsumerKeySecret(getContext(), consumerKey, consumerSecret))
			return twitter.showConversation(mInReplyToStatusId, paging);
		return Collections.emptyList();
	}

}
