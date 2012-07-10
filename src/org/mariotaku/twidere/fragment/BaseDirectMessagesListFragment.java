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

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.adapter.DirectMessagesCursorAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.DirectMessageViewHolder;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.DirectMessagesAdapterInterface;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

abstract class BaseDirectMessagesListFragment extends PullToRefreshListFragment implements LoaderCallbacks<Cursor>,
		OnScrollListener, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	private ServiceInterface mServiceInterface;

	private SharedPreferences mPreferences;
	private AsyncTaskManager mAsyncTaskManager;

	private Handler mHandler;
	private Runnable mTicker;
	private ListView mListView;

	private PopupMenu mPopupMenu;

	private ParcelableDirectMessage mSelectedDirectMessage;
	private int mRunningTaskId;

	private boolean mLoadMoreAutomatically;

	private volatile boolean mBusy, mTickerStopped, mReachedBottom, mActivityFirstCreated,
			mNotReachedBottomBefore = true;

	private DirectMessagesCursorAdapter mAdapter;

	private static final long TICKER_DURATION = 5000L;

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public abstract Uri getContentUri();

	public abstract int getDirectMessages(long account_id, long max_id);

	public long getLastDirectMessageId() {
		return -1;
	};

	@Override
	public DirectMessagesAdapterInterface getListAdapter() {
		return mAdapter;
	}

	public ParcelableDirectMessage getSelectedDirectMessage() {
		return mSelectedDirectMessage;
	}

	public ServiceInterface getServiceInterface() {
		return mServiceInterface;
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public boolean isActivityFirstCreated() {
		return mActivityFirstCreated;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAsyncTaskManager = AsyncTaskManager.getInstance();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mServiceInterface = getApplication().getServiceInterface();
		final LazyImageLoader imageloader = ((TwidereApplication) getActivity().getApplication())
				.getProfileImageLoader();
		mAdapter = new DirectMessagesCursorAdapter(getActivity(), imageloader);
		setListAdapter(mAdapter);
		setShowIndicator(false);
		mListView = getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivityFirstCreated = true;
		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (args == null || !args.containsKey(INTENT_KEY_ACCOUNT_ID)) return null;
		final String[] cols = DirectMessages.COLUMNS;
		final Uri uri = getContentUri();
		final String where = DirectMessages.ACCOUNT_ID + " = " + args.getLong(INTENT_KEY_ACCOUNT_ID);
		return new CursorLoader(getActivity(), uri, cols, where, null, DirectMessages.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof DirectMessageViewHolder) {
			final ParcelableDirectMessage message = getListAdapter().findItem(id);
			final DirectMessageViewHolder holder = (DirectMessageViewHolder) tag;
			if (holder.show_as_gap || position == adapter.getCount() - 1 && !mLoadMoreAutomatically) {
				getDirectMessages(message.account_id, message.message_id);
			} else {
				openStatus(message);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof DirectMessageViewHolder) {
			final DirectMessageViewHolder holder = (DirectMessageViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedDirectMessage = getListAdapter().findItem(id);

			return true;
		}
		return false;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);

	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mSelectedDirectMessage != null) {
			final long status_id = mSelectedDirectMessage.message_id;
			final long account_id = mSelectedDirectMessage.account_id;
			switch (item.getItemId()) {
				case MENU_DELETE: {
					mServiceInterface.destroyStatus(account_id, status_id);
					break;
				}
				default:
					return false;
			}
		}
		return true;
	}

	public abstract void onPostStart();

	@Override
	public void onRefresh() {
		final Bundle args = getArguments();
		if (args == null || !args.containsKey(INTENT_KEY_ACCOUNT_ID)) return;
		mRunningTaskId = getDirectMessages(args.getLong(INTENT_KEY_ACCOUNT_ID), -1);
	};

	@Override
	public void onResume() {
		super.onResume();
		final DirectMessagesAdapterInterface adapter = getListAdapter();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		adapter.setDisplayProfileImage(display_profile_image);
		adapter.setDisplayName(display_name);
		adapter.setTextSize(text_size);
		adapter.setShowLastItemAsGap(!mLoadMoreAutomatically);
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
			if (mLoadMoreAutomatically && mReachedBottom && getListAdapter().getCount() > visibleItemCount) {
				if (!mAsyncTaskManager.isExcuting(mRunningTaskId)) {
					final Bundle args = getArguments();
					if (args == null || !args.containsKey(INTENT_KEY_ACCOUNT_ID)) return;
					mRunningTaskId = getDirectMessages(args.getLong(INTENT_KEY_ACCOUNT_ID),
							mAdapter.findItemIdByPosition(mAdapter.getCount() - 1));
				}
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
			case SCROLL_STATE_TOUCH_SCROLL:
				mBusy = true;
				break;
			case SCROLL_STATE_IDLE:
				mBusy = false;
				break;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mTickerStopped = false;
		mHandler = new Handler();

		mTicker = new Runnable() {

			@Override
			public void run() {
				if (mTickerStopped) return;
				if (mListView != null && !mBusy) {
					getListAdapter().notifyDataSetChanged();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + TICKER_DURATION - now % TICKER_DURATION;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();

		onPostStart();
	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		mActivityFirstCreated = false;
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private void openStatus(ParcelableDirectMessage status) {

	}

}
