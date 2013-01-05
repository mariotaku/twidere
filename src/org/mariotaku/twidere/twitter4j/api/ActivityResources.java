package org.mariotaku.twidere.twitter4j.api;

import org.mariotaku.twidere.twitter4j.Activity;
import org.mariotaku.twidere.twitter4j.Paging;
import org.mariotaku.twidere.twitter4j.ResponseList;
import org.mariotaku.twidere.twitter4j.TwitterException;

public interface ActivityResources {
	public ResponseList<Activity> getActivitiesAboutMe() throws TwitterException;

	public ResponseList<Activity> getActivitiesAboutMe(Paging paging) throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends() throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends(Paging paging) throws TwitterException;
}
