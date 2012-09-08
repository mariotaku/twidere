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
import org.mariotaku.twidere.loader.UserFavoritesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserFavoritesFragment extends ParcelableStatusesListFragment {

	private boolean isAllItemsLoaded = false;
	private long mUserId;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_FAVORITE_CHANGED.equals(action)) {
				final long status_id = intent.getLongExtra(INTENT_KEY_STATUS_ID, -1);
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mUserId && status_id > 0
						&& !intent.getBooleanExtra(INTENT_KEY_FAVORITED, true)) {
					deleteStatus(status_id);
				}
			}
		}

	};

	@Override
	public boolean isListLoadFinished() {
		return isAllItemsLoaded;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		long account_id = -1, user_id = -1, max_id = -1;
		String screen_name = null;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			if (user_id > 0) {
				mUserId = user_id;
			}
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		if (user_id != -1)
			return new UserFavoritesLoader(getActivity(), account_id, user_id, max_id, getData(), getClass()
					.getSimpleName());
		else
			return new UserFavoritesLoader(getActivity(), account_id, screen_name, max_id, getData(), getClass()
					.getSimpleName());
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {
		if (loader instanceof UserFavoritesLoader) {
			final int total = ((UserFavoritesLoader) loader).getTotalItemsCount();
			if (mUserId <= 0) {
				mUserId = ((UserFavoritesLoader) loader).getUserId();
			}
			isAllItemsLoaded = total != -1 && total == adapter.getCount();
		}
	}

	@Override
	public void onDestroy() {
		UserFavoritesLoader.writeSerializableStatuses(this, getActivity(), getData(), getArguments());
		super.onDestroy();
	}
	
	@Override
	public void onDestroyView() {
		UserFavoritesLoader.writeSerializableStatuses(this, getActivity(), getData(), getArguments());
		super.onDestroyView();
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_FAVORITE_CHANGED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

}
