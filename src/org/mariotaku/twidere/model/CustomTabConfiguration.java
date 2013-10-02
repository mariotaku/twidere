
package org.mariotaku.twidere.model;

import android.support.v4.app.Fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.MentionsFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;

import java.io.File;
import java.util.HashMap;

public class CustomTabConfiguration implements Constants {

    public static final int FIELD_TYPE_NONE = 0;
    public static final int FIELD_TYPE_USER = 1;
    public static final int FIELD_TYPE_USER_LIST = 2;
    public static final int FIELD_TYPE_TEXT_FIELD = 3;

    private static final HashMap<String, CustomTabConfiguration> CUSTOM_TABS_CONFIGURATION_MAP = new HashMap<String, CustomTabConfiguration>();
    private static final HashMap<String, Integer> CUSTOM_TABS_ICON_NAME_MAP = new HashMap<String, Integer>();

    static {
        CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_HOME_TIMELINE, new CustomTabConfiguration(
                HomeTimelineFragment.class, R.string.home, R.drawable.ic_tab_home, false,
                FIELD_TYPE_NONE));
        CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_MENTIONS_TIMELINE, new CustomTabConfiguration(
                MentionsFragment.class, R.string.mentions, R.drawable.ic_tab_mention, false,
                FIELD_TYPE_NONE));
        CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_DIRECT_MESSAGES,
                new CustomTabConfiguration(HomeTimelineFragment.class, R.string.direct_messages,
                        R.drawable.ic_tab_message, false, FIELD_TYPE_NONE));
        CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_FAVORITES, new CustomTabConfiguration(
                UserFavoritesFragment.class,
                R.string.favorites, R.drawable.ic_tab_star, true, FIELD_TYPE_USER));
        CUSTOM_TABS_CONFIGURATION_MAP.put(TAB_TYPE_USER_TIMELINE, new CustomTabConfiguration(
                UserTimelineFragment.class, R.string.statuses, R.drawable.ic_tab_list, true,
                FIELD_TYPE_USER));

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
        CUSTOM_TABS_ICON_NAME_MAP.put(ICON_SPECIAL_TYPE_CUSTOMIZE, -1);
    }

    private final int mDefaultTitle, mDefaultIcon, mSecondaryFieldType;
    private final boolean mAccountIdRequired;
    private final Class<? extends Fragment> mFragmentClass;

    public CustomTabConfiguration(final Class<? extends Fragment> cls, final int title,
            final int icon,
            final boolean account_id_required, final int secondary_field_type) {
        mFragmentClass = cls;
        mDefaultTitle = title;
        mDefaultIcon = icon;
        mAccountIdRequired = account_id_required;
        mSecondaryFieldType = secondary_field_type;
    }

    public int getDefaultIcon() {
        return mDefaultIcon;
    }

    public int getDefaultTitle() {
        return mDefaultTitle;
    }

    public Class<? extends Fragment> getFragmentClass() {
        return mFragmentClass;
    }

    public int getSecondaryFieldType() {
        return mSecondaryFieldType;
    }

    public boolean isAccountIdRequired() {
        return mAccountIdRequired;
    }

    public static CustomTabConfiguration get(final String key) {
        return CUSTOM_TABS_CONFIGURATION_MAP.get(key);
    }

    public static HashMap<String, CustomTabConfiguration> getConfiguraionMap() {
        return new HashMap<String, CustomTabConfiguration>(CUSTOM_TABS_CONFIGURATION_MAP);
    }

    public static Object getTabIconObject(final String type) {
        if (type == null)
            return R.drawable.ic_tab_list;
        final Integer value = CUSTOM_TABS_ICON_NAME_MAP.get(type);
        if (value != null)
            return value;
        else if (type.contains("/")) {
            try {
                final File file = new File(type);
                if (file.exists())
                    return file;
            } catch (final Exception e) {
                return R.drawable.ic_tab_list;
            }
        }
        return R.drawable.ic_tab_list;
    }
}
