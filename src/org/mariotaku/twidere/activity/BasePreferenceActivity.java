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
import org.mariotaku.twidere.util.ThemeUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

class BasePreferenceActivity extends PreferenceActivity implements Constants, IThemedActivity {

	private int mThemeRes;
	private boolean mIsSolidColorBackground;

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	public boolean isSolidColorBackground() {
		return mIsSolidColorBackground;
	}

	public boolean isThemeChanged() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int theme_res = ThemeUtils.getThemeResource(this);
		final boolean solid_color_background = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		return theme_res != mThemeRes || solid_color_background != mIsSolidColorBackground;
	}

	public void restart() {
		restartActivity(this);
	}

	public void setActionBarBackground() {
		// final ActionBar ab = getActionBar();
		// final TypedArray a = obtainStyledAttributes(new int[] {
		// R.attr.actionBarBackground });
		// final int color = getThemeColor(this);
		// final Drawable d = a.getDrawable(0);
		// if (d == null) return;
		// if (mIsDarkTheme) {
		// final Drawable mutated = d.mutate();
		// mutated.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		// ab.setBackgroundDrawable(mutated);
		// } else if (d instanceof LayerDrawable) {
		// final LayerDrawable ld = (LayerDrawable) d.mutate();
		// ld.findDrawableByLayerId(R.id.color_layer).setColorFilter(color,
		// PorterDuff.Mode.MULTIPLY);
		// ab.setBackgroundDrawable(ld);
		// }
	}

	protected int getDarkThemeRes() {
		return R.style.Theme_Twidere;
	}

	protected int getLightThemeRes() {
		return R.style.Theme_Twidere_Light;
	}

	protected boolean isDarkTheme() {
		return ThemeUtils.isDarkTheme(mThemeRes);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
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

	protected boolean shouldSetBackground() {
		return true;
	}

	private void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mThemeRes = ThemeUtils.getThemeResource(this);
		final boolean is_dark_theme = ThemeUtils.isDarkTheme(mThemeRes);
		mIsSolidColorBackground = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		setTheme(mThemeRes);
		if (mIsSolidColorBackground && shouldSetBackground()) {
			getWindow().setBackgroundDrawableResource(is_dark_theme ? android.R.color.black : android.R.color.white);
		}
	}

}
