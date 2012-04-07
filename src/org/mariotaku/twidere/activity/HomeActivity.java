package org.mariotaku.twidere.activity;

import java.util.ArrayList;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ConnectTabFragment;
import org.mariotaku.twidere.fragment.DiscoverTabFragment;
import org.mariotaku.twidere.fragment.HomeTabFragment;
import org.mariotaku.twidere.fragment.MeTabFragment;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitleProvider;

public class HomeActivity extends SherlockFragmentActivity implements Constants {

	private ActionBar mActionBar;
	private TabsAdapter mAdapter;
	private ViewPager mViewPager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Cursor cur = getContentResolver().query(Accounts.CONTENT_URI, new String[] {}, null, null,
				null);
		int accounts_count = cur.getCount();
		cur.close();
		if (accounts_count <= 0) {
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			return;
		}
		setContentView(R.layout.main);
		mActionBar = getSupportActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(false);
		View view = mActionBar.getCustomView();

		mAdapter = new TabsAdapter(getSupportFragmentManager());
		mAdapter.addTab(HomeTabFragment.class, null, R.drawable.ic_tab_home);
		mAdapter.addTab(ConnectTabFragment.class, null, R.drawable.ic_tab_connect);
		mAdapter.addTab(DiscoverTabFragment.class, null, R.drawable.ic_tab_discover);
		mAdapter.addTab(MeTabFragment.class, null, R.drawable.ic_tab_me);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAdapter);
		TabPageIndicator mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
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
			case android.R.id.home:
				finish();
				break;
			case R.id.compose:
				startActivity(new Intent(this, ComposeActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
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