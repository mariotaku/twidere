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

import static org.mariotaku.twidere.util.Utils.openUserListDetails;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SeparatedListAdapter;
import org.mariotaku.twidere.adapter.UserListsAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.UserListsLoader;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUserList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class UserListsListFragment extends BaseListFragment implements LoaderCallbacks<UserListsLoader.UserListsData>,
		OnItemClickListener, OnItemLongClickListener, Panes.Left, OnMenuItemClickListener {

	private SeparatedListAdapter<UserListsAdapter> mAdapter;
	private UserListsAdapter mUserListsAdapter, mUserListMembershipsAdapter;

	private SharedPreferences mPreferences;
	private ListView mListView;
	private long mAccountId, mUserId;
	private String mScreenName;

	private PopupMenu mPopupMenu;
	private ParcelableUserList mSelectedUserList;

	private TwidereApplication mApplication;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mApplication = getApplication();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final Bundle args = getArguments() != null ? getArguments() : new Bundle();
		if (args != null) {
			mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			mUserId = args.getLong(INTENT_KEY_USER_ID, -1);
			mScreenName = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		mAdapter = new SeparatedListAdapter<UserListsAdapter>(getActivity());
		mUserListsAdapter = new UserListsAdapter(getActivity());
		mUserListMembershipsAdapter = new UserListsAdapter(getActivity());
		mAdapter.addSection(getString(R.string.users_lists), mUserListsAdapter);
		mAdapter.addSection(getString(R.string.lists_following_user), mUserListMembershipsAdapter);
		mListView = getListView();
		mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		setListAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public Loader<UserListsLoader.UserListsData> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return new UserListsLoader(getActivity(), mAccountId, mUserId, mScreenName);
	}

	@Override
	public final void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return;
		final Object selected = mAdapter.getItem(position - mListView.getHeaderViewsCount());
		final ParcelableUserList user_list = selected instanceof ParcelableUserList ? (ParcelableUserList) selected
				: null;
		if (user_list == null) return;
		openUserListDetails(getActivity(), mAccountId, user_list.list_id, user_list.user_id,
				user_list.user_screen_name, user_list.name);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return true;
		mSelectedUserList = null;
		final ListAdapter adapter = getListAdapter();
		final Object selected = adapter.getItem(position - mListView.getHeaderViewsCount());
		mSelectedUserList = selected instanceof ParcelableUserList ? (ParcelableUserList) selected : null;
		if (mSelectedUserList == null) return false;
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_user_list);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<UserListsLoader.UserListsData> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(final Loader<UserListsLoader.UserListsData> loader,
			final UserListsLoader.UserListsData data) {
		setProgressBarIndeterminateVisibility(false);
		if (data != null) {
			mUserListsAdapter.setData(data.getLists());
			mUserListMembershipsAdapter.setData(data.getMemberships());
			mAdapter.notifyDataSetChanged();
		}
		setListShown(true);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedUserList == null) return false;
		switch (item.getItemId()) {
			case MENU_VIEW_USER_LIST: {
				openUserListDetails(getActivity(), mAccountId, mSelectedUserList.list_id, mSelectedUserList.user_id,
						mSelectedUserList.user_screen_name, mSelectedUserList.name);
				break;
			}
			case MENU_EXTENSIONS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
				final Bundle extras = new Bundle();
				extras.putParcelable(INTENT_KEY_USER_LIST, mSelectedUserList);
				intent.putExtras(extras);
				startActivity(Intent.createChooser(intent, getString(R.string.open_with_extensions)));
				break;
			}
		}
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		final float text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		for (final UserListsAdapter item : mAdapter.getAdapters()) {
			item.setDisplayProfileImage(display_profile_image);
			item.setTextSize(text_size);
		}
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}
}
