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

import org.mariotaku.twidere.loader.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.UserListSubscribersLoader;

import android.content.Context;
import android.os.Bundle;

public class UserListSubscribersFragment extends CursorSupportUsersListFragment {

	@Override
	public CursorSupportUsersLoader newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final int list_id = args.getInt(INTENT_KEY_LIST_ID, -1);
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final String list_name = args.getString(INTENT_KEY_LIST_NAME);
		return new UserListSubscribersLoader(context, account_id, list_id, user_id, screen_name, list_name,
				getNextCursor(), getData());
	}

}
