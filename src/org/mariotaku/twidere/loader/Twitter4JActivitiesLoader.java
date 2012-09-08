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
import android.support.v4.content.AsyncTaskLoader;

public abstract class Twitter4JActivitiesLoader extends AsyncTaskLoader<List<Activity>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId;
	private final List<Activity> mData;

	public Twitter4JActivitiesLoader(Context context, long account_id, List<Activity> data) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
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
		ResponseList<Activity> statuses = null;
		try {
			final Paging paging = new Paging();
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			final int load_item_limit = prefs
					.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			paging.setCount(load_item_limit > 100 ? 100 : load_item_limit);
			statuses = getActivities(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (statuses != null) {
			Collections.sort(statuses);
		}
		return statuses;
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	abstract ResponseList<Activity> getActivities(Paging paging) throws TwitterException;

}
