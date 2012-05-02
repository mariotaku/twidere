package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.ViewStatusFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.actionbarsherlock.view.MenuItem;

public class ViewStatusActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			finish();
			return;
		}
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ViewStatusFragment fragment = new ViewStatusFragment();
		fragment.setArguments(bundle);
		ft.replace(android.R.id.content, fragment);
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
