package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.ConnectFragment;
import org.mariotaku.twidere.fragment.DashboardFragment;
import org.mariotaku.twidere.fragment.DiscoverFragment;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.widget.TabsAdapter;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;

@ContentView(R.layout.main)
public class HomeActivity extends BaseActivity {

	@InjectView(R.id.pager) private ViewPager mViewPager;

	private ActionBar mActionBar;
	private ProgressBar mProgress;
	private TabsAdapter mAdapter;
	private ServiceInterface mInterface;
	private TabPageIndicator mIndicator;
	private boolean mBottomActions;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				setRefreshState();
			}
		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setUiOptions();
		super.onCreate(savedInstanceState);
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		StringBuilder where = new StringBuilder();
		where.append(Accounts.IS_ACTIVATED + "=1");
		Cursor cur = getContentResolver().query(Accounts.CONTENT_URI, new String[0],
				where.toString(), null, null);
		int accounts_count = cur == null ? 0 : cur.getCount();
		cur.close();

		if (accounts_count <= 0) {
			startActivity(new Intent(INTENT_ACTION_TWITTER_LOGIN));
			finish();
			return;
		}

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.getBoolean(INTENT_KEY_REFRESH_ALL)) {
			Cursor refresh_cur = getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.USER_ID }, where.toString(), null, null);
			if (refresh_cur != null) {
				long[] account_ids = new long[refresh_cur.getCount()];
				refresh_cur.moveToFirst();
				int idx = 0;
				while (!refresh_cur.isAfterLast()) {
					account_ids[idx] = refresh_cur.getLong(refresh_cur
							.getColumnIndexOrThrow(Accounts.USER_ID));
					refresh_cur.moveToNext();
					idx++;
				}
				mInterface.refreshHomeTimeline(account_ids, null);
				mInterface.refreshMentions(account_ids, null);
				mInterface.refreshMessages(account_ids, null);
				refresh_cur.close();
			}
		}
		mActionBar = getSupportActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		View view = mActionBar.getCustomView();
		mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mAdapter = new TabsAdapter(this, getSupportFragmentManager());
		mAdapter.addTab(HomeTimelineFragment.class, null, R.drawable.ic_tab_home);
		mAdapter.addTab(ConnectFragment.class, null, R.drawable.ic_tab_connect);
		mAdapter.addTab(DiscoverFragment.class, null, R.drawable.ic_tab_discover);
		mAdapter.addTab(DashboardFragment.class, null, R.drawable.ic_tab_me);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_COMPOSE:
				startActivity(new Intent(INTENT_ACTION_COMPOSE));
				break;
			case MENU_SELECT_ACCOUNT:
				startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT),
						REQUEST_SELECT_ACCOUNT);
				break;
			case MENU_SETTINGS:
				startActivity(new Intent(INTENT_ACTION_GLOBAL_SETTINGS));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isUiOptionsChanged()) {
			CommonUtils.restartActivity(this);
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		setRefreshState();
		IntentFilter filter = new IntentFilter(BROADCAST_REFRESHSTATE_CHANGED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	private boolean isUiOptionsChanged() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		boolean bottom_actions = preferences.getBoolean(PREFERENCE_KEY_BOTTOM_ACTIONS, false);
		return bottom_actions != mBottomActions;
	}

	private void setRefreshState() {
		boolean is_refresh = false;
		if (mInterface != null) {
			is_refresh = mInterface.hasActivatedTask();
		}
		mProgress.setVisibility(is_refresh ? View.VISIBLE : View.INVISIBLE);
	}

	private void setUiOptions() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mBottomActions = preferences.getBoolean(PREFERENCE_KEY_BOTTOM_ACTIONS, false);
		if (mBottomActions) {
			CommonUtils.setUiOptions(getWindow(),
					ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
		}
	}
}