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

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

public abstract class BaseThemedActivity extends Activity implements IThemedActivity {

	private int mCurrentThemeResource, mCurrentThemeColor;

	@Override
	public void finish() {
		super.finish();
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public final int getCurrentThemeResource() {
		return mCurrentThemeResource;
	}

	@Override
	public final Resources getDefaultResources() {
		return super.getResources();
	}

	@Override
	public Resources getResources() {
		return getThemedResources();
	}

	@Override
	public abstract int getThemeColor();

	@Override
	public final Resources getThemedResources() {
		return super.getResources();
	}

	@Override
	public abstract int getThemeResource();

	@Override
	public void navigateUpFromSameTask() {
		NavUtils.navigateUpFromSameTask(this);
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public void overrideCloseAnimationIfNeeded() {
		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityCloseAnimation(this);
		} else {
			ThemeUtils.overrideNormalActivityCloseAnimation(this);
		}
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return true;
	}

	protected final boolean isThemeChanged() {
		return getThemeResource() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (Utils.isDebugBuild()) {
			StrictModeUtils.detectAllVmPolicy();
			StrictModeUtils.detectAllThreadPolicy();
		}

		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityOpenAnimation(this);
		}
		setTheme();
		super.onCreate(savedInstanceState);
		setActionBarBackground();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isThemeChanged()) {
			restart();
		}
	}

	protected final void restart() {
		restartActivity(this);
	}

	private final void setActionBarBackground() {
		ThemeUtils.applyActionBarBackground(getActionBar(), this);
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResource();
		mCurrentThemeColor = getThemeColor();
		setTheme(mCurrentThemeResource);
	}
}