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

import org.mariotaku.twidere.loader.UserFavoritesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.Loader;

public class UserFavoritesFragment extends ParcelableStatusesListFragment {

	private long mUserId;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
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

	private boolean mIsStatusesSaved = false;

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Bundle args) {
		long account_id = -1, user_id = -1, max_id = -1;
		String screen_name = null;
		boolean is_home_tab = false;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			if (user_id > 0) {
				mUserId = user_id;
			}
			max_id = args.getLong(INTENT_KEY_MAX_ID, -1);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			is_home_tab = args.getBoolean(INTENT_KEY_IS_HOME_TAB);
		}
		return new UserFavoritesLoader(getActivity(), account_id, user_id, screen_name, max_id, getData(), getClass()
				.getSimpleName(), is_home_tab);
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

	private void saveStatuses() {
		if (mIsStatusesSaved) return;
		final int first_visible_position = getListView().getFirstVisiblePosition();
		final long status_id = getListAdapter().findItemIdByPosition(first_visible_position);
		UserFavoritesLoader.writeSerializableStatuses(this, getActivity(), getData(), status_id, getArguments());
		mIsStatusesSaved = true;
	}

}
