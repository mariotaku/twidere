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

import static org.mariotaku.twidere.util.Utils.findUserList;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableUser;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import android.content.Context;

public class ListSubscribersLoader extends ParcelableUsersLoader {

	private final int mListId;
	private final long mAccountId, mUserId, mCursor;
	private final String mScreenName, mListName;

	private long mNextCursor, mPrevCursor;

	public ListSubscribersLoader(Context context, long account_id, int list_id, long user_id, String screen_name,
			String list_name, long cursor, List<ParcelableUser> users_list) {
		super(context, account_id, users_list);
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
	public List<ParcelableUser> getUsers() throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		final PagableResponseList<User> users;
		if (mListId > 0) {
			users = twitter.getUserListSubscribers(mListId, mCursor);
		} else if (mUserId > 0) {
			final UserList list = findUserList(twitter, mUserId, mListName);
			if (list != null && list.getId() > 0) {
				users = twitter.getUserListSubscribers(list.getId(), mCursor);
			} else
				return null;
		} else if (mScreenName != null && mListName != null) {
			final UserList list = findUserList(twitter, mScreenName, mListName);
			if (list != null && list.getId() > 0) {
				users = twitter.getUserListSubscribers(list.getId(), mCursor);
			} else
				return null;
		} else
			return null;
		mNextCursor = users.getNextCursor();
		mPrevCursor = users.getPreviousCursor();
		final List<ParcelableUser> result = new ArrayList<ParcelableUser>();
		final int size = users.size();
		for (int i = 0; i < size; i++) {
			result.add(new ParcelableUser(users.get(i), mAccountId, (mCursor + 1) * 20 + i));
		}
		return result;
	}

}
