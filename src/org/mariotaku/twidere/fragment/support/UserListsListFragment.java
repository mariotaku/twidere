/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.Utils.getAccountScreenName;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableUserListsAdapter;
import org.mariotaku.twidere.loader.UserListsLoader;
import org.mariotaku.twidere.model.ParcelableUserList;

import java.util.List;

public class UserListsListFragment extends BaseUserListsListFragment {

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_USER_LIST_DELETED.equals(action)) {
				final ParcelableUserList list = intent.getParcelableExtra(EXTRA_USER_LIST);
				if (list != null && intent.getBooleanExtra(EXTRA_SUCCEED, false)) {
					removeUserList(list.id);
				}
			}
		}
	};

	@Override
	public Loader<List<ParcelableUserList>> newLoaderInstance(final long account_id, final long user_id,
			final String screen_name) {
		return new UserListsLoader(getActivity(), account_id, user_id, screen_name, getData());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_user_list_created, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.new_user_list: {
				final DialogFragment f = new CreateUserListDialogFragment();
				final Bundle args = new Bundle();
				args.putLong(EXTRA_ACCOUNT_ID, getAccountId());
				f.setArguments(args);
				f.show(getFragmentManager(), null);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		final MenuItem item = menu.findItem(R.id.new_user_list);
		if (item == null) return;
		final long account_id = getAccountId(), user_id = getUserId();
		final String screen_name = getAccountScreenName(getActivity(), account_id);
		item.setVisible(user_id == account_id || screen_name != null && screen_name.equalsIgnoreCase(getScreenName()));
	}

	public void onPullUpToRefresh() {
	}

	@Override
	public void onStart() {
		super.onStart();
		registerReceiver(mStatusReceiver, new IntentFilter(BROADCAST_USER_LIST_DELETED));
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void removeUserList(final int id) {
		final ParcelableUserListsAdapter adapter = getListAdapter();
		final int lists_idx = adapter.findItemPosition(id);
		if (lists_idx >= 0) {
			adapter.remove(lists_idx);
		}
	}

}
