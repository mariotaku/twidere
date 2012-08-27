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
import static org.mariotaku.twidere.util.Utils.getLastMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;

import org.mariotaku.twidere.adapter.DirectMessagesEntryAdapter;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

public class DirectMessagesFragment extends PullToRefreshListFragment implements LoaderCallbacks<Cursor>,
		OnScrollListener, OnItemClickListener, OnTouchListener {
	private ServiceInterface mService;

	private SharedPreferences mPreferences;
	private Handler mHandler;
	private Runnable mTicker;
	private ListView mListView;

	private volatile boolean mBusy, mTickerStopped, mReachedBottom, mNotReachedBottomBefore = true;

	private DirectMessagesEntryAdapter mAdapter;

	private static final long TICKER_DURATION = 5000L;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				if (isAdded() && !isDetached()) {
					getLoaderManager().restartLoader(0, null, DirectMessagesFragment.this);
				}
			} else if (BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)
					|| BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DirectMessagesFragment.this);
				onRefreshComplete();
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getApplication().getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		mService.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
		final LazyImageLoader imageloader = getApplication().getProfileImageLoader();
		mAdapter = new DirectMessagesEntryAdapter(getActivity(), imageloader);

		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnTouchListener(this);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		setMode(Mode.BOTH);

		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri uri = DirectMessages.ConversationsEntry.CONTENT_URI;
		final String where = DirectMessages.ACCOUNT_ID + " IN ("
				+ ArrayUtils.buildString(getActivatedAccountIds(getActivity()), ',', false) + ")";
		return new CursorLoader(getActivity(), uri, null, where, null, null);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final long conversation_id = mAdapter.findConversationId(id);
		final long account_id = mAdapter.findAccountId(id);
		if (conversation_id > 0 && account_id > 0) {
			openDirectMessagesConversation(getActivity(), account_id, conversation_id);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		mAdapter.setShowAccountColor(getActivatedAccountIds(getActivity()).length > 1);
		setListShown(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_COMPOSE: {
				openDMConversation();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPullDownToRefresh() {
		if (mService == null) return;
		final long[] account_ids = getActivatedAccountIds(getActivity());
		mService.getReceivedDirectMessages(account_ids, null);
		mService.getSentDirectMessages(account_ids, null);
	}

	@Override
	public void onPullUpToRefresh() {
		if (mService == null) return;
		final long[] account_ids = getActivatedAccountIds(getActivity());
		final long[] inbox_ids = getLastMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
		final long[] outbox_ids = getLastMessageIdsFromDatabase(getActivity(), DirectMessages.Outbox.CONTENT_URI);
		mService.getReceivedDirectMessages(account_ids, inbox_ids);
		mService.getSentDirectMessages(account_ids, outbox_ids);
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
					mAdapter.notifyDataSetChanged();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + TICKER_DURATION - now % TICKER_DURATION;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED);
		filter.addAction(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		if (mService.isReceivedDirectMessagesRefreshing() || mService.isSentDirectMessagesRefreshing()) {
			setRefreshing(false);
		} else {
			onRefreshComplete();
		}

		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean hires_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_HIRES_PROFILE_IMAGE, false);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final boolean show_absolute_time = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayHiResProfileImage(hires_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		mTickerStopped = true;
		super.onStop();
	}

	@Override
	public boolean onTouch(View view, MotionEvent ev) {
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mService.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
				break;
			}
		}
		return false;
	}

	public void openDMConversation() {
		openDirectMessagesConversation(getActivity(), -1, -1);
	}
}
