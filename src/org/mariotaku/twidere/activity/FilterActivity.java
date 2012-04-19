package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.FilterFragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;


public class FilterActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, new FilterFragment());
		ft.commit();
	}

}
