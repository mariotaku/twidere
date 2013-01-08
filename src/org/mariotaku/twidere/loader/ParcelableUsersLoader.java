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

import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUser;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ParcelableUsersLoader extends AsyncTaskLoader<List<ParcelableUser>> implements Constants {

	protected final Twitter mTwitter;
	protected final long mAccountId;
	protected final boolean mHiResProfileImage;
	private final List<ParcelableUser> mUsersList;

	public ParcelableUsersLoader(final Context context, final long account_id, final List<ParcelableUser> users_list) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mUsersList = users_list;
		mAccountId = account_id;
		mHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	public long getAccountId() {
		return mAccountId;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	public abstract List<ParcelableUser> getUsers() throws TwitterException;

	@Override
	public List<ParcelableUser> loadInBackground() {
		List<ParcelableUser> list_loaded = null;
		try {
			list_loaded = getUsers();
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (list_loaded != null) {
			for (final ParcelableUser user : list_loaded) {
				if (!hasId(user.user_id)) {
					mUsersList.add(user);
				}
			}
		}
		Collections.sort(mUsersList);
		return mUsersList;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	private boolean hasId(final long id) {
		for (final ParcelableUser user : mUsersList) {
			if (user.user_id == id) return true;
		}
		return false;
	}

}
