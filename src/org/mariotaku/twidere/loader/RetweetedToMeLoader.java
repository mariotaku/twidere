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
import org.mariotaku.twidere.util.NoDuplicatesStateSavedList;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.os.Bundle;

public class RetweetedToMeLoader extends Twitter4JStatusLoader {

	public RetweetedToMeLoader(final Context context, final long account_id, final long max_id,
			final List<ParcelableStatus> data, final String class_name, final boolean is_home_tab) {
		super(context, account_id, max_id, data, class_name, is_home_tab);
	}

	@Override
	public ResponseList<Status> getStatuses(final Paging paging) throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		return twitter.getRetweetedToMe(paging);
	}

	@Override
	public synchronized List<ParcelableStatus> loadInBackground() {
		if (isFirstLoad() && isHomeTab() && getClassName() != null) {
			try {
				final File f = new File(getContext().getCacheDir(), getClassName() + "." + getAccountId());
				final FileInputStream fis = new FileInputStream(f);
				final ObjectInputStream in = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				final NoDuplicatesStateSavedList<SerializableStatus, Long> statuses = (NoDuplicatesStateSavedList<SerializableStatus, Long>) in
						.readObject();
				setLastViewedId(statuses.getState());
				in.close();
				fis.close();
				final ArrayList<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
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
		return super.loadInBackground();
	}

	public static void writeSerializableStatuses(final Object instance, final Context context,
			final List<ParcelableStatus> data, final long last_viewed_id, final Bundle args) {
		if (instance == null || context == null || data == null || args == null) return;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final int items_limit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);
		try {
			final NoDuplicatesStateSavedList<SerializableStatus, Long> statuses = new NoDuplicatesStateSavedList<SerializableStatus, Long>();
			if (last_viewed_id > 0) {
				statuses.setState(last_viewed_id);
			}
			final int count = data.size();
			for (int i = 0; i < count; i++) {
				if (i >= items_limit) {
					break;
				}
				statuses.add(new SerializableStatus(data.get(i)));
			}
			final FileOutputStream fos = new FileOutputStream(new File(context.getCacheDir(), instance.getClass()
					.getSimpleName() + "." + account_id));
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(statuses);
			os.close();
			fos.close();
		} catch (final IOException e) {
		}
	}

}
