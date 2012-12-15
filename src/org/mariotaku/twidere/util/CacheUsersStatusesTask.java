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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusContentValues;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.util.TwitterWrapper.TwitterListResponse;

import twitter4j.User;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.twitter.Extractor;

public class CacheUsersStatusesTask extends AsyncTask<Void, Void, Void> {

	private final TwitterListResponse<twitter4j.Status>[] all_statuses;
	private final ContentResolver resolver;
	private final boolean large_profile_image;

	public CacheUsersStatusesTask(final Context context, final TwitterListResponse<twitter4j.Status>... all_statuses) {
		resolver = context.getContentResolver();
		this.all_statuses = all_statuses;
		large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	protected Void doInBackground(final Void... args) {
		if (all_statuses == null || all_statuses.length == 0) return null;
		final ArrayList<ContentValues> cached_users_values = new ArrayList<ContentValues>();
		final ArrayList<ContentValues> cached_statuses_values = new ArrayList<ContentValues>();
		final ArrayList<ContentValues> hashtag_values = new ArrayList<ContentValues>();
		final ArrayList<Long> user_ids = new ArrayList<Long>(), status_ids = new ArrayList<Long>();
		final Extractor extractor = new Extractor();

		final ArrayList<String> hashtags = new ArrayList<String>();

		for (final TwitterListResponse<twitter4j.Status> values : all_statuses) {
			if (values == null || values.list == null) {
				continue;
			}
			final List<twitter4j.Status> list = values.list;
			for (final twitter4j.Status status : list) {
				final twitter4j.Status retweeted_status = status.getRetweetedStatus();
				final User user = retweeted_status == null ? status.getUser() : retweeted_status.getUser();
				if (user == null) {
					continue;
				}
				final long user_id = user.getId();
				status_ids.add(status.getId());
				if (!user_ids.contains(user_id)) {
					user_ids.add(user_id);
					cached_users_values.add(makeCachedUserContentValues(user, large_profile_image));
					cached_statuses_values.add(makeStatusContentValues(status, values.account_id, large_profile_image));
				}
				hashtags.addAll(extractor.extractHashtags(status.getText()));
			}
		}
		for (final String hashtag : hashtags) {
			final ContentValues hashtag_value = new ContentValues();
			hashtag_value.put(CachedHashtags.NAME, hashtag);
			hashtag_values.add(hashtag_value);
		}
		resolver.delete(CachedUsers.CONTENT_URI,
				CachedUsers.USER_ID + " IN (" + ListUtils.toString(user_ids, ',', true) + " )", null);
		resolver.bulkInsert(CachedUsers.CONTENT_URI,
				cached_users_values.toArray(new ContentValues[cached_users_values.size()]));
		resolver.delete(CachedStatuses.CONTENT_URI,
				CachedStatuses.STATUS_ID + " IN (" + ListUtils.toString(status_ids, ',', true) + " )", null);
		resolver.bulkInsert(CachedStatuses.CONTENT_URI,
				cached_statuses_values.toArray(new ContentValues[cached_statuses_values.size()]));
		resolver.delete(CachedHashtags.CONTENT_URI,
				CachedHashtags.NAME + " IN (" + ListUtils.toStringForSQL(hashtags.size()) + ")",
				hashtags.toArray(new String[hashtags.size()]));
		resolver.bulkInsert(CachedHashtags.CONTENT_URI,
				hashtag_values.toArray(new ContentValues[hashtag_values.size()]));
		return null;
	}

	public static Runnable getRunnable(final Context context,
			final TwitterListResponse<twitter4j.Status>... all_statuses) {
		return new ExecuteCacheUserStatusesTaskRunnable(context, all_statuses);

	}

	static class ExecuteCacheUserStatusesTaskRunnable implements Runnable {
		final Context context;
		final TwitterListResponse<twitter4j.Status>[] all_statuses;

		ExecuteCacheUserStatusesTaskRunnable(final Context context,
				final TwitterListResponse<twitter4j.Status>... all_statuses) {
			this.context = context;
			this.all_statuses = all_statuses;
		}

		@Override
		public void run() {
			new CacheUsersStatusesTask(context, all_statuses).execute();
		}
	}
}
