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
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getNewestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getOldestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;

import org.mariotaku.twidere.adapter.DirectMessagesEntryAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTask;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;

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
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class DirectMessagesFragment extends BasePullToRefreshListFragment implements LoaderCallbacks<Cursor> {

	private static final long TICKER_DURATION = 5000L;

	private TwidereApplication mApplication;
	private MultiSelectManager mMultiSelectManager;
	private SharedPreferences mPreferences;
	private Handler mHandler;
	private Runnable mTicker;

	private ListView mListView;

	private boolean mBusy, mTickerStopped;
	private boolean mLoadMoreAutomatically;

	private DirectMessagesEntryAdapter mAdapter;
	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DirectMessagesFragment.this);
			} else if (BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)
					|| BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DirectMessagesFragment.this);
			} else if (BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED.equals(action)
					|| BROADCAST_SENT_DIRECT_MESSAGES_REFRESHED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DirectMessagesFragment.this);
			} else if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				setRefreshing(twitter != null
						&& (twitter.isReceivedDirectMessagesRefreshing() || twitter.isSentDirectMessagesRefreshing()));
			}
		}
	};

	@Override
	public String getPullToRefreshTag() {
		return "direct_messages";
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onActivityCreated(savedInstanceState);
		mApplication = getApplication();
		mMultiSelectManager = getMultiSelectManager();
		mAdapter = new DirectMessagesEntryAdapter(getActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = DirectMessages.ConversationsEntry.CONTENT_URI;
		final String where = DirectMessages.ACCOUNT_ID + " IN ("
				+ ArrayUtils.toString(getActivatedAccountIds(getActivity()), ',', false) + ")";
		return new CursorLoader(getActivity(), uri, null, where, null, null);
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return;
		final int pos = position - l.getHeaderViewsCount();
		final long conversation_id = mAdapter.getConversationId(pos);
		final long account_id = mAdapter.getAccountId(pos);
		final String screen_name = mAdapter.getScreenName(pos);
		if (conversation_id > 0 && account_id > 0) {
			openDirectMessagesConversation(getActivity(), account_id, conversation_id, screen_name);
		}
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		if (getActivity() == null) return;
		mAdapter.changeCursor(cursor);
		mAdapter.setShowAccountColor(getActivatedAccountIds(getActivity()).length > 1);
		setListShown(true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_COMPOSE: {
				openDirectMessagesConversation(getActivity(), -1, -1, null);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostStart() {
		if (!isActivityFirstCreated()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onRefreshStarted() {
		super.onRefreshStarted();
		new AsyncTask<Void, Void, long[][]>() {

			@Override
			protected long[][] doInBackground(final Void... params) {
				final long[][] result = new long[2][];
				result[0] = getActivatedAccountIds(getActivity());
				result[1] = getNewestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
				return result;
			}

			@Override
			protected void onPostExecute(final long[][] result) {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (twitter == null) return;
				twitter.getReceivedDirectMessages(result[0], null, result[1]);
				twitter.getSentDirectMessages(result[0], null, null);
			}

		}.execute();
	}

	@Override
	public void onResume() {
		super.onResume();
		mListView.setFastScrollEnabled(mPreferences.getBoolean(PREFERENCE_KEY_FAST_SCROLL_THUMB, false));
		final float text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean show_absolute_time = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ABSOLUTE_TIME, false);
		final String name_display_option = mPreferences.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION,
				NAME_DISPLAY_OPTION_BOTH);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setTextSize(text_size);
		mAdapter.setShowAbsoluteTime(show_absolute_time);
		mAdapter.setNameDisplayOption(name_display_option);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
			case SCROLL_STATE_TOUCH_SCROLL: {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (twitter != null) {
					twitter.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
				}
				mBusy = true;
				break;
			}
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
		filter.addAction(BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED);
		filter.addAction(BROADCAST_SENT_DIRECT_MESSAGES_REFRESHED);
		filter.addAction(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		setRefreshing(twitter != null
				&& (twitter.isReceivedDirectMessagesRefreshing() || twitter.isSentDirectMessagesRefreshing()));
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		mTickerStopped = true;
		super.onStop();
	}

	@Override
	protected void onPullUp() {
		if (mLoadMoreAutomatically) return;
		loadMoreMessages();
	}

	@Override
	protected void onReachedBottom() {
		if (!mLoadMoreAutomatically) return;
		loadMoreMessages();
	}

	private void loadMoreMessages() {
		if (isRefreshing()) return;
		new AsyncTask<Void, Void, long[][]>() {

			@Override
			protected long[][] doInBackground(final Void... params) {
				final long[][] result = new long[3][];
				result[0] = getActivatedAccountIds(getActivity());
				result[1] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Inbox.CONTENT_URI);
				result[2] = getOldestMessageIdsFromDatabase(getActivity(), DirectMessages.Outbox.CONTENT_URI);
				return result;
			}

			@Override
			protected void onPostExecute(final long[][] result) {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (twitter == null) return;
				twitter.getReceivedDirectMessages(result[0], result[1], null);
				twitter.getSentDirectMessages(result[0], result[2], null);
			}

		}.execute();
	}

}
