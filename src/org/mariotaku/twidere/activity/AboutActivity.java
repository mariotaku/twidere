package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.os.Bundle;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class AboutActivity extends BasePreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		addPreferencesFromResource(R.xml.about);
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
