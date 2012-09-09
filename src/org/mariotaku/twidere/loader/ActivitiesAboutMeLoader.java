package org.mariotaku.twidere.loader;

import java.util.List;

import twitter4j.Activity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;

public class ActivitiesAboutMeLoader extends Twitter4JActivitiesLoader {

	public ActivitiesAboutMeLoader(Context context, long account_id, List<Activity> data, String class_name,
			boolean is_home_tab) {
		super(context, account_id, data, class_name, is_home_tab);
	}

	@Override
	ResponseList<Activity> getActivities(Paging paging) throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		return twitter.getActivitiesAboutMe(paging);
	}

}
