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

import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ThemeUtils;

class BasePreferenceActivity extends PreferenceActivity implements Constants, IThemedActivity {

	private int mCurrentThemeResource;

	@Override
	public void finish() {
		super.finish();
		if (shouldOverrideActivityAnimation()) {
			ThemeUtils.overrideActivityCloseAnimation(this);
		}
	}

	@Override
	public final int getCurrentThemeResource() {
		return mCurrentThemeResource;
	}

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return true;
	}

	protected int getThemeResource() {
		return ThemeUtils.getThemeResource(this);
	}

	protected final boolean isThemeChanged() {
		return getThemeResource() != mCurrentThemeResource;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
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
		if (shouldRestartWhenThemeChanged() && isThemeChanged()) {
			restart();
		}
	}

	protected final void restart() {
		restartActivity(this);
	}

	protected boolean shouldRestartWhenThemeChanged() {
		return true;
	}

	private final void setActionBarBackground() {
		ThemeUtils.applyActionBarBackground(getActionBar(), this);
	}

	private final void setTheme() {
		mCurrentThemeResource = getThemeResource();
		setTheme(mCurrentThemeResource);
	}

}
