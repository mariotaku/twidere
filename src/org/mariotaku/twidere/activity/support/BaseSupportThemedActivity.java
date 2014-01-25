/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import static org.mariotaku.twidere.util.Utils.restartActivity;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;

import com.negusoft.holoaccent.AccentHelper;

import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.theme.TwidereAccentHelper;
import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.StrictModeUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

public abstract class BaseSupportThemedActivity extends FragmentActivity implements IThemedActivity {

	private int mCurrentThemeResource, mCurrentThemeColor, mCurrentThemeBackgroundAlpha;

	private String mCurrentThemeFontFamily;

	private AccentHelper mAccentHelper;

	@Override
	public void finish() {
		super.finish();
		overrideCloseAnimationIfNeeded();
	}

	@Override
	public final int getCurrentThemeResourceId() {
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
	public int getThemeBackgroundAlpha() {
		return ThemeUtils.isTransparentBackground(this) ? ThemeUtils.getUserThemeBackgroundAlpha(this) : 0xff;
	}

	@Override
	public abstract int getThemeColor();

	@Override
	public final Resources getThemedResources() {
		if (mAccentHelper == null) {
			mAccentHelper = new TwidereAccentHelper(ThemeUtils.getUserThemeColor(this));
		}
		return mAccentHelper.getResources(this, super.getResources());
	}

	@Override
	public String getThemeFontFamily() {
		return ThemeUtils.getThemeFontFamily(this);
	}

	@Override
	public abstract int getThemeResourceId();

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

	protected boolean isThemeChanged() {
		return getThemeResourceId() != mCurrentThemeResource || getThemeColor() != mCurrentThemeColor
				|| !CompareUtils.objectEquals(getThemeFontFamily(), mCurrentThemeFontFamily)
				|| getThemeBackgroundAlpha() != mCurrentThemeBackgroundAlpha;
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

	protected boolean shouldSetWindowBackground() {
		return true;
	}

	private final void setActionBarBackground() {
		ThemeUtils.applyActionBarBackground(getActionBar(), this, mCurrentThemeResource);
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResourceId();
		mCurrentThemeColor = getThemeColor();
		mCurrentThemeFontFamily = getThemeFontFamily();
		mCurrentThemeBackgroundAlpha = getThemeBackgroundAlpha();
		setTheme(mCurrentThemeResource);
		if (shouldSetWindowBackground() && ThemeUtils.isTransparentBackground(mCurrentThemeResource)) {
			getWindow().setBackgroundDrawable(ThemeUtils.getWindowBackground(this));
		}
	}
}
