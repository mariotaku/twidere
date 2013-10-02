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

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

import twitter4j.Activity;
import twitter4j.Paging;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public abstract class Twitter4JActivitiesLoader extends AsyncTaskLoader<List<ParcelableActivity>> implements Constants {
	private final Context mContext;

	private final long mAccountId;
	private final List<ParcelableActivity> mData = Collections
			.synchronizedList(new NoDuplicatesArrayList<ParcelableActivity>());
	private final boolean mIsFirstLoad;
	private final int mTabPosition, mLoadItemLimit;

	private final boolean mHiResProfileImage;

	private final Object[] mSavedActivitiesFileArgs;

	public Twitter4JActivitiesLoader(final Context context, final long account_id, final List<ParcelableActivity> data,
			final String[] save_file_args, final int tab_position) {
		super(context);
		mContext = context;
		mAccountId = account_id;
		mIsFirstLoad = data == null;
		mTabPosition = tab_position;
		mSavedActivitiesFileArgs = save_file_args;
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mLoadItemLimit = Math
				.min(100, prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT));
	}

	@Override
	public List<ParcelableActivity> loadInBackground() {
		if (mIsFirstLoad && mTabPosition >= 0 && mSavedActivitiesFileArgs != null) {
			try {
				final File file = JSONSerializer.getSerializationFile(mContext, mSavedActivitiesFileArgs);
				final List<ParcelableActivity> cached = JSONSerializer.listFromFile(file);
				if (cached != null) {
					mData.addAll(cached);
					Collections.sort(mData);
					return mData;
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		final List<Activity> activities;
		try {
			final Paging paging = new Paging();
			paging.setCount(mLoadItemLimit);
			activities = getActivities(getTwitter(), paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
			return mData;
		}
		if (activities == null) return mData;
		mData.clear();
		for (final Activity activity : activities) {
			mData.add(new ParcelableActivity(activity, mAccountId, mHiResProfileImage));
		}
		Collections.sort(mData);
		return mData;
	}

	protected final long getAccountId() {
		return mAccountId;
	}

	protected abstract List<Activity> getActivities(Twitter twitter, Paging paging) throws TwitterException;

	protected List<ParcelableActivity> getData() {
		return mData;
	}

	protected Twitter getTwitter() {
		return getTwitterInstance(mContext, mAccountId, true);
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}
