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
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.view.ExtendedViewPager;

import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerTabStrip;
import android.view.MenuItem;

public class SearchActivity extends MultiSelectActivity {

	private ActionBar mActionBar;
	private TabsAdapter mAdapter;

	private Uri mData;
	private final Bundle mArguments = new Bundle();

	private ExtendedViewPager mViewPager;
	private PagerTabStrip mPagerTab;
	private boolean mDisplayAppIcon;

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
		mPagerTab = (PagerTabStrip) findViewById(R.id.pager_tab);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(android.R.string.search_go);
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
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mAdapter = new TabsAdapter(this, getSupportFragmentManager(), null);
		mAdapter.addTab(SearchTweetsFragment.class, mArguments, getString(R.string.tweets), R.drawable.ic_tab_twitter,
				0);
		mAdapter.addTab(SearchUsersFragment.class, mArguments, getString(R.string.users), R.drawable.ic_tab_person, 1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(is_search_user ? 1 : 0);
		mPagerTab.setTabIndicatorColorResource(R.color.holo_blue_light);
		final Resources res = getResources();
		mPagerTab.setTextColor(res.getColor(isDarkTheme() ? R.color.primary_text_holo_dark : R.color.primary_text_holo_light));
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
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	protected void setPagingEnabled(final boolean enabled) {
		if (mViewPager != null) {
			mViewPager.setPagingEnabled(enabled);
			mViewPager.setEnabled(enabled);
		}
	}

	@Override
	int getDualPaneLayoutRes() {
		return R.layout.search_dual_pane;
	}

	@Override
	int getNormalLayoutRes() {
		return R.layout.search;
	}
}
