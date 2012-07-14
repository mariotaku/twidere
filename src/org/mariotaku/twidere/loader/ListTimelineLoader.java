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

import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;

public class ListTimelineLoader extends Twitter4JStatusLoader {

	private final long mUserId;
	private final String mScreenName, mListName;
	private final int mListId;

	public ListTimelineLoader(Context context, long account_id, int list_id, long user_id, String screen_name, String list_name, long max_id,
			List<ParcelableStatus> data) {
		super(context, account_id, max_id, data);
		mListId = list_id;
		mUserId = user_id;
		mScreenName = screen_name;
		mListName = list_name;
	}

	@Override
	public ResponseList<Status> getStatuses(Paging paging) throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mListId > 0) {
			return twitter.getUserListStatuses(mListId, paging);
		} else if (mUserId > 0) {
			final UserList list = findUserList(twitter, mUserId, mListName);
			if (list != null && list.getId() > 0)
				return twitter.getUserListStatuses(list.getId(), paging);
		} else if (mScreenName != null && mListName != null) {
			final UserList list = findUserList(twitter, mScreenName, mListName);
			if (list != null && list.getId() > 0)
				return twitter.getUserListStatuses(list.getId(), paging);
		}
		return null;
	}

}