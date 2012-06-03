package org.mariotaku.twidere.activity;

import org.mariotaku.actionbarcompat.app.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.FilterFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class FilterActivity extends BaseActivity implements OnCheckedChangeListener {

	private ActionBar mActionBar;
	private CompoundButton mToggle;
	SharedPreferences mPrefs;

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mPrefs.edit().putBoolean(PREFERENCE_KEY_ENABLE_FILTER, isChecked).commit();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		boolean filter_enabled = mPrefs.getBoolean(PREFERENCE_KEY_ENABLE_FILTER, false);
		setContentView(R.layout.base_layout);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.actionbar_filter);
		mToggle = (CompoundButton) mActionBar.getCustomView().findViewById(R.id.toggle);
		mToggle.setOnCheckedChangeListener(this);
		mToggle.setChecked(filter_enabled);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, new FilterFragment());
		ft.commit();
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
