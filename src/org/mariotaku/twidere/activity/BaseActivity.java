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

import org.mariotaku.actionbarcompat.ActionBarFragmentActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ActivityThemeChangeInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Window;
import android.view.WindowManager;

public class BaseActivity extends ActionBarFragmentActivity implements Constants, ActivityThemeChangeInterface {

	private boolean mIsDarkTheme, mIsSolidColorBackground, mHardwareAccelerated;

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	public boolean isDarkTheme() {
		return mIsDarkTheme;
	}

	public boolean isHardwareAccelerationChanged() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return false;
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean hardware_acceleration = preferences.getBoolean(PREFERENCE_KEY_HARDWARE_ACCELERATION, false);
		return mHardwareAccelerated != hardware_acceleration;
	}

	public boolean isSolidColorBackground() {
		return mIsSolidColorBackground;
	}

	@Override
	public boolean isThemeChanged() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		final boolean solid_color_background = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		return is_dark_theme != mIsDarkTheme || solid_color_background != mIsSolidColorBackground;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setHardwareAcceleration();
		setTheme();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isThemeChanged() || isHardwareAccelerationChanged()) {
			restart();
		}
	}

	public void restart() {
		boolean show_anim = false;
		try {
			final float transition_animation = Settings.System.getFloat(getContentResolver(),
					Settings.System.TRANSITION_ANIMATION_SCALE);
			show_anim = transition_animation > 0.0;
		} catch (final SettingNotFoundException e) {
			e.printStackTrace();
		}
		restartActivity(this, show_anim);
	}

	public void setHardwareAcceleration() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			final boolean hardware_acceleration = mHardwareAccelerated = preferences.getBoolean(
					PREFERENCE_KEY_HARDWARE_ACCELERATION, false);
			final Window w = getWindow();
			if (hardware_acceleration) {
				w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
						WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
			}
		}
	}

	@Override
	public void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		mIsDarkTheme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		mIsSolidColorBackground = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		setTheme(is_dark_theme ? R.style.Theme_Twidere : R.style.Theme_Twidere_Light);
		if (mIsSolidColorBackground) {
			getWindow().setBackgroundDrawableResource(is_dark_theme ? android.R.color.black : android.R.color.white);
		}
	}
}
