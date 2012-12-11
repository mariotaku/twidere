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

import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsAdapter;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.content.SharedPreferences;

public class SelectAccountActivity extends BaseDialogActivity implements LoaderCallbacks<Cursor>, OnItemClickListener,
		OnClickListener {

	private ListView mListView;
	private AccountsAdapter mAdapter;
	private final List<Long> mSelectedIds = new NoDuplicatesArrayList<Long>();

	private SharedPreferences mPreferences;

	@Override
	public void onBackPressed() {
		if (mSelectedIds.size() <= 0) {
			Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show();
			return;
		}
		final Bundle bundle = new Bundle();
		final long[] ids = new long[mSelectedIds.size()];
		int i = 0;
		for (final Long id_long : mSelectedIds) {
			ids[i] = id_long;
			i++;
		}
		bundle.putLongArray(INTENT_KEY_IDS, ids);
		setResult(RESULT_OK, new Intent().putExtras(bundle));
		finish();
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.save: {
				onBackPressed();
				break;
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		final Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		setContentView(R.layout.select_account);
		mListView = (ListView) findViewById(android.R.id.list);
		mAdapter = new AccountsAdapter(this, true);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		final long[] activated_ids = bundle != null ? bundle.getLongArray(Constants.INTENT_KEY_IDS) : null;
		mSelectedIds.clear();
		if (activated_ids != null) {
			for (final long id : activated_ids) {
				mSelectedIds.add(id);
			}
		}
		getSupportLoaderManager().initLoader(0, null, this);

	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(this, Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, null);
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final boolean checked = mAdapter.isChecked(position);
		mAdapter.setItemChecked(position, !checked);
		final long user_id = mAdapter.getAccountIdAt(position);
		if (checked) {
			mSelectedIds.remove(user_id);
		} else {
			mSelectedIds.add(user_id);
		}
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.swapCursor(cursor);
		final SparseBooleanArray checked = new SparseBooleanArray();
		cursor.moveToFirst();
		if (mSelectedIds.size() == 0) {
			while (!cursor.isAfterLast()) {
				final boolean is_activated = cursor.getInt(cursor.getColumnIndexOrThrow(Accounts.IS_ACTIVATED)) == 1;
				final long user_id = cursor.getLong(cursor.getColumnIndexOrThrow(Accounts.ACCOUNT_ID));
				if (is_activated) {
					mSelectedIds.add(user_id);
				}
				mAdapter.setItemChecked(cursor.getPosition(), is_activated);
				cursor.moveToNext();
			}
		} else {
			while (!cursor.isAfterLast()) {
				final long user_id = cursor.getLong(cursor.getColumnIndexOrThrow(Accounts.ACCOUNT_ID));
				if (mSelectedIds.contains(user_id)) {
					checked.put(cursor.getPosition(), true);
					mAdapter.setItemChecked(cursor.getPosition(), true);
				}
				cursor.moveToNext();
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLongArray(Constants.INTENT_KEY_IDS, ArrayUtils.fromList(mSelectedIds));
		super.onSaveInstanceState(outState);
	}

}
