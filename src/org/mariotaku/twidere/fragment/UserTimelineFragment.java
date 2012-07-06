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

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.UserTimelineLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserTimelineFragment extends ParcelableStatusesListFragment {

	private boolean isAllItemsLoaded = false;

	@Override
	public boolean mustShowLastAsGap() {
		return !isAllItemsLoaded;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		if (args != null) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			final long max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			return new UserTimelineLoader(getActivity(), account_id, user_id, screen_name, max_id, getData());
		}
		return null;
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {
		if (loader instanceof UserTimelineLoader) {
			final int total = ((UserTimelineLoader) loader).getTotalItemsCount();
			isAllItemsLoaded = total != -1 && total == adapter.getCount();
		}

	}

}
