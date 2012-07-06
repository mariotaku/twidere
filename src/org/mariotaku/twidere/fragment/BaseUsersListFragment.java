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

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.UsersAdapter;
import org.mariotaku.twidere.loader.IDsUsersLoader;
import org.mariotaku.twidere.model.ParcelableUser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

abstract class BaseUsersListFragment extends BaseListFragment implements LoaderCallbacks<List<ParcelableUser>>,
		OnItemClickListener, OnScrollListener {

	private UsersAdapter mAdapter;
	private SharedPreferences mPreferences;
	private boolean mLoadMoreAutomatically;
	private ListView mListView;
	private long mAccountId;
	private final ArrayList<ParcelableUser> mData = new ArrayList<ParcelableUser>();

	private volatile boolean mReachedBottom, mNotReachedBottomBefore = true;

	private Fragment mDetailFragment;

	private boolean mAllItemsLoaded = false;

	public final ArrayList<ParcelableUser> getData() {
		return mData;
	}

	public abstract Loader<List<ParcelableUser>> newLoaderInstance();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mAdapter = new UsersAdapter(getActivity());
		mListView = getListView();
		final Bundle args = getArguments() != null ? getArguments() : new Bundle();
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
		if (mAccountId != account_id) {
			mAdapter.clear();
			mData.clear();
		}
		mAccountId = account_id;
		mListView.setOnItemClickListener(this);
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
	public Loader<List<ParcelableUser>> onCreateLoader(int id, Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return newLoaderInstance();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final ParcelableUser user = mAdapter.getItem(position);
		if (user == null) return;
		if (mAdapter.isGap(position) && !mLoadMoreAutomatically) {
			final Bundle args = getArguments();
			if (args != null) {
				args.putLong(INTENT_KEY_MAX_ID, user.user_id);
			}
			if (!getLoaderManager().hasRunningLoaders()) {
				getLoaderManager().restartLoader(0, args, this);
			}
		} else {
			openUserProfile(user.user_id, user.screen_name);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<ParcelableUser>> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.setData(data);
		if (loader instanceof IDsUsersLoader) {
			final long[] ids = ((IDsUsersLoader) loader).getIDsArray();
			mAllItemsLoaded = ids != null && ids.length == mAdapter.getCount();
			mAdapter.setShowLastItemAsGap(!(mAllItemsLoaded || mLoadMoreAutomatically));
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_LOAD_MORE_AUTOMATICALLY, false);
		mAdapter.setDisplayProfileImage(display_profile_image);
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

	private void openUserProfile(long user_id, String screen_name) {
		final FragmentActivity activity = getActivity();
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			if (mDetailFragment instanceof UserProfileFragment && mDetailFragment.isAdded()) {
				((UserProfileFragment) mDetailFragment).getUserInfo(mAccountId, user_id, screen_name);
			} else {
				mDetailFragment = new UserProfileFragment();
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				args.putLong(INTENT_KEY_USER_ID, user_id);
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
				mDetailFragment.setArguments(args);
				home_activity.showAtPane(HomeActivity.PANE_RIGHT, mDetailFragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(mAccountId));
			builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

}
