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

import static org.mariotaku.twidere.util.Utils.cancelRetweet;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.showOkMessage;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.PositionManager;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

abstract class BaseStatusesListFragment<Data> extends BasePullToRefreshListFragment implements LoaderCallbacks<Data>,
		OnScrollListener, OnItemLongClickListener, OnMenuItemClickListener, Panes.Left, MultiSelectManager.Callback {

	private static final long TICKER_DURATION = 5000L;

	private AsyncTaskManager mAsyncTaskManager;
	private AsyncTwitterWrapper mTwitterWrapper;
	private SharedPreferences mPreferences;

	private Handler mHandler;
	private Runnable mTicker;

	private ListView mListView;
	private IStatusesAdapter<Data> mAdapter;
	private PopupMenu mPopupMenu;

	private Data mData;
	private ParcelableStatus mSelectedStatus;

	private boolean mLoadMoreAutomatically;

	private boolean mBusy, mTickerStopped, mReachedBottom, mNotReachedBottomBefore = true;
	private int mListScrollOffset;

	private MultiSelectManager mMultiSelectManager;
	private PositionManager mPositionManager;

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public final Data getData() {
		return mData;
	}

	@Override
	public IStatusesAdapter<Data> getListAdapter() {
		return mAdapter;
	}

	public ParcelableStatus getSelectedStatus() {
		return mSelectedStatus;
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public abstract int getStatuses(long[] account_ids, long[] max_ids, long[] since_ids);

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAsyncTaskManager = getAsyncTaskManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPositionManager = new PositionManager(getActivity());
		mTwitterWrapper = getTwitterWrapper();
		mMultiSelectManager = getMultiSelectManager();
		mListView = getListView();
		mAdapter = newAdapterInstance();
		setListAdapter(null);
		setListHeaderFooters(mListView);
		setListAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemLongClickListener(this);
		setMode(Mode.BOTH);
		setListShown(false);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public abstract Loader<Data> onCreateLoader(int id, Bundle args);

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mSelectedStatus = null;
		final Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			final boolean click_to_open_menu = mPreferences.getBoolean(PREFERENCE_KEY_CLICK_TO_OPEN_MENU, false);
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			final ParcelableStatus status = mSelectedStatus = mAdapter.getStatus(position
					- mListView.getHeaderViewsCount());
			if (mMultiSelectManager.isActive() || click_to_open_menu) {
				if (!mMultiSelectManager.isSelected(status)) {
					mMultiSelectManager.selectItem(status);
				} else {
					mMultiSelectManager.unselectItem(mSelectedStatus);
				}
				return true;
			}
			openMenu(view, status);
			return true;
		}
		return false;
	}

	@Override
	public void onItemsCleared() {
		mAdapter.setMultiSelectEnabled(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemSelected(final Object item) {
		mAdapter.setMultiSelectEnabled(true);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemUnselected(final Object item) {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		mSelectedStatus = null;
		final Object tag = v.getTag();
		if (tag instanceof StatusViewHolder) {
			final boolean click_to_open_menu = mPreferences.getBoolean(PREFERENCE_KEY_CLICK_TO_OPEN_MENU, false);
			final ParcelableStatus status = mSelectedStatus = mAdapter.getStatus(position - l.getHeaderViewsCount());
			if (status == null) return;
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) {
				getStatuses(new long[] { status.account_id }, new long[] { status.id }, null);
			} else {
				if (mMultiSelectManager.isActive()) {
					if (!mMultiSelectManager.isSelected(status)) {
						mMultiSelectManager.selectItem(status);
					} else {
						mMultiSelectManager.unselectItem(status);
					}
					return;
				}
				if (click_to_open_menu) {
					openMenu(v, status);
				} else {
					openStatus(getActivity(), status);
				}
			}
		}
	}

	@Override
	public final void onLoaderReset(final Loader<Data> loader) {
		mAdapter.setData(mData = null);
	}

	@Override
	public final void onLoadFinished(final Loader<Data> loader, final Data data) {
		setData(data);
		final int first_visible_position = mListView.getFirstVisiblePosition();
		if (mListView.getChildCount() > 0) {
			final View first_child = mListView.getChildAt(0);
			mListScrollOffset = first_child != null ? first_child.getTop() : 0;
		}
		final long last_viewed_id = mAdapter.findItemIdByPosition(first_visible_position);
		mAdapter.setData(data);
		setListShown(true);
		onRefreshComplete();
		setProgressBarIndeterminateVisibility(false);
		mAdapter.setShowAccountColor(getActivatedAccountIds(getActivity()).length > 1);
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		final int curr_first_visible_position = mListView.getFirstVisiblePosition();
		final long curr_viewed_id = mAdapter.findItemIdByPosition(curr_first_visible_position);
		final long status_id;
		if (last_viewed_id <= 0) {
			if (!remember_position) return;
			status_id = mPositionManager.getPosition(getPositionKey());
		} else if ((first_visible_position > 0 || remember_position) && curr_viewed_id > 0
				&& last_viewed_id != curr_viewed_id) {
			status_id = last_viewed_id;
		} else
			return;
		final int position = mAdapter.findItemPositionByStatusId(status_id);
		if (position > -1 && position < mListView.getCount()) {
			mListView.setSelectionFromTop(position, mListScrollOffset);
			mListScrollOffset = 0;
		}
	}

	@Override
	public final boolean onMenuItemClick(final MenuItem item) {
		final ParcelableStatus status = mSelectedStatus;
		if (status == null) return false;
		switch (item.getItemId()) {
			case MENU_VIEW: {
				openStatus(getActivity(), status);
				break;
			}
			case MENU_SHARE: {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "@" + status.user_screen_name + ": " + status.text_plain);
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
			case MENU_COPY: {
				if (ClipboardUtils.setText(getActivity(), status.text_plain)) {
					showOkMessage(getActivity(), R.string.text_copied, false);
				}
				break;
			}
			case R.id.direct_retweet:
			case MENU_RETWEET: {
				if (isMyRetweet(status)) {
					cancelRetweet(mTwitterWrapper, status);
				} else {
					final long id_to_retweet = status.retweet_id > 0 ? status.retweet_id : status.id;
					mTwitterWrapper.retweetStatus(status.account_id, id_to_retweet);
				}
				break;
			}
			case R.id.direct_quote:
			case MENU_QUOTE: {
				final Intent intent = new Intent(INTENT_ACTION_QUOTE);
				final Bundle bundle = new Bundle();
				bundle.putParcelable(INTENT_KEY_STATUS, status);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_REPLY: {
				final Intent intent = new Intent(INTENT_ACTION_REPLY);
				final Bundle bundle = new Bundle();
				bundle.putParcelable(INTENT_KEY_STATUS, status);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_FAVORITE: {
				if (status.is_favorite) {
					mTwitterWrapper.destroyFavorite(status.account_id, status.id);
				} else {
					mTwitterWrapper.createFavoriteAsync(status.account_id, status.id);
				}
				break;
			}
			case MENU_DELETE: {
				mTwitterWrapper.destroyStatus(status.account_id, status.id);
				break;
			}
			case MENU_LOAD_FROM_POSITION: {
				getStatuses(new long[] { status.account_id }, new long[] { status.id }, null);
				break;
			}
			case MENU_MULTI_SELECT: {
				mMultiSelectManager.selectItem(status);
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

	@Override
	public abstract void onPullDownToRefresh();

	@Override
	public void onResume() {
		super.onResume();
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		mListView.setFastScrollEnabled(mPreferences.getBoolean(PREFERENCE_KEY_FAST_SCROLL_THUMB, false));
		final float text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final String image_preview_display_option = mPreferences.getString(PREFERENCE_KEY_IMAGE_PREVIEW_DISPLAY_OPTION,
				IMAGE_PREVIEW_DISPLAY_OPTION_NONE);
		final boolean show_absolute_time = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false);
		final boolean display_sensitive_contents = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS,
				false);
		final boolean link_highlighting = mPreferences.getBoolean(PREFERENCE_KEY_LINK_HIGHLIGHTING, false);
		final boolean indicate_my_status = mPreferences.getBoolean(PREFERENCE_KEY_INDICATE_MY_STATUS, true);
		final String name_display_option = mPreferences.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION,
				NAME_DISPLAY_OPTION_BOTH);
		final boolean link_underline_only = mPreferences.getBoolean(PREFERENCE_KEY_LINK_UNDERLINE_ONLY, false);
		mAdapter.setMultiSelectEnabled(mMultiSelectManager.isActive());
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
		mAdapter.setNameDisplayOption(name_display_option);
		mAdapter.setImagePreviewDisplayOption(image_preview_display_option);
		mAdapter.setDisplaySensitiveContents(display_sensitive_contents);
		mAdapter.setLinkHightlightingEnabled(link_highlighting);
		mAdapter.setIndicateMyStatusDisabled(isMyTimeline() || !indicate_my_status);
		mAdapter.setLinkUnderlineOnly(link_underline_only);
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
			if (mLoadMoreAutomatically && mReachedBottom && getListAdapter().getCount() > visibleItemCount) {
				if (!isRefreshing()) {
					onPullUpToRefresh();
				}
			}
		}

	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
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
					mAdapter.notifyDataSetChanged();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + TICKER_DURATION - now % TICKER_DURATION;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
		mMultiSelectManager.registerCallback(this);
	}

	@Override
	public void onStop() {
		savePosition();
		mMultiSelectManager.unregisterCallback(this);
		mTickerStopped = true;
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	protected abstract long[] getNewestStatusIds();

	protected abstract long[] getOldestStatusIds();

	protected abstract String getPositionKey();

	protected abstract IStatusesAdapter<Data> newAdapterInstance();

	protected void savePosition() {
		final int first_visible_position = mListView.getFirstVisiblePosition();
		if (mListView.getChildCount() > 0) {
			final View first_child = mListView.getChildAt(0);
			mListScrollOffset = first_child != null ? first_child.getTop() : 0;
		}
		final long status_id = mAdapter.findItemIdByPosition(first_visible_position);
		mPositionManager.setPosition(getPositionKey(), status_id);
	}

	protected final void setData(final Data data) {
		mData = data;
	}

	protected void setListHeaderFooters(final ListView list) {

	}

	private boolean isMyTimeline() {
		final Bundle args = getArguments();
		if (args != null && this instanceof UserTimelineFragment) {
			final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			final long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			final String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			if (account_id == user_id || screen_name != null
					&& screen_name.equals(getAccountScreenName(getActivity(), account_id))) return true;
		}
		return false;
	}

	private void openMenu(final View view, final ParcelableStatus status) {
		if (view == null || status == null) return;
		final int activated_color = ThemeUtils.getThemeColor(getActivity());
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_status);
		final boolean click_to_open_menu = mPreferences.getBoolean(PREFERENCE_KEY_CLICK_TO_OPEN_MENU, false);
		final boolean separate_retweet_action = mPreferences.getBoolean(PREFERENCE_KEY_SEPARATE_RETWEET_ACTION, false);
		final Menu menu = mPopupMenu.getMenu();
		setMenuForStatus(getActivity(), menu, status);
		final MenuItem view_status = menu.findItem(MENU_VIEW);
		if (view_status != null) {
			view_status.setVisible(click_to_open_menu);
		}
		final MenuItem retweet_submenu = menu.findItem(R.id.retweet_submenu);
		if (retweet_submenu != null) {
			retweet_submenu.setVisible(!separate_retweet_action);
		}
		final MenuItem direct_quote = menu.findItem(R.id.direct_quote);
		if (direct_quote != null) {
			direct_quote.setVisible(separate_retweet_action);
		}
		final MenuItem direct_retweet = menu.findItem(R.id.direct_retweet);
		if (direct_retweet != null) {
			final Drawable icon = direct_retweet.getIcon().mutate();
			direct_retweet.setVisible(separate_retweet_action && (!status.user_is_protected || isMyRetweet(status)));
			if (isMyRetweet(status)) {
				icon.setColorFilter(activated_color, PorterDuff.Mode.MULTIPLY);
				direct_retweet.setTitle(R.string.cancel_retweet);
			} else {
				icon.clearColorFilter();
				direct_retweet.setTitle(R.string.retweet);
			}
		}
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
	}

}
