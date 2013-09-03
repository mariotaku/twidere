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

package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ThemeUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

public class ThemeColorPreference extends ColorPickerPreference implements Constants {

	public ThemeColorPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ThemeColorPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final boolean dark_theme = ThemeUtils.isDarkTheme(context);
		final int def = ThemeUtils.getThemeColor(context);
		final String key = dark_theme ? PREFERENCE_KEY_DARK_THEME_COLOR : PREFERENCE_KEY_LIGHT_THEME_COLOR;
		setDefaultValue(def);
		setKey(key);
	}

	public static int getThemeColor(final Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean dark_theme = ThemeUtils.isDarkTheme(context);
		final int def = ThemeUtils.getThemeColor(context);
		final String key = dark_theme ? PREFERENCE_KEY_DARK_THEME_COLOR : PREFERENCE_KEY_LIGHT_THEME_COLOR;
		return prefs.getInt(key, def);
	}
}
