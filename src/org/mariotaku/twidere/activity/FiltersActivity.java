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
import org.mariotaku.twidere.fragment.FiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.FiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.FiltersFragment.FilteredUsersFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.Spinner;

public class FiltersActivity extends BaseActivity implements OnItemSelectedListener {

	private ActionBar mActionBar;
	private SharedPreferences mPrefs;
	private ArrayAdapter<TabSpec> mAdapter;
	private Spinner mSpinner;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setContentView(new FrameLayout(this));
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_spinner_navigation);
		final View view = mActionBar.getCustomView();
		mSpinner = (Spinner) view.findViewById(R.id.navigate);
		mAdapter = new ArrayAdapter<TabSpec>(this, R.layout.spinner_item_white_text);
		mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		mAdapter.add(new TabSpec(FilteredUsersFragment.class, getString(R.string.users)));
		mAdapter.add(new TabSpec(FilteredKeywordsFragment.class, getString(R.string.keywords)));
		mAdapter.add(new TabSpec(FilteredSourcesFragment.class, getString(R.string.sources)));
		mSpinner.setAdapter(mAdapter);
		mSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_filter, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
		final Fragment fragment = Fragment.instantiate(this, mAdapter.getItem(position).cls.getName());
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, fragment);
		ft.commit();

	}

	@Override
	public void onNothingSelected(final AdapterView<?> parent) {

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
			case MENU_ADD:
				return false;
		}
		return super.onOptionsItemSelected(item);
	}

	static class TabSpec {
		public final Class<? extends Fragment> cls;
		public final String name;

		public TabSpec(final Class<? extends Fragment> cls, final String name) {
			this.cls = cls;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
