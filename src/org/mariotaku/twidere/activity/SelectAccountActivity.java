package org.mariotaku.twidere.activity;

import java.util.ArrayList;
import java.util.List;

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

	public Cursor getAccountsCursor() {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = new String[] { Accounts._ID, Accounts.USER_ID, Accounts.USERNAME,
				Accounts.IS_ACTIVATED };
		return getContentResolver().query(uri, cols, null, null, null);
	}

	@Override
	public void onBackPressed() {
		if (mActivatedUsersId.size() <= 0) {
			Toast.makeText(this, R.string.no_account_selected, Toast.LENGTH_SHORT).show();
			return;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basic_list);
		mListView = (ListView) findViewById(android.R.id.list);
		int layoutRes = MULTIPLE_ACCOUNTS_ENABLED ? android.R.layout.simple_list_item_multiple_choice
				: android.R.layout.simple_list_item_single_choice;
		String[] from = new String[] { Accounts.USERNAME };
		int[] to = new int[] { android.R.id.text1 };
		mCursor = getAccountsCursor();
		if (mCursor == null) {
			finish();
			return;
		}
		mAdapter = new SimpleCursorAdapter(this, layoutRes, mCursor, from, to, 0);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setChoiceMode(MULTIPLE_ACCOUNTS_ENABLED ? ListView.CHOICE_MODE_MULTIPLE
				: ListView.CHOICE_MODE_SINGLE);

		Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		long[] activated_ids = bundle != null ? bundle.getLongArray(Accounts.USER_IDS) : null;
		mActivatedUsersId.clear();
		if (activated_ids == null) {
			mCursor.moveToFirst();
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
		} else {
			for (long id : activated_ids) {
				mCursor.moveToFirst();
				while (!mCursor.isAfterLast()) {
					long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
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
		int choise_mode = mListView.getChoiceMode();
		if (choise_mode == ListView.CHOICE_MODE_NONE) return;

		if (choise_mode == ListView.CHOICE_MODE_SINGLE) {
			mActivatedUsersId.clear();
		}
		SparseBooleanArray checkedpositions = mListView.getCheckedItemPositions();
		boolean checked = checkedpositions.get(position, false);
		mCursor.moveToPosition(position);
		long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
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
		long[] ids = new long[mActivatedUsersId.size()];
		for (int i = 0; i < mActivatedUsersId.size(); i++) {
			ids[i] = mActivatedUsersId.get(i);
		}
		outState.putLongArray(Accounts.USER_IDS, ids);
		super.onSaveInstanceState(outState);
	}

}
