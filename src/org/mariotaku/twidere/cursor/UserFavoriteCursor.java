package org.mariotaku.twidere.cursor;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class UserFavoriteCursor extends StatusesCursor {

	public UserFavoriteCursor(Twitter twitter, long id, Paging paging, String[] cols) {
		super(twitter, id, paging, cols);
	}

	public UserFavoriteCursor(Twitter twitter, String screen_name, Paging paging, String[] cols) {
		super(twitter, screen_name, paging, cols);
	}

	@Override
	public List<Status> getStatuses(Twitter twitter, long id, Paging paging) throws TwitterException {
		if (paging == null) return twitter.getFavorites(String.valueOf(id));
		return twitter.getFavorites(String.valueOf(id), paging);
	}

	@Override
	public List<Status> getStatuses(Twitter twitter, String screen_name, Paging paging) throws TwitterException {
		if (paging == null) return twitter.getFavorites(screen_name);
		return twitter.getFavorites(screen_name, paging);
	}

}
