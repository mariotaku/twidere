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

import static org.mariotaku.twidere.util.Utils.restartActivity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.fragment.ExtensionsListFragment;
import org.mariotaku.twidere.fragment.FiltersListFragment;
import org.mariotaku.twidere.fragment.InternalSettingsFragment;
import org.mariotaku.twidere.fragment.SettingsDetailsFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class SettingsActivity extends DualPaneActivity implements OnSharedPreferenceChangeListener,
		OnPreferenceClickListener {

	private SharedPreferences mPreferences;

	private static final String KEY_ABOUT = "about";
	private static final String KEY_CUSTOM_TABS = "custom_tabs";
	private static final String KEY_EXTENSIONS = "extensions";
	private static final String KEY_FILTERS = "filters";
	private static final String KEY_SETTINGS_APPEARANCE = "settings_appearance";
	private static final String KEY_SETTINGS_CONTENT_AND_STORAGE = "settings_content_and_storage";
	private static final String KEY_SETTINGS_NETWORK = "settings_network";
	private static final String KEY_SETTINGS_REFRESH_AND_NOTIFICATIONS = "settings_refresh_and_notifications";
	private static final String KEY_SETTINGS_OTHER = "settings_other";

	@Override
	public void onBackPressed() {
		NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		final InternalSettingsFragment fragment = (InternalSettingsFragment) getSupportFragmentManager()
				.findFragmentById(R.id.main);
		if (fragment == null) return;
		final InternalSettingsActivity activity = fragment.getAttachedActivity();
		if (activity != null) {
			activity.findPreference(KEY_ABOUT).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_EXTENSIONS).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_CUSTOM_TABS).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_FILTERS).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_SETTINGS_APPEARANCE).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_SETTINGS_CONTENT_AND_STORAGE).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_SETTINGS_NETWORK).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_SETTINGS_REFRESH_AND_NOTIFICATIONS).setOnPreferenceClickListener(this);
			activity.findPreference(KEY_SETTINGS_OTHER).setOnPreferenceClickListener(this);
		}
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		final String key = preference.getKey();
		final Bundle args = new Bundle();
		final int res_id;
		if (KEY_CUSTOM_TABS.equals(key)) {
			if (isDualPaneMode()) {
				showFragment(new CustomTabsFragment());
			} else {
				final Intent intent = new Intent(INTENT_ACTION_CUSTOM_TABS);
				intent.setPackage(getPackageName());
				startActivity(intent);
			}
			return true;
		} else if (KEY_EXTENSIONS.equals(key)) {
			if (isDualPaneMode()) {
				showFragment(new ExtensionsListFragment());
			} else {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSIONS);
				intent.setPackage(getPackageName());
				startActivity(intent);
			}
			return true;
		} else if (KEY_FILTERS.equals(key)) {
			if (isDualPaneMode()) {
				showFragment(new FiltersListFragment());
			} else {
				final Intent intent = new Intent(INTENT_ACTION_FILTERS);
				intent.setClass(this, FiltersActivity.class);
				startActivity(intent);
			}
			return true;
		} else if (KEY_SETTINGS_APPEARANCE.equals(key)) {
			res_id = R.xml.settings_appearance;
		} else if (KEY_SETTINGS_CONTENT_AND_STORAGE.equals(key)) {
			res_id = R.xml.settings_content_and_storage;
		} else if (KEY_SETTINGS_NETWORK.equals(key)) {
			res_id = R.xml.settings_network;
		} else if (KEY_SETTINGS_REFRESH_AND_NOTIFICATIONS.equals(key)) {
			res_id = R.xml.settings_refresh_and_notifications;
		} else if (KEY_SETTINGS_OTHER.equals(key)) {
			res_id = R.xml.settings_other;
		} else if (KEY_ABOUT.equals(key)) {
			res_id = R.xml.about;
		} else {
			res_id = -1;
		}
		if (res_id > 0) {
			args.putInt(INTENT_KEY_RESID, res_id);
			if (isDualPaneMode()) {
				final Fragment fragment = new SettingsDetailsFragment();
				fragment.setArguments(args);
				showFragment(fragment);
			} else {
				final Intent intent = new Intent(this, SettingsDetailsActivity.class);
				intent.putExtras(args);
				startActivity(intent);
			}
		}
		return true;
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (PREFERENCE_KEY_DARK_THEME.equals(key) || PREFERENCE_KEY_SOLID_COLOR_BACKGROUND.equals(key)) {
			restartActivity(this);
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		setIntent(getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		final Fragment fragment = new InternalSettingsFragment();
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.main, fragment);
		ft.commit();
	}

	@Override
	protected boolean shouldForceEnableDualPaneMode() {
		return getResources().getBoolean(R.bool.is_large_screen);
	}

	void showFragment(final Fragment fragment) {
		final FragmentManager fm = getSupportFragmentManager();
		final int entry_count = fm.getBackStackEntryCount();
		final FragmentTransaction ft = fm.beginTransaction();
		for (int i = 0; i < entry_count; i++) {
			final int id = fm.getBackStackEntryAt(i).getId();
			ft.remove(fm.findFragmentById(id));
		}
		ft.commitAllowingStateLoss();
		showFragment(fragment, false);
	}

}
