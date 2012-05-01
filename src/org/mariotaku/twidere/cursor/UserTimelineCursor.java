package org.mariotaku.twidere.cursor;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class UserTimelineCursor extends StatusesCursor {

	public UserTimelineCursor(Twitter twitter, long id, Paging paging, String[] cols) {
		super(twitter, id, paging, cols);
	}

	public UserTimelineCursor(Twitter twitter, String screen_name, Paging paging, String[] cols) {
		super(twitter, screen_name, paging, cols);
	}

	@Override
	public List<Status> getStatuses(Twitter twitter, long id, Paging paging) throws TwitterException {
		if (paging == null) return twitter.getUserTimeline(id);
		return twitter.getUserTimeline(id, paging);
	}

	@Override
	public List<Status> getStatuses(Twitter twitter, String screen_name, Paging paging) throws TwitterException {
		if (paging == null) return twitter.getUserTimeline(screen_name);
		return twitter.getUserTimeline(screen_name, paging);
	}

}
