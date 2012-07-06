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

import java.util.List;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;

public class UserFavoritesLoader extends Twitter4JStatusLoader {

	private final long mUserId;
	private final String mUserScreenName;
	private int mTotalItemsCount;

	public UserFavoritesLoader(Context context, long account_id, long user_id, long max_id, List<ParcelableStatus> data) {
		this(context, account_id, user_id, null, max_id, data);
	}

	public UserFavoritesLoader(Context context, long account_id, String user_screenname, long max_id,
			List<ParcelableStatus> data) {
		this(context, account_id, -1, user_screenname, max_id, data);
	}

	private UserFavoritesLoader(Context context, long account_id, long user_id, String user_screenname, long max_id,
			List<ParcelableStatus> data) {
		super(context, account_id, max_id, data);
		mUserId = user_id;
		mUserScreenName = user_screenname;
	}

	@Override
	public ResponseList<Status> getStatuses(Paging paging) throws TwitterException {
		final Twitter twitter = getTwitter();
		if (twitter == null) return null;
		if (mUserId != -1) {
			if (mTotalItemsCount == -1) {
				try {
					mTotalItemsCount = twitter.showUser(mUserId).getFavouritesCount();
				} catch (final TwitterException e) {
					mTotalItemsCount = -1;
				}
			}
			return twitter.getFavorites(String.valueOf(mUserId), paging);
		} else if (mUserScreenName != null) return twitter.getFavorites(mUserScreenName, paging);
		return null;
	}

	public int getTotalItemsCount() {
		return mTotalItemsCount;
	}

}
