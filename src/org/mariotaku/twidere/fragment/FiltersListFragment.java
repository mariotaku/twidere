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

package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredUsersFragment;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.TabPageIndicator;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class FiltersListFragment extends BaseFragment implements Panes.Right {

	private ExtendedViewPager mViewPager;
	private TabPageIndicator mIndicator;

	private TabsAdapter mAdapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		new Handler().post(new Runnable() {

			@Override
			public void run() {
				mAdapter = new TabsAdapter(getActivity(), getFragmentManager(), mIndicator);
				mAdapter.addTab(FilteredUsersFragment.class, null, getString(R.string.users), null, 0);
				mAdapter.addTab(FilteredKeywordsFragment.class, null, getString(R.string.keywords), null, 1);
				mAdapter.addTab(FilteredSourcesFragment.class, null, getString(R.string.sources), null, 2);
				mViewPager.setAdapter(mAdapter);
				mIndicator.setViewPager(mViewPager);
				mIndicator.setDisplayLabel(true);
				mIndicator.setDisplayIcon(false);
			}

		});
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_filter, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.filters, null);
		mViewPager = (ExtendedViewPager) view.findViewById(R.id.pager);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		return view;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}
}
