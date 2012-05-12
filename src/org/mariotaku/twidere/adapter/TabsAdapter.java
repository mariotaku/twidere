package org.mariotaku.twidere.adapter;

import java.util.ArrayList;

import org.mariotaku.twidere.activity.HomeActivity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.viewpagerindicator.TitleProvider;

public class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

	private ArrayList<TabInfo> mTabsInfo = new ArrayList<TabInfo>();

	private Context mContext;

	public TabsAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
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
		return Fragment.instantiate(mContext, mTabsInfo.get(position).cls.getName());
	}

	@Override
	public String getTitle(int position) {
		return mTabsInfo.get(position).name;
	}

	@Override
	public void onPageReselected(int position) {
		String action = mTabsInfo.get(position).cls.getName() + HomeActivity.SHUFFIX_SCROLL_TO_TOP;
		mContext.sendBroadcast(new Intent(action));
	}

	@Override
	public void onPageSelected(int position) {

	}

	private class TabInfo {

		private String name;
		private Integer icon;
		private Class<? extends Fragment> cls;

		public TabInfo(String name, Integer icon, Class<? extends Fragment> cls) {
			if (name == null && icon == null)
				throw new IllegalArgumentException("You must specify a name or icon for this tab!");
			this.name = name;
			this.icon = icon;
			this.cls = cls;

		}
	}

}