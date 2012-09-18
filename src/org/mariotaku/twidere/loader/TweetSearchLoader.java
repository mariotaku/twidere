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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SerializableStatus;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

import twitter4j.Query;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class TweetSearchLoader extends ParcelableStatusesLoader {

	private final String mQuery;
	private final long mMaxId;

	public TweetSearchLoader(Context context, long account_id, String query, long max_id, List<ParcelableStatus> data,
			String class_name, boolean is_home_tab) {
		super(context, account_id, data, class_name, is_home_tab);
		mQuery = query;
		mMaxId = max_id;
	}

	@Override
	public synchronized List<ParcelableStatus> loadInBackground() {
		if (isFirstLoad() && isHomeTab() && getClassName() != null) {
			try {
				final File f = new File(getContext().getCacheDir(), getClassName() + "." + getAccountId() + "."
						+ mQuery);
				final FileInputStream fis = new FileInputStream(f);
				final ObjectInputStream in = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				final ArrayList<SerializableStatus> statuses = (ArrayList<SerializableStatus>) in.readObject();
				in.close();
				fis.close();
				final NoDuplicatesArrayList<ParcelableStatus> result = new NoDuplicatesArrayList<ParcelableStatus>();
				for (final SerializableStatus status : statuses) {
					result.add(new ParcelableStatus(status));
				}
				final List<ParcelableStatus> data = getData();
				if (data != null) {
					data.addAll(result);
				}
				Collections.sort(data);
				return data;
			} catch (final IOException e) {
			} catch (final ClassNotFoundException e) {
			}
		}
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
			try {
				Collections.sort(tweets);
			} catch (final ConcurrentModificationException e) {
				// This shouldn't happen.
				e.printStackTrace();
			}
			final int size = tweets.size();
			for (int i = 0; i < size; i++) {
				final Tweet tweet = tweets.get(i);
				deleteStatus(tweet.getId());
				data.add(new ParcelableStatus(tweet, account_id, false));
			}
		}
		try {
			Collections.sort(data);
		} catch (final ConcurrentModificationException e) {
			// This shouldn't happen.
			e.printStackTrace();
		}
		return data;
	}

	public static void writeSerializableStatuses(Object instance, Context context, List<ParcelableStatus> data,
			Bundle args) {
		if (instance == null || context == null || data == null || args == null) return;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final String query = screen_name != null ? screen_name.startsWith("@") ? screen_name : "@" + screen_name : args
				.getString(INTENT_KEY_QUERY);
		final int items_limit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);
		try {
			final NoDuplicatesArrayList<SerializableStatus> statuses = new NoDuplicatesArrayList<SerializableStatus>();
			int i = 0;
			for (final ParcelableStatus status : data) {
				if (i >= items_limit) {
					break;
				}
				statuses.add(new SerializableStatus(status));
				i++;
			}
			final FileOutputStream fos = new FileOutputStream(new File(context.getCacheDir(), instance.getClass()
					.getSimpleName() + "." + account_id + "." + query));
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(statuses);
			os.close();
			fos.close();
		} catch (final IOException e) {
		} catch (final ArrayIndexOutOfBoundsException e) {
		}
	}

}
