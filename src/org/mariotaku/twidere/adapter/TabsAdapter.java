/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter;

import java.util.ArrayList;

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.view.TabPageIndicator.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

	private final ArrayList<TabInfo> mTabsInfo = new ArrayList<TabInfo>();

	private final Context mContext;

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
		final Fragment fragment = Fragment.instantiate(mContext, mTabsInfo.get(position).cls.getName());
		fragment.setArguments(mTabsInfo.get(position).args);
		return fragment;
	}

	@Override
	public String getTitle(int position) {
		return mTabsInfo.get(position).name;
	}

	@Override
	public void onPageReselected(int position) {
		final String action = mTabsInfo.get(position).cls.getName() + HomeActivity.SHUFFIX_SCROLL_TO_TOP;
		final Intent intent = new Intent(action);
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
