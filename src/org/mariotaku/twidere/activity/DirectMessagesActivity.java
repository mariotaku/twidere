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

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.DirectMessagesEntryAdapter;
import org.mariotaku.twidere.fragment.DirectMessagesConversationFragment;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.Utils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class DirectMessagesActivity extends BaseActivity implements LoaderCallbacks<Cursor>, OnScrollListener,
		OnItemClickListener {
	private ServiceInterface mService;

	private SharedPreferences mPreferences;

	private Handler mHandler;
	private Runnable mTicker;
	private ListView mListView;

	private volatile boolean mBusy, mTickerStopped, mReachedBottom, mNotReachedBottomBefore = true;

	private DirectMessagesEntryAdapter mAdapter;

	private static final long TICKER_DURATION = 5000L;

	private final Bundle mArguments = new Bundle();
	private long mAccountId;

	private DirectMessagesConversationFragment mDetailsFragment;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)
					|| BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)) {
				getSupportLoaderManager().restartLoader(0, mArguments, DirectMessagesActivity.this);
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				setProgressBarIndeterminateVisibility(mService.isReceivedDirectMessagesRefreshing()
						|| mService.isSentDirectMessagesRefreshing());
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getTwidereApplication().getServiceInterface();
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		mAccountId = extras != null ? extras.getLong(INTENT_KEY_ACCOUNT_ID, -1) : intent.getLongExtra(
				INTENT_KEY_ACCOUNT_ID, -1);
		mArguments.clear();
		if (isMyActivatedAccount(this, mAccountId)) {
			mArguments.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		} else {
			finish();
			return;
		}
		if (extras != null && extras.getBoolean(INTENT_KEY_FROM_NOTIFICATION)) {
			mService.clearNewNotificationCount(NOTIFICATION_ID_DIRECT_MESSAGES);
		}
		setContentView(R.layout.direct_messages);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayShowTitleEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);

		final LazyImageLoader imageloader = getTwidereApplication().getProfileImageLoader();
		mAdapter = new DirectMessagesEntryAdapter(this, imageloader);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		getSupportLoaderManager().initLoader(0, mArguments, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (args == null || !args.containsKey(INTENT_KEY_ACCOUNT_ID)) return null;
		final long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
		final Uri uri = Utils.buildDirectMessageConversationsEntryUri(account_id);
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_direct_messages, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final long conversation_id = mAdapter.findConversationId(id);
		if (conversation_id > 0) {
			openDirectMessagesConversation(mAccountId, conversation_id);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_REFRESH: {
				if (mService == null) return false;
				mService.getReceivedDirectMessages(mAccountId, -1);
				mService.getSentDirectMessages(mAccountId, -1);
				break;
			}
			case MENU_COMPOSE: {
				openDirectMessagesConversation(mAccountId, -1);
				break;
			}
			case MENU_LOAD_MORE: {
				if (mService == null) return false;
				final ContentResolver resolver = getContentResolver();
				final String where = DirectMessages.ACCOUNT_ID + " = " + mAccountId;
				final String[] cols = new String[] { "MIN(" + DirectMessages.MESSAGE_ID + ")" };
				final Cursor inbox_cur = resolver.query(DirectMessages.Inbox.CONTENT_URI, cols, where, null, null);
				final Cursor outbox_cur = resolver.query(DirectMessages.Outbox.CONTENT_URI, cols, where, null, null);
				inbox_cur.moveToFirst();
				final long inbox_min_id = inbox_cur.getLong(0);
				if (inbox_min_id > 0) {
					mService.getReceivedDirectMessages(mAccountId, inbox_min_id);
				}
				outbox_cur.moveToFirst();
				final long outbox_min_id = outbox_cur.getLong(0);
				if (outbox_min_id > 0) {
					mService.getSentDirectMessages(mAccountId, outbox_min_id);
				}
				inbox_cur.close();
				outbox_cur.close();
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setTextSize(text_size);
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
		setSupportProgressBarIndeterminateVisibility(mService.isReceivedDirectMessagesRefreshing()
				|| mService.isSentDirectMessagesRefreshing());
		final IntentFilter filter = new IntentFilter(BROADCAST_REFRESHSTATE_CHANGED);
		filter.addAction(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED);
		filter.addAction(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		mTickerStopped = true;
		super.onStop();
	}

	private boolean isDualPaneMode() {
		return findViewById(PANE_LEFT) instanceof ViewGroup && findViewById(PANE_RIGHT) instanceof ViewGroup;
	}

	private void openDirectMessagesConversation(long account_id, long conversation_id) {
		if (isDualPaneMode()) {
			if (mDetailsFragment == null) {
				mDetailsFragment = new DirectMessagesConversationFragment();
			}

			if (mDetailsFragment.isAdded()) {
				mDetailsFragment.showConversation(account_id, conversation_id);
			} else {
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				args.putLong(INTENT_KEY_CONVERSATION_ID, conversation_id);
				mDetailsFragment.setArguments(args);
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(PANE_RIGHT, mDetailsFragment);
				ft.addToBackStack(null);
				ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, String.valueOf(conversation_id));
			startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}
}
