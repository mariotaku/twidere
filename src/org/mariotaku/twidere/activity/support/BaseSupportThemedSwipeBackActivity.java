/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity.support;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import org.mariotaku.twidere.util.ThemeUtils;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityBase;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

@SuppressLint("Registered")
public class BaseSupportThemedSwipeBackActivity extends BaseSupportActivity implements SwipeBackActivityBase {

	private SwipeBackActivityHelper mHelper;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v == null && mHelper != null) return mHelper.findViewById(id);
		return v;
	}

	@Override
	public SwipeBackLayout getSwipeBackLayout() {
		return mHelper.getSwipeBackLayout();
	}

	public boolean isSwiping() {
		final SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
		return swipeBackLayout != null && swipeBackLayout.isSwiping();
	}

	@Override
	public void scrollToFinishActivity() {
		getSwipeBackLayout().scrollToFinishActivity();
	}

	@Override
	public void setSwipeBackEnable(final boolean enable) {
		getSwipeBackLayout().setEnableGesture(enable);
	}

	public void setSwipeListener(final SwipeListener listener) {
		final SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
		if (swipeBackLayout == null) return;
		swipeBackLayout.setSwipeListener(listener);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper = new SwipeBackActivityHelper(this);
		mHelper.onActivtyCreate();
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mHelper.onPostCreate();
	}

	@Override
	protected int getThemeResource() {
		return ThemeUtils.getSwipeBackThemeResource(this);
	}
}
