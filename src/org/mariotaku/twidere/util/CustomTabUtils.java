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

import static org.mariotaku.twidere.util.CompareUtils.classEquals;

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
import org.mariotaku.twidere.fragment.support.ActivitiesAboutMeFragment;
import org.mariotaku.twidere.fragment.support.ActivitiesByFriendsFragment;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.InvalidTabFragment;
import org.mariotaku.twidere.fragment.support.MentionsFragment;
import org.mariotaku.twidere.fragment.support.SearchStatusesFragment;
import org.mariotaku.twidere.fragment.support.StaggeredHomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggectionsFragment;
import org.mariotaku.twidere.fragment.support.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.support.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.support.UserTimelineFragment;
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
				HomeTimelineFragment.class, R.string.home, R.drawable.ic_tab_home,
				CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 0, false));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_MENTIONS_TIMELINE, new CustomTabConfiguration(
				MentionsFragment.class, R.string.mentions, R.drawable.ic_tab_mention,
				CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 1, false));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_DIRECT_MESSAGES, new CustomTabConfiguration(
				DirectMessagesFragment.class, R.string.direct_messages, R.drawable.ic_tab_message,
				CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 2, false));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_TRENDS_SUGGESTIONS, new CustomTabConfiguration(
				TrendsSuggectionsFragment.class, R.string.trends, R.drawable.ic_tab_trends,
				CustomTabConfiguration.ACCOUNT_NONE, CustomTabConfiguration.FIELD_TYPE_NONE, 3, true));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_FAVORITES, new CustomTabConfiguration(UserFavoritesFragment.class,
				R.string.favorites, R.drawable.ic_tab_star, CustomTabConfiguration.ACCOUNT_REQUIRED,
				CustomTabConfiguration.FIELD_TYPE_USER, 4));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_USER_TIMELINE, new CustomTabConfiguration(
				UserTimelineFragment.class, R.string.users_statuses, R.drawable.ic_tab_quote,
				CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER, 5));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_SEARCH_STATUSES, new CustomTabConfiguration(
				SearchStatusesFragment.class, R.string.search_statuses, R.drawable.ic_tab_search,
				CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_TEXT, R.string.query,
				EXTRA_QUERY, 6));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_LIST_TIMELINE, new CustomTabConfiguration(
				UserListTimelineFragment.class, R.string.list_timeline, R.drawable.ic_tab_list,
				CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_USER_LIST, 7));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_ACTIVITIES_ABOUT_ME, new CustomTabConfiguration(
				ActivitiesAboutMeFragment.class, R.string.activities_about_me, R.drawable.ic_tab_person,
				CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_NONE, 8));
		CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_ACTIVITIES_BY_FRIENDS, new CustomTabConfiguration(
				ActivitiesByFriendsFragment.class, R.string.activities_by_friends, R.drawable.ic_tab_accounts,
				CustomTabConfiguration.ACCOUNT_REQUIRED, CustomTabConfiguration.FIELD_TYPE_NONE, 9));
		if (Utils.hasStaggeredTimeline()) {
			CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_STAGGERED_HOME_TIMELINE, new CustomTabConfiguration(
					StaggeredHomeTimelineFragment.class, R.string.staggered_home_timeline, R.drawable.ic_tab_staggered,
					CustomTabConfiguration.ACCOUNT_OPTIONAL, CustomTabConfiguration.FIELD_TYPE_NONE, 10, false));
		}

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
		CUSTOM_TABS_ICON_NAME_MAP.put("quote", R.drawable.ic_tab_quote);
		CUSTOM_TABS_ICON_NAME_MAP.put("ribbon", R.drawable.ic_tab_ribbon);
		CUSTOM_TABS_ICON_NAME_MAP.put("search", R.drawable.ic_tab_search);
		CUSTOM_TABS_ICON_NAME_MAP.put("staggered", R.drawable.ic_tab_staggered);
		CUSTOM_TABS_ICON_NAME_MAP.put("star", R.drawable.ic_tab_star);
		CUSTOM_TABS_ICON_NAME_MAP.put("trends", R.drawable.ic_tab_trends);
		CUSTOM_TABS_ICON_NAME_MAP.put("twidere", R.drawable.ic_tab_twidere);
		CUSTOM_TABS_ICON_NAME_MAP.put("twitter", R.drawable.ic_tab_twitter);
	}

	public static String findTabIconKey(final int iconRes) {
		for (final Entry<String, Integer> entry : getIconMap().entrySet()) {
			if (entry.getValue() == iconRes) return entry.getKey();
		}
		return null;
	}

	public static String findTabType(final Class<? extends Fragment> cls) {
		for (final Entry<String, CustomTabConfiguration> entry : getConfiguraionMap().entrySet()) {
			if (classEquals(cls, entry.getValue().getFragmentClass())) return entry.getKey();
		}
		return null;
	}

	public static SupportTabSpec getAddedTabAt(final Context context, final int position) {
		if (context == null || position < 0) return null;
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, Tabs.POSITION + " = " + position, null,
				Tabs.DEFAULT_SORT_ORDER);
		final int idx_name = cur.getColumnIndex(Tabs.NAME), idx_icon = cur.getColumnIndex(Tabs.ICON), idx_type = cur
				.getColumnIndex(Tabs.TYPE), idx_arguments = cur.getColumnIndex(Tabs.ARGUMENTS);
		try {
			if (cur.getCount() == 0) return null;
			cur.moveToFirst();
			final String type = cur.getString(idx_type);
			final CustomTabConfiguration conf = getTabConfiguration(type);
			if (conf == null) return null;
			final String icon_type = cur.getString(idx_icon);
			final String name = cur.getString(idx_name);
			final Bundle args = ParseUtils.jsonToBundle(cur.getString(idx_arguments));
			args.putInt(EXTRA_TAB_POSITION, position);
			final Class<? extends Fragment> fragment = conf.getFragmentClass();
			return new SupportTabSpec(name != null ? name : getTabTypeName(context, type), getTabIconObject(icon_type),
					type, fragment, args, position);
		} finally {
			cur.close();
		}
	}

	public static CustomTabConfiguration getAddedTabConfigurationAt(final Context context, final int position) {
		if (context == null || position < 0) return null;
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, Tabs.POSITION + " = " + position, null,
				Tabs.DEFAULT_SORT_ORDER);
		final int idx_type = cur.getColumnIndex(Tabs.TYPE);
		try {
			if (cur.getCount() == 0) return null;
			cur.moveToFirst();
			final String type = cur.getString(idx_type);
			return getTabConfiguration(type);
		} finally {
			cur.close();
		}
	}

	public static int getAddedTabPosition(final Context context, final String type) {
		if (context == null || type == null) return -1;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Tabs.TYPE + " = ?";
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, new String[] { Tabs.POSITION }, where,
				new String[] { type }, Tabs.DEFAULT_SORT_ORDER);
		if (cur == null) return -1;
		final int position;
		if (cur.getCount() > 0) {
			cur.moveToFirst();
			position = cur.getInt(cur.getColumnIndex(Tabs.POSITION));
		} else {
			position = -1;
		}
		cur.close();
		return position;
	}

	public static String getAddedTabTypeAt(final Context context, final int position) {
		if (context == null || position < 0) return null;
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, Tabs.POSITION + " = " + position, null,
				Tabs.DEFAULT_SORT_ORDER);
		final int idx_type = cur.getColumnIndex(Tabs.TYPE);
		try {
			if (cur.getCount() == 0) return null;
			cur.moveToFirst();
			return cur.getString(idx_type);
		} finally {
			cur.close();
		}
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
		final int idx_name = cur.getColumnIndex(Tabs.NAME), idxIcon = cur.getColumnIndex(Tabs.ICON), idxType = cur
				.getColumnIndex(Tabs.TYPE), idxArguments = cur.getColumnIndex(Tabs.ARGUMENTS), idxPosition = cur
				.getColumnIndex(Tabs.POSITION);
		while (!cur.isAfterLast()) {
			final String type = cur.getString(idxType);
			final int position = cur.getInt(idxPosition);
			final String iconType = cur.getString(idxIcon);
			final String name = cur.getString(idx_name);
			final Bundle args = ParseUtils.jsonToBundle(cur.getString(idxArguments));
			args.putInt(EXTRA_TAB_POSITION, position);
			final CustomTabConfiguration conf = getTabConfiguration(type);
			final Class<? extends Fragment> cls = conf != null ? conf.getFragmentClass() : InvalidTabFragment.class;
			tabs.add(new SupportTabSpec(name != null ? name : getTabTypeName(context, type),
					getTabIconObject(iconType), type, cls, args, position));
			cur.moveToNext();
		}
		cur.close();
		Collections.sort(tabs);
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
		final CustomTabConfiguration conf = getTabConfiguration(type);
		final Integer res_id = conf != null ? conf.getDefaultTitle() : null;
		return res_id != null ? context.getString(res_id) : null;
	}

	public static boolean isSingleTab(final String type) {
		if (type == null) return false;
		final CustomTabConfiguration conf = getTabConfiguration(type);
		return conf != null && conf.isSingleTab();
	}

	public static boolean isTabAdded(final Context context, final String type) {
		if (context == null || type == null) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Tabs.TYPE + " = ?";
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, new String[0], where, new String[] { type },
				Tabs.DEFAULT_SORT_ORDER);
		if (cur == null) return false;
		final boolean added = cur.getCount() > 0;
		cur.close();
		return added;
	}

	public static boolean isTabTypeValid(final String type) {
		return type != null && CUSTOM_TABS_CONFIGURATION_MAP.containsKey(type);
	}
}
