package org.mariotaku.twidere.activity;

import java.util.ArrayList;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ViewStatusFragment;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import roboguice.inject.InjectExtra;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ViewStatusActivity extends BaseActivity {

	private ActionBar mActionBar;

	@InjectExtra(Statuses.ACCOUNT_ID) private long mAccountId;
	@InjectExtra(Statuses.STATUS_ID) private long mStatusId;
	@InjectExtra(TweetStore.KEY_TYPE) private int mType;
	private ContentResolver mResolver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResolver = getContentResolver();
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ViewStatusFragment fragment = new ViewStatusFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Statuses.ACCOUNT_ID, mAccountId);
		bundle.putLong(Statuses.STATUS_ID, mStatusId);
		fragment.setArguments(bundle);
		ft.replace(android.R.id.content, fragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.view_status, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		String[] accounts_cols = new String[] { Accounts.USER_ID };
		Cursor accounts_cur = mResolver
				.query(Accounts.CONTENT_URI, accounts_cols, null, null, null);
		ArrayList<Long> ids = new ArrayList<Long>();
		if (accounts_cur != null) {
			accounts_cur.moveToFirst();
			int idx = accounts_cur.getColumnIndexOrThrow(Accounts.USER_ID);
			while (!accounts_cur.isAfterLast()) {
				ids.add(accounts_cur.getLong(idx));
				accounts_cur.moveToNext();
			}
			accounts_cur.close();
		}
		Uri uri;
		String[] cols;
		String where;
		switch (mType) {
			case TweetStore.VALUE_TYPE_MENTION:
				uri = Mentions.CONTENT_URI;
				cols = Mentions.COLUMNS;
				where = Mentions.STATUS_ID + "=" + mStatusId;
				break;
			case TweetStore.VALUE_TYPE_STATUS:
			default:
				uri = Statuses.CONTENT_URI;
				cols = Statuses.COLUMNS;
				where = Statuses.STATUS_ID + "=" + mStatusId;
				break;
		}
		
		Cursor cur = mResolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			int idx = cur.getColumnIndexOrThrow(Statuses.USER_ID);
			long user_id = cur.getLong(idx);
			menu.findItem(MENU_DELETE).setVisible(ids.contains(user_id));
		}
		return super.onPrepareOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
