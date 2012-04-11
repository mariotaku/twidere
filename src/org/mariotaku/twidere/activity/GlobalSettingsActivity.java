package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;

@SuppressWarnings("deprecation")
public class GlobalSettingsActivity extends BasePreferenceActivity implements Constants {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getPreferenceManager().setSharedPreferencesName(PREFERENCE_NAME);
		addPreferencesFromResource(R.xml.global_settings);
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
}
