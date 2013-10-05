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

package org.mariotaku.twidere.activity;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.ViewAccessor;

@SuppressLint("Registered")
public class BaseThemedSupportSwipeBackActivity extends BaseSupportActivity {

	private SwipeBackLayout mSwipeBackLayout;

	private boolean mOverrideExitAniamtion = true;

	private boolean mIsFinishing;

	@Override
	public View findViewById(final int id) {
		final View v = super.findViewById(id);
		if (v != null) return v;
		return mSwipeBackLayout != null ? mSwipeBackLayout.findViewById(id) : null;
	}

	@Override
	public void finish() {
		if (mOverrideExitAniamtion && !mIsFinishing) {
			scrollToFinishActivity();
			mIsFinishing = true;
			return;
		}
		mIsFinishing = false;
		super.finish();
	}

	public SwipeBackLayout getSwipeBackLayout() {
		return mSwipeBackLayout;
	}

	public boolean isSwiping() {
		return mSwipeBackLayout != null && mSwipeBackLayout.isSwiping();
	}

	/**
	 * Scroll out contentView and finish the activity
	 */
	public void scrollToFinishActivity() {
		if (mSwipeBackLayout == null) return;
		mSwipeBackLayout.scrollToFinishActivity();
	}

	/**
	 * Override Exit Animation
	 * 
	 * @param override
	 */
	public void setOverrideExitAniamtion(final boolean override) {
		mOverrideExitAniamtion = override;
	}

	public void setSwipeBackEnable(final boolean enable) {
		if (mSwipeBackLayout == null) return;
		mSwipeBackLayout.setEnableGesture(enable);
	}

	public void setSwipeListener(final SwipeListener listener) {
		if (mSwipeBackLayout == null) return;
		mSwipeBackLayout.setSwipeListener(listener);
	}

	@Override
	protected int getThemeResource() {
		return ThemeUtils.getSwipeBackThemeResource(this);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Window w = getWindow();
		w.setBackgroundDrawable(new ColorDrawable(0));
		ViewAccessor.setBackground(w.getDecorView(), null);
		mSwipeBackLayout = new SwipeBackLayout(this);
	}

	@Override
	protected void onPostCreate(final Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (mSwipeBackLayout != null) {
			mSwipeBackLayout.attachToActivity(this);
		}
	}
}
