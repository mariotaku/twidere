package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.UserTimelineFragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class UserProfileActivity extends BaseActivity {

	private UserTimelineFragment mFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFragment = new UserTimelineFragment();
		Uri data = getIntent().getData();
		if (data != null) {
			Bundle bundle = new Bundle();
			bundle.putString(INTENT_KEY_SCREEN_NAME, data.getQueryParameter(QUERY_PARAM_SCREEN_NAME));
			try {
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, Long.valueOf(data.getQueryParameter(QUERY_PARAM_ACCOUNT_ID)));
			} catch (NumberFormatException e) {

			}
			mFragment.setArguments(bundle);
		} else {
			mFragment.setArguments(getIntent().getExtras());
		}
		ft.replace(android.R.id.content, mFragment);
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
