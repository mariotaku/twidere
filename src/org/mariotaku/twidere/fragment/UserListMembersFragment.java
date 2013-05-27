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
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UsersAdapter;
import org.mariotaku.twidere.loader.UserListMembersLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

public class UserListMembersFragment extends BaseUsersListFragment implements OnMenuItemClickListener {

	private long mCursor = -1;
	private int mUserListId = -1;
	private boolean mIsMyUserList;
	private ParcelableUser mSelectedUser;

	private PopupMenu mPopupMenu;
	private AsyncTwitterWrapper mTwitterWrapper;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_USER_LIST_MEMBERS_DELETED.equals(action)) {
				if (!intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) return;
				if (intent.getIntExtra(INTENT_KEY_LIST_ID, -1) == mUserListId) {
					final long user_id = intent.getLongExtra(INTENT_KEY_USER_ID, -1);
					if (user_id > 0) {
						removeUser(user_id);
					}
				}
			}
		}
	};

	@Override
	public Loader<List<ParcelableUser>> newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final int list_id = args.getInt(INTENT_KEY_LIST_ID, -1);
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
		final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		final String list_name = args.getString(INTENT_KEY_LIST_NAME);
		return new UserListMembersLoader(context, account_id, list_id, user_id, screen_name, list_name, mCursor,
				getData());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCursor = savedInstanceState.getLong(INTENT_KEY_PAGE, -1);
		}
		mTwitterWrapper = getApplication().getTwitterWrapper();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		mCursor = -1;
		super.onDestroyView();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mSelectedUser = null;
		if (!mIsMyUserList) return false;
		final UsersAdapter adapter = getListAdapter();
		mSelectedUser = adapter.getItem(position);
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_user_list_member);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableUser>> loader, final List<ParcelableUser> data) {
		if (loader instanceof UserListMembersLoader) {
			final long cursor = ((UserListMembersLoader) loader).getNextCursor();
			if (cursor != -2) {
				mCursor = cursor;
			}
		}
		super.onLoadFinished(loader, data);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedUser == null) return false;
		switch (item.getItemId()) {
			case MENU_DELETE: {
				mTwitterWrapper.deleteUserListMembers(getAccountId(), mUserListId, mSelectedUser.user_id);
				break;
			}
			case MENU_VIEW_PROFILE: {
				openUserProfile(getActivity(), mSelectedUser);
				break;
			}
			default: {
				if (item.getIntent() != null) {
					try {
						startActivity(item.getIntent());
					} catch (final ActivityNotFoundException e) {
						Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return false;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLong(INTENT_KEY_PAGE, mCursor);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_MEMBERS_DELETED);
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

}
