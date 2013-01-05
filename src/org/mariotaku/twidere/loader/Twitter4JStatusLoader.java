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

import static org.mariotaku.twidere.util.Utils.getInlineImagePreviewDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.isFiltered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.twitter4j.Paging;
import org.mariotaku.twidere.twitter4j.Status;
import org.mariotaku.twidere.twitter4j.TwitterException;
import org.mariotaku.twidere.util.CacheUsersStatusesTask;
import org.mariotaku.twidere.util.SynchronizedStateSavedList;
import org.mariotaku.twidere.util.TwitterWrapper.StatusListResponse;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class Twitter4JStatusLoader extends ParcelableStatusesLoader {

	private final long mMaxId, mSinceId;
	private final boolean mHiResProfileImage;
	private final boolean mLargeInlineImagePreview;
	private final SQLiteDatabase mDatabase;

	public Twitter4JStatusLoader(final Context context, final long account_id, final long max_id, final long since_id,
			final List<ParcelableStatus> data, final String class_name, final boolean is_home_tab) {
		super(context, account_id, data, class_name, is_home_tab);
		mMaxId = max_id;
		mSinceId = since_id;
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
		mLargeInlineImagePreview = getInlineImagePreviewDisplayOptionInt(context) == INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE;
		mDatabase = TwidereApplication.getInstance(context).getSQLiteDatabase();
	}

	public abstract List<Status> getStatuses(Paging paging) throws TwitterException;

	@SuppressWarnings("unchecked")
	@Override
	public SynchronizedStateSavedList<ParcelableStatus, Long> loadInBackground() {
		final SynchronizedStateSavedList<ParcelableStatus, Long> data = getData();
		List<Status> statuses = null;
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
			statuses = getStatuses(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (statuses != null) {
			final boolean insert_gap = load_item_limit == statuses.size() && data.size() > 0;
			final Status min_status = statuses.size() > 0 ? Collections.min(statuses) : null;
			final long min_status_id = min_status != null ? min_status.getId() : -1;
			if (context instanceof Activity) {
				((Activity) context).runOnUiThread(CacheUsersStatusesTask.getRunnable(context, new StatusListResponse(
						mAccountId, statuses)));
			}
			for (final Status status : statuses) {
				final long id = status.getId();
				deleteStatus(id);
				data.add(new ParcelableStatus(status, mAccountId, min_status_id > 0 && min_status_id == id
						&& insert_gap, mHiResProfileImage, mLargeInlineImagePreview));
			}
		}
		try {
			final List<ParcelableStatus> statuses_to_remove = new ArrayList<ParcelableStatus>();
			for (final ParcelableStatus status : data) {
				if (isFiltered(mDatabase, status) && !status.is_gap) {
					statuses_to_remove.add(status);
				}
			}
			data.removeAll(statuses_to_remove);
			Collections.sort(data);
		} catch (final ConcurrentModificationException e) {
			Log.w(LOGTAG, e);
		}
		return data;
	}

}
