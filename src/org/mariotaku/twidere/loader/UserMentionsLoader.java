package org.mariotaku.twidere.loader;

import android.content.Context;

import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

public class UserMentionsLoader extends TweetSearchLoader {

	public UserMentionsLoader(final Context context, final long accountId, final String screenName, final long maxId,
			final long sinceId, final List<ParcelableStatus> data, final String[] savedStatusesArgs,
			final int tabPosition) {
		super(context, accountId, screenName, maxId, sinceId, data, savedStatusesArgs, tabPosition);
	}

	@Override
	protected String processQuery(final String query) {
		if (query == null) return null;
		final String screenName = query.startsWith("@") ? query : String.format("@%s", query);
		return String.format("%s exclude:retweets", screenName);
	}

}
