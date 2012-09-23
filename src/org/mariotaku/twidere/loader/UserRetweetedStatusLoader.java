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
import java.util.List;

import org.mariotaku.twidere.model.ParcelableUser;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;

public class UserRetweetedStatusLoader extends ParcelableUsersLoader {

	private final long mStatusId;
	private final SharedPreferences mPreferences;
	private final int mPage, mLoadItemLimit;
	private final List<ParcelableUser> mUsersList;

	public UserRetweetedStatusLoader(final Context context, final long account_id, final long status_id,
			final int page, final List<ParcelableUser> users_list) {
		super(context, account_id, users_list);
		mStatusId = status_id;
		mPage = page;
		mUsersList = users_list;
		mPreferences = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int prefs_load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
				PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
		mLoadItemLimit = prefs_load_item_limit > 100 ? 100 : prefs_load_item_limit;
	}

	@Override
	public List<ParcelableUser> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mStatusId > 0) {
			final Paging paging = new Paging();
			paging.setCount(mLoadItemLimit);
			if (mPage > 0) {
				paging.setPage(mPage);
			}
			final ResponseList<User> users = twitter.getRetweetedBy(mStatusId, paging);
			if (users == null) return null;
			final List<ParcelableUser> result = new ArrayList<ParcelableUser>();
			final int users_list_size = mUsersList.size();
			final int size = users.size();
			for (int i = 0; i < size; i++) {
				final User user = users.get(i);
				result.add(new ParcelableUser(user, getAccountId(), users_list_size + i));
			}
			return result;
		}
		return null;
	}

}
