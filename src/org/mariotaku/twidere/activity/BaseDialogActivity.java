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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.app.TwidereApplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("Registered")
class BaseDialogActivity extends FragmentActivity implements Constants, IThemedActivity {

	private boolean mIsDarkTheme, mHardwareAccelerated;

	private boolean mInstanceStateSaved;

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	public boolean isThemeChanged() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		return is_dark_theme != mIsDarkTheme;
	}

	public void restart() {
		restartActivity(this);
	}

	protected int getDarkThemeRes() {
		return R.style.Theme_Twidere_Dialog;
	}

	protected int getLightThemeRes() {
		return R.style.Theme_Twidere_Light_Dialog;
	}

	protected boolean isDarkTheme() {
		return mIsDarkTheme;
	}

	protected boolean isHardwareAccelerationChanged() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return false;
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean hardware_acceleration = preferences.getBoolean(PREFERENCE_KEY_HARDWARE_ACCELERATION,
				PREFERENCE_DEFAULT_HARDWARE_ACCELERATION);
		return mHardwareAccelerated != hardware_acceleration;
	}

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setHardwareAcceleration();
		setTheme();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mInstanceStateSaved = false;
		if (isThemeChanged() || isHardwareAccelerationChanged()) {
			restart();
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mInstanceStateSaved = true;
		super.onSaveInstanceState(outState);
	}

	private void setHardwareAcceleration() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return;
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mHardwareAccelerated = preferences.getBoolean(PREFERENCE_KEY_HARDWARE_ACCELERATION,
				PREFERENCE_DEFAULT_HARDWARE_ACCELERATION);
		final Window w = getWindow();
		if (mHardwareAccelerated) {
			w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
	}

	private void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		mIsDarkTheme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		setTheme(is_dark_theme ? getDarkThemeRes() : getLightThemeRes());
	}

}
