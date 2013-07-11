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
import org.mariotaku.twidere.util.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ThemeColorPreference extends ColorPickerPreference implements Constants {

	public ThemeColorPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ThemeColorPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean dark_theme = prefs.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		final int def = Utils.getThemeColor(context);
		final String key = dark_theme ? PREFERENCE_KEY_DARK_THEME_COLOR : PREFERENCE_KEY_LIGHT_THEME_COLOR;
		setDefaultValue(def);
		setKey(key);
	}

	public static void applyBackground(final View view) {
		if (view == null) return;
		applyBackground(view, getThemeColor(view.getContext()));
	}

	public static void applyBackground(final View view, final int color) {
		if (view == null) return;
		try {
			final Drawable bg = view.getBackground();
			if (bg == null) return;
			final Drawable mutated = bg.mutate();
			if (mutated == null) return;
			mutated.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			view.invalidate();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static int getThemeColor(final Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean dark_theme = prefs.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		final int def = Utils.getThemeColor(context);
		final String key = dark_theme ? PREFERENCE_KEY_DARK_THEME_COLOR : PREFERENCE_KEY_LIGHT_THEME_COLOR;
		return prefs.getInt(key, def);
	}
}
