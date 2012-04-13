package org.mariotaku.twidere.activity;

import java.util.ArrayList;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.ConnectFragment;
import org.mariotaku.twidere.fragment.DashboardFragment;
import org.mariotaku.twidere.fragment.DiscoverFragment;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ServiceInterface;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

@ContentView(R.layout.main)
public class HomeActivity extends BaseActivity {

	@InjectView(R.id.pager) ViewPager mViewPager;

	private ActionBar mActionBar;
	private ProgressBar mProgress;
	private TabsAdapter mAdapter;
	private ServiceInterface mInterface;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				setRefreshState();
			}
		}

	};
	private TabPageIndicator mIndicator;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInterface = ((TwidereApplication) getApplication()).getServiceInterface();
		StringBuilder where = new StringBuilder();
		where.append(Accounts.IS_ACTIVATED + "=1");
		Cursor cur = getContentResolver().query(Accounts.CONTENT_URI, new String[0],
				where.toString(), null, null);
		int accounts_count = cur == null ? 0 : cur.getCount();
		cur.close();

		if (accounts_count <= 0) {
			startActivity(new Intent(this, LoginActivity.class));
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
		mAdapter = new TabsAdapter(getSupportFragmentManager());
		mAdapter.addTab(HomeTimelineFragment.class, null, R.drawable.ic_tab_home);
		mAdapter.addTab(ConnectFragment.class, null, R.drawable.ic_tab_connect);
		mAdapter.addTab(DiscoverFragment.class, null, R.drawable.ic_tab_discover);
		mAdapter.addTab(DashboardFragment.class, null, R.drawable.ic_tab_me);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);

		mViewPager.getChildCount();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_COMPOSE:
				startActivity(new Intent(INTENT_ACTION_COMPOSE));
				break;
			case MENU_SELECT_ACCOUNT:
				break;
			case MENU_SETTINGS:
				startActivity(new Intent(INTENT_ACTION_GLOBAL_SETTINGS));
				break;
		}
		return super.onOptionsItemSelected(item);
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

	private void setRefreshState() {
		boolean is_refresh = false;
		if (mInterface != null) {
			is_refresh = mInterface.isHomeTimelineRefreshing() || mInterface.isMentionsRefreshing();
		}
		mProgress.setVisibility(is_refresh ? View.VISIBLE : View.INVISIBLE);
	}

	private class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

		private ArrayList<TabInfo> mTabsInfo = new ArrayList<TabInfo>();

		public TabsAdapter(FragmentManager fm) {
			super(fm);
			mTabsInfo.clear();
		}

		public void addTab(Class<? extends Fragment> cls, String name, Integer icon) {

			if (cls == null) throw new IllegalArgumentException("Fragment cannot be null!");
			if (name == null && icon == null)
				throw new IllegalArgumentException("You must specify a name or icon for this tab!");
			mTabsInfo.add(new TabInfo(name, icon, cls));
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mTabsInfo.size();
		}

		@Override
		public Integer getIcon(int position) {
			return mTabsInfo.get(position).icon;
		}

		@Override
		public Fragment getItem(int position) {
			return Fragment.instantiate(getApplicationContext(),
					mTabsInfo.get(position).cls.getName());
		}

		@Override
		public String getTitle(int position) {
			return mTabsInfo.get(position).name;
		}

		@Override
		public void onPageReselected(int position) {
			String action = mTabsInfo.get(position).cls.getName() + SHUFFIX_SCROLL_TO_TOP;
			sendBroadcast(new Intent(action));
		}

		private class TabInfo {

			private String name;
			private Integer icon;
			private Class<? extends Fragment> cls;

			public TabInfo(String name, Integer icon, Class<? extends Fragment> cls) {
				if (name == null && icon == null)
					throw new IllegalArgumentException(
							"You must specify a name or icon for this tab!");
				this.name = name;
				this.icon = icon;
				this.cls = cls;

			}
		}

	}
}