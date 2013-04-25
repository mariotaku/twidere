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
import org.mariotaku.twidere.util.ArrayUtils;

import twitter4j.IDs;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.Arrays;
import twitter4j.Paging;

public abstract class IDsUsersLoader extends Twitter4JUsersLoader {

	private final long mMaxId;
	private final SharedPreferences mPreferences;
	private final int mLoadItemLimit;

	private IDs mIDs;

	public IDsUsersLoader(final Context context, final long account_id, final long max_id,
			final List<ParcelableUser> data) {
		super(context, account_id, data);
		mMaxId = max_id;
		mPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int prefs_load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
				PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		mLoadItemLimit = Math.min(100, prefs_load_item_limit);
	}

	protected abstract IDs getIDs(Twitter twitter) throws TwitterException;

	protected final long[] getIDsArray() {
		return mIDs != null ? mIDs.getIDs() : null;
	}

	@Override
	public List<User> getUsers(final Twitter twitter) throws TwitterException {
		if (twitter == null) return null;
		if (mIDs == null) {
			mIDs = getIDs(twitter);
			if (mIDs == null) return null;
		}
		final long[] ids = mIDs.getIDs();
		final int max_id_idx = mMaxId > 0 ? ArrayUtils.indexOf(ids, mMaxId) : 0;
		if (max_id_idx < 0) return null;
		if (max_id_idx == ids.length - 1) return Collections.emptyList();
		final int count = max_id_idx + mLoadItemLimit < ids.length ? mLoadItemLimit : ids.length - max_id_idx;
		final long[] ids_to_load = new long[count];
		System.arraycopy(ids, max_id_idx, ids_to_load, 0, count);
		return twitter.lookupUsers(ids_to_load);
	}
	
	protected long getUserPosition(final User user, final int index) {
		final long[] ids = getIDsArray();
		if (ids == null) return -1;
		return ArrayUtils.indexOf(ids, user.getId());
	}

}
