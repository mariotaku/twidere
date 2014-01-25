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

import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.clearListViewChoices;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

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
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import org.mariotaku.menucomponent.widget.PopupMenu;
import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter.MenuButtonClickListener;
import org.mariotaku.twidere.loader.DummyParcelableUsersLoader;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList;

import java.util.Collections;
import java.util.List;

abstract class BaseUsersListFragment extends BasePullToRefreshListFragment implements
		LoaderCallbacks<List<ParcelableUser>>, OnItemLongClickListener, Panes.Left, OnMenuItemClickListener,
		MultiSelectManager.Callback, MenuButtonClickListener {

	private SharedPreferences mPreferences;
	private PopupMenu mPopupMenu;
	private MultiSelectManager mMultiSelectManager;

	private ParcelableUsersAdapter mAdapter;

	private boolean mLoadMoreAutomatically;
	private ListView mListView;
	private long mAccountId;

	private final List<ParcelableUser> mData = Collections
			.synchronizedList(new NoDuplicatesArrayList<ParcelableUser>());
	private ParcelableUser mSelectedUser;

	public long getAccountId() {
		return mAccountId;
	}

	public final List<ParcelableUser> getData() {
		return mData;
	}

	@Override
	public ParcelableUsersAdapter getListAdapter() {
		return mAdapter;
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public void loadMoreUsers() {
		if (isRefreshing()) return;
		final int count = mAdapter.getCount();
		if (count - 1 > 0) {
			final Bundle args = getArguments();
			if (args != null) {
				args.putLong(EXTRA_MAX_ID, mAdapter.getItem(count - 1).id);
			}
			if (!getLoaderManager().hasRunningLoaders()) {
				getLoaderManager().restartLoader(0, args, this);
			}
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mAdapter = new ParcelableUsersAdapter(getActivity());
		mAdapter.setMenuButtonClickListener(this);
		mMultiSelectManager = getMultiSelectManager();
		mListView = getListView();
		mListView.setFastScrollEnabled(mPreferences.getBoolean(KEY_FAST_SCROLL_THUMB, false));
		final Bundle args = getArguments() != null ? getArguments() : new Bundle();
		final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
		if (mAccountId != account_id) {
			mAdapter.clear();
			mData.clear();
		}
		mAccountId = account_id;
		mListView.setDivider(null);
		mListView.setSelector(android.R.color.transparent);
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
	}

	@Override
	public Loader<List<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		final Loader<List<ParcelableUser>> loader = newLoaderInstance(getActivity(), args);
		return loader != null ? loader : new DummyParcelableUsersLoader(getActivity());
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final ParcelableUsersAdapter adapter = getListAdapter();
		final ParcelableUser user = adapter.findItem(id);
		if (user == null) return false;
		setItemSelected(user, position, !mMultiSelectManager.isSelected(user));
		return true;
	}

	@Override
	public void onItemsCleared() {
		clearListViewChoices(mListView);
	}

	@Override
	public void onItemSelected(final Object item) {
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	public void onItemUnselected(final Object item) {
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		final ParcelableUser user = mAdapter.findItem(id);
		if (user == null) return;
		if (mMultiSelectManager.isActive()) {
			setItemSelected(user, position, !mMultiSelectManager.isSelected(user));
			return;
		}
		openUserProfile(getActivity(), user);
	}

	@Override
	public void onLoaderReset(final Loader<List<ParcelableUser>> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(final Loader<List<ParcelableUser>> loader, final List<ParcelableUser> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.setData(data);
		mAdapter.setShowAccountColor(shouldShowAccountColor());
		setRefreshComplete();
		setListShown(true);
	}

	@Override
	public void onMenuButtonClick(final View button, final int position, final long id) {
		final ParcelableUser user = mAdapter.getItem(position - mListView.getHeaderViewsCount());
		if (user == null) return;
		showMenu(button, user);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedUser == null) return false;
		switch (item.getItemId()) {
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

	@Override
	public void onReachedBottom() {
		if (!mLoadMoreAutomatically) return;
		loadMoreUsers();
	}

	@Override
	public void onRefreshFromEnd() {
		if (mLoadMoreAutomatically) return;
		loadMoreUsers();
	}

	@Override
	public void onRefreshFromStart() {
		if (isRefreshing()) return;
		getLoaderManager().restartLoader(0, getArguments(), this);
	}

	@Override
	public void onResume() {
		super.onResume();
		mLoadMoreAutomatically = mPreferences.getBoolean(KEY_LOAD_MORE_AUTOMATICALLY, false);
		configBaseCardAdapter(getActivity(), mAdapter);
	}

	@Override
	public void onStart() {
		super.onStart();
		mMultiSelectManager.registerCallback(this);
		final int choiceMode = mListView.getChoiceMode();
		if (mMultiSelectManager.isActive()) {
			if (choiceMode != ListView.CHOICE_MODE_MULTIPLE) {
				mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			}
		} else {
			if (choiceMode != ListView.CHOICE_MODE_NONE) {
				Utils.clearListViewChoices(mListView);
			}
		}
	}

	@Override
	public void onStop() {
		mMultiSelectManager.unregisterCallback(this);
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	protected ParcelableUser getSelectedUser() {
		return mSelectedUser;
	}

	protected int getUserMenuResource() {
		return 0;
	}

	protected abstract Loader<List<ParcelableUser>> newLoaderInstance(Context context, Bundle args);

	protected void onPrepareItemMenu(final Menu menu) {

	}

	protected final void removeUsers(final long... user_ids) {
		if (user_ids == null || user_ids.length == 0) return;
		for (final long user_id : user_ids) {
			mData.remove(mAdapter.findItem(user_id));
		}
		mAdapter.setData(mData, true);
	}

	protected void setItemSelected(final ParcelableUser user, final int position, final boolean selected) {
		if (selected) {
			mMultiSelectManager.selectItem(user);
		} else {
			mMultiSelectManager.unselectItem(user);
		}
		mListView.setItemChecked(position, selected);
	}

	protected boolean shouldShowAccountColor() {
		return getActivatedAccountIds(getActivity()).length > 1;
	}

	private void showMenu(final View view, final ParcelableUser user) {
		mSelectedUser = user;
		if (view == null || user == null) return;
		if (mPopupMenu != null && mPopupMenu.isShowing()) {
			mPopupMenu.dismiss();
		}
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		final int menuRes = getUserMenuResource();
		if (menuRes != 0) {
			mPopupMenu.inflate(menuRes);
		}
		final Menu menu = mPopupMenu.getMenu();
		onPrepareItemMenu(menu);
		final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
		final Bundle extensionsExtras = new Bundle();
		extensionsExtras.putParcelable(EXTRA_USER, user);
		extensionsIntent.putExtras(extensionsExtras);
		addIntentToMenu(getActivity(), menu, extensionsIntent);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
	}
}
