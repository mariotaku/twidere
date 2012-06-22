package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.GoogleWebMapFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class MapActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		final Fragment fragment = Fragment.instantiate(this, GoogleWebMapFragment.class.getName());
		fragment.setArguments(getIntent().getExtras());
		ft.replace(android.R.id.content, fragment);
		ft.commit();
	}
}
