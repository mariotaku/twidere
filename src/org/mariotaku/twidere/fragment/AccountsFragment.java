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
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.EditUserProfileActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.activity.SetColorActivity;
import org.mariotaku.twidere.activity.SignInActivity;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ArrayUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class AccountsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemLongClickListener,
		OnMenuItemClickListener, Panes.Left {

	private SharedPreferences mPreferences;
	private TwidereApplication mApplication;
	private ContentResolver mResolver;

	private AccountsAdapter mAdapter;

	private ListView mListView;
	private PopupMenu mPopupMenu;

	private int mSelectedColor;
	private long mSelectedAccountId;
	private Cursor mCursor;

	private boolean mActivityFirstCreated;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, AccountsFragment.this);
				if (getActivity() instanceof HomeActivity) {
					((HomeActivity) getActivity()).setDefaultAccount();
				}
			}
		}
	};

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mApplication = getApplication();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = getContentResolver();
		mAdapter = new AccountsAdapter(getActivity(), false);
		getLoaderManager().initLoader(0, null, this);
		mListView = getListView();
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SET_COLOR: {
				if (resultCode == Activity.RESULT_OK) if (data != null && data.getExtras() != null) {
					final int color = data.getIntExtra(Accounts.USER_COLOR, Color.WHITE);
					final ContentValues values = new ContentValues();
					values.put(Accounts.USER_COLOR, color);
					final String where = Accounts.ACCOUNT_ID + " = " + mSelectedAccountId;
					mResolver.update(Accounts.CONTENT_URI, values, where, null);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		mActivityFirstCreated = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final Uri uri = Accounts.CONTENT_URI;
		final String[] cols = Accounts.COLUMNS;
		final String where = Accounts.IS_ACTIVATED + " = 1";
		return new CursorLoader(getActivity(), uri, cols, where, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.accounts, null);
	}

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return true;
		if (isDefaultAccountValid() && mCursor != null && position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			mSelectedColor = mCursor.getInt(mCursor.getColumnIndexOrThrow(Accounts.USER_COLOR));
			mSelectedAccountId = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.ACCOUNT_ID));
			mPopupMenu = PopupMenu.getInstance(getActivity(), view);
			mPopupMenu.inflate(R.menu.action_account);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();
			return true;
		}
		return false;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		if (mApplication.isMultiSelectActive()) return;
		if (mCursor != null && position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			final long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.ACCOUNT_ID));
			if (isDefaultAccountValid()) {
				openUserProfile(getActivity(), user_id, user_id, null);
			} else {
				setDefaultAccount(user_id);
			}
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mCursor = null;
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mCursor = data;
		mAdapter.swapCursor(data);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedAccountId <= 0) return false;
		switch (item.getItemId()) {
			case MENU_VIEW_PROFILE: {
				openUserProfile(getActivity(), mSelectedAccountId, mSelectedAccountId, null);
				break;
			}
			case MENU_EDIT: {
				final Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, mSelectedAccountId);
				final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
				intent.setClass(getActivity(), EditUserProfileActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_SET_COLOR: {
				final Intent intent = new Intent(getActivity(), SetColorActivity.class);
				final Bundle bundle = new Bundle();
				bundle.putInt(Accounts.USER_COLOR, mSelectedColor);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case MENU_SET_AS_DEFAULT: {
				setDefaultAccount(mSelectedAccountId);
				break;
			}
			case MENU_DELETE: {
				mResolver.delete(Accounts.CONTENT_URI, Accounts.ACCOUNT_ID + " = " + mSelectedAccountId, null);
				// Also delete tweets related to the account we previously
				// deleted.
				mResolver.delete(Statuses.CONTENT_URI, Statuses.ACCOUNT_ID + " = " + mSelectedAccountId, null);
				mResolver.delete(Mentions.CONTENT_URI, Mentions.ACCOUNT_ID + " = " + mSelectedAccountId, null);
				mResolver.delete(DirectMessages.Inbox.CONTENT_URI, DirectMessages.ACCOUNT_ID + " = "
						+ mSelectedAccountId, null);
				mResolver.delete(DirectMessages.Outbox.CONTENT_URI, DirectMessages.ACCOUNT_ID + " = "
						+ mSelectedAccountId, null);
				if (getActivatedAccountIds(getActivity()).length > 0) {
					getLoaderManager().restartLoader(0, null, AccountsFragment.this);
				} else {
					final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
					intent.setClass(getActivity(), SignInActivity.class);
					startActivity(intent);
					getActivity().finish();
				}
				break;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		if (!mActivityFirstCreated) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mActivityFirstCreated = false;
		super.onStop();
	}

	private boolean isDefaultAccountValid() {
		final long default_account_id = mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
		if (default_account_id == -1) return false;
		final long[] activated_ids = getActivatedAccountIds(getActivity());
		return ArrayUtils.contains(activated_ids, default_account_id);
	}

	private void setDefaultAccount(final long account_id) {
		mPreferences.edit().putLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, account_id).commit();
		mAdapter.notifyDataSetChanged();
		if (getActivity() instanceof HomeActivity) {
			((HomeActivity) getActivity()).setDefaultAccount();
		}
	}
}
