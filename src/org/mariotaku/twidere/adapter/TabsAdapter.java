package org.mariotaku.twidere.adapter;

import java.util.ArrayList;

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.view.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

	private ArrayList<TabInfo> mTabsInfo = new ArrayList<TabInfo>();

	private Context mContext;

	public TabsAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
		mTabsInfo.clear();
	}

	public void addTab(Class<? extends Fragment> cls, Bundle args, String name, Integer icon) {

		if (cls == null) throw new IllegalArgumentException("Fragment cannot be null!");
		if (name == null && icon == null)
			throw new IllegalArgumentException("You must specify a name or icon for this tab!");
		mTabsInfo.add(new TabInfo(name, icon, cls, args));
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
		Fragment fragment = Fragment.instantiate(mContext, mTabsInfo.get(position).cls.getName());
		fragment.setArguments(mTabsInfo.get(position).args);
		return fragment;
	}

	@Override
	public String getTitle(int position) {
		return mTabsInfo.get(position).name;
	}

	@Override
	public void onPageReselected(int position) {
		String action = mTabsInfo.get(position).cls.getName() + HomeActivity.SHUFFIX_SCROLL_TO_TOP;
		Intent intent = new Intent(action);
		intent.setPackage(mContext.getPackageName());
		mContext.sendBroadcast(intent);
	}

	@Override
	public void onPageSelected(int position) {

	}

	private class TabInfo {

		private final String name;
		private final Integer icon;
		private final Class<? extends Fragment> cls;
		private final Bundle args;

		public TabInfo(String name, Integer icon, Class<? extends Fragment> cls, Bundle args) {
			if (name == null && icon == null)
				throw new IllegalArgumentException("You must specify a name or icon for this tab!");
			this.name = name;
			this.icon = icon;
			this.cls = cls;
			this.args = args;

		}
	}

}