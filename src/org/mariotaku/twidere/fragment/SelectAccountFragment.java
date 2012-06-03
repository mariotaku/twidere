package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.ListView;

public class SelectAccountFragment extends BaseListFragment implements Constants, LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mAdapter;
	private ListView mListView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		int layoutRes = MULTIPLE_ACCOUNTS_ENABLED ? android.R.layout.simple_list_item_multiple_choice
				: android.R.layout.simple_list_item_single_choice;
		String[] cols = new String[] { Accounts.USERNAME };
		int[] ids = new int[] { android.R.id.text1 };
		mAdapter = new SimpleCursorAdapter(getActivity(), layoutRes, null, cols, ids, 0);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView
				.setChoiceMode(MULTIPLE_ACCOUNTS_ENABLED ? ListView.CHOICE_MODE_MULTIPLE : ListView.CHOICE_MODE_SINGLE);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = new String[] { Accounts._ID, Accounts.USER_ID, Accounts.USERNAME, Accounts.IS_ACTIVATED };
		return new CursorLoader(getActivity(), uri, cols, null, null, null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);

	}

}