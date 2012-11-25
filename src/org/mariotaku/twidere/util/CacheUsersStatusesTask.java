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

import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

public class CacheUsersStatusesTask extends AsyncTask<Void, Void, Void> {

	private final ArrayList<ContentValues> all_statuses;
	private final ContentResolver resolver;

	public CacheUsersStatusesTask(final Context context, final ArrayList<ContentValues> all_statuses) {
		resolver = context.getContentResolver();
		this.all_statuses = all_statuses;
	}

	public CacheUsersStatusesTask(final Context context, final List<twitter4j.Status> statuses, final long account_id) {
		resolver = context.getContentResolver();
		all_statuses = new ArrayList<ContentValues>();
		for (final twitter4j.Status status : statuses) {
			all_statuses.add(makeStatusContentValues(status, account_id));
		}
	}

	@Override
	protected Void doInBackground(final Void... args) {
		final List<ContentValues> cached_users_list = new ArrayList<ContentValues>();
		final List<Long> user_ids = new ArrayList<Long>(), status_ids = new ArrayList<Long>();
		for (final ContentValues values : all_statuses) {
			if (values == null) {
				continue;
			}
			final long user_id = values.getAsLong(Statuses.USER_ID);
			status_ids.add(values.getAsLong(Statuses.STATUS_ID));
			if (!user_ids.contains(user_id)) {
				user_ids.add(user_id);
				cached_users_list.add(makeCachedUserContentValues(values));
			}
		}
		resolver.delete(CachedUsers.CONTENT_URI,
				CachedUsers.USER_ID + " IN (" + ListUtils.toString(user_ids, ',', true) + " )", null);
		resolver.bulkInsert(CachedUsers.CONTENT_URI,
				cached_users_list.toArray(new ContentValues[cached_users_list.size()]));
		resolver.delete(CachedStatuses.CONTENT_URI,
				CachedStatuses.STATUS_ID + " IN (" + ListUtils.toString(status_ids, ',', true) + " )", null);
		resolver.bulkInsert(CachedStatuses.CONTENT_URI, all_statuses.toArray(new ContentValues[all_statuses.size()]));

		return null;
	}

	public static Runnable getRunnable(final Context context, final List<twitter4j.Status> statuses,
			final long account_id) {
		return new ExecuteCacheUserStatusesTaskRunnable(context, statuses, account_id);

	}

	static class ExecuteCacheUserStatusesTaskRunnable implements Runnable {
		final Context context;
		final List<twitter4j.Status> statuses;
		final long account_id;

		ExecuteCacheUserStatusesTaskRunnable(final Context context, final List<twitter4j.Status> statuses,
				final long account_id) {
			this.context = context;
			this.statuses = statuses;
			this.account_id = account_id;
		}

		@Override
		public void run() {
			new CacheUsersStatusesTask(context, statuses, account_id).execute();
		}
	}
}
