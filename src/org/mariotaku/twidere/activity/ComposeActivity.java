package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.ComposeFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.view.MenuItem;

public class ComposeActivity extends BaseActivity {

	private static final String TAG_COMPOSE = "compose";
	private Fragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		FragmentManager fm = getSupportFragmentManager();

		// Check to see if we have retained the worker fragment.
		mFragment = fm.findFragmentByTag(TAG_COMPOSE);

		// If not retained (or first time running), we need to create it.
		if (mFragment == null) {
			mFragment = new ComposeFragment();
			mFragment.setArguments(getIntent().getExtras());
			fm.beginTransaction().replace(android.R.id.content, mFragment, TAG_COMPOSE).commit();
		}
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
