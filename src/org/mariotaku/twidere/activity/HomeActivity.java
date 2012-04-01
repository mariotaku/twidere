package org.mariotaku.twidere.activity;

import java.util.ArrayList;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ConnectTabFragment;
import org.mariotaku.twidere.fragment.DiscoverTabFragment;
import org.mariotaku.twidere.fragment.HomeTabFragment;
import org.mariotaku.twidere.fragment.MeTabFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
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
		setContentView(R.layout.main);
		mActionBar = getSupportActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(false);
		mActionBar.setDisplayShowTitleEnabled(false);
		View view = mActionBar.getCustomView();

		mAdapter = new TabsAdapter(getSupportFragmentManager());
		mAdapter.addTab(new HomeTabFragment(), null, R.drawable.ic_tab_home);
		mAdapter.addTab(new ConnectTabFragment(), null, R.drawable.ic_tab_connect);
		mAdapter.addTab(new DiscoverTabFragment(), null, R.drawable.ic_tab_discover);
		mAdapter.addTab(new MeTabFragment(), null, R.drawable.ic_tab_me);
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAdapter);
		TabPageIndicator indicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		indicator.setViewPager(mViewPager);

	}

	/**
	 * This is a helper class that implements the management of tabs and all
	 * details of connecting a ViewPager with associated TabHost. It relies on a
	 * trick. Normally a tab host has a simple API for supplying a View or
	 * Intent that each tab will show. This is not sufficient for switching
	 * between pages. So instead we make the content part of the tab host 0dp
	 * high (it is not shown) and the TabsAdapter supplies its own dummy view to
	 * show as the tab content. It listens to changes in tabs, and takes care of
	 * switch to the correct paged in the ViewPager whenever the selected tab
	 * changes.
	 */
	private class TabsAdapter extends FragmentPagerAdapter implements TitleProvider {

		private final ArrayList<TabInfo> mTabsInfo = new ArrayList<TabInfo>();

		public TabsAdapter(FragmentManager fm) {
			super(fm);
		}

		public void addTab(Fragment fragment, String name, Integer icon) {

			if (fragment == null) throw new IllegalArgumentException("Fragment cannot be null!");
			if (name == null && icon == null)
				throw new IllegalArgumentException("You must specify a name or icon for this tab!");
			mTabsInfo.add(new TabInfo(name, icon, fragment));
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
			return mTabsInfo.get(position).fragment;
		}

		@Override
		public String getTitle(int position) {
			return mTabsInfo.get(position).name;
		}

		private class TabInfo {

			String name;
			Integer icon;
			Fragment fragment;

			public TabInfo(String name, Integer icon, Fragment fragment) {
				if (name == null && icon == null)
					throw new IllegalArgumentException(
							"You must specify a name or icon for this tab!");
				this.name = name;
				this.icon = icon;
				this.fragment = fragment;

			}
		}
	}
}