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

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.UserListsAdapter;
import org.mariotaku.twidere.loader.BaseUserListsLoader;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUserList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

abstract class BaseUserListsListFragment extends BaseListFragment implements LoaderCallbacks<List<ParcelableUserList>>,
		OnItemClickListener, OnScrollListener, OnItemLongClickListener, Panes.Left, OnMenuItemClickListener {

	private UserListsAdapter mAdapter;

	private SharedPreferences mPreferences;
	private boolean mLoadMoreAutomatically;
	private ListView mListView;
	private long mAccountId, mUserId;
	private String mScreenName;
	private final ArrayList<ParcelableUserList> mData = new ArrayList<ParcelableUserList>();
	private volatile boolean mReachedBottom, mNotReachedBottomBefore = true;

	private Fragment mDetailFragment;

	private PopupMenu mPopupMenu;
	private ParcelableUserList mSelectedUserList;

	private boolean mAllItemsLoaded = false;
	private long mCursor = -1;

	public void addHeaders(ListView list) {

	}

	public long getAccountId() {
		return mAccountId;
	}

	public long getCursor() {
		return mCursor;
	}

	public final ArrayList<ParcelableUserList> getData() {
		return mData;
	}

	@Override
	public UserListsAdapter getListAdapter() {
		return mAdapter;
	}

	public String getScreenName() {
		return mScreenName;
	}

	public long getUserId() {
		return mUserId;
	}

	public abstract Loader<List<ParcelableUserList>> newLoaderInstance(long account_id, long user_id, String screen_name);

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final Bundle args = getArguments() != null ? getArguments() : new Bundle();
		if (args != null) {
			mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			mUserId = args.getLong(INTENT_KEY_USER_ID, -1);
			mScreenName = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		mAdapter = new UserListsAdapter(getActivity());
		setListAdapter(null);
		mListView = getListView();
		addHeaders(mListView);
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		if (mAccountId != account_id) {
			mAdapter.clear();
			mData.clear();
		}
		mAccountId = account_id;
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnScrollListener(this);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public Loader<List<ParcelableUserList>> onCreateLoader(int id, Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return newLoaderInstance(mAccountId, mUserId, mScreenName);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final ParcelableUserList user_list = mAdapter.findItem(id);
		if (user_list == null) return;
		if (mAdapter.isGap(id) && !mLoadMoreAutomatically) {
			final Bundle args = getArguments();
			if (!getLoaderManager().hasRunningLoaders()) {
				getLoaderManager().restartLoader(0, args, this);
			}
		} else {
			openUserListDetails(getActivity(), mAccountId, user_list.list_id, user_list.user_id,
					user_list.user_screen_name, user_list.name);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mSelectedUserList = null;
		final UserListsAdapter adapter = getListAdapter();
		if (adapter.isGap(position)) return false;
		mSelectedUserList = adapter.findItem(id);
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_user_list);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoaderReset(Loader<List<ParcelableUserList>> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableUserList>> loader, List<ParcelableUserList> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.setData(data);
		if (loader instanceof BaseUserListsLoader) {
			final long cursor = ((BaseUserListsLoader) loader).getNextCursor();
			mAllItemsLoaded = cursor != -2 && cursor == mCursor;
			mAdapter.setShowLastItemAsGap(!(mAllItemsLoaded || mLoadMoreAutomatically));
			if (cursor != -2) {
				mCursor = cursor;
			}
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
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
	public void onResume() {
		super.onResume();
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean hires_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_HIRES_PROFILE_IMAGE, false);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final boolean force_ssl_connection = mPreferences.getBoolean(PREFERENCE_KEY_FORCE_SSL_CONNECTION, false);
		mAdapter.setForceSSLConnection(force_ssl_connection);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayHiResProfileImage(hires_profile_image);
		mAdapter.setTextSize(text_size);
		mAdapter.setDisplayName(display_name);
		mAdapter.setShowLastItemAsGap(!(mAllItemsLoaded || mLoadMoreAutomatically));
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		final boolean reached = firstVisibleItem + visibleItemCount >= totalItemCount
				&& totalItemCount >= visibleItemCount;

		if (mReachedBottom != reached) {
			mReachedBottom = reached;
			if (mReachedBottom && mNotReachedBottomBefore) {
				mNotReachedBottomBefore = false;
				return;
			}
			final int count = mAdapter.getCount();
			if (mLoadMoreAutomatically && mReachedBottom && count > visibleItemCount && count - 1 > 0) {
				final Bundle args = getArguments();
				if (args != null) {
					args.putLong(INTENT_KEY_MAX_ID, mAdapter.getItem(count - 1).user_id);
				}
				if (!getLoaderManager().hasRunningLoaders()) {
					getLoaderManager().restartLoader(0, args, this);
				}
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	public void setAllItemsLoaded(boolean loaded) {
		mAllItemsLoaded = loaded;
	}

	private void openUserListDetails(Activity activity, long account_id, int list_id, long user_id, String screen_name,
			String list_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			if (mDetailFragment instanceof UserProfileFragment && mDetailFragment.isAdded()) {
				((UserProfileFragment) mDetailFragment).getUserInfo(mAccountId, user_id, screen_name);
			} else {
				mDetailFragment = new UserListDetailsFragment();
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				args.putInt(INTENT_KEY_LIST_ID, list_id);
				args.putLong(INTENT_KEY_USER_ID, user_id);
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
				args.putString(INTENT_KEY_LIST_NAME, list_name);
				mDetailFragment.setArguments(args);
				home_activity.showAtPane(HomeActivity.PANE_RIGHT, mDetailFragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_DETAILS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (list_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(list_id));
			}
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			if (list_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, list_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}
}
