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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.refreshnow.widget.RefreshNowProgressIndicator.IndicatorConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.theme.TwidereContextWrapper;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.iface.ICardItemView;

public class ThemeUtils implements Constants {

	private static final int[] ANIM_OPEN_STYLE_ATTRS = { android.R.attr.activityOpenEnterAnimation,
			android.R.attr.activityOpenExitAnimation };
	private static final int[] ANIM_CLOSE_STYLE_ATTRS = { android.R.attr.activityCloseEnterAnimation,
			android.R.attr.activityCloseExitAnimation };

	private ThemeUtils() {
		throw new AssertionError();
	}

	@Deprecated
	public static void applyActionBarBackground(final ActionBar actionBar, final Context context) {
		applyActionBarBackground(actionBar, context, true);
	}

	@Deprecated
	public static void applyActionBarBackground(final ActionBar actionBar, final Context context,
			final boolean applyAlpha) {
		if (actionBar == null || context == null) return;
		actionBar.setBackgroundDrawable(getActionBarBackground(context, applyAlpha));
		actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, applyAlpha));
		actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, applyAlpha));
	}

	public static void applyActionBarBackground(final ActionBar actionBar, final Context context, final int themeRes) {
		if (actionBar == null || context == null) return;
		actionBar.setBackgroundDrawable(getActionBarBackground(context, themeRes));
		actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, themeRes));
		actionBar.setStackedBackgroundDrawable(getActionBarStackedBackground(context, themeRes));
	}

	public static void applyBackground(final View view) {
		if (view == null) return;
		applyBackground(view, getUserThemeColor(view.getContext()));
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

	public static void applyThemeAlphaToDrawable(final Context context, final Drawable d) {
		if (context == null || d == null) return;
		d.setAlpha(getThemeAlpha(getThemeResource(context)));
	}

	public static void applyThemeBackgroundAlphaToDrawable(final Context context, final Drawable d) {
		if (context == null || d == null) return;
		d.setAlpha(getUserThemeBackgroundAlpha(context));
	}

	public static IndicatorConfig buildRefreshIndicatorConfig(final Context context) {
		final IndicatorConfig.Builder builder = new IndicatorConfig.Builder(context);
		final Resources res = context.getResources();
		final float width = 3 * res.getDisplayMetrics().density;
		final int themeColor = getUserThemeColor(context);
		builder.progressColor(themeColor);
		builder.indeterminateColor(themeColor);
		builder.progressStrokeWidth(width);
		builder.indeterminateStrokeWidth(width);
		return builder.build();
	}

	@Deprecated
	public static Drawable getActionBarBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.background },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, applyAlpha);
	}

	public static Drawable getActionBarBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.background },
				android.R.attr.actionBarStyle, themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
	}

	public static Context getActionBarContext(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.actionBarWidgetTheme });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) return context;
		return new ContextThemeWrapper(context, resId);
	}

	@Deprecated
	public static Drawable getActionBarSplitBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, applyAlpha);
	}

	public static Drawable getActionBarSplitBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
				android.R.attr.actionBarStyle, themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
	}

	@Deprecated
	public static Drawable getActionBarStackedBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundStacked },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, applyAlpha);
	}

	public static Drawable getActionBarStackedBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundStacked },
				android.R.attr.actionBarStyle, themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return applyActionBarDrawable(context, d, isTransparentBackground(themeRes));
	}

	public static int getActionIconColor(final Context context) {
		return getActionIconColor(getThemeResource(context));
	}

	public static int getActionIconColor(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Light:
			case R.style.Theme_Twidere_Light_SolidBackground:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_Compose:
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_Compose:
				return 0xc0333333;
		}
		return Color.WHITE;
	}

	public static Drawable getCardItemBackground(final Context context) {

		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.cardItemBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return d;
	}

	public static int getColorBackgroundCacheHint(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.colorBackgroundCacheHint });
		final int color = a.getColor(0, Color.TRANSPARENT);
		a.recycle();
		return color;
	}

	public static int getComposeThemeResource(final Context context) {
		return getComposeThemeResource(getThemeNameOption(context), getDarkActionBarOption(context));
	}

	public static int getComposeThemeResource(final String name, final boolean darkActionBar) {
		if (VALUE_THEME_NAME_TWIDERE.equals(name))
			return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar_Compose
					: R.style.Theme_Twidere_Colored_Compose;
		else if (VALUE_THEME_NAME_LIGHT.equals(name))
			return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar_Compose
					: R.style.Theme_Twidere_Light_Compose;
		else if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Compose;
		return R.style.Theme_Twidere_Colored_Compose;
	}

	public static boolean getDarkActionBarOption(final Context context) {
		if (context == null) return true;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref == null || pref.getBoolean(KEY_THEME_DARK_ACTIONBAR, true);
	}

	public static Context getDialogThemedContext(final Context context) {
		return new ContextThemeWrapper(context, getDialogThemeResource(context));
	}

	public static int getDialogThemeResource(final Context context) {
		return getDialogThemeResource(getThemeNameOption(context));
	}

	public static int getDialogThemeResource(final String name) {
		if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Dark_Dialog;
		return R.style.Theme_Twidere_Light_Dialog;
	}

	public static int getDrawerThemeResource(final Context context) {
		return getDrawerThemeResource(getThemeResource(context));
	}

	public static int getDrawerThemeResource(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_Transparent:
				return R.style.Theme_Twidere_Drawer_Dark_Transparent;
		}
		return R.style.Theme_Twidere_Drawer_Dark;
	}

	public static Drawable getImageHighlightDrawable(final Context context) {
		final Drawable d = getSelectableItemBackgroundDrawable(context);
		if (d != null) {
			d.setAlpha(0x80);
		}
		return d;
	}

	public static Drawable getListMenuOverflowButtonDrawable(final Context context) {
		final Resources res = getResources(context);
		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.listMenuOverflowButton });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null) return res.getDrawable(R.drawable.ic_list_menu_moreoverflow_normal_holo_light);
		return d;
	}

	public static int getNoDisplayThemeResource(final Context context) {
		if (context == null) return R.style.Theme_Twidere_Dark_NoDisplay;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String theme = pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE);
		if (VALUE_THEME_NAME_DARK.equals(theme)) return R.style.Theme_Twidere_Dark_NoDisplay;
		return R.style.Theme_Twidere_Light_NoDisplay;
	}

	public static Resources getResources(final Context context) {
		if (context instanceof IThemedActivity) {
			final Resources defRes = ((IThemedActivity) context).getDefaultResources();
			return defRes;
		}
		return context.getResources();
	}

	public static Drawable getSelectableItemBackgroundDrawable(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground });
		try {
			return a.getDrawable(0);
		} finally {
			a.recycle();
		}
	}

	public static int getSettingsThemeResource(final Context context) {
		return getSettingsThemeResource(getThemeNameOption(context), getDarkActionBarOption(context));
	}

	public static int getSettingsThemeResource(final String name, final boolean darkActionBar) {
		if (VALUE_THEME_NAME_TWIDERE.equals(name) || VALUE_THEME_NAME_LIGHT.equals(name))
			return darkActionBar ? R.style.Theme_Twidere_Settings_Light_DarkActionBar
					: R.style.Theme_Twidere_Settings_Light;
		else if (VALUE_THEME_NAME_DARK.equals(name)) return R.style.Theme_Twidere_Settings_Dark;
		return R.style.Theme_Twidere_Settings_Light_DarkActionBar;
	}

	public static int getTextAppearanceLarge(final Context context) {
		// final Context wrapped = getThemedContext(context,
		// getResources(context));
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textAppearanceLarge });
		final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo_Large);
		a.recycle();
		return textAppearance;
	}

	public static int getTextColorPrimary(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textColorPrimary });
		try {
			return a.getColor(0, Color.TRANSPARENT);
		} finally {
			a.recycle();
		}
	}

	public static int getTextColorSecondary(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
		try {
			return a.getColor(0, Color.TRANSPARENT);
		} finally {
			a.recycle();
		}
	}

	public static int getThemeAlpha(final Context context) {
		return getThemeAlpha(getThemeResource(context));
	}

	public static int getThemeAlpha(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
				return 0xa0;
		}
		return 0xff;
	}

	public static String getThemeBackgroundOption(final Context context) {
		if (context == null) return VALUE_THEME_BACKGROUND_DEFAULT;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (pref == null) return VALUE_THEME_BACKGROUND_DEFAULT;
		return pref.getString(KEY_THEME_BACKGROUND, VALUE_THEME_BACKGROUND_DEFAULT);
	}

	public static int getThemeColor(final Context context) {
		final Resources res = getResources(context);
		final Context wrapped = getThemedContext(context, res);
		final TypedArray a = wrapped.obtainStyledAttributes(new int[] { android.R.attr.colorActivatedHighlight });
		try {
			return a.getColor(0, res.getColor(android.R.color.holo_blue_light));
		} finally {
			a.recycle();
		}
	}

	public static Context getThemedContext(final Context context) {
		return new TwidereContextWrapper(context, getResources(context));
	}

	public static Context getThemedContext(final Context context, final Resources res) {
		return new TwidereContextWrapper(context, res);
	}

	public static String getThemeFontFamily(final Context context) {
		if (context == null) return VALUE_THEME_FONT_FAMILY_REGULAR;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String fontFamily = pref.getString(KEY_THEME_FONT_FAMILY, VALUE_THEME_FONT_FAMILY_REGULAR);
		if (!TextUtils.isEmpty(fontFamily)) return fontFamily;
		return VALUE_THEME_FONT_FAMILY_REGULAR;
	}

	public static String getThemeNameOption(final Context context) {
		if (context == null) return VALUE_THEME_NAME_TWIDERE;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref != null ? pref.getString(KEY_THEME, VALUE_THEME_NAME_TWIDERE) : VALUE_THEME_NAME_TWIDERE;
	}

	public static int getThemeResource(final Context context) {
		return getThemeResource(getThemeNameOption(context), getThemeBackgroundOption(context),
				getDarkActionBarOption(context));
	}

	public static int getThemeResource(final String name, final String background, final boolean darkActionBar) {
		if (VALUE_THEME_NAME_TWIDERE.equals(name)) {
			if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground
						: R.style.Theme_Twidere_Colored_SolidBackground;
			else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar_Transparent
						: R.style.Theme_Twidere_Colored_Transparent;
			return darkActionBar ? R.style.Theme_Twidere_Colored_DarkActionBar : R.style.Theme_Twidere_Colored;
		} else if (VALUE_THEME_NAME_LIGHT.equals(name)) {
			if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground
						: R.style.Theme_Twidere_Light_SolidBackground;
			else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
				return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar_Transparent
						: R.style.Theme_Twidere_Light_Transparent;
			return darkActionBar ? R.style.Theme_Twidere_Light_DarkActionBar : R.style.Theme_Twidere_Light;

		} else if (VALUE_THEME_NAME_DARK.equals(name)) {
			if (VALUE_THEME_BACKGROUND_SOLID.equals(background))
				return R.style.Theme_Twidere_Dark_SolidBackground;
			else if (VALUE_THEME_BACKGROUND_TRANSPARENT.equals(background))
				return R.style.Theme_Twidere_Dark_Transparent;
			return R.style.Theme_Twidere_Dark;
		}
		return R.style.Theme_Twidere_Colored_DarkActionBar;
	}

	public static int getTitleTextAppearance(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.titleTextStyle },
				android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
		final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo);
		a.recycle();
		return textAppearance;
	}

	public static int getUserHighlightColor(final Context context) {
		final int color = getUserLinkTextColor(context);
		final int red = Color.red(color), green = Color.green(color), blue = Color.blue(color);
		return Color.argb(0x66, red, green, blue);
	}

	public static int getUserLinkTextColor(final Context context) {
		if (context == null) return new TextPaint().linkColor;
		final int themeColor = getUserThemeColor(context);
		final float[] hsv = new float[3];
		Color.colorToHSV(themeColor, hsv);
		if (isDarkTheme(context)) {
			hsv[2] = MathUtils.clamp(hsv[2], 1, 0.5f);
		} else {
			hsv[2] = MathUtils.clamp(hsv[2], 0.1f, 0.75f);
		}
		return Color.HSVToColor(hsv);
	}

	public static int getUserThemeBackgroundAlpha(final Context context) {
		if (context == null) return DEFAULT_THEME_BACKGROUND_ALPHA;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref.getInt(KEY_THEME_BACKGROUND_ALPHA, DEFAULT_THEME_BACKGROUND_ALPHA);
	}

	public static int getUserThemeColor(final Context context) {
		if (context == null) return Color.TRANSPARENT;
		final Resources res = getResources(context);
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int def = res.getColor(android.R.color.holo_blue_light);
		return pref.getInt(KEY_THEME_COLOR, def);
	}

	public static Typeface getUserTypeface(final Context context, final Typeface defTypeface) {
		if (context == null) return Typeface.DEFAULT;
		final int fontStyle = defTypeface != null ? defTypeface.getStyle() : Typeface.NORMAL;
		final String fontFamily = getThemeFontFamily(context);
		final Typeface tf = Typeface.create(fontFamily, fontStyle);
		if (tf != null) return tf;
		return Typeface.create(Typeface.DEFAULT, fontStyle);
	}

	public static int getViewerThemeResource(final Context context) {
		final boolean isTransparent = VALUE_THEME_BACKGROUND_TRANSPARENT.equals(getThemeBackgroundOption(context));
		return isTransparent ? R.style.Theme_Twidere_Viewer_Transparent : R.style.Theme_Twidere_Viewer;
	}

	public static Drawable getWindowBackground(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (isTransparentBackground(context)) {
			applyThemeBackgroundAlphaToDrawable(context, d);
		}
		return d;
	}

	public static Drawable getWindowBackground(final Context context, final int themeRes) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.windowBackground }, 0,
				themeRes);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (isTransparentBackground(themeRes)) {
			applyThemeBackgroundAlphaToDrawable(context, d);
		}
		return d;
	}

	public static Drawable getWindowContentOverlay(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowContentOverlay });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return d;
	}

	public static Drawable getWindowContentOverlayForCompose(final Context context) {
		final int themeRes = getThemeResource(context);
		return getWindowContentOverlay(new ContextThemeWrapper(context, themeRes));
	}

	public static boolean isColoredActionBar(final Context context) {
		return isColoredActionBar(getThemeResource(context));
	}

	public static boolean isColoredActionBar(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar:
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose:
				return true;
		}
		return false;
	}

	public static boolean isDarkTheme(final Context context) {
		return isDarkTheme(getThemeResource(context));
	}

	public static boolean isDarkTheme(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Dark:
			case R.style.Theme_Twidere_Dark_SolidBackground:
			case R.style.Theme_Twidere_Dark_Dialog:
			case R.style.Theme_Twidere_Dark_Compose:
			case R.style.Theme_Twidere_Dark_Transparent:
				return true;
		}
		return false;
	}

	public static boolean isFloatingWindow(final Context context) {

		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowIsFloating });
		final boolean b = a.getBoolean(0, false);
		a.recycle();
		return b;
	}

	public static boolean isLightActionBar(final Context context) {
		return isLightActionBar(getThemeResource(context));
	}

	public static boolean isLightActionBar(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Light:
			case R.style.Theme_Twidere_Light_SolidBackground:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_Compose:
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_Compose:
				return true;
		}
		return false;
	}

	public static boolean isSolidBackground(final Context context) {
		return VALUE_THEME_BACKGROUND_SOLID.equals(getThemeBackgroundOption(context));
	}

	public static boolean isTransparentBackground(final Context context) {
		return isTransparentBackground(getThemeResource(context));
	}

	public static boolean isTransparentBackground(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
				return true;
		}
		return false;
	}

	public static void overrideActivityCloseAnimation(final Activity activity) {
		TypedArray a = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
		final int windowAnimationStyleResId = a.getResourceId(0, 0);
		a.recycle();
		// Now retrieve the resource ids of the actual animations used in the
		// animation style pointed to by
		// the window animation resource id.
		a = activity.obtainStyledAttributes(windowAnimationStyleResId, ANIM_CLOSE_STYLE_ATTRS);
		final int activityCloseEnterAnimation = a.getResourceId(0, 0);
		final int activityCloseExitAnimation = a.getResourceId(1, 0);
		a.recycle();
		activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}

	public static void overrideActivityOpenAnimation(final Activity activity) {

		TypedArray a = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
		final int windowAnimationStyleResId = a.getResourceId(0, 0);
		a.recycle();
		// Now retrieve the resource ids of the actual animations used in the
		// animation style pointed to by
		// the window animation resource id.
		a = activity.obtainStyledAttributes(windowAnimationStyleResId, ANIM_OPEN_STYLE_ATTRS);
		final int activityOpenEnterAnimation = a.getResourceId(0, 0);
		final int activityOpenExitAnimation = a.getResourceId(1, 0);
		a.recycle();
		activity.overridePendingTransition(activityOpenEnterAnimation, activityOpenExitAnimation);
	}

	public static void overrideNormalActivityCloseAnimation(final Activity activity) {

		final TypedArray a = activity
				.obtainStyledAttributes(android.R.style.Animation_Activity, ANIM_CLOSE_STYLE_ATTRS);
		final int activityCloseEnterAnimation = a.getResourceId(0, 0);
		final int activityCloseExitAnimation = a.getResourceId(1, 0);
		a.recycle();
		activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}

	public static void setPreviewView(final Context context, final View view, final int themeRes) {
		final ContextThemeWrapper theme = new ContextThemeWrapper(context, themeRes);
		final View windowBackgroundView = view.findViewById(R.id.theme_preview_window_background);
		final View windowContentOverlayView = view.findViewById(R.id.theme_preview_window_content_overlay);
		final View actionBarView = view.findViewById(R.id.actionbar);
		final TextView actionBarTitleView = (TextView) view.findViewById(R.id.actionbar_title);
		final View actionBarSplitView = view.findViewById(R.id.actionbar_split);
		final View statusContentView = view.findViewById(R.id.theme_preview_status_content);

		final int defaultTextSize = getDefaultTextSize(context);
		final int textColorPrimary = getTextColorPrimary(theme);
		final int textColorSecondary = getTextColorSecondary(theme);
		final int titleTextAppearance = getTitleTextAppearance(theme);

		ViewAccessor.setBackground(windowBackgroundView, getWindowBackground(theme));
		ViewAccessor.setBackground(windowContentOverlayView, getWindowContentOverlay(theme));
		ViewAccessor.setBackground(actionBarView, getActionBarBackground(theme, true));
		ViewAccessor.setBackground(actionBarSplitView, getActionBarSplitBackground(theme, true));

		actionBarTitleView.setTextAppearance(theme, titleTextAppearance);
		if (statusContentView != null) {
			ViewAccessor.setBackground(statusContentView, getWindowBackground(theme));

			final ICardItemView cardView = (ICardItemView) statusContentView.findViewById(R.id.card);
			final View profileView = statusContentView.findViewById(R.id.profile);
			final ImageView profileImageView = (ImageView) statusContentView.findViewById(R.id.profile_image);
			final TextView nameView = (TextView) statusContentView.findViewById(R.id.name);
			final TextView screenNameView = (TextView) statusContentView.findViewById(R.id.screen_name);
			final TextView textView = (TextView) statusContentView.findViewById(R.id.text);
			final TextView timeSourceView = (TextView) statusContentView.findViewById(R.id.time_source);
			final TextView retweetView = (TextView) statusContentView.findViewById(R.id.retweet_view);
			final TextView repliesView = (TextView) statusContentView.findViewById(R.id.replies_view);

			cardView.setItemSelector(null);
			cardView.setItemBackground(getCardItemBackground(theme));

			nameView.setTextColor(textColorPrimary);
			screenNameView.setTextColor(textColorSecondary);
			textView.setTextColor(textColorPrimary);
			timeSourceView.setTextColor(textColorSecondary);
			retweetView.setTextColor(textColorSecondary);
			repliesView.setTextColor(textColorSecondary);

			nameView.setTextSize(defaultTextSize * 1.25f);
			textView.setTextSize(defaultTextSize * 1.25f);
			screenNameView.setTextSize(defaultTextSize * 0.85f);
			timeSourceView.setTextSize(defaultTextSize * 0.85f);
			retweetView.setTextSize(defaultTextSize * 0.85f);
			repliesView.setTextSize(defaultTextSize * 0.85f);

			profileView.setBackgroundResource(0);
			retweetView.setBackgroundResource(0);
			repliesView.setBackgroundResource(0);
			textView.setTextIsSelectable(false);

			profileImageView.setImageResource(R.drawable.ic_launcher);
			nameView.setText(TWIDERE_PREVIEW_NAME);
			screenNameView.setText("@" + TWIDERE_PREVIEW_SCREEN_NAME);
			textView.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));

			final String time = formatToLongTimeString(context, System.currentTimeMillis());
			timeSourceView.setText(toPlainText(context.getString(R.string.time_source, time, TWIDERE_PREVIEW_SOURCE)));
		}
	}

	public static boolean shouldApplyColorFilter(final Context context) {
		return shouldApplyColorFilter(getThemeResource(context));
	}

	public static boolean shouldApplyColorFilter(final int res) {
		switch (res) {
			case R.style.Theme_Twidere_Colored:
			case R.style.Theme_Twidere_Colored_SolidBackground:
			case R.style.Theme_Twidere_Colored_Compose:
			case R.style.Theme_Twidere_Colored_Transparent:
			case R.style.Theme_Twidere_Colored_DarkActionBar:
			case R.style.Theme_Twidere_Colored_DarkActionBar_SolidBackground:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Compose:
			case R.style.Theme_Twidere_Colored_DarkActionBar_Transparent:
				return false;
		}
		return true;
	}

	public static boolean shouldApplyColorFilterToTabIcons(final Context context) {
		return shouldApplyColorFilterToTabIcons(getThemeResource(context));
	}

	public static boolean shouldApplyColorFilterToTabIcons(final int res) {
		return isLightActionBar(res);
	}

	private static Drawable applyActionBarDrawable(final Context context, final Drawable d, final boolean applyAlpha) {
		if (d == null) return null;
		d.mutate();
		if (d instanceof LayerDrawable) {
			final Drawable colorLayer = ((LayerDrawable) d).findDrawableByLayerId(R.id.color_layer);
			if (colorLayer != null) {
				final int color = getUserThemeColor(context);
				colorLayer.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			}
		}
		if (applyAlpha) {
			d.setAlpha(getThemeAlpha(context));
		}
		return d;
	}

}
