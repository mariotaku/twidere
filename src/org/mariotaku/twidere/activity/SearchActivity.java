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

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.parseLong;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.provider.RecentSearchProvider;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SearchActivity extends MultiSelectActivity implements OnItemSelectedListener {

	private TwidereApplication mApplication;

	private ActionBar mActionBar;
	private ArrayAdapter<SpinnerSpec> mAdapter;

	private Uri mData;
	private final Bundle mArguments = new Bundle();

	private Spinner mSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mApplication = getTwidereApplication();
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		mArguments.clear();
		mData = intent.getData();
		final boolean is_search_user = mData != null ? QUERY_PARAM_VALUE_USERS.equals(mData
				.getQueryParameter(QUERY_PARAM_TYPE)) : false;
		final String query = Intent.ACTION_SEARCH.equals(intent.getAction()) ? intent
				.getStringExtra(SearchManager.QUERY) : mData != null ? mData.getQueryParameter(QUERY_PARAM_QUERY)
				: null;
		if (query == null) {
			finish();
			return;
		}
		if (savedInstanceState == null) {
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
					RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
			suggestions.saveRecentQuery(query, null);
		}
		mArguments.putString(INTENT_KEY_QUERY, query);
		final String param_account_id = mData != null ? mData.getQueryParameter(QUERY_PARAM_ACCOUNT_ID) : null;
		if (param_account_id != null) {
			mArguments.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
		} else {
			final String param_account_name = mData != null ? mData.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME) : null;
			if (param_account_name != null) {
				mArguments.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId(this, param_account_name));
			} else {
				final long account_id = getDefaultAccountId(this);
				if (isMyAccount(this, account_id)) {
					mArguments.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				} else {
					finish();
					return;
				}
			}
		}
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_spinner_navigation);
		final View view = mActionBar.getCustomView();
		mSpinner = (Spinner) view.findViewById(R.id.navigate);
		mAdapter = new ArrayAdapter<SpinnerSpec>(this, R.layout.spinner_item_white_text);
		mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		mAdapter.add(new SpinnerSpec(SearchTweetsFragment.class, getString(R.string.search_tweets)));
		mAdapter.add(new SpinnerSpec(SearchUsersFragment.class, getString(R.string.search_users)));
		mSpinner.setAdapter(mAdapter);
		mSpinner.setOnItemSelectedListener(this);
		mSpinner.setSelection(is_search_user ? 1 : 0);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		final Fragment fragment = Fragment.instantiate(this, mAdapter.getItem(position).cls.getName());
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		fragment.setArguments(mArguments);
		ft.replace(R.id.content, fragment);
		ft.commit();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

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

	static class SpinnerSpec {
		public final Class<? extends Fragment> cls;
		public final String name;

		public SpinnerSpec(Class<? extends Fragment> cls, String name) {
			this.cls = cls;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
