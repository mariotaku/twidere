package org.mariotaku.twidere.activity;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class SelectAccountActivity extends ListActivity implements Constants {

	private SimpleCursorAdapter mAdapter;
	private Cursor mCursor;
	private ListView mListView;
	private List<Long> mActivatedUsersId = new ArrayList<Long>();
	private int mThemeId;

	public Cursor getAccountsCursor() {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = new String[] { Accounts._ID, Accounts.USER_ID, Accounts.USERNAME,
				Accounts.IS_ACTIVATED };
		return getContentResolver().query(uri, cols, null, null, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme();
		super.onCreate(savedInstanceState);
		int layoutRes = MULTIPLE_ACCOUNTS_ENABLED ? android.R.layout.simple_list_item_multiple_choice
				: android.R.layout.simple_list_item_single_choice;
		String[] cols = new String[] { Accounts.USERNAME };
		int[] ids = new int[] { android.R.id.text1 };
		mCursor = getAccountsCursor();
		if (mCursor == null) {
			finish();
			return;
		}
		mAdapter = new SimpleCursorAdapter(this, layoutRes, mCursor, cols, ids, 0);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setChoiceMode(MULTIPLE_ACCOUNTS_ENABLED ? ListView.CHOICE_MODE_MULTIPLE
				: ListView.CHOICE_MODE_SINGLE);

		mCursor.moveToFirst();
		mActivatedUsersId.clear();
		while (!mCursor.isAfterLast()) {
			boolean is_activated = mCursor.getInt(mCursor
					.getColumnIndexOrThrow(Accounts.IS_ACTIVATED)) == 1;
			long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
			if (is_activated) {
				mActivatedUsersId.add(user_id);
			}
			mListView.setItemChecked(mCursor.getPosition(), is_activated);
			mCursor.moveToNext();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (mActivatedUsersId.size() <= 0) {
					Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show();
					return false;
				} else {
					Bundle bundle = new Bundle();
					long[] ids = new long[mActivatedUsersId.size()];
					int i = 0;
					for (Long id_long : mActivatedUsersId) {
						ids[i] = id_long;
						i++;
					}
					bundle.putLongArray(Accounts.USER_IDS, ids);
					setResult(RESULT_OK, new Intent().putExtras(bundle));
					finish();
				}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		int choise_mode = mListView.getChoiceMode();
		if (choise_mode == ListView.CHOICE_MODE_NONE) return;

		if (choise_mode == ListView.CHOICE_MODE_SINGLE) {
			// Clear all items in users id list.
			mActivatedUsersId.clear();

			// Set all accounts in databases deactivated.
			Uri uri = Accounts.CONTENT_URI;
			ContentValues values = new ContentValues();
			values.put(Accounts.IS_ACTIVATED, 0);
			getContentResolver().update(uri, values, null, null);
		}
		SparseBooleanArray checkedpositions = mListView.getCheckedItemPositions();
		boolean checked = checkedpositions.get(position, false);
		mCursor.moveToPosition(position);
		long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
		if (!checked) {
			if (mActivatedUsersId.contains(user_id)) {
				mActivatedUsersId.remove(user_id);
			}
		} else {
			if (!mActivatedUsersId.contains(user_id)) {
				mActivatedUsersId.add(user_id);
			}
		}

		Uri uri = Accounts.CONTENT_URI;
		ContentValues values = new ContentValues();
		values.put(Accounts.IS_ACTIVATED, checked ? 1 : 0);
		String where = Accounts._ID + "=" + id;
		getContentResolver().update(uri, values, where, null);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isThemeChanged()) {
			restartActivity();
		}
	}

	private boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere_Dialog
				: R.style.Theme_Twidere_Light_Dialog;
		return new_theme_id != mThemeId;
	}

	private void restartActivity() {
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);
		startActivity(getIntent());
	}

	private void setTheme() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere_Dialog
				: R.style.Theme_Twidere_Light_Dialog;
		setTheme(mThemeId);

	}

}
