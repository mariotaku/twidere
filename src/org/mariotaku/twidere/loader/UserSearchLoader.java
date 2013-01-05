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
import org.mariotaku.twidere.twitter4j.ResponseList;
import org.mariotaku.twidere.twitter4j.Twitter;
import org.mariotaku.twidere.twitter4j.TwitterException;
import org.mariotaku.twidere.twitter4j.User;

import android.content.Context;

public class UserSearchLoader extends ParcelableUsersLoader {

	private final String mQuery;
	private final int mPage;
	private final long mAccountId;

	public UserSearchLoader(final Context context, final long account_id, final String query, final int page,
			final List<ParcelableUser> users_list) {
		super(context, account_id, users_list);
		mQuery = query;
		mPage = page;
		mAccountId = account_id;
	}

	@Override
	public List<ParcelableUser> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		final ResponseList<User> users = twitter.searchUsers(mQuery, mPage);
		final List<ParcelableUser> result = new ArrayList<ParcelableUser>();
		final int size = users.size();
		for (int i = 0; i < size; i++) {
			result.add(new ParcelableUser(users.get(i), mAccountId, (mPage - 1) * 20 + i, mHiResProfileImage));
		}
		return result;
	}

}
