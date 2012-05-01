package org.mariotaku.twidere.cursor;

import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class FavoriteCursor extends StatusesCursor {

	public FavoriteCursor(Twitter twitter, Paging paging, String[] cols) {
		super(twitter, null, paging, cols);
	}

	@Override
	public List<Status> getStatuses(Twitter twitter, long id, Paging paging) throws TwitterException {
		if (paging == null) return twitter.getFavorites();
		return twitter.getFavorites(paging);
	}

	@Override
	public List<Status> getStatuses(Twitter twitter, String screen_name, Paging paging) throws TwitterException {
		if (paging == null) return twitter.getFavorites();
		return twitter.getFavorites(paging);
	}

}
