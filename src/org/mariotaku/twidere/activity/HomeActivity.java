package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.DashboardFragment;
import org.mariotaku.twidere.fragment.DiscoverFragment;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.MentionsFragment;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.widget.TabsAdapter;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;

public class HomeActivity extends BaseActivity {

	private ViewPager mViewPager;

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
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		StringBuilder where = new StringBuilder();
		where.append(Accounts.IS_ACTIVATED + "=1");
		long[] activated_ids = CommonUtils.getActivatedAccounts(this);

		if (activated_ids.length <= 0) {
			startActivity(new Intent(INTENT_ACTION_TWITTER_LOGIN));
			finish();
			return;
		}

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.getLongArray(INTENT_KEY_IDS) != null) {
			long[] refreshed_ids = bundle.getLongArray(INTENT_KEY_IDS);
			mInterface.getHomeTimeline(refreshed_ids, null);
			mInterface.getMentions(refreshed_ids, null);
			mInterface.getMessages(refreshed_ids, null);
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
		mAdapter.addTab(MentionsFragment.class, null, R.drawable.ic_tab_connect);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		ContentResolver resolver = getContentResolver();
		ContentValues values;
		switch (requestCode) {
			case REQUEST_SELECT_ACCOUNT:
				if (resultCode != RESULT_OK) {
					break;
				}
				if (intent == null || intent.getExtras() == null) {
					break;
				}
				Bundle bundle = intent.getExtras();
				if (bundle == null) {
					break;
				}
				long[] account_ids = bundle.getLongArray(INTENT_KEY_USER_IDS);
				if (account_ids != null) {
					values = new ContentValues();
					values.put(Accounts.IS_ACTIVATED, 0);
					resolver.update(Accounts.CONTENT_URI, values, null, null);
					values = new ContentValues();
					values.put(Accounts.IS_ACTIVATED, 1);
					for (long account_id : account_ids) {
						String where = Accounts.USER_ID + "=" + account_id;
						resolver.update(Accounts.CONTENT_URI, values, where, null);
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}
}
