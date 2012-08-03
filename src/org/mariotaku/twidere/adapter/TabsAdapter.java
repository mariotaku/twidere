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
import java.util.Collection;

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.view.TabPageIndicator.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

	private final ArrayList<TabSpec> mTabsInfo = new ArrayList<TabSpec>();

	private final Context mContext;

	public TabsAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
		clear();
	}
	
	public void clear() {
		mTabsInfo.clear();
		notifyDataSetChanged();
	}

	public void addTab(Class<? extends Fragment> cls, Bundle args, String name, Integer icon, int position) {
		addTab(new TabSpec(name, icon, cls, args, position));
	}
	
	@Deprecated
	public void addTab(Class<? extends Fragment> cls, Bundle args, String name, Integer icon) {
		addTab(new TabSpec(name, icon, cls, args));
	}
	
	public void addTab(TabSpec spec) {
		mTabsInfo.add(spec);
		notifyDataSetChanged();
	}
	
	public void addTabs(Collection<? extends TabSpec> specs) {
		mTabsInfo.addAll(specs);
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

}
