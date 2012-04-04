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
import android.widget.AbsListView;

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
		TabPageIndicator mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mIndicator.setViewPager(mViewPager);

		mViewPager.getChildCount();

	}

	private class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

		private ArrayList<TabInfo> mTabsInfo = new ArrayList<TabInfo>();

		public TabsAdapter(FragmentManager fm) {
			super(fm);
			mTabsInfo.clear();
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

		@Override
		public void onPageReselected(int position) {
			View fragmentview = mViewPager.getChildAt(position);
			if (fragmentview != null) {
				View view = fragmentview.findViewById(android.R.id.list);
				if (view != null && view instanceof AbsListView) {
					((AbsListView) view).smoothScrollToPosition(0);
				}
			}
		}

		private class TabInfo {

			private String name;
			private Integer icon;
			private Fragment fragment;

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