package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.R;
import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.ListView;


public class SelectAccountsActivity extends ListActivity implements Constants, OnItemClickListener {
	
	private SimpleCursorAdapter mAdapter;
	private boolean isMultipleAccountsEnabled = false;
	private Cursor mCursor;
	private ListView mListView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int layoutRes = isMultipleAccountsEnabled ? R.layout.select_dialog_multichoice : R.layout.select_dialog_singlechoice;
		String[] cols = new String[]{Accounts.USERNAME};
		int[] ids = new int[]{R.id.text1};
		mCursor = getAccountsCursor();
		mAdapter = new SimpleCursorAdapter(this, layoutRes, mCursor, cols, ids, 0);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setChoiceMode(isMultipleAccountsEnabled ? ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(this);
	}
	
	@Override
	public void onDestroy() {
		if (mCursor != null && !mCursor.isClosed()) mCursor.close();
		super.onDestroy();
	}

	public Cursor getAccountsCursor() {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = new String[]{Accounts._ID, Accounts.USER_ID, Accounts.USERNAME};
		return getContentResolver().query(uri, cols, null, null, null);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (view instanceof CheckedTextView) {
			boolean checked = ((CheckedTextView)view).isChecked();
			mCursor.moveToPosition(position);
			long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
			Log.d("Debug", "user_id " + user_id + " checked = " + checked);
		}
		
	}

}
