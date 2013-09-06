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

import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.openUserListDetails;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UserListsAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.BaseUserListsLoader;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUserList;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

abstract class BaseUserListsListFragment extends BasePullToRefreshListFragment implements
		LoaderCallbacks<List<ParcelableUserList>>, OnItemClickListener, OnScrollListener, OnItemLongClickListener,
		Panes.Left, OnMenuItemClickListener {

	private UserListsAdapter mAdapter;

	private SharedPreferences mPreferences;
	private boolean mLoadMoreAutomatically;
	private ListView mListView;
	private long mAccountId, mUserId;
	private String mScreenName;
	private final ArrayList<ParcelableUserList> mData = new ArrayList<ParcelableUserList>();
	private volatile boolean mReachedBottom, mNotReachedBottomBefore = true;

	private PopupMenu mPopupMenu;
	private ParcelableUserList mSelectedUserList;

	private long mCursor = -1;

	private TwidereApplication mApplication;

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
		mAdapter = new UserListsAdapter(getActivity());
		mListView = getListView();
		mListView.setFastScrollEnabled(mPreferences.getBoolean(PREFERENCE_KEY_FAST_SCROLL_THUMB, false));
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
		setListShown(false);
	}

	@Override
	public Loader<List<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return newLoaderInstance(mAccountId, mUserId, mScreenName);
	}

	@Override
	public final void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return;
		final ParcelableUserList user_list = mAdapter.findItem(id);
		if (user_list == null) return;
		openUserListDetails(getActivity(), mAccountId, user_list.id, user_list.user_id, user_list.user_screen_name,
				user_list.name);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return true;
		mSelectedUserList = null;
		final UserListsAdapter adapter = getListAdapter();
		mSelectedUserList = adapter.findItem(id);
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_user_list);
		final Menu menu = mPopupMenu.getMenu();
		final Intent extensions_intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
		final Bundle extensions_extras = new Bundle();
		extensions_extras.putParcelable(INTENT_KEY_USER_LIST, mSelectedUserList);
		extensions_intent.putExtras(extensions_extras);
		addIntentToMenu(getActivity(), menu, extensions_intent);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<List<ParcelableUserList>> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableUserList>> loader, final List<ParcelableUserList> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.appendData(data);
		if (loader instanceof BaseUserListsLoader) {
			final long cursor = ((BaseUserListsLoader) loader).getNextCursor();
			if (cursor != -2) {
				mCursor = cursor;
			}
		}
		setRefreshComplete();
		setListShown(true);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedUserList == null) return false;
		switch (item.getItemId()) {
			case MENU_VIEW_USER_LIST: {
				openUserListDetails(getActivity(), mAccountId, mSelectedUserList.id, mSelectedUserList.user_id,
						mSelectedUserList.user_screen_name, mSelectedUserList.name);
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
		return true;
	}

	public void onPullUpToRefresh() {
		final int count = mAdapter.getCount();
		if (count - 1 > 0) {
			final Bundle args = getArguments();
			if (args != null) {
				args.putLong(INTENT_KEY_MAX_ID, mAdapter.getItem(count - 1).user_id);
			}
			// if (!getLoaderManager().hasRunningLoaders()) {
			getLoaderManager().restartLoader(0, args, this);
			// }
		}
	}

	@Override
	public void onRefreshStarted() {
		super.onRefreshStarted();
		getLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
			final int totalItemCount) {
		final boolean reached = firstVisibleItem + visibleItemCount >= totalItemCount
				&& totalItemCount >= visibleItemCount;

		if (mReachedBottom != reached) {
			mReachedBottom = reached;
			if (mReachedBottom && mNotReachedBottomBefore) {
				mNotReachedBottomBefore = false;
				return;
			}
			final int count = mAdapter.getCount();
			if (mLoadMoreAutomatically && mReachedBottom && count > visibleItemCount) {
				onPullUpToRefresh();
			}
		}

	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
	}

	@Override
	public void onStart() {
		super.onStart();
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		final float text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setTextSize(text_size);
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

}
