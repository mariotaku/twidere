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

import static org.mariotaku.twidere.util.ContentResolverUtils.bulkDelete;
import static org.mariotaku.twidere.util.ContentResolverUtils.bulkInsert;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusContentValues;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
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

public class CacheUsersStatusesTask extends AsyncTask<Void, Void, Void> implements Constants {

	private final TwitterListResponse<twitter4j.Status>[] all_statuses;
	private final ContentResolver resolver;
	private final boolean large_profile_image;
	private final Context context;

	public CacheUsersStatusesTask(final Context context, final TwitterListResponse<twitter4j.Status>... all_statuses) {
		resolver = context.getContentResolver();
		this.context = context;
		this.all_statuses = all_statuses;
		large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	protected Void doInBackground(final Void... args) {
		if (all_statuses == null || all_statuses.length == 0) return null;
		final Extractor extractor = new Extractor();
		final ArrayList<ContentValues> cached_users_values = new NoDuplicatesArrayList<ContentValues>();
		final ArrayList<ContentValues> cached_statuses_values = new NoDuplicatesArrayList<ContentValues>();
		final ArrayList<ContentValues> hashtag_values = new NoDuplicatesArrayList<ContentValues>();
		final ArrayList<Long> user_ids = new NoDuplicatesArrayList<Long>();
		final ArrayList<Long> status_ids = new NoDuplicatesArrayList<Long>();
		final ArrayList<String> hashtags = new NoDuplicatesArrayList<String>();
		final boolean large_preview_image = Utils.getImagePreviewDisplayOptionInt(context) == IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE;

		for (final TwitterListResponse<twitter4j.Status> values : all_statuses) {
			if (values == null || values.list == null) {
				continue;
			}
			final List<twitter4j.Status> list = values.list;
			for (final twitter4j.Status status : list) {
				if (status == null || status.getId() <= 0) {
					continue;
				}
				status_ids.add(status.getId());
				cached_statuses_values.add(makeStatusContentValues(status, values.account_id, large_profile_image, large_preview_image));
				hashtags.addAll(extractor.extractHashtags(status.getText()));
				final User user = status.getUser();
				if (user == null || user.getId() <= 0) {
					continue;
				}
				user_ids.add(user.getId());
				cached_users_values.add(makeCachedUserContentValues(user, large_profile_image));
			}
		}
		for (final String hashtag : hashtags) {
			final ContentValues hashtag_value = new ContentValues();
			hashtag_value.put(CachedHashtags.NAME, hashtag);
			hashtag_values.add(hashtag_value);
		}
		bulkDelete(resolver, CachedUsers.CONTENT_URI, CachedUsers.USER_ID, user_ids, null, false);
		bulkInsert(resolver, CachedUsers.CONTENT_URI, cached_users_values);
		bulkDelete(resolver, CachedStatuses.CONTENT_URI, CachedStatuses.STATUS_ID, status_ids, null, false);
		bulkInsert(resolver, CachedStatuses.CONTENT_URI, cached_statuses_values);
		bulkDelete(resolver, CachedHashtags.CONTENT_URI, CachedHashtags.NAME, hashtags, null, true);
		bulkInsert(resolver, CachedHashtags.CONTENT_URI, hashtag_values);
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
