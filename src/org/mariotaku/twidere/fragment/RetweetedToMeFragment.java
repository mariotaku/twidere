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

import org.mariotaku.twidere.loader.RetweetedToMeLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class RetweetedToMeFragment extends ParcelableStatusesListFragment {

	private boolean mIsStatusesSaved = false;

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Bundle args) {
		long account_id = -1, max_id = -1;
		boolean is_home_tab = false;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			is_home_tab = args.getBoolean(INTENT_KEY_IS_HOME_TAB);
		}
		return new RetweetedToMeLoader(getActivity(), account_id, max_id, getData(), getClass().getSimpleName(),
				is_home_tab);
	}

	@Override
	public void onDestroy() {
		saveStatuses();
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		saveStatuses();
		super.onDestroyView();
	}

	private void saveStatuses() {
		if (mIsStatusesSaved) return;
		final int first_visible_position = getListView().getFirstVisiblePosition();
		final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
		RetweetedToMeLoader.writeSerializableStatuses(this, getActivity(), getData(), status_id, getArguments());
		mIsStatusesSaved = true;
	}

}
