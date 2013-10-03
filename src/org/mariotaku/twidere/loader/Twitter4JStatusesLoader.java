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
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.CacheUsersStatusesTask;
import org.mariotaku.twidere.util.TwitterWrapper.StatusListResponse;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

public abstract class Twitter4JStatusesLoader extends ParcelableStatusesLoader {

	private final Context mContext;
	private final long mAccountId;
	private final long mMaxId, mSinceId;
	private final boolean mHiResProfileImage;
	private final SQLiteDatabase mDatabase;
	private final Handler mHandler;
	private final Object[] mSavedStatusesFileArgs;

	public Twitter4JStatusesLoader(final Context context, final long account_id, final long max_id,
			final long since_id, final List<ParcelableStatus> data, final String[] saved_statuses_args,
			final int tab_position) {
		super(context, data, tab_position);
		mContext = context;
		mAccountId = account_id;
		mMaxId = max_id;
		mSinceId = since_id;
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
		mDatabase = TwidereApplication.getInstance(context).getSQLiteDatabase();
		mHandler = new Handler();
		mSavedStatusesFileArgs = saved_statuses_args;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ParcelableStatus> loadInBackground() {
		final List<ParcelableStatus> data = getData();
		if (isFirstLoad() && getTabPosition() >= 0) {
			try {
				final File file = JSONSerializer.getSerializationFile(getContext(), mSavedStatusesFileArgs);
				final List<ParcelableStatus> statuses = JSONSerializer.listFromFile(file);
				if (data != null && statuses != null) {
					data.addAll(statuses);
					Collections.sort(data);
				}
				return data;
			} catch (final IOException e) {
			}
		}
		final List<Status> statuses;
		final Context context = getContext();
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int load_item_limit = prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		try {
			final Paging paging = new Paging();
			paging.setCount(load_item_limit);
			if (mMaxId > 0) {
				paging.setMaxId(mMaxId);
			}
			if (mSinceId > 0) {
				paging.setSinceId(mSinceId);
			}
			statuses = getStatuses(getTwitter(), paging);
		} catch (final TwitterException e) {
			// mHandler.post(new ShowErrorRunnable(e));
			e.printStackTrace();
			return data;
		}
		if (statuses != null) {
			final Status min_status = statuses.size() > 0 ? Collections.min(statuses) : null;
			final long min_status_id = min_status != null ? min_status.getId() : -1;
			final boolean insert_gap = min_status_id > 0 && load_item_limit <= statuses.size() && data.size() > 0;
			mHandler.post(CacheUsersStatusesTask.getRunnable(context, new StatusListResponse(mAccountId, statuses)));
			for (final Status status : statuses) {
				final long id = status.getId();
				deleteStatus(id);
				data.add(new ParcelableStatus(status, mAccountId, min_status_id == id && insert_gap, mHiResProfileImage));
			}
		}
		try {
			Collections.sort(data);
			final List<ParcelableStatus> statuses_to_remove = new ArrayList<ParcelableStatus>();
			for (int i = 0, size = data.size(); i < size; i++) {
				final ParcelableStatus status = data.get(i);
				if (shouldFilterStatus(mDatabase, status) && !status.is_gap && i != size - 1) {
					statuses_to_remove.add(status);
				}
			}
			data.removeAll(statuses_to_remove);
		} catch (final ConcurrentModificationException e) {
			Log.w(LOGTAG, e);
		}
		return data;
	}

	protected abstract List<Status> getStatuses(Twitter twitter, Paging paging) throws TwitterException;

	protected final Twitter getTwitter() {
		return getTwitterInstance(mContext, mAccountId, true, shouldIncludeRetweets());
	}

	protected abstract boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status);

	protected boolean shouldIncludeRetweets() {
		return true;
	}
}
