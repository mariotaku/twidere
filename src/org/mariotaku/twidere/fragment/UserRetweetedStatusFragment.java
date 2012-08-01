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

import org.mariotaku.twidere.loader.UserRetweetedStatusLoader;
import org.mariotaku.twidere.model.ParcelableUser;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserRetweetedStatusFragment extends BaseUsersListFragment {

	private int mPage = 1;

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		long account_id = -1, max_id = -1, status_id = -1;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			status_id = args.getLong(INTENT_KEY_STATUS_ID, -1);
		}
		int page = 1;
		if (max_id > 0) {
			final int prefs_load_item_limit = getSharedPreferences().getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			final int load_item_limit = prefs_load_item_limit > 100 ? 100 : prefs_load_item_limit;
			final int pos = getListAdapter().findItemPositionByUserId(max_id);
			if (pos > 0) {
				page = pos / load_item_limit + 1;
				setAllItemsLoaded(mPage == page);
				mPage = page;
			}
		}
		return new UserRetweetedStatusLoader(getActivity(), account_id, status_id, page, getData());
	}

}
