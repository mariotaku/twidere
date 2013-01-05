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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.twitter4j.IDs;
import org.mariotaku.twidere.twitter4j.ResponseList;
import org.mariotaku.twidere.twitter4j.Twitter;
import org.mariotaku.twidere.twitter4j.TwitterException;
import org.mariotaku.twidere.twitter4j.User;
import org.mariotaku.twidere.util.ArrayUtils;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class IDsUsersLoader extends ParcelableUsersLoader {

	private final long mMaxId;
	private IDs mIDs;
	private final SharedPreferences mPreferences;
	private final int mLoadItemLimit;

	public IDsUsersLoader(final Context context, final long account_id, final long max_id,
			final List<ParcelableUser> users_list) {
		super(context, account_id, users_list);
		mMaxId = max_id;
		mPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int prefs_load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
				PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		mLoadItemLimit = prefs_load_item_limit > 100 ? 100 : prefs_load_item_limit;
	}

	public abstract IDs getIDs() throws TwitterException;

	public long[] getIDsArray() {
		return mIDs != null ? mIDs.getIDs() : null;
	}

	public int getLoadItemLimit() {
		return mLoadItemLimit;
	}

	@Override
	public List<ParcelableUser> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mIDs == null) {
			mIDs = getIDs();
			if (mIDs == null) return null;
		}
		final long[] ids = mIDs.getIDs();
		final int max_id_idx = mMaxId > 0 ? ArrayUtils.indexOf(ids, mMaxId) : 0;
		if (max_id_idx < 0) return null;
		if (max_id_idx == ids.length - 1) return Collections.emptyList();
		final int count = max_id_idx + mLoadItemLimit < ids.length ? mLoadItemLimit : ids.length - max_id_idx;
		final long[] ids_to_load = new long[count];
		int temp_idx = max_id_idx;
		for (int i = 0; i < count; i++) {
			ids_to_load[i] = ids[temp_idx];
			temp_idx++;
		}
		final ResponseList<User> users = twitter.lookupUsers(ids_to_load);
		final List<ParcelableUser> result = new ArrayList<ParcelableUser>();
		for (final User user : users) {
			final int position = ArrayUtils.indexOf(mIDs.getIDs(), user.getId());
			result.add(new ParcelableUser(user, mAccountId, position, mHiResProfileImage));
		}
		return result;
	}

}
