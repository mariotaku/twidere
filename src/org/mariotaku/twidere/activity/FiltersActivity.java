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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SourceAutoCompleteAdapter;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredLinksFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredUsersFragment;
import org.mariotaku.twidere.provider.TweetStore.Filters;

public class FiltersActivity extends BaseActivity implements TabListener, OnPageChangeListener {

	private static final String EXTRA_AUTO_COMPLETE_TYPE = "auto_complete_type";
	private static final int AUTO_COMPLETE_TYPE_USERS = 1;
	private static final int AUTO_COMPLETE_TYPE_SOURCES = 2;

	private ViewPager mViewPager;
	private TabsAdapter mAdapter;

	private ActionBar mActionBar;
	private SharedPreferences mPreferences;

	@Override
	public void onContentChanged() {
		mViewPager = (ViewPager) findViewById(R.id.pager);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		setContentView(R.layout.filters);
		mActionBar = getActionBar();
		mAdapter = new TabsAdapter(this, getFragmentManager(), null);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		addTab(FilteredUsersFragment.class, getString(R.string.users), 0);
		addTab(FilteredKeywordsFragment.class, getString(R.string.keywords), 1);
		addTab(FilteredSourcesFragment.class, getString(R.string.sources), 2);
		addTab(FilteredLinksFragment.class, getString(R.string.links), 3);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_filters, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				NavUtils.navigateUpFromSameTask(this);
				return true;
			}
			case MENU_ADD: {
				final Fragment filter = mAdapter.getItem(mViewPager.getCurrentItem());
				if (!(filter instanceof BaseFiltersFragment)) return true;
				final Bundle args = new Bundle();
				if (filter instanceof FilteredUsersFragment) {
					args.putInt(EXTRA_AUTO_COMPLETE_TYPE, AUTO_COMPLETE_TYPE_USERS);
				} else if (filter instanceof FilteredSourcesFragment) {
					args.putInt(EXTRA_AUTO_COMPLETE_TYPE, AUTO_COMPLETE_TYPE_SOURCES);
				}
				args.putParcelable(EXTRA_URI, ((BaseFiltersFragment) filter).getContentUri());
				final AddItemFragment dialog = new AddItemFragment();
				dialog.setArguments(args);
				dialog.show(getFragmentManager(), "add_rule");
				return true;
			}
			case R.id.enable_in_home_timeline: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(PREFERENCE_KEY_FILTERS_IN_HOME_TIMELINE, !item.isChecked());
				editor.apply();
				break;
			}
			case R.id.enable_in_mentions: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(PREFERENCE_KEY_FILTERS_IN_MENTIONS, !item.isChecked());
				editor.apply();
				break;
			}
			case R.id.enable_for_rts: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(PREFERENCE_KEY_FILTERS_FOR_RTS, !item.isChecked());
				editor.apply();
				break;
			}
		}
		return false;
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(final int state) {

	}

	@Override
	public void onPageSelected(final int position) {
		if (mActionBar == null) return;
		mActionBar.setSelectedNavigationItem(position);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean enable_in_home_timeline = mPreferences.getBoolean(PREFERENCE_KEY_FILTERS_IN_HOME_TIMELINE, true);
		final boolean enable_in_mentions = mPreferences.getBoolean(PREFERENCE_KEY_FILTERS_IN_MENTIONS, true);
		final boolean enable_for_rts = mPreferences.getBoolean(PREFERENCE_KEY_FILTERS_FOR_RTS, true);
		menu.findItem(R.id.enable_in_home_timeline).setChecked(enable_in_home_timeline);
		menu.findItem(R.id.enable_in_mentions).setChecked(enable_in_mentions);
		menu.findItem(R.id.enable_for_rts).setChecked(enable_for_rts);
		return true;
	}

	@Override
	public void onTabReselected(final Tab tab, final FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(final Tab tab, final FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(final Tab tab, final FragmentTransaction ft) {

	}

	private void addTab(final Class<? extends Fragment> cls, final String name, final int position) {
		if (mActionBar == null || mAdapter == null) return;
		mActionBar.addTab(mActionBar.newTab().setText(name).setTabListener(this));
		mAdapter.addTab(cls, null, name, null, position);
	}

	public static final class AddItemFragment extends BaseDialogFragment implements OnClickListener {

		private AutoCompleteTextView mEditText;

		private SimpleCursorAdapter mUserAutoCompleteAdapter;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (mEditText.length() <= 0) return;
					final ContentValues values = new ContentValues();
					final String text = mEditText.getText().toString();
					values.put(Filters.VALUE, text);
					final Bundle args = getArguments();
					final Uri uri = args.getParcelable(EXTRA_URI);
					getContentResolver().insert(uri, values);
					break;
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Context context = getActivity();
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			final View view = LayoutInflater.from(context).inflate(R.layout.auto_complete_textview, null);
			builder.setView(view);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			final Bundle args = getArguments();
			final int auto_complete_type = args != null ? args.getInt(EXTRA_AUTO_COMPLETE_TYPE, 0) : 0;
			if (auto_complete_type != 0) {
				if (auto_complete_type == AUTO_COMPLETE_TYPE_SOURCES) {
					mUserAutoCompleteAdapter = new SourceAutoCompleteAdapter(getActivity());
				} else {
					mUserAutoCompleteAdapter = new UserHashtagAutoCompleteAdapter(getActivity());
				}
				mEditText.setAdapter(mUserAutoCompleteAdapter);
				mEditText.setThreshold(1);
			}
			builder.setTitle(R.string.add_rule);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

	}

}
