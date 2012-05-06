package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.fragment.UserTimelineFragment;

import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class LinkHandlerActivity extends BaseActivity {

	private UserTimelineFragment mFragment;

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFragment = new UserTimelineFragment();
		Uri data = getIntent().getData();
		if (data != null) {
			String param_screen_name = data.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
			String param_account_id = data.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);

			Bundle bundle = new Bundle();
			bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
			bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
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

	private long parseLong(String source) {
		if (source == null) return -1;
		try {
			return Long.parseLong(source);
		} catch (NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}
}
