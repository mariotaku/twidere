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

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ArrayUtils;

public class AccountSelectorActivity extends BaseSupportDialogActivity implements LoaderCallbacks<Cursor>,
		OnClickListener {

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				if (!isFinishing()) {
					getLoaderManager().restartLoader(0, null, AccountSelectorActivity.this);
				}
			}
		}
	};

	private SharedPreferences mPreferences;

	private ListView mListView;
	private AccountsAdapter mAdapter;

	private boolean mFirstCreated;

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.save: {
				final long[] checked_ids = mListView.getCheckedItemIds();
				if (checked_ids == null || checked_ids.length == 0 && !isSelectNoneAllowed()) {
					Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show();
					return;
				}
				final Bundle extras = new Bundle();
				extras.putLongArray(EXTRA_IDS, checked_ids);
				setResult(RESULT_OK, new Intent().putExtras(extras));
				finish();
				break;
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final String where = isOAuthOnly() ? Accounts.AUTH_TYPE + " = " + Accounts.AUTH_TYPE_OAUTH : null;
		return new CursorLoader(this, Accounts.CONTENT_URI, Accounts.COLUMNS, where, null, null);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.swapCursor(cursor);
		if (cursor != null && mFirstCreated) {
			final Bundle extras = getIntent().getExtras();
			final long[] activated_ids = extras != null ? extras.getLongArray(EXTRA_IDS) : null;
			for (int i = 0, j = mAdapter.getCount(); i < j; i++) {
				mListView.setItemChecked(i, ArrayUtils.contains(activated_ids, mAdapter.getItemId(i)));
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFirstCreated = savedInstanceState == null;
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		setContentView(R.layout.select_account);
		mListView = (ListView) findViewById(android.R.id.list);
		mAdapter = new AccountsAdapter(this);
		mAdapter.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
	}

	@Override
	protected void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		registerReceiver(mStateReceiver, filter);

	}

	@Override
	protected void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	private boolean isOAuthOnly() {
		final Bundle extras = getIntent().getExtras();
		return extras != null && extras.getBoolean(EXTRA_OAUTH_ONLY, false);
	}

	private boolean isSelectNoneAllowed() {
		final Bundle extras = getIntent().getExtras();
		return extras != null && extras.getBoolean(EXTRA_ALLOW_SELECT_NONE, false);
	}

}
