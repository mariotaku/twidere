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
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.SearchRecentSuggestions;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class SettingsActivity extends BasePreferenceActivity implements OnPreferenceChangeListener,
		OnPreferenceClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
		addPreferencesFromResource(R.xml.settings);
		findPreference(PREFERENCE_KEY_DARK_THEME).setOnPreferenceChangeListener(this);
		findPreference(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND).setOnPreferenceChangeListener(this);
		findPreference(PREFERENCE_KEY_CLEAR_DATABASES).setOnPreferenceClickListener(this);
		findPreference(PREFERENCE_KEY_CLEAR_CACHE).setOnPreferenceClickListener(this);
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

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		final String key = preference.getKey();
		if (PREFERENCE_KEY_DARK_THEME.equals(key) || PREFERENCE_KEY_SOLID_COLOR_BACKGROUND.equals(key)) {
			boolean show_anim = false;
			try {
				final float transition_animation = Settings.System.getFloat(getContentResolver(),
						Settings.System.TRANSITION_ANIMATION_SCALE);
				show_anim = transition_animation > 0.0;
			} catch (final SettingNotFoundException e) {
				e.printStackTrace();
			}
			restartActivity(this, show_anim);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (PREFERENCE_KEY_CLEAR_DATABASES.equals(preference.getKey())) {
			final ContentResolver resolver = getContentResolver();
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
					RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
			resolver.delete(Statuses.CONTENT_URI, null, null);
			resolver.delete(Mentions.CONTENT_URI, null, null);
			resolver.delete(CachedUsers.CONTENT_URI, null, null);
			resolver.delete(DirectMessages.Inbox.CONTENT_URI, null, null);
			resolver.delete(DirectMessages.Outbox.CONTENT_URI, null, null);
			suggestions.clearHistory();
		} else if (PREFERENCE_KEY_CLEAR_CACHE.equals(preference.getKey())) {
			getTwidereApplication().clearCache();
		}
		return true;
	}
}
