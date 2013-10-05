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

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.loader.CursorSupportUsersLoader;
import org.mariotaku.twidere.loader.UserListMembersLoader;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.AsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class UserListMembersFragment extends CursorSupportUsersListFragment implements OnMenuItemClickListener {

	private ParcelableUser mSelectedUser;
	private ParcelableUserList mUserList;

	private PopupMenu mPopupMenu;
	private AsyncTwitterWrapper mTwitterWrapper;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_USER_LIST_MEMBERS_DELETED.equals(action)) {
				if (!intent.getBooleanExtra(EXTRA_SUCCEED, false)) return;
				final ParcelableUserList list = intent.getParcelableExtra(EXTRA_USER_LIST);
				if (mUserList != null && list != null && list.id == mUserList.id) {
					removeUsers(intent.getLongArrayExtra(EXTRA_USER_IDS));
				}
			}
		}
	};

	@Override
	public CursorSupportUsersLoader newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final int list_id = args.getInt(EXTRA_LIST_ID, -1);
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
		final long user_id = args.getLong(EXTRA_USER_ID, -1);
		final String screen_name = args.getString(EXTRA_SCREEN_NAME);
		final String list_name = args.getString(EXTRA_LIST_NAME);
		return new UserListMembersLoader(context, account_id, list_id, user_id, screen_name, list_name,
				getNextCursor(), getData());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		final Bundle args = getArguments();
		if (savedInstanceState != null) {
			mUserList = savedInstanceState.getParcelable(EXTRA_USER_LIST);
		} else if (args != null) {
			mUserList = args.getParcelable(EXTRA_USER_LIST);
		}
		mTwitterWrapper = getApplication().getTwitterWrapper();
		super.onActivityCreated(savedInstanceState);
		if (mUserList == null && args != null) {
			final int list_id = args.getInt(EXTRA_LIST_ID, -1);
			final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
			final long user_id = args.getLong(EXTRA_USER_ID, -1);
			final String screen_name = args.getString(EXTRA_SCREEN_NAME);
			final String list_name = args.getString(EXTRA_LIST_NAME);
			new GetUserListTask(account_id, list_id, list_name, user_id, screen_name).execute();
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mSelectedUser = null;
		if (mUserList == null || mUserList.account_id != mUserList.user_id) return false;
		final ParcelableUsersAdapter adapter = getListAdapter();
		mSelectedUser = adapter.getItem(position - getListView().getHeaderViewsCount());
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_user_list_member);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedUser == null || mUserList == null) return false;
		switch (item.getItemId()) {
			case MENU_DELETE: {
				mTwitterWrapper.deleteUserListMembersAsync(getAccountId(), mUserList.id, mSelectedUser.id);
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
		outState.putParcelable(EXTRA_USER_LIST, mUserList);
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

	private class GetUserListTask extends AsyncTask<Void, Void, ParcelableUserList> {

		private final long account_id, user_id;
		private final int list_id;
		private final String screen_name, list_name;
		private final boolean hires_profile_image;

		private GetUserListTask(final long account_id, final int list_id, final String list_name, final long user_id,
				final String screen_name) {
			this.account_id = account_id;
			this.user_id = user_id;
			this.list_id = list_id;
			this.screen_name = screen_name;
			this.list_name = list_name;
			hires_profile_image = getResources().getBoolean(R.bool.hires_profile_image);
		}

		@Override
		protected ParcelableUserList doInBackground(final Void... params) {
			final Twitter twitter = getTwitterInstance(getActivity(), account_id, true);
			if (twitter == null) return null;
			try {
				final UserList list;
				if (list_id > 0) {
					list = twitter.showUserList(list_id);
				} else if (user_id > 0) {
					list = twitter.showUserList(list_name, user_id);
				} else if (screen_name != null) {
					list = twitter.showUserList(list_name, screen_name);
				} else
					return null;
				return new ParcelableUserList(list, account_id, hires_profile_image);
			} catch (final TwitterException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(final ParcelableUserList result) {
			if (mUserList != null) return;
			mUserList = result;
		}
	}
}
