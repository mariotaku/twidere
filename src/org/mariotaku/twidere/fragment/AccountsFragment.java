package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.GlobalSettingsActivity;
import org.mariotaku.twidere.activity.LoginActivity;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockListFragment;

public class AccountsFragment extends SherlockListFragment implements Constants,
		LoaderCallbacks<Cursor>, OnClickListener {

	private AccountsAdapter mAdapter;
	private Button mAddAccountButton, mGlobalSettingsButton;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new AccountsAdapter(getSherlockActivity());
		getLoaderManager().initLoader(0, null, this);
		View view = getLayoutInflater(null).inflate(R.layout.accounts_list_header, null, false);
		mAddAccountButton = (Button) view.findViewById(R.id.add_account);
		mAddAccountButton.setOnClickListener(this);
		mGlobalSettingsButton = (Button) view.findViewById(R.id.global_settings);
		mGlobalSettingsButton.setOnClickListener(this);
		getListView().addHeaderView(view);
		setListAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.add_account:
				startActivity(new Intent(getSherlockActivity(), LoginActivity.class));
				break;
			case R.id.global_settings:
				startActivity(new Intent(getSherlockActivity(), GlobalSettingsActivity.class));
				break;
		}

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = new String[] { Accounts._ID, Accounts.USERNAME, Accounts.USER_ID,
				Accounts.IS_ACTIVATED };
		StringBuilder where = new StringBuilder();
		where.append(Accounts.IS_ACTIVATED + "='1'");
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
	}

	private class AccountsAdapter extends SimpleCursorAdapter {

		public AccountsAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1, null,
					new String[] { Accounts.USERNAME }, new int[] { android.R.id.text1 }, 0);
		}
	}

}
