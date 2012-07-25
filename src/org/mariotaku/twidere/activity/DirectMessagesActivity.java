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

import static org.mariotaku.twidere.util.Utils.getActivatedAccountScreenNames;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

public class DirectMessagesActivity extends BaseActivity implements LoaderCallbacks<Cursor>, OnScrollListener,
		OnItemClickListener, OnClickListener, OnItemSelectedListener {
	private ServiceInterface mService;

	private SharedPreferences mPreferences;
	private Button mAccountConfirmButton;
	private Spinner mAccountSelector;
	private View mAccountSelectContainer, mDirectMessagesContainer;
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
				getSupportLoaderManager().restartLoader(0, null, DirectMessagesActivity.this);
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				setProgressBarIndeterminateVisibility(mService.isReceivedDirectMessagesRefreshing()
						|| mService.isSentDirectMessagesRefreshing());
			}
		}
	};

	private ArrayAdapter<String> mAccountsAdapter;

	private String mSelectedScreenName;

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.account_confirm: {
				mAccountId = Utils.getAccountId(this, mSelectedScreenName);
				final boolean is_my_activated_account = isMyActivatedAccount(this, mAccountId);
				if (is_my_activated_account) {
					mArguments.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				}
				mDirectMessagesContainer.setVisibility(is_my_activated_account ? View.VISIBLE : View.GONE);
				mAccountSelectContainer.setVisibility(!is_my_activated_account ? View.VISIBLE : View.GONE);
				getSupportLoaderManager().restartLoader(0, null, this);
				break;
			}
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getTwidereApplication().getServiceInterface();
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Bundle args = savedInstanceState == null ? intent.getExtras() : savedInstanceState;
		mAccountId = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : intent.getLongExtra(
				INTENT_KEY_ACCOUNT_ID, -1);
		mArguments.clear();
		if (args != null) {
			mArguments.putAll(args);
		}
		mService.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
		setContentView(R.layout.direct_messages);
		final ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayShowTitleEnabled(true);
		actionbar.setDisplayHomeAsUpEnabled(true);

		final LazyImageLoader imageloader = getTwidereApplication().getProfileImageLoader();
		mAdapter = new DirectMessagesEntryAdapter(this, imageloader);
		mDirectMessagesContainer = findViewById(R.id.direct_messages_content);
		mAccountSelectContainer = findViewById(R.id.account_select_container);
		mListView = (ListView) findViewById(android.R.id.list);
		mAccountConfirmButton = (Button) findViewById(R.id.account_confirm);
		mAccountConfirmButton.setOnClickListener(this);
		mAccountSelector = (Spinner) findViewById(R.id.account_selector);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);

		final boolean is_my_activated_account = isMyActivatedAccount(this, mAccountId);

		if (is_my_activated_account) {
			mArguments.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		}
		mDirectMessagesContainer.setVisibility(is_my_activated_account ? View.VISIBLE : View.GONE);
		mAccountSelectContainer.setVisibility(!is_my_activated_account ? View.VISIBLE : View.GONE);
		if (!is_my_activated_account) {
			final String[] account_screen_names = getActivatedAccountScreenNames(this);
			mAccountsAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, account_screen_names);
			mAccountsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			mAccountSelector.setAdapter(mAccountsAdapter);
			mAccountSelector.setOnItemSelectedListener(this);
		}

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri uri = Utils.buildDirectMessageConversationsEntryUri(mAccountId);
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
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		mSelectedScreenName = mAccountsAdapter.getItem(pos);
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
	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final boolean is_my_activated_account = isMyActivatedAccount(this, mAccountId);
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_REFRESH: {
				if (mService == null || !is_my_activated_account) return false;
				mService.getReceivedDirectMessages(new long[] { mAccountId }, null);
				mService.getSentDirectMessages(new long[] { mAccountId }, null);
				break;
			}
			case MENU_COMPOSE: {
				if (!is_my_activated_account) return false;
				openDirectMessagesConversation(mAccountId, -1);
				break;
			}
			case MENU_LOAD_MORE: {
				if (mService == null || !is_my_activated_account) return false;
				final ContentResolver resolver = getContentResolver();
				final String where = DirectMessages.ACCOUNT_ID + " = " + mAccountId;
				final String[] cols = new String[] { "MIN(" + DirectMessages.MESSAGE_ID + ")" };
				final Cursor inbox_cur = resolver.query(DirectMessages.Inbox.CONTENT_URI, cols, where, null, null);
				final Cursor outbox_cur = resolver.query(DirectMessages.Outbox.CONTENT_URI, cols, where, null, null);
				inbox_cur.moveToFirst();
				final long inbox_min_id = inbox_cur.getLong(0);
				if (inbox_min_id > 0) {
					mService.getReceivedDirectMessages(new long[] { mAccountId }, new long[] { inbox_min_id });
				}
				outbox_cur.moveToFirst();
				final long outbox_min_id = outbox_cur.getLong(0);
				if (outbox_min_id > 0) {
					mService.getSentDirectMessages(new long[] { mAccountId }, new long[] { outbox_min_id });
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
		final boolean hires_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_HIRES_PROFILE_IMAGE, false);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayHiResProfileImage(hires_profile_image);
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		outState.putBundle(INTENT_KEY_DATA, mArguments);
		super.onSaveInstanceState(outState);
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
