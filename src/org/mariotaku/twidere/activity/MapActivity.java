package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.GoogleMapFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

public class MapActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment fragment = Fragment.instantiate(this, GoogleMapFragment.class.getName());
		ft.replace(android.R.id.content, fragment);
		ft.commit();
	}
}
