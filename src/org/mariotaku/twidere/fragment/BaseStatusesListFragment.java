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

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.openConversation;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.StatusViewHolder;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.NoDuplicatesLinkedList;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusesAdapterInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.twitter.Extractor;

abstract class BaseStatusesListFragment<Data> extends PullToRefreshListFragment implements LoaderCallbacks<Data>,
		OnScrollListener, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener, Panes.Left {

	private static final long TICKER_DURATION = 5000L;

	private ServiceInterface mService;
	private TwidereApplication mApplication;
	private SharedPreferences mPreferences;
	private AsyncTaskManager mAsyncTaskManager;

	private Handler mHandler;
	private Runnable mTicker;

	private ListView mListView;

	private StatusesAdapterInterface mAdapter;
	private PopupMenu mPopupMenu;

	private Data mData;
	private ParcelableStatus mSelectedStatus;

	private boolean mLoadMoreAutomatically;

	private volatile boolean mBusy, mTickerStopped, mReachedBottom, mActivityFirstCreated,
			mNotReachedBottomBefore = true;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MULTI_SELECT_STATE_CHANGED.equals(action)) {
				mAdapter.setMultiSelectEnabled(mApplication.isMultiSelectActive());
			} else if (BROADCAST_MULTI_SELECT_ITEM_CHANGED.equals(action)) {
				mAdapter.notifyDataSetChanged();
			}
		}

	};

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public final Data getData() {
		return mData;
	}

	public abstract long[] getLastStatusIds();

	@Override
	public abstract StatusesAdapterInterface getListAdapter();

	public ParcelableStatus getSelectedStatus() {
		return mSelectedStatus;
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public abstract int getStatuses(long[] account_ids, long[] max_ids);

	public boolean isActivityFirstCreated() {
		return mActivityFirstCreated;
	}

	public abstract boolean isListLoadFinished();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mApplication = getApplication();
		mAsyncTaskManager = getAsyncTaskManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getServiceInterface();
		mAdapter = getListAdapter();
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		setMode(Mode.BOTH);
		getLoaderManager().initLoader(0, getArguments(), this);
		setListShown(false);
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
	public abstract Loader<Data> onCreateLoader(int id, Bundle args);

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			final ParcelableStatus status = getListAdapter().findStatus(id);
			if (status == null) return;
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) {
				getStatuses(new long[] { status.account_id }, new long[] { status.status_id });
			} else {
				if (mApplication.isMultiSelectActive()) {
					final NoDuplicatesLinkedList<Object> list = mApplication.getSelectedItems();
					if (!list.contains(status)) {
						list.add(status);
					} else {
						list.remove(status);
					}
					return;
				}
				openStatus(getActivity(), status);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = getListAdapter().findStatus(id);
			if (mApplication.isMultiSelectActive()) {
				final NoDuplicatesLinkedList<Object> list = mApplication.getSelectedItems();
				if (!list.contains(mSelectedStatus)) {
					list.add(mSelectedStatus);
				} else {
					list.remove(mSelectedStatus);
				}
				return true;
			}
			mPopupMenu = PopupMenu.getInstance(getActivity(), view);
			mPopupMenu.inflate(R.menu.action_status);
			setMenuForStatus(getActivity(), mPopupMenu.getMenu(), mSelectedStatus);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();

			return true;
		}
		return false;
	}

	@Override
	public void onLoaderReset(Loader<Data> loader) {
		mData = null;
	}

	@Override
	public void onLoadFinished(Loader<Data> loader, Data data) {
		mData = data;
		mAdapter.setShowAccountColor(getActivatedAccountIds(getActivity()).length > 1);
		setListShown(true);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		final ParcelableStatus status = mSelectedStatus;
		if (status == null) return false;
		switch (item.getItemId()) {
			case MENU_SHARE: {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "@" + status.screen_name + ": " + status.text_plain);
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
			case MENU_RETWEET: {
				if (isMyRetweet(status)) {
					mService.cancelRetweet(status.account_id, status.status_id);
				} else {
					final long id_to_retweet = mSelectedStatus.is_retweet && mSelectedStatus.retweet_id > 0 ? mSelectedStatus.retweet_id
							: mSelectedStatus.status_id;
					mService.retweetStatus(status.account_id, id_to_retweet);
				}
				break;
			}
			case MENU_QUOTE: {
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, status.account_id);
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status.status_id);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, status.screen_name);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, status.name);
				bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
				bundle.putString(INTENT_KEY_TEXT, getQuoteStatus(getActivity(), status.screen_name, status.text_plain));
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_REPLY: {
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle bundle = new Bundle();
				final List<String> mentions = new Extractor().extractMentionedScreennames(status.text_plain);
				mentions.remove(status.screen_name);
				mentions.add(0, status.screen_name);
				bundle.putStringArray(INTENT_KEY_MENTIONS, mentions.toArray(new String[mentions.size()]));
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, status.account_id);
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status.status_id);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, status.screen_name);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, status.name);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_FAV: {
				if (mSelectedStatus.is_favorite) {
					mService.destroyFavorite(status.account_id, status.status_id);
				} else {
					mService.createFavorite(status.account_id, status.status_id);
				}
				break;
			}
			case MENU_CONVERSATION: {
				openConversation(getActivity(), status.account_id, status.status_id);
				break;
			}
			case MENU_DELETE: {
				mService.destroyStatus(status.account_id, status.status_id);
				break;
			}
			case MENU_EXTENSIONS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_STATUS);
				final Bundle extras = new Bundle();
				extras.putParcelable(INTENT_KEY_STATUS, status);
				intent.putExtras(extras);
				startActivity(Intent.createChooser(intent, getString(R.string.open_with_extensions)));
				break;
			}
			case MENU_MULTI_SELECT: {
				if (!mApplication.isMultiSelectActive()) {
					mApplication.startMultiSelect();
				}
				final NoDuplicatesLinkedList<Object> list = mApplication.getSelectedItems();
				if (!list.contains(status)) {
					list.add(status);
				}
				break;
			}
		}
		return true;
	}

	public abstract void onPostStart();

	@Override
	public abstract void onPullDownToRefresh();

	@Override
	public void onResume() {
		super.onResume();
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_image_preview = mPreferences.getBoolean(PREFERENCE_KEY_INLINE_IMAGE_PREVIEW, false);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final boolean show_absolute_time = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false);
		mAdapter.setMultiSelectEnabled(mApplication.isMultiSelectActive());
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayImagePreview(display_image_preview);
		mAdapter.setDisplayName(display_name);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
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
				if (!isRefreshing()) {
					onPullUpToRefresh();
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

		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_MULTI_SELECT_STATE_CHANGED);
		filter.addAction(BROADCAST_MULTI_SELECT_ITEM_CHANGED);
		registerReceiver(mStateReceiver, filter);

		onPostStart();

	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		mActivityFirstCreated = false;
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}
}
