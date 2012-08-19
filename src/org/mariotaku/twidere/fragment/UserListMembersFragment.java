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

import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UsersAdapter;
import org.mariotaku.twidere.loader.ListMembersLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ServiceInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class UserListMembersFragment extends BaseUsersListFragment implements OnMenuItemClickListener {

	private long mCursor = -1, mOwnerId = -1;
	private int mUserListId = -1;
	private ParcelableUser mSelectedUser;

	private PopupMenu mPopupMenu;
	private ServiceInterface mService;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_USER_LIST_MEMBER_DELETED.equals(action)) {
				if (!intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) return;
				if (intent.getIntExtra(INTENT_KEY_LIST_ID, -1) == mUserListId) {
					final long user_id = intent.getLongExtra(INTENT_KEY_USER_ID, -1);
					if (user_id > 0) {
						deleteItem(user_id);
					}
				}
			}
		}
	};

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance() {
		final Bundle args = getArguments();
		int list_id = -1;
		long account_id = -1, user_id = -1;
		String screen_name = null, list_name = null;
		if (args != null) {
			list_id = args.getInt(INTENT_KEY_LIST_ID, -1);
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			list_name = args.getString(INTENT_KEY_LIST_NAME);
		}
		return new ListMembersLoader(getActivity(), account_id, list_id, user_id, screen_name, list_name, mCursor,
				getData());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCursor = savedInstanceState.getLong(INTENT_KEY_PAGE, -1);
		}
		mService = getApplication().getServiceInterface();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		mCursor = -1;
		super.onDestroyView();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mSelectedUser = null;
		final UsersAdapter adapter = getListAdapter();
		if (!isMyActivatedAccount(getActivity(), mOwnerId)) return false;
		mSelectedUser = adapter.getItem(position);
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_user_list_member);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
		if (loader instanceof ListMembersLoader) {
			final long cursor = ((ListMembersLoader) loader).getNextCursor();
			if (mOwnerId <= 0) {
				mOwnerId = ((ListMembersLoader) loader).getOwnerId();
			}
			if (mUserListId <= 0) {
				mUserListId = ((ListMembersLoader) loader).getUserListId();
			}
			if (cursor != -2) {
				mCursor = cursor;
			}
		}
		super.onLoadFinished(loader, data);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mSelectedUser == null) return false;
		switch (item.getItemId()) {
			case MENU_DELETE: {
				mService.deleteUserListMember(getAccountId(), mUserListId, mSelectedUser.user_id);
				break;
			}
			case MENU_VIEW_PROFILE: {
				openUserProfile(mSelectedUser.user_id, mSelectedUser.screen_name);
				break;
			}
			case MENU_EXTENSIONS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
				final Bundle extras = new Bundle();
				extras.putParcelable(INTENT_KEY_USER, mSelectedUser);
				intent.putExtras(extras);
				startActivity(Intent.createChooser(intent, getString(R.string.open_with_extensions)));
				break;
			}
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(INTENT_KEY_PAGE, mCursor);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_MEMBER_DELETED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private void deleteItem(long user_id) {
		final ArrayList<ParcelableUser> data = getData();
		final ArrayList<ParcelableUser> users_to_delete = new ArrayList<ParcelableUser>();
		for (final ParcelableUser item : data) {
			if (item.user_id == user_id) {
				users_to_delete.add(item);
			}
		}
		data.removeAll(users_to_delete);
		getListAdapter().setData(data, true);
	}

}
