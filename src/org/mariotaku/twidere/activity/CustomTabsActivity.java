package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.CustomTabsFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

public class CustomTabsActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new FrameLayout(this));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final Fragment fragment = new CustomTabsFragment();
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, fragment);
		ft.commit();
	}
}
