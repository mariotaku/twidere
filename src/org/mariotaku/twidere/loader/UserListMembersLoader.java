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

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import android.content.Context;

public class UserListMembersLoader extends Twitter4JUsersLoader {

	private final int mListId;
	private final long mAccountId, mUserId, mCursor;
	private final String mScreenName, mListName;

	private long mNextCursor = -2, mPrevCursor = -2;

	public UserListMembersLoader(final Context context, final long account_id, final int list_id, final long user_id,
			final String screen_name, final String list_name, final long cursor, final List<ParcelableUser> data) {
		super(context, account_id, data);
		mListId = list_id;
		mCursor = cursor;
		mAccountId = account_id;
		mUserId = user_id;
		mScreenName = screen_name;
		mListName = list_name;
	}

	public long getNextCursor() {
		return mNextCursor;
	}

	public long getPrevCursor() {
		return mPrevCursor;
	}

	@Override
	public List<User> getUsers(final Twitter twitter) throws TwitterException {
		if (twitter == null) return null;
		final PagableResponseList<User> users;
		if (mListId > 0) {
			users = twitter.getUserListMembers(mListId, mCursor);
		} else if (mUserId > 0) {
			users = twitter.getUserListMembers(mListName, mUserId, mCursor);
		} else if (mScreenName != null) {
			users = twitter.getUserListMembers(mListName, mScreenName, mCursor);
		} else {
			return null;
		}
		mNextCursor = users.getNextCursor();
		mPrevCursor = users.getPreviousCursor();
		return users;
	}

	protected long getUserPosition(final User user, final int index) {
		return (mCursor + 1) * 20 + index;
	}

}
