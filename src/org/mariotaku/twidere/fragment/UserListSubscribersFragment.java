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

package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.loader.UserListSubscribersLoader;
import org.mariotaku.twidere.model.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserListSubscribersFragment extends BaseUsersListFragment {

	private long mCursor = -1;

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance(final Bundle args) {
		if (args == null) return null;
		final int list_id = args.getInt(INTENT_KEY_LIST_ID, -1);
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final String list_name = args.getString(INTENT_KEY_LIST_NAME);
		return new UserListSubscribersLoader(getActivity(), account_id, list_id, user_id, screen_name, list_name,
				mCursor, getData());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCursor = savedInstanceState.getLong(INTENT_KEY_PAGE, -1);
		}
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		mCursor = -1;
		super.onDestroyView();
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableUser>> loader, final List<ParcelableUser> data) {
		if (loader instanceof UserListSubscribersLoader) {
			final long cursor = ((UserListSubscribersLoader) loader).getNextCursor();
			if (cursor != -2) {
				mCursor = cursor;
			}
		}
		super.onLoadFinished(loader, data);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLong(INTENT_KEY_PAGE, mCursor);
		super.onSaveInstanceState(outState);
	}

}
