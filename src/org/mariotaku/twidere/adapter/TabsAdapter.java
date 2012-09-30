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

import static org.mariotaku.twidere.util.Utils.getTabIconDrawable;

import java.util.ArrayList;
import java.util.Collection;

import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.view.TabPageIndicator;
import org.mariotaku.twidere.view.TabPageIndicator.TitleProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsAdapter extends FragmentStatePagerAdapter implements TitleProvider {

	private final ArrayList<TabSpec> mTabs = new ArrayList<TabSpec>();

	private final Context mContext;
	private final TabPageIndicator mIndicator;

	private boolean mDisplayLabel;

	public TabsAdapter(final Context context, final FragmentManager fm, final TabPageIndicator indicator) {
		super(fm);
		mContext = context;
		mIndicator = indicator;
		clear();
	}

	public void addTab(final Class<? extends Fragment> cls, final Bundle args, final String name, final Integer icon,
			final int position) {
		addTab(new TabSpec(name, icon, cls, args, position));
	}

	public void addTab(final TabSpec spec) {
		mTabs.add(spec);
		notifyDataSetChanged();
	}

	public void addTabs(final Collection<? extends TabSpec> specs) {
		mTabs.addAll(specs);
		notifyDataSetChanged();
	}

	public void clear() {
		mTabs.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTabs.size();
	}

	@Override
	public Drawable getIcon(final int position) {
		return getTabIconDrawable(mContext, mTabs.get(position).icon);
	}

	@Override
	public Fragment getItem(final int position) {
		final Fragment fragment = Fragment.instantiate(mContext, mTabs.get(position).cls.getName());
		fragment.setArguments(mTabs.get(position).args);
		return fragment;
	}

	@Override
	public String getTitle(final int position) {
		return mDisplayLabel ? mTabs.get(position).name : null;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (mIndicator != null) {
			mIndicator.notifyDataSetChanged();
		}
	}

	@Override
	public void onPageReselected(final int position) {
		final String action = mTabs.get(position).cls.getName() + HomeActivity.SHUFFIX_SCROLL_TO_TOP;
		final Intent intent = new Intent(action);
		intent.setPackage(mContext.getPackageName());
		mContext.sendBroadcast(intent);
	}

	@Override
	public void onPageSelected(final int position) {

	}

	public void setDisplayLabel(final boolean display_label) {
		mDisplayLabel = display_label;

	}

}
