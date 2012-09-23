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
import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.SerializableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.Bundle;

public class UserTimelineLoader extends Twitter4JStatusLoader {

	private final long mUserId;
	private final String mUserScreenName;
	private int mTotalItemsCount;

	public UserTimelineLoader(final Context context, final long account_id, final long user_id,
			final String user_screenname, final long max_id, final List<ParcelableStatus> data,
			final String class_name, final boolean is_home_tab) {
		super(context, account_id, max_id, data, class_name, is_home_tab);
		mUserId = user_id;
		mUserScreenName = user_screenname;
	}

	@Override
	public ResponseList<Status> getStatuses(final Paging paging) throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mUserId != -1) {
			if (mTotalItemsCount == -1) {
				try {
					mTotalItemsCount = twitter.showUser(mUserId).getStatusesCount();
				} catch (final TwitterException e) {
					mTotalItemsCount = -1;
				}
			}
			return twitter.getUserTimeline(mUserId, paging);
		} else if (mUserScreenName != null) return twitter.getUserTimeline(mUserScreenName, paging);
		return null;
	}

	public int getTotalItemsCount() {
		return mTotalItemsCount;
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		if (isFirstLoad() && isHomeTab() && getClassName() != null) {
			try {
				final File f = new File(getContext().getCacheDir(), getClassName() + "." + getAccountId() + "."
						+ mUserId + "." + mUserScreenName);
				final FileInputStream fis = new FileInputStream(f);
				final ObjectInputStream in = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				final ArrayList<SerializableStatus> statuses = (ArrayList<SerializableStatus>) in.readObject();
				in.close();
				fis.close();
				final ArrayList<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
				for (final SerializableStatus status : statuses) {
					result.add(new ParcelableStatus(status));
				}
				final List<ParcelableStatus> data = getData();
				data.addAll(result);
				Collections.sort(data);
				return data;
			} catch (final IOException e) {
			} catch (final ClassNotFoundException e) {
			}
		}
		return super.loadInBackground();
	}

	public static void writeSerializableStatuses(final Object instance, final Context context,
			final List<ParcelableStatus> data, final Bundle args) {
		if (instance == null || context == null || data == null || args == null) return;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final int items_limit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);
		try {
			final ArrayList<SerializableStatus> statuses = new ArrayList<SerializableStatus>();
			final int count = data.size();
			for (int i = 0; i < count; i++) {
				if (i >= items_limit) {
					break;
				}
				statuses.add(new SerializableStatus(data.get(i)));
			}
			final FileOutputStream fos = new FileOutputStream(new File(context.getCacheDir(), instance.getClass()
					.getSimpleName() + "." + account_id + "." + user_id + "." + screen_name));
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(statuses);
			os.close();
			fos.close();
		} catch (final IOException e) {
		}
	}

}
