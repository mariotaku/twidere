package org.mariotaku.twidere.util;

import java.util.HashMap;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.preference.ThemeColorPreference;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

public class ThemeUtils implements Constants {

	private static final String THEME_NAME_TWIDERE = "twidere";
	private static final String THEME_NAME_DARK = "dark";
	private static final String THEME_NAME_LIGHT = "light";

	private static final HashMap<String, Integer> THEMES = new HashMap<String, Integer>();
	private static final HashMap<String, Integer> SWIPEBACK_THEMES = new HashMap<String, Integer>();
	private static final HashMap<String, Integer> DIALOG_THEMES = new HashMap<String, Integer>();
	private static final HashMap<String, Integer> COMPOSE_THEMES = new HashMap<String, Integer>();

	static {
		THEMES.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere);
		THEMES.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dark);
		THEMES.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light);
		DIALOG_THEMES.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere_Light_Dialog);
		DIALOG_THEMES.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dialog);
		DIALOG_THEMES.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light_Dialog);
		COMPOSE_THEMES.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere_Light_Compose);
		COMPOSE_THEMES.put(THEME_NAME_DARK, R.style.Theme_Twidere_Compose);
		COMPOSE_THEMES.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light_Compose);
		SWIPEBACK_THEMES.put(THEME_NAME_TWIDERE, R.style.Theme_SwipeBack);
		SWIPEBACK_THEMES.put(THEME_NAME_DARK, R.style.Theme_SwipeBack_Dark);
		SWIPEBACK_THEMES.put(THEME_NAME_LIGHT, R.style.Theme_SwipeBack_Light);
	}

	private ThemeUtils() {
		throw new AssertionError();
	}

	public static void applyBackground(final View view) {
		if (view == null) return;
		ThemeUtils.applyBackground(view, ThemeColorPreference.getThemeColor(view.getContext()));
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

	public static int getComposeThemeResource(final Context context) {
		return getComposeThemeResource(getThemeName(context));
	}

	public static int getComposeThemeResource(final String name) {
		final Integer res = COMPOSE_THEMES.get(name);
		return res != null ? res : R.style.Theme_Twidere_Compose;
	}

	public static int getDialogThemeResource(final Context context) {
		return getDialogThemeResource(getThemeName(context));
	}

	public static int getDialogThemeResource(final String name) {
		final Integer res = DIALOG_THEMES.get(name);
		return res != null ? res : R.style.Theme_Twidere_Dialog;
	}

	public static int getSwipeBackThemeResource(final Context context) {
		return getSwipeBackThemeResource(getThemeName(context));
	}

	public static int getSwipeBackThemeResource(final String name) {
		final Integer res = SWIPEBACK_THEMES.get(name);
		return res != null ? res : R.style.Theme_SwipeBack;
	}

	@SuppressLint("InlinedApi")
	public static int getThemeColor(final Context context) {
		if (context == null) return Color.TRANSPARENT;
		final int def = context.getResources().getColor(android.R.color.holo_blue_light);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return def;
		try {
			final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.colorActivatedHighlight });
			final int color = a.getColor(0, def);
			a.recycle();
			return color;
		} catch (final Exception e) {
			return def;
		}
	}

	public static String getThemeName(final Context context) {
		if (context == null) return THEME_NAME_TWIDERE;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref.getString(PREFERENCE_KEY_THEME, THEME_NAME_TWIDERE);
	}

	public static int getThemeResource(final Context context) {
		return getThemeResource(getThemeName(context));
	}

	public static int getThemeResource(final String name) {
		final Integer res = THEMES.get(name);
		return res != null ? res : R.style.Theme_Twidere;
	}

	public static boolean isDarkTheme(final Context context) {
		return isDarkTheme(getThemeResource(context));
	}

	public static boolean isDarkTheme(final int res) {
		switch (res) {
			case R.style.Theme_Twidere_Dark:
				return true;
		}
		return false;
	}

}
