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
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.parseURL;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.LazyImageLoader;

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
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
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
	private long mSelectedUserId;
	private Cursor mCursor;

	private boolean mActivityFirstCreated;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				if (getActivity() == null) return;
				getLoaderManager().restartLoader(0, null, AccountsFragment.this);
				if (getActivity() instanceof HomeActivity) {
					((HomeActivity) getActivity()).checkDefaultAccountSet();
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
		mAdapter = new AccountsAdapter(getActivity());
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
					final String where = Accounts.USER_ID + " = " + mSelectedUserId;
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
			mSelectedUserId = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
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
			final long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
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
		if (mSelectedUserId <= 0) return false;
		switch (item.getItemId()) {
			case MENU_VIEW_PROFILE: {
				openUserProfile(getActivity(), mSelectedUserId, mSelectedUserId, null);
				break;
			}
			case MENU_SET_COLOR: {
				final Intent intent = new Intent(INTENT_ACTION_SET_COLOR);
				final Bundle bundle = new Bundle();
				bundle.putInt(Accounts.USER_COLOR, mSelectedColor);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case MENU_SET_AS_DEFAULT: {
				setDefaultAccount(mSelectedUserId);
				break;
			}
			case MENU_DELETE: {
				mResolver.delete(Accounts.CONTENT_URI, Accounts.USER_ID + " = " + mSelectedUserId, null);
				// Also delete tweets related to the account we previously
				// deleted.
				mResolver.delete(Statuses.CONTENT_URI, Statuses.ACCOUNT_ID + " = " + mSelectedUserId, null);
				mResolver.delete(Mentions.CONTENT_URI, Mentions.ACCOUNT_ID + " = " + mSelectedUserId, null);
				mResolver.delete(DirectMessages.Inbox.CONTENT_URI, DirectMessages.ACCOUNT_ID + " = " + mSelectedUserId,
						null);
				mResolver.delete(DirectMessages.Outbox.CONTENT_URI,
						DirectMessages.ACCOUNT_ID + " = " + mSelectedUserId, null);
				if (getActivatedAccountIds(getActivity()).length > 0) {
					getLoaderManager().restartLoader(0, null, AccountsFragment.this);
				} else {
					getActivity().finish();
				}
				break;
			}
		}
		return super.onContextItemSelected(item);
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
			((HomeActivity) getActivity()).checkDefaultAccountSet();
		}
	}

	public static class ViewHolder {

		public final ImageView profile_image;
		private final View content, default_indicator;

		public ViewHolder(final View view) {
			content = view;
			profile_image = (ImageView) view.findViewById(android.R.id.icon);
			default_indicator = view.findViewById(android.R.id.text2);
		}

		public void setAccountColor(final int color) {
			content.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			content.invalidate();
		}

		public void setIsDefault(final boolean is_default) {
			default_indicator.setVisibility(is_default ? View.VISIBLE : View.GONE);
		}
	}

	static class AccountsAdapter extends SimpleCursorAdapter {

		private final LazyImageLoader mProfileImageLoader;
		private final SharedPreferences mPreferences;
		private int mUserColorIdx, mProfileImageIdx, mUserIdIdx;
		private long mDefaultAccountId;
		private final boolean mDisplayHiResProfileImage;

		public AccountsAdapter(final Context context) {
			super(context, R.layout.account_list_item, null, new String[] { Accounts.USERNAME },
					new int[] { android.R.id.text1 }, 0);
			final TwidereApplication application = TwidereApplication.getInstance(context);
			mProfileImageLoader = application.getProfileImageLoader();
			mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
		}

		@Override
		public void bindView(final View view, final Context context, final Cursor cursor) {
			final int color = cursor.getInt(mUserColorIdx);
			final ViewHolder holder = (ViewHolder) view.getTag();
			holder.setAccountColor(color);
			holder.setIsDefault(mDefaultAccountId != -1 && mDefaultAccountId == cursor.getLong(mUserIdIdx));
			final String profile_image_url_string = cursor.getString(mProfileImageIdx);
			if (mDisplayHiResProfileImage) {
				mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(profile_image_url_string)),
						holder.profile_image);
			} else {
				mProfileImageLoader.displayImage(parseURL(profile_image_url_string), holder.profile_image);
			}
			super.bindView(view, context, cursor);
		}

		@Override
		public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {

			final View view = super.newView(context, cursor, parent);
			final ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		@Override
		public void notifyDataSetChanged() {
			mDefaultAccountId = mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
			super.notifyDataSetChanged();
		}

		@Override
		public Cursor swapCursor(final Cursor cursor) {
			if (cursor != null) {
				mUserColorIdx = cursor.getColumnIndex(Accounts.USER_COLOR);
				mProfileImageIdx = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
				mUserIdIdx = cursor.getColumnIndex(Accounts.USER_ID);
			}
			return super.swapCursor(cursor);
		}
	}
}
