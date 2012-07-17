/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.loader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Query;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.SharedPreferences;

public class TweetSearchLoader extends ParcelableStatusesLoader {

	private final String mQuery;
	private final long mMaxId;

	public static final Comparator<Tweet> TWITTER4J_TWEET_ID_COMPARATOR = new Comparator<Tweet>() {

		@Override
		public int compare(Tweet object1, Tweet object2) {
			final long diff = object2.getId() - object1.getId();
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public TweetSearchLoader(Context context, long account_id, String query, long max_id, List<ParcelableStatus> data) {
		super(context, account_id, data);
		mQuery = query;
		mMaxId = max_id;
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		final List<ParcelableStatus> data = getData();
		final long account_id = getAccountId();
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		List<Tweet> tweets = null;
		try {
			final Query query = new Query(mQuery);
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			final int load_item_limit = prefs
					.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			query.setRpp(load_item_limit);
			if (mMaxId > 0) {
				query.setMaxId(mMaxId);
			}
			tweets = twitter != null ? twitter.search(query).getTweets() : null;
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (tweets != null) {
			Collections.sort(tweets, TWITTER4J_TWEET_ID_COMPARATOR);
			int deleted_count = 0;
			final int size = tweets.size();
			for (int i = 0; i < size; i++) {
				final Tweet status = tweets.get(i);
				if (deleteStatus(status.getId())) {
					deleted_count++;
				}
				data.add(new ParcelableStatus(status, account_id, i == tweets.size() - 1 ? deleted_count > 1 : false));
			}
		}
		Collections.sort(data, ParcelableStatus.STATUS_ID_COMPARATOR);
		return data;
	}

}
