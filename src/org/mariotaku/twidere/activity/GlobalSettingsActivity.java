package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

import com.actionbarsherlock.view.MenuItem;

@SuppressWarnings("deprecation")
public class GlobalSettingsActivity extends BasePreferenceActivity implements
		OnPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.global_settings);
		findPreference(PREFERENCE_KEY_DARK_THEME).setOnPreferenceChangeListener(this);
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
			restartActivity();
		}
		return true;
	}
}
