package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;

import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.view.MenuItem;

@SuppressWarnings("deprecation")
public class GlobalSettingsActivity extends BasePreferenceActivity implements
		OnPreferenceChangeListener, OnPreferenceClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.global_settings);
		findPreference(PREFERENCE_KEY_DARK_THEME).setOnPreferenceChangeListener(this);
		findPreference(PREFERENCE_KEY_CLEAR_DATABASES).setOnPreferenceClickListener(this);
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
		if (PREFERENCE_KEY_DARK_THEME.equals(preference.getKey())) {
			CommonUtils.restartActivity(this);
		}
		return true;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (PREFERENCE_KEY_CLEAR_DATABASES.equals(preference.getKey())) {
			ContentResolver resolver = getContentResolver();
			resolver.delete(Statuses.CONTENT_URI, null, null);
			resolver.delete(Mentions.CONTENT_URI, null, null);
		}
		return true;
	}
}
