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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

import twitter4j.CursorSupport;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

public class UserListsLoader extends AsyncTaskLoader<UserListsLoader.UserListsData> {

	public static final String LOGTAG = UserListsLoader.class.getSimpleName();

	private final long mAccountId;
	private final boolean mHiResProfileImage;
	private final long mUserId;
	private final String mScreenName;
	private final List<ParcelableUserList> mUserLists;
	private final List<ParcelableUserList> mUserListMemberships;

	private final long mCursor;
	private final int mPage;

	public UserListsLoader(final Context context, final long account_id, final long user_id, final String screen_name,
			final UserListsData data, final long cursor, final int page) {
		super(context);
		mAccountId = account_id;
		mUserId = user_id;
		mCursor = cursor;
		mPage = page;
		mScreenName = screen_name;
		mUserLists = data != null ? data.lists : null;
		mUserListMemberships = data != null ? data.memberships : new ArrayList<ParcelableUserList>();
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public UserListsData loadInBackground() {
		final Twitter twitter = getTwitterInstance(getContext(), mAccountId, false);
		if (twitter == null) return null;
		try {
			final List<UserList> user_lists;
			final List<UserList> user_list_memberships;
			if (mUserId > 0) {
				if (mUserLists != null) {
					user_lists = Collections.emptyList();
				} else {
					user_lists = twitter.getUserLists(mUserId);
				}
				user_list_memberships = twitter.getUserListMemberships(mUserId, mCursor);
			} else if (mScreenName != null) {
				if (mUserLists != null) {
					user_lists = Collections.emptyList();
				} else {
					user_lists = twitter.getUserLists(mScreenName);
				}
				user_list_memberships = twitter.getUserListMemberships(mScreenName, mCursor);
			} else
				return null;
			final int user_lists_size = user_lists.size(), user_list_memberships_size = user_list_memberships.size();
			final long prev_cursor, next_cursor;
			if (user_list_memberships instanceof PagableResponseList) {
				next_cursor = ((CursorSupport) user_list_memberships).getNextCursor();
				prev_cursor = ((CursorSupport) user_list_memberships).getPreviousCursor();
			} else {
				next_cursor = -1;
				prev_cursor = -1;
			}
			final UserListsData data = new UserListsData(prev_cursor, mCursor, next_cursor);
			if (mUserLists == null) {
				for (int i = 0; i < user_lists_size; i++) {
					final ParcelableUserList list = new ParcelableUserList(user_lists.get(i), mAccountId, i,
							mHiResProfileImage);
					if (!data.lists.contains(list)) {
						data.lists.add(list);
					}
				}
			} else {
				data.lists.addAll(mUserLists);
			}
			if (user_list_memberships instanceof PagableResponseList) {
				for (int i = 0; i < user_list_memberships_size; i++) {
					final ParcelableUserList list = new ParcelableUserList(user_list_memberships.get(i), mAccountId,
							mPage * 20 + i, mHiResProfileImage);
					if (!data.memberships.contains(list)) {
						data.memberships.add(list);
					}
				}
			} else {
				for (int i = 0; i < user_list_memberships_size; i++) {
					final ParcelableUserList list = new ParcelableUserList(user_list_memberships.get(i), mAccountId, i,
							mHiResProfileImage);
					if (!data.memberships.contains(list)) {
						data.memberships.add(list);
					}
				}
			}
			if (mUserListMemberships != null) {
				data.memberships.addAll(mUserListMemberships);
			}
			Collections.sort(data.lists);
			Collections.sort(data.memberships);
			return data;
		} catch (final TwitterException e) {
			Log.w(LOGTAG, e);
			return null;
		}
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	public static final class UserListsData {
		private final List<ParcelableUserList> lists = new NoDuplicatesArrayList<ParcelableUserList>();
		private final List<ParcelableUserList> memberships = new NoDuplicatesArrayList<ParcelableUserList>();
		private final long prev_cursor, cursor, next_cursor;

		private UserListsData(final long prev_cursor, final long cursor, final long next_cursor) {
			this.prev_cursor = prev_cursor;
			this.cursor = cursor;
			this.next_cursor = next_cursor;
		}

		public long getCursor() {
			return cursor;
		}

		public List<ParcelableUserList> getLists() {
			return lists;
		}

		public List<ParcelableUserList> getMemberships() {
			return memberships;
		}

		public long getNextCursor() {
			return next_cursor;
		}

		public long getPrevCursor() {
			return prev_cursor;
		}

	}
}
