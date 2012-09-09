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
import org.mariotaku.twidere.loader.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class SearchTweetsFragment extends ParcelableStatusesListFragment {

	@Override
	public boolean isListLoadFinished() {
		return false;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		long account_id = -1, max_id = -1;
		String query = null;
		boolean is_home_tab = false;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			query = args.getString(INTENT_KEY_QUERY);
			is_home_tab = args.getBoolean(INTENT_KEY_IS_HOME_TAB);
		}
		return new TweetSearchLoader(getActivity(), account_id, query, max_id, getData(), getClass().getSimpleName(),
				is_home_tab);
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {

	}

	@Override
	public void onDestroy() {
		TweetSearchLoader.writeSerializableStatuses(this, getActivity(), getData(), getArguments());
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		TweetSearchLoader.writeSerializableStatuses(this, getActivity(), getData(), getArguments());
		super.onDestroyView();
	}

}
