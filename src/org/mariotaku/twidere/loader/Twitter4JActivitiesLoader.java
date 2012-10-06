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

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.Constants;

import twitter4j.Activity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import org.mariotaku.twidere.util.SerializationUtil;

public abstract class Twitter4JActivitiesLoader extends AsyncTaskLoader<List<Activity>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId;
	private final List<Activity> mData;
	private final boolean mIsFirstLoad, mIsHomeTab;
	private final String mClassName;

	public Twitter4JActivitiesLoader(final Context context, final long account_id, final List<Activity> data,
			final String class_name, final boolean is_home_tab) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mIsFirstLoad = data == null;
		mIsHomeTab = is_home_tab;
		mClassName = class_name;
		mData = data != null ? data : new ArrayList<Activity>();
	}

	public long getAccountId() {
		return mAccountId;
	}

	public List<Activity> getData() {
		return mData;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	@Override
	public List<Activity> loadInBackground() {
		if (mIsFirstLoad && mIsHomeTab && mClassName != null) {
			try {
				final File f = new File(getContext().getCacheDir(), mClassName + "." + getAccountId());
				@SuppressWarnings("unchecked")
				final List<Activity> cached_activities = (List<Activity>) SerializationUtil.read(f.getPath());
				return cached_activities;
			} catch (final IOException e) {
			} catch (final ClassNotFoundException e) {
			} catch (final ClassCastException e) {
			}
		}
		ResponseList<Activity> activities = null;
		try {
			final Paging paging = new Paging();
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			final int load_item_limit = prefs
					.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			paging.setCount(load_item_limit > 100 ? 100 : load_item_limit);
			activities = getActivities(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (activities != null) {
			Collections.sort(activities);
		}
		return activities;
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	abstract ResponseList<Activity> getActivities(Paging paging) throws TwitterException;

	public static void writeSerializableStatuses(final Object instance, final Context context,
			final List<Activity> data, final Bundle args) {
		if (instance == null || context == null || data == null || args == null) return;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		try {
			final FileOutputStream fos = new FileOutputStream(new File(context.getCacheDir(), instance.getClass()
					.getSimpleName() + "." + account_id));
			final ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(data);
			os.close();
			fos.close();
		} catch (final IOException e) {
		}
	}

}
