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

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredUsersFragment;

import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class FiltersActivity extends BaseActivity {

	private ActionBar mActionBar;
	
	private ViewPager mViewPager;
	private PagerTabStrip mPagerTab;

	private TabsAdapter mAdapter;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filters);
		mActionBar = getSupportActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
		}
		mAdapter = new TabsAdapter(this, getSupportFragmentManager(), null);
		mAdapter.addTab(FilteredUsersFragment.class, null, getString(R.string.users), null, 0);
		mAdapter.addTab(FilteredKeywordsFragment.class, null, getString(R.string.keywords), null, 1);
		mAdapter.addTab(FilteredSourcesFragment.class, null, getString(R.string.sources), null, 2);
		mViewPager.setAdapter(mAdapter);
		mPagerTab.setTabIndicatorColorResource(R.color.holo_blue_light);
	}


	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mPagerTab = (PagerTabStrip) findViewById(R.id.pager_tab);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_filter, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}
}
