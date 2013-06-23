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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getThemeColor;
import static org.mariotaku.twidere.util.Utils.isMyAccount;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.view.ExtendedViewPager;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerTabStrip;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SearchActivity extends MultiSelectActivity {

	private ActionBar mActionBar;
	private TabsAdapter mAdapter;

	private ExtendedViewPager mViewPager;
	private PagerTabStrip mIndicator;

	private final Bundle mArguments = new Bundle();
	private Uri mData;
	private String mQuery;

	private boolean mDisplayAppIcon;
	private boolean mIsSearchUsers, mIsSearchTweets;

	@Override
	public void onBackStackChanged() {
		super.onBackStackChanged();
		if (!isDualPaneMode()) return;
		final FragmentManager fm = getSupportFragmentManager();
		final Fragment left_pane_fragment = fm.findFragmentById(PANE_LEFT);
		final boolean left_pane_used = left_pane_fragment != null && left_pane_fragment.isAdded();
		setPagingEnabled(!left_pane_used);
		final int count = fm.getBackStackEntryCount();
		if (mActionBar != null && mDisplayAppIcon) {
			mActionBar.setDisplayHomeAsUpEnabled(count > 0);
		}
		if (count == 0) {
			showLeftPane();
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ExtendedViewPager) findViewById(R.id.main);
		mIndicator = (PagerTabStrip) findViewById(R.id.pager_tab);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_search, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final FragmentManager fm = getSupportFragmentManager();
				if (isDualPaneMode()) {
					final int count = fm.getBackStackEntryCount();
					if (count == 0) {
						NavUtils.navigateUpFromSameTask(this);
					} else if (!FragmentManagerTrojan.isStateSaved(fm)) {
						for (int i = 0; i < count; i++) {
							fm.popBackStackImmediate();
						}
						setSupportProgressBarIndeterminateVisibility(false);
					}
				} else {
					NavUtils.navigateUpFromSameTask(this);
				}
				return true;
			}
			case MENU_COMPOSE: {
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle extras = new Bundle();
				if (mQuery.startsWith("#")) {
					extras.putString(Intent.EXTRA_TEXT, mQuery + " ");
				} else {
					extras.putString(Intent.EXTRA_TEXT, "#" + mQuery + " ");
				}
				intent.putExtras(extras);
				startActivity(intent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem compose = menu.findItem(MENU_COMPOSE);
		if (compose != null) {
			compose.setVisible(!mIsSearchUsers);
		}
		return true;
	}

	@Override
	protected int getDualPaneLayoutRes() {
		return R.layout.search_dual_pane;
	}

	@Override
	protected int getNormalLayoutRes() {
		return R.layout.search;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(android.R.string.search_go);
		final Intent intent = getIntent();
		mArguments.clear();
		mData = intent.getData();
		final String type = mData != null ? mData.getQueryParameter(QUERY_PARAM_TYPE) : null;
		mQuery = Intent.ACTION_SEARCH.equals(intent.getAction()) ? intent.getStringExtra(SearchManager.QUERY)
				: mData != null ? mData.getQueryParameter(QUERY_PARAM_QUERY) : null;
		mIsSearchUsers = QUERY_PARAM_VALUE_USERS.equals(type);
		mIsSearchTweets = QUERY_PARAM_VALUE_TWEETS.equals(type);
		if (isEmpty(mQuery)) {
			finish();
			return;
		}
		if (savedInstanceState == null) {
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
					RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
			suggestions.saveRecentQuery(mQuery, null);
		}
		mArguments.putString(INTENT_KEY_QUERY, mQuery);
		final String param_account_id = mData != null ? mData.getQueryParameter(QUERY_PARAM_ACCOUNT_ID) : null;
		if (param_account_id != null) {
			mArguments.putLong(INTENT_KEY_ACCOUNT_ID, ParseUtils.parseLong(param_account_id));
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
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setSubtitle(mQuery);
		mAdapter = new TabsAdapter(this, getSupportFragmentManager(), null);
		if (!mIsSearchUsers) {
			mAdapter.addTab(SearchTweetsFragment.class, mArguments, getString(R.string.tweets),
					R.drawable.ic_tab_twitter, 0);
		}
		if (!mIsSearchTweets) {
			mAdapter.addTab(SearchUsersFragment.class, mArguments, getString(R.string.users), R.drawable.ic_tab_person,
					1);
		}
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(mIsSearchUsers ? 1 : 0);
		mIndicator.setTabIndicatorColor(getThemeColor(this));
		mIndicator.setVisibility(mIsSearchUsers || mIsSearchTweets ? View.GONE : View.VISIBLE);
	}

	protected void setPagingEnabled(final boolean enabled) {
		if (mViewPager != null) {
			mViewPager.setPagingEnabled(enabled);
			mViewPager.setEnabled(enabled);
		}
	}
}
