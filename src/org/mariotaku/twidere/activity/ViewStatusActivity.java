package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ViewStatusFragment;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

public class ViewStatusActivity extends BaseActivity {

	@InjectResource(R.color.holo_blue_bright) public int mActivedMenuColor;
	@InjectExtra(Statuses.ACCOUNT_ID) private long mAccountId;
	@InjectExtra(Statuses.STATUS_ID) private long mStatusId;

	private ActionBar mActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ViewStatusFragment fragment = new ViewStatusFragment();
		Bundle bundle = new Bundle();
		bundle.putLong(Statuses.ACCOUNT_ID, mAccountId);
		bundle.putLong(Statuses.STATUS_ID, mStatusId);
		fragment.setArguments(bundle);
		ft.replace(android.R.id.content, fragment);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
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
