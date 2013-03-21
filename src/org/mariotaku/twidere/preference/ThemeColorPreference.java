package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

public class ThemeColorPreference extends ColorPickerPreference implements Constants {

	public ThemeColorPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ThemeColorPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = context.getResources();
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean dark_theme = prefs.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		final int def = res.getColor(dark_theme ? R.color.holo_blue_dark : R.color.holo_blue_light);
		final String key = dark_theme ? PREFERENCE_KEY_DARK_THEME_COLOR : PREFERENCE_KEY_LIGHT_THEME_COLOR;
		setDefaultValue(def);
		setKey(key);
	}

	public static void applyBackground(final View view, final int color) {
		if (view == null) return;
		final Drawable bg = view.getBackground();
		if (bg == null) return;
		bg.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		view.invalidate();
	}

	public static int getThemeColor(final Context context) {
		final Resources res = context.getResources();
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean dark_theme = prefs.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		final int def = res.getColor(dark_theme ? R.color.holo_blue_dark : R.color.holo_blue_light);
		final String key = dark_theme ? PREFERENCE_KEY_DARK_THEME_COLOR : PREFERENCE_KEY_LIGHT_THEME_COLOR;
		return prefs.getInt(key, def);
	}
}
