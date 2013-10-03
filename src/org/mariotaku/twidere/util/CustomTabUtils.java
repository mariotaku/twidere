package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.MentionsFragment;
import org.mariotaku.twidere.fragment.SearchStatusesFragment;
import org.mariotaku.twidere.fragment.TrendsFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class CustomTabUtils implements Constants {
	private static final HashMap<String, CustomTabConfiguration> CUSTOM_TABS_CONFIGURATION_MAP = new HashMap<String, CustomTabConfiguration>();
	private static final HashMap<String, Integer> CUSTOM_TABS_ICON_NAME_MAP = new HashMap<String, Integer>();

	static {
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_HOME_TIMELINE, new CustomTabConfiguration(
				HomeTimelineFragment.class, R.string.home, R.drawable.ic_tab_home, false,
				CustomTabConfiguration.FIELD_TYPE_NONE, 0));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_MENTIONS_TIMELINE, new CustomTabConfiguration(
				MentionsFragment.class, R.string.mentions, R.drawable.ic_tab_mention, false,
				CustomTabConfiguration.FIELD_TYPE_NONE, 1));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_DIRECT_MESSAGES, new CustomTabConfiguration(
				HomeTimelineFragment.class, R.string.direct_messages, R.drawable.ic_tab_message, false,
				CustomTabConfiguration.FIELD_TYPE_NONE, 2));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_TRENDS, new CustomTabConfiguration(TrendsFragment.class,
				R.string.trends, R.drawable.ic_tab_trends, true, CustomTabConfiguration.FIELD_TYPE_NONE, 3));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_FAVORITES, new CustomTabConfiguration(UserFavoritesFragment.class,
				R.string.favorites, R.drawable.ic_tab_star, true, CustomTabConfiguration.FIELD_TYPE_USER, 4));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_USER_TIMELINE, new CustomTabConfiguration(
				UserTimelineFragment.class, R.string.statuses, R.drawable.ic_tab_list, true,
				CustomTabConfiguration.FIELD_TYPE_USER, 5));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_SEARCH_STATUSES, new CustomTabConfiguration(
				SearchStatusesFragment.class, R.string.search_statuses, R.drawable.ic_tab_search, true,
				CustomTabConfiguration.FIELD_TYPE_TEXT, R.string.query, EXTRA_QUERY, 6));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_LIST_TIMELINE, new CustomTabConfiguration(
				UserListTimelineFragment.class, R.string.list_timeline, R.drawable.ic_tab_list, true,
				CustomTabConfiguration.FIELD_TYPE_USER_LIST, 7));

		CUSTOM_TABS_ICON_NAME_MAP.put("accounts", R.drawable.ic_tab_accounts);
		CUSTOM_TABS_ICON_NAME_MAP.put("fire", R.drawable.ic_tab_fire);
		CUSTOM_TABS_ICON_NAME_MAP.put("hamster", R.drawable.ic_tab_hamster);
		CUSTOM_TABS_ICON_NAME_MAP.put("heart", R.drawable.ic_tab_heart);
		CUSTOM_TABS_ICON_NAME_MAP.put("home", R.drawable.ic_tab_home);
		CUSTOM_TABS_ICON_NAME_MAP.put("list", R.drawable.ic_tab_list);
		CUSTOM_TABS_ICON_NAME_MAP.put("mention", R.drawable.ic_tab_mention);
		CUSTOM_TABS_ICON_NAME_MAP.put("message", R.drawable.ic_tab_message);
		CUSTOM_TABS_ICON_NAME_MAP.put("neko", R.drawable.ic_tab_neko);
		CUSTOM_TABS_ICON_NAME_MAP.put("person", R.drawable.ic_tab_person);
		CUSTOM_TABS_ICON_NAME_MAP.put("pin", R.drawable.ic_tab_pin);
		CUSTOM_TABS_ICON_NAME_MAP.put("ribbon", R.drawable.ic_tab_ribbon);
		CUSTOM_TABS_ICON_NAME_MAP.put("search", R.drawable.ic_tab_search);
		CUSTOM_TABS_ICON_NAME_MAP.put("star", R.drawable.ic_tab_star);
		CUSTOM_TABS_ICON_NAME_MAP.put("trends", R.drawable.ic_tab_trends);
		CUSTOM_TABS_ICON_NAME_MAP.put("twitter", R.drawable.ic_tab_twitter);
		// CUSTOM_TABS_ICON_NAME_MAP.put(ICON_SPECIAL_TYPE_CUSTOMIZE, -1);
	}

	public static String findTabIconKey(final int iconRes) {
		for (final Entry<String, Integer> entry : getIconMap().entrySet()) {
			if (entry.getValue() == iconRes) return entry.getKey();
		}
		return null;
	}

	public static HashMap<String, CustomTabConfiguration> getConfiguraionMap() {
		return new HashMap<String, CustomTabConfiguration>(CUSTOM_TABS_CONFIGURATION_MAP);
	}

	public static List<SupportTabSpec> getHomeTabs(final Context context) {
		if (context == null) return Collections.emptyList();
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
		if (cur == null) return Collections.emptyList();
		final ArrayList<SupportTabSpec> tabs = new ArrayList<SupportTabSpec>();
		cur.moveToFirst();
		final int idx_name = cur.getColumnIndex(Tabs.NAME), idx_icon = cur.getColumnIndex(Tabs.ICON), idx_type = cur
				.getColumnIndex(Tabs.TYPE), idx_arguments = cur.getColumnIndex(Tabs.ARGUMENTS), idx_position = cur
				.getColumnIndex(Tabs.POSITION);
		while (!cur.isAfterLast()) {
			final int position = cur.getInt(idx_position);
			final String icon_type = cur.getString(idx_icon);
			final String type = cur.getString(idx_type);
			final String name = cur.getString(idx_name);
			final Bundle args = ParseUtils.jsonToBundle(cur.getString(idx_arguments));
			args.putInt(EXTRA_TAB_POSITION, position);
			final Class<? extends Fragment> fragment = getTabConfiguration(type).getFragmentClass();
			if (name != null && fragment != null) {
				tabs.add(new SupportTabSpec(name, getTabIconObject(icon_type), fragment, args, position));
			}
			cur.moveToNext();
		}
		cur.close();
		return tabs;
	}

	public static HashMap<String, Integer> getIconMap() {
		return new HashMap<String, Integer>(CUSTOM_TABS_ICON_NAME_MAP);
	}

	public static CustomTabConfiguration getTabConfiguration(final String key) {
		return CUSTOM_TABS_CONFIGURATION_MAP.get(key);
	}

	public static Drawable getTabIconDrawable(final Context context, final Object icon_obj) {
		if (context == null) return null;
		final Resources res = context.getResources();
		if (icon_obj instanceof Integer) {
			try {
				return res.getDrawable((Integer) icon_obj);
			} catch (final Resources.NotFoundException e) {
				// Ignore.
			}
		} else if (icon_obj instanceof Bitmap)
			return new BitmapDrawable(res, (Bitmap) icon_obj);
		else if (icon_obj instanceof Drawable)
			return (Drawable) icon_obj;
		else if (icon_obj instanceof File) {
			final Bitmap b = Utils.getTabIconFromFile((File) icon_obj, res);
			if (b != null) return new BitmapDrawable(res, b);
		}
		return res.getDrawable(R.drawable.ic_tab_list);
	}

	public static Object getTabIconObject(final String type) {
		if (type == null) return R.drawable.ic_tab_list;
		final Integer value = CUSTOM_TABS_ICON_NAME_MAP.get(type);
		if (value != null)
			return value;
		else if (type.contains("/")) {
			try {
				final File file = new File(type);
				if (file.exists()) return file;
			} catch (final Exception e) {
				return R.drawable.ic_tab_list;
			}
		}
		return R.drawable.ic_tab_list;
	}

	public static String getTabTypeName(final Context context, final String type) {
		if (context == null) return null;
		final Integer res_id = getTabConfiguration(type).getDefaultTitle();
		return res_id != null ? context.getString(res_id) : null;
	}
}
