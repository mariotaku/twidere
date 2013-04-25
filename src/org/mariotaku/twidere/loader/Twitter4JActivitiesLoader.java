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
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

public abstract class Twitter4JActivitiesLoader extends AsyncTaskLoader<List<ParcelableActivity>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId;
	private final List<ParcelableActivity> mData = Collections.synchronizedList(new NoDuplicatesArrayList<ParcelableActivity>());
	private final boolean mIsFirstLoad;
	private final int mTabPosition;

	private final boolean mHiResProfileImage;

	private final String[] mSavedActivitiesFileArgs;

	public Twitter4JActivitiesLoader(final Context context, final long account_id, final List<Activity> data,
			final String[] save_file_args, final int tab_position) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mIsFirstLoad = data == null;
		mTabPosition = tab_position;
		mSavedActivitiesFileArgs = save_file_args;
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	protected final long getAccountId() {
		return mAccountId;
	}

	public List<ParcelableActivity> getData() {
		return mData;
	}

	protected Twitter getTwitter() {
		return mTwitter;
	}

	@Override
	public List<ParcelableActivity> loadInBackground() {
//		if (mIsFirstLoad && mIsHomeTab && mClassName != null) {
//			try {
//				final File f = new File(getContext().getCacheDir(), mClassName + "." + getAccountId());
//				@SuppressWarnings("unchecked")
//				final List<Activity> cached_activities = (List<Activity>) SerializationUtil.read(f.getPath());
//				return cached_activities;
//			} catch (final IOException e) {
//			}
//		}
		final ResponseList<Activity> activities;
		try {
			final Paging paging = new Paging();
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			final int load_item_limit = prefs
					.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			paging.setCount(Math.min(100, load_item_limit));
			activities = getActivities(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
			return mData;
		}
		mData.clear();
		for (final Activity activity : activities) {
			mData.add(new ParcelableActivity(activity, mAccountId, mHiResProfileImage));
		}
		Collections.sort(mData);
		return mData;
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	protected abstract ResponseList<Activity> getActivities(Paging paging) throws TwitterException;

}
