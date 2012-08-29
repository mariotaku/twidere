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

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class SelectAccountActivity extends BaseDialogActivity implements OnItemClickListener {

	private ListView mListView;
	private SimpleCursorAdapter mAdapter;
	private Cursor mCursor;
	private List<Long> mActivatedUsersId = new ArrayList<Long>();

	public Cursor getAccountsCursor(boolean activated_only) {
		final Uri uri = Accounts.CONTENT_URI;
		final String[] cols = new String[] { Accounts._ID, Accounts.USER_ID, Accounts.USERNAME, Accounts.IS_ACTIVATED };
		final String where = activated_only ? Accounts.IS_ACTIVATED + " = " + 1 : null;
		return getContentResolver().query(uri, cols, where, null, null);
	}

	@Override
	public void onBackPressed() {
		if (mActivatedUsersId.size() <= 0) {
			Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show();
			return;
		}
		final Bundle bundle = new Bundle();
		final long[] ids = new long[mActivatedUsersId.size()];
		int i = 0;
		for (final Long id_long : mActivatedUsersId) {
			ids[i] = id_long;
			i++;
		}
		bundle.putLongArray(INTENT_KEY_IDS, ids);
		setResult(RESULT_OK, new Intent().putExtras(bundle));
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		setContentView(R.layout.select_account);
		mListView = (ListView) findViewById(android.R.id.list);
		final String[] from = new String[] { Accounts.USERNAME };
		final int[] to = new int[] { android.R.id.text1 };
		mCursor = getAccountsCursor(bundle != null ? bundle.getBoolean(INTENT_KEY_ACTIVATED_ONLY, false) : false);
		if (mCursor == null) {
			finish();
			return;
		}
		mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, mCursor, from, to,
				0);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		final long[] activated_ids = bundle != null ? bundle.getLongArray(Constants.INTENT_KEY_IDS) : null;
		mActivatedUsersId.clear();
		if (activated_ids == null) {
			mCursor.moveToFirst();
			while (!mCursor.isAfterLast()) {
				final boolean is_activated = mCursor.getInt(mCursor.getColumnIndexOrThrow(Accounts.IS_ACTIVATED)) == 1;
				final long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
				if (is_activated) {
					mActivatedUsersId.add(user_id);
				}
				mListView.setItemChecked(mCursor.getPosition(), is_activated);
				mCursor.moveToNext();
			}
		} else {
			for (final long id : activated_ids) {
				mCursor.moveToFirst();
				while (!mCursor.isAfterLast()) {
					final long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
					if (id == user_id) {
						mListView.setItemChecked(mCursor.getPosition(), true);
						mActivatedUsersId.add(user_id);
					}
					mCursor.moveToNext();
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCursor == null || mCursor.isClosed()) return;
		final int choise_mode = mListView.getChoiceMode();
		if (choise_mode == ListView.CHOICE_MODE_NONE) return;

		final SparseBooleanArray checkedpositions = mListView.getCheckedItemPositions();
		final boolean checked = checkedpositions.get(position, false);
		mCursor.moveToPosition(position);
		final long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
		if (!checked) {
			if (mActivatedUsersId.contains(user_id)) {
				mActivatedUsersId.remove(user_id);
			}
		} else if (!mActivatedUsersId.contains(user_id)) {
			mActivatedUsersId.add(user_id);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		final int ids_size = mActivatedUsersId.size();
		final long[] ids = new long[ids_size];
		for (int i = 0; i < ids_size; i++) {
			ids[i] = mActivatedUsersId.get(i);
		}
		outState.putLongArray(Constants.INTENT_KEY_IDS, ids);
		super.onSaveInstanceState(outState);
	}

}
