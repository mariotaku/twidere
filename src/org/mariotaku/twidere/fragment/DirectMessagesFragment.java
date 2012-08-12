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

import static org.mariotaku.twidere.util.Utils.buildDirectMessageConversationsEntryUri;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.DirectMessagesEntryAdapter;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;

public class DirectMessagesFragment extends PullToRefreshListFragment implements LoaderCallbacks<Cursor>,
		OnScrollListener, OnItemClickListener, OnClickListener, OnItemSelectedListener {
	private ServiceInterface mService;

	private SharedPreferences mPreferences;
	private Button mAccountConfirmButton;
	private Spinner mAccountSelector;
	private View mAccountSelectContainer, mListContainer;
	private Handler mHandler;
	private Runnable mTicker;
	private ListView mListView;

	private volatile boolean mBusy, mTickerStopped, mReachedBottom, mNotReachedBottomBefore = true;

	private DirectMessagesEntryAdapter mAdapter;

	private static final long TICKER_DURATION = 5000L;

	private final Bundle mArguments = new Bundle();
	private long mAccountId;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)
					|| BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DirectMessagesFragment.this);
				onRefreshComplete();
			}
		}
	};

	private AccountsAdapter mAccountsAdapter;
	private Account mSelectedAccount;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getApplication().getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		final Bundle args = savedInstanceState == null ? getArguments() : savedInstanceState;
		mAccountId = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		mArguments.clear();
		if (args != null) {
			mArguments.putAll(args);
		}
		mService.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);

		final LazyImageLoader imageloader = getApplication().getProfileImageLoader();
		mAdapter = new DirectMessagesEntryAdapter(getActivity(), imageloader);

		mAccountConfirmButton.setOnClickListener(this);
		setListAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);

		final long[] activated_ids = getActivatedAccountIds(getActivity());

		if (!isMyActivatedAccount(getActivity(), mAccountId) && activated_ids.length == 1) {
			mAccountId = activated_ids[0];
		}

		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), mAccountId);

		if (is_my_activated_account) {
			mArguments.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		}
		mListContainer.setVisibility(is_my_activated_account ? View.VISIBLE : View.GONE);
		mAccountSelectContainer.setVisibility(!is_my_activated_account ? View.VISIBLE : View.GONE);
		if (!is_my_activated_account) {
			mAccountsAdapter = new AccountsAdapter(getActivity());
			mAccountSelector.setAdapter(mAccountsAdapter);
			mAccountSelector.setOnItemSelectedListener(this);
		}

		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.account_confirm: {
				if (mSelectedAccount == null) return;
				mAccountId = mSelectedAccount.account_id;
				final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), mAccountId);
				if (is_my_activated_account) {
					mArguments.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				}
				mListContainer.setVisibility(is_my_activated_account ? View.VISIBLE : View.GONE);
				mAccountSelectContainer.setVisibility(!is_my_activated_account ? View.VISIBLE : View.GONE);
				getLoaderManager().restartLoader(0, null, this);
				break;
			}
		}

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri uri = buildDirectMessageConversationsEntryUri(mAccountId);
		return new CursorLoader(getActivity(), uri, null, null, null, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mAccountSelectContainer = inflater.inflate(R.layout.direct_messages_account_selector, null);
		mListContainer = super.onCreateView(inflater, container, savedInstanceState);
		final FrameLayout view = new FrameLayout(getActivity());
		mAccountConfirmButton = (Button) mAccountSelectContainer.findViewById(R.id.account_confirm);
		mAccountSelector = (Spinner) mAccountSelectContainer.findViewById(R.id.account_selector);
		mListView = (ListView) mListContainer.findViewById(android.R.id.list);
		view.addView(mListContainer);
		view.addView(mAccountSelectContainer);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final long conversation_id = mAdapter.findConversationId(id);
		if (conversation_id > 0) {
			openDirectMessagesConversation(getActivity(), mAccountId, conversation_id);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		mSelectedAccount = null;
		if (mAccountsAdapter == null) return;
		mSelectedAccount = mAccountsAdapter.getItem(pos);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		setListShown(true);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	@Override
	public void onRefresh() {
		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), mAccountId);
		if (mService == null || !is_my_activated_account) return;
		mService.getReceivedDirectMessages(new long[] { mAccountId }, null);
		mService.getSentDirectMessages(new long[] { mAccountId }, null);
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

	public void openDMConversation() {
		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), mAccountId);
		if (!is_my_activated_account) return;
		openDirectMessagesConversation(getActivity(), mAccountId, -1);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean hires_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_HIRES_PROFILE_IMAGE, false);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final boolean force_ssl_connection = mPreferences.getBoolean(PREFERENCE_KEY_FORCE_SSL_CONNECTION, false);
		mAdapter.setForceSSLConnection(force_ssl_connection);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayHiResProfileImage(hires_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setTextSize(text_size);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		outState.putBundle(INTENT_KEY_DATA, mArguments);
		super.onSaveInstanceState(outState);
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
		filter.addAction(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED);
		filter.addAction(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		if (mService.isReceivedDirectMessagesRefreshing() || mService.isSentDirectMessagesRefreshing()) {
			setRefreshing(false);
		} else {
			onRefreshComplete();
		}
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		mTickerStopped = true;
		super.onStop();
	}

	private static class AccountsAdapter extends ArrayAdapter<Account> {

		public AccountsAdapter(Context context) {
			super(context, R.layout.spinner_item, Account.getAccounts(context, true));
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		}

	}
}
