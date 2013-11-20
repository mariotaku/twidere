package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.CardItemLinearLayout;
import org.mariotaku.twidere.view.ForegroundImageView;
import org.mariotaku.twidere.view.ShortTimeView;

import java.util.HashMap;

public class ThemeUtils implements Constants {

	private static final int[] ANIM_OPEN_STYLE_ATTRS = { android.R.attr.activityOpenEnterAnimation,
			android.R.attr.activityOpenExitAnimation };
	private static final int[] ANIM_CLOSE_STYLE_ATTRS = { android.R.attr.activityCloseEnterAnimation,
			android.R.attr.activityCloseExitAnimation };

	private static final String THEME_BACKGROUND_DEFAULT = "default";
	private static final String THEME_BACKGROUND_SOLID = "solid";
	private static final String THEME_BACKGROUND_TRANSPARENT = "transparent";

	private static final String THEME_NAME_TWIDERE = "twidere";
	private static final String THEME_NAME_DARK = "dark";
	private static final String THEME_NAME_LIGHT = "light";
	private static final String THEME_NAME_LIGHT_DARKACTIONBAR = "light_darkactionbar";

	private static final HashMap<String, ThemeMap> THEME_MAPS = new HashMap<String, ThemeMap>();
	private static final ThemeMap THEMES = new ThemeMap();
	private static final ThemeMap THEMES_SOLID = new ThemeMap();
	private static final ThemeMap THEMES_TRANSPARENT = new ThemeMap();
	private static final ThemeMap THEMES_DIALOG = new ThemeMap();
	private static final ThemeMap THEMES_COMPOSE = new ThemeMap();

	static {
		THEMES.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere);
		THEMES.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dark);
		THEMES.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light);
		THEMES.put(THEME_NAME_LIGHT_DARKACTIONBAR, R.style.Theme_Twidere_Light_DarkActionBar);
		THEMES_SOLID.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere_SolidBackground);
		THEMES_SOLID.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dark_SolidBackground);
		THEMES_SOLID.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light_SolidBackground);
		THEMES_SOLID.put(THEME_NAME_LIGHT_DARKACTIONBAR, R.style.Theme_Twidere_Light_DarkActionBar_SolidBackground);
		THEMES_TRANSPARENT.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere_Transparent);
		THEMES_TRANSPARENT.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dark_Transparent);
		THEMES_TRANSPARENT.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light_Transparent);
		THEMES_TRANSPARENT.put(THEME_NAME_LIGHT_DARKACTIONBAR, R.style.Theme_Twidere_Light_DarkActionBar_Transparent);
		THEMES_DIALOG.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere_Light_Dialog);
		THEMES_DIALOG.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dark_Dialog);
		THEMES_DIALOG.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light_Dialog);
		THEMES_DIALOG.put(THEME_NAME_LIGHT_DARKACTIONBAR, R.style.Theme_Twidere_Light_Dialog);
		THEMES_COMPOSE.put(THEME_NAME_TWIDERE, R.style.Theme_Twidere_Compose);
		THEMES_COMPOSE.put(THEME_NAME_DARK, R.style.Theme_Twidere_Dark_Compose);
		THEMES_COMPOSE.put(THEME_NAME_LIGHT, R.style.Theme_Twidere_Light_Compose);
		THEMES_COMPOSE.put(THEME_NAME_LIGHT_DARKACTIONBAR, R.style.Theme_Twidere_Light_DarkActionBar_Compose);

		THEME_MAPS.put(THEME_BACKGROUND_DEFAULT, THEMES);
		THEME_MAPS.put(THEME_BACKGROUND_SOLID, THEMES_SOLID);
		THEME_MAPS.put(THEME_BACKGROUND_TRANSPARENT, THEMES_TRANSPARENT);
	}

	private ThemeUtils() {
		throw new AssertionError();
	}

	public static void applyActionBarBackground(final ActionBar actionBar, final Context context) {
		applyActionBarBackground(actionBar, context, true);
	}

	public static void applyActionBarBackground(final ActionBar actionBar, final Context context,
			final boolean applyAlpha) {
		if (actionBar == null || context == null) return;
		actionBar.setBackgroundDrawable(getActionBarBackground(context, applyAlpha));
		actionBar.setSplitBackgroundDrawable(getActionBarSplitBackground(context, applyAlpha));
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

	public static void applyThemeAlphaToDrawable(final Context context, final Drawable d) {
		if (context == null || d == null) return;
		d.setAlpha(getThemeAlpha(getThemeResource(context)));
	}

	public static Drawable getActionBarBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.background },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null) return null;
		final Drawable mutated = d.mutate();
		if (mutated instanceof LayerDrawable) {
			final Drawable colorLayer = ((LayerDrawable) mutated).findDrawableByLayerId(R.id.color_layer);
			if (colorLayer != null) {
				final int color = getThemeColor(context);
				colorLayer.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			}
		}
		if (applyAlpha) {
			final boolean isTransparentBackground = isTransparentBackground(context);
			mutated.setAlpha(isTransparentBackground ? 0xa0 : 0xff);
		}
		return mutated;
	}

	public static Context getActionBarContext(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.actionBarWidgetTheme });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) return context;
		return new ContextThemeWrapper(context, resId);
	}

	public static Drawable getActionBarSplitBackground(final Context context, final boolean applyAlpha) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
				android.R.attr.actionBarStyle, 0);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null) return null;
		final Drawable mutated = d.mutate();
		if (mutated instanceof LayerDrawable) {
			final Drawable colorLayer = ((LayerDrawable) mutated).findDrawableByLayerId(R.id.color_layer);
			if (colorLayer != null) {
				final int color = getThemeColor(context);
				colorLayer.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			}
		}
		if (applyAlpha) {
			final boolean isTransparentBackground = isTransparentBackground(context);
			mutated.setAlpha(isTransparentBackground ? 0xa0 : 0xff);
		}
		return mutated;
	}

	public static Drawable getCardItemBackground(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.cardItemBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		return d;
	}

	public static int getComposeThemeResource(final Context context) {
		return getComposeThemeResource(getThemeName(context));
	}

	public static int getComposeThemeResource(final String name) {
		final Integer res = THEMES_COMPOSE.get(name);
		return res != null ? res : R.style.Theme_Twidere_Compose;
	}

	public static int getDialogThemeResource(final Context context) {
		return getDialogThemeResource(getThemeName(context));
	}

	public static int getDialogThemeResource(final String name) {
		final Integer res = THEMES_DIALOG.get(name);
		return res != null ? res : R.style.Theme_Twidere_Dark_Dialog;
	}

	public static int getDrawerThemeResource(final Context context) {
		return getDrawerThemeResource(getThemeResource(context));
	}

	public static int getDrawerThemeResource(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Transparent:
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
				return R.style.Theme_Twidere_Drawer_Dark_Transparent;
		}
		return R.style.Theme_Twidere_Drawer_Dark;
	}

	public static Drawable getListMenuOverflowButtonDrawable(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.listMenuOverflowButton });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null)
			return context.getResources().getDrawable(R.drawable.ic_list_menu_moreoverflow_normal_holo_light);
		return d;
	}

	public static Drawable getSelectableItemBackgroundDrawable(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
		if (d == null) return context.getResources().getDrawable(R.drawable.item_background_holo_dark);
		return d;
	}

	public static int getTabIconColor(final Context context) {
		return getTabIconColor(getThemeResource(context));
	}

	public static int getTabIconColor(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Light:
			case R.style.Theme_Twidere_Light_SolidBackground:
			case R.style.Theme_Twidere_Light_Compose:
			case R.style.Theme_Twidere_Light_Transparent:
				return 0xC0333333;
		}
		return Color.WHITE;
	}

	public static int getTextAppearanceLarge(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.textAppearanceLarge });
		final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo_Large);
		a.recycle();
		return textAppearance;
	}

	public static int getTextColorPrimary(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textColorPrimary });
		final int color = a.getColor(0, Color.TRANSPARENT);
		a.recycle();
		return color;
	}

	public static int getTextColorSecondary(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.textColorSecondary });
		final int color = a.getColor(0, Color.TRANSPARENT);
		a.recycle();
		return color;
	}

	public static int getThemeAlpha(final Context context) {
		return getThemeAlpha(getThemeResource(context));
	}

	public static int getThemeAlpha(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Transparent:
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
				return 0xa0;
		}
		return 0xff;
	}

	public static String getThemeBackgroundOption(final Context context) {
		if (context == null) return THEME_BACKGROUND_DEFAULT;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return pref.getString(PREFERENCE_KEY_THEME_BACKGROUND, THEME_BACKGROUND_DEFAULT);
	}

	public static int getThemeColor(final Context context) {
		if (context == null) return Color.TRANSPARENT;
		final int def = context.getResources().getColor(android.R.color.holo_blue_light);
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
		return pref != null ? pref.getString(PREFERENCE_KEY_THEME, THEME_NAME_TWIDERE) : THEME_NAME_TWIDERE;
	}

	public static int getThemeResource(final Context context) {
		return getThemeResource(getThemeName(context), getThemeBackgroundOption(context));
	}

	public static int getThemeResource(final String name, final String background) {
		final ThemeMap themes = THEME_MAPS.containsKey(background) ? THEME_MAPS.get(background) : THEMES;
		return themes.containsKey(name) ? themes.get(name) : R.style.Theme_Twidere;
	}

	public static int getTitleTextAppearance(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.titleTextStyle },
				android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
		final int textAppearance = a.getResourceId(0, android.R.style.TextAppearance_Holo);
		a.recycle();
		return textAppearance;
	}

	public static Drawable getWindowBackground(final Context context) {
		final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.windowBackground });
		final Drawable d = a.getDrawable(0);
		a.recycle();
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
			case R.style.Theme_Twidere_Light_Compose:
			case R.style.Theme_Twidere_Light_Transparent:
				return true;
		}
		return false;
	}

	public static boolean isSolidBackground(final Context context) {
		return THEME_BACKGROUND_SOLID.equals(getThemeBackgroundOption(context));
	}

	public static boolean isTransparentBackground(final Context context) {
		return isTransparentBackground(getThemeResource(context));
	}

	public static boolean isTransparentBackground(final int themeRes) {
		switch (themeRes) {
			case R.style.Theme_Twidere_Transparent:
			case R.style.Theme_Twidere_Dark_Transparent:
			case R.style.Theme_Twidere_Light_Transparent:
			case R.style.Theme_Twidere_Light_DarkActionBar_Transparent:
				return true;
		}
		return false;
	}

	public static void overrideActivityCloseAnimation(final Activity activity) {
		TypedArray activityStyle = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
		final int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
		activityStyle.recycle();
		// Now retrieve the resource ids of the actual animations used in the
		// animation style pointed to by
		// the window animation resource id.
		activityStyle = activity.obtainStyledAttributes(windowAnimationStyleResId, ANIM_CLOSE_STYLE_ATTRS);
		final int activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
		final int activityCloseExitAnimation = activityStyle.getResourceId(1, 0);
		activityStyle.recycle();
		activity.overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
	}

	public static void overrideActivityOpenAnimation(final Activity activity) {
		TypedArray activityStyle = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
		final int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
		activityStyle.recycle();
		// Now retrieve the resource ids of the actual animations used in the
		// animation style pointed to by
		// the window animation resource id.
		activityStyle = activity.obtainStyledAttributes(windowAnimationStyleResId, ANIM_OPEN_STYLE_ATTRS);
		final int activityOpenEnterAnimation = activityStyle.getResourceId(0, 0);
		final int activityOpenExitAnimation = activityStyle.getResourceId(1, 0);
		activityStyle.recycle();
		activity.overridePendingTransition(activityOpenEnterAnimation, activityOpenExitAnimation);
	}

	public static void overrideNormalActivityCloseAnimation(final Activity activity) {
		TypedArray a = activity.obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
		a = activity.obtainStyledAttributes(android.R.style.Animation_Activity, ANIM_CLOSE_STYLE_ATTRS);
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
		final View statusListPane = view.findViewById(R.id.theme_preview_list_pane);

		final int defaultTextSize = getDefaultTextSize(context);
		final int textColorPrimary = getTextColorPrimary(theme);
		final int textColorSecondary = getTextColorSecondary(theme);
		final int titleTextAppearance = getTitleTextAppearance(theme);

		ViewAccessor.setBackground(windowBackgroundView, getWindowBackground(theme));
		ViewAccessor.setBackground(windowContentOverlayView, getWindowContentOverlay(theme));
		ViewAccessor.setBackground(actionBarView, getActionBarBackground(theme, true));
		ViewAccessor.setBackground(actionBarSplitView, getActionBarSplitBackground(theme, true));

		actionBarTitleView.setTextAppearance(theme, titleTextAppearance);
		if (statusListPane != null) {
			final CardItemLinearLayout statusListItemContent = (CardItemLinearLayout) statusListPane
					.findViewById(R.id.content);
			statusListItemContent.setItemSelector(null);

			final ForegroundImageView profileImageView = (ForegroundImageView) statusListItemContent
					.findViewById(R.id.profile_image);
			final TextView nameView = (TextView) statusListItemContent.findViewById(R.id.name);
			final TextView screenNameView = (TextView) statusListItemContent.findViewById(R.id.screen_name);
			final TextView textView = (TextView) statusListItemContent.findViewById(R.id.text);
			final ShortTimeView timeSourceView = (ShortTimeView) statusListItemContent.findViewById(R.id.time);
			final TextView replyRetweetView = (TextView) statusListItemContent.findViewById(R.id.reply_retweet_status);
			final Drawable cardItemBackground = getCardItemBackground(theme);
			applyThemeAlphaToDrawable(theme, cardItemBackground);
			ViewAccessor.setBackground(statusListItemContent, cardItemBackground);

			replyRetweetView.setVisibility(View.GONE);

			nameView.setTextColor(textColorPrimary);
			screenNameView.setTextColor(textColorSecondary);
			textView.setTextColor(textColorPrimary);
			timeSourceView.setTextColor(textColorSecondary);

			nameView.setTextSize(defaultTextSize);
			textView.setTextSize(defaultTextSize);
			screenNameView.setTextSize(defaultTextSize * 0.75f);
			timeSourceView.setTextSize(defaultTextSize * 0.65f);
			textView.setTextIsSelectable(false);

			profileImageView.setImageResource(R.drawable.ic_launcher);
			profileImageView.setForeground(null);
			nameView.setText(TWIDERE_PREVIEW_NAME);
			screenNameView.setText("@" + TWIDERE_PREVIEW_SCREEN_NAME);
			textView.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));

			timeSourceView.setTime(System.currentTimeMillis());
		}
		if (statusContentView != null) {
			ViewAccessor.setBackground(statusContentView, getWindowBackground(theme));

			final View cardView = statusContentView.findViewById(R.id.card);
			final View profileView = statusContentView.findViewById(R.id.profile);
			final ImageView profileImageView = (ImageView) statusContentView.findViewById(R.id.profile_image);
			final TextView nameView = (TextView) statusContentView.findViewById(R.id.name);
			final TextView screenNameView = (TextView) statusContentView.findViewById(R.id.screen_name);
			final TextView textView = (TextView) statusContentView.findViewById(R.id.text);
			final TextView timeSourceView = (TextView) statusContentView.findViewById(R.id.time_source);
			final TextView retweetView = (TextView) statusContentView.findViewById(R.id.retweet_view);
			final Drawable cardItemBackground = getCardItemBackground(theme);
			applyThemeAlphaToDrawable(theme, cardItemBackground);
			ViewAccessor.setBackground(cardView, cardItemBackground);

			nameView.setTextColor(textColorPrimary);
			screenNameView.setTextColor(textColorSecondary);
			textView.setTextColor(textColorPrimary);
			timeSourceView.setTextColor(textColorSecondary);
			retweetView.setTextColor(textColorSecondary);

			nameView.setTextSize(defaultTextSize * 1.25f);
			textView.setTextSize(defaultTextSize * 1.25f);
			screenNameView.setTextSize(defaultTextSize * 0.85f);
			timeSourceView.setTextSize(defaultTextSize * 0.85f);
			retweetView.setTextSize(defaultTextSize * 0.85f);

			profileView.setBackgroundResource(0);
			retweetView.setBackgroundResource(0);
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
			case R.style.Theme_Twidere:
			case R.style.Theme_Twidere_SolidBackground:
			case R.style.Theme_Twidere_Compose:
			case R.style.Theme_Twidere_Transparent:
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

	private static final class ThemeMap extends HashMap<String, Integer> {

		private static final long serialVersionUID = -4526137301872382454L;

	}

}
