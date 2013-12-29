/*
 *				Twidere - Twitter client for Android
 *
 * Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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
package org.mariotaku.twidere;

import org.mariotaku.twidere.annotation.PreferenceType;

/**
 * Public constants for both Twidere app and its extensions
 * 
 * @author mariotaku
 * 
 */
public interface TwidereConstants {

	public static final String APP_NAME = "Twidere";
	public static final String APP_PROJECT_URL = "https://github.com/mariotaku/twidere";
	public static final String APP_PROJECT_EMAIL = "twidere.project@gmail.com";

	public static final String LOGTAG = APP_NAME;

	public static final String USER_NICKNAME_PREFERENCES_NAME = "user_nicknames";
	public static final String USER_COLOR_PREFERENCES_NAME = "user_colors";
	public static final String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
	public static final String SHARED_PREFERENCES_NAME = "preferences";
	public static final String PERMISSION_PREFERENCES_NAME = "permissions";
	public static final String SILENT_NOTIFICATIONS_PREFERENCE_NAME = "silent_notifications";
	public static final String TIMELINE_POSITIONS_PREFERENCES_NAME = "timeline_positions";
	public static final String ACCOUNT_PREFERENCES_NAME_PREFIX = "account_preferences_";

	public static final String TWITTER_CONSUMER_KEY = "uAFVpMhBntJutfVj6abfA";
	public static final String TWITTER_CONSUMER_SECRET = "JARXkJTfxo0F8MyctYy9bUmrLISjo8vXAHsZHYuk2E";
	public static final String TWITTER_CONSUMER_KEY_2 = "UyaS0xmUQXKiJ48vZP4dXQ";
	public static final String TWITTER_CONSUMER_SECRET_2 = "QlYVMWA751Dl5yNve41CNEN46GV4nxk57FmLeAXAV0";

	public static final String SCHEME_HTTP = "http";
	public static final String SCHEME_HTTPS = "https";
	public static final String SCHEME_CONTENT = "content";
	public static final String SCHEME_TWIDERE = "twidere";

	public static final String PROTOCOL_HTTP = SCHEME_HTTP + "://";
	public static final String PROTOCOL_HTTPS = SCHEME_HTTPS + "://";
	public static final String PROTOCOL_CONTENT = SCHEME_CONTENT + "://";
	public static final String PROTOCOL_TWIDERE = SCHEME_TWIDERE + "://";

	public static final String AUTHORITY_USER = "user";
	public static final String AUTHORITY_USERS = "users";
	public static final String AUTHORITY_USER_TIMELINE = "user_timeline";
	public static final String AUTHORITY_USER_FAVORITES = "user_favorites";
	public static final String AUTHORITY_USER_FOLLOWERS = "user_followers";
	public static final String AUTHORITY_USER_FRIENDS = "user_friends";
	public static final String AUTHORITY_USER_BLOCKS = "user_blocks";
	public static final String AUTHORITY_STATUS = "status";
	public static final String AUTHORITY_STATUSES = "statuses";
	public static final String AUTHORITY_DIRECT_MESSAGES_CONVERSATION = "direct_messages_conversation";
	public static final String AUTHORITY_SEARCH = "search";
	public static final String AUTHORITY_MAP = "map";
	public static final String AUTHORITY_USER_LIST = "user_list";
	public static final String AUTHORITY_USER_LIST_TIMELINE = "user_list_timeline";
	public static final String AUTHORITY_USER_LIST_MEMBERS = "user_list_members";
	public static final String AUTHORITY_USER_LIST_MEMBERSHIPS = "user_list_memberships";
	public static final String AUTHORITY_USER_LISTS = "user_lists";
	public static final String AUTHORITY_USERS_RETWEETED_STATUS = "users_retweeted_status";
	public static final String AUTHORITY_SAVED_SEARCHES = "saved_searches";
	public static final String AUTHORITY_SEARCH_USERS = "search_users";
	public static final String AUTHORITY_SEARCH_TWEETS = "search_tweets";
	public static final String AUTHORITY_TRENDS = "trends";
	public static final String AUTHORITY_USER_MENTIONS = "user_mentions";
	public static final String AUTHORITY_ACTIVITIES_ABOUT_ME = "activities_about_me";
	public static final String AUTHORITY_ACTIVITIES_BY_FRIENDS = "activities_by_friends";
	public static final String AUTHORITY_INCOMING_FRIENDSHIPS = "incoming_friendships";
	public static final String AUTHORITY_STATUS_RETWEETERS = "status_retweeters";
	public static final String AUTHORITY_STATUS_REPLIES = "status_replies";

	public static final String QUERY_PARAM_ACCOUNT_ID = "account_id";
	public static final String QUERY_PARAM_ACCOUNT_IDS = "account_ids";
	public static final String QUERY_PARAM_ACCOUNT_NAME = "account_name";
	public static final String QUERY_PARAM_STATUS_ID = "status_id";
	public static final String QUERY_PARAM_USER_ID = "user_id";
	public static final String QUERY_PARAM_LIST_ID = "list_id";
	public static final String QUERY_PARAM_SCREEN_NAME = "screen_name";
	public static final String QUERY_PARAM_LIST_NAME = "list_name";
	public static final String QUERY_PARAM_QUERY = "query";
	public static final String QUERY_PARAM_TYPE = "type";
	public static final String QUERY_PARAM_VALUE_USERS = "users";
	public static final String QUERY_PARAM_VALUE_TWEETS = "tweets";
	public static final String QUERY_PARAM_NOTIFY = "notify";
	public static final String QUERY_PARAM_LAT = "lat";
	public static final String QUERY_PARAM_LNG = "lng";
	public static final String QUERY_PARAM_URL = "url";
	public static final String QUERY_PARAM_NAME = "name";
	public static final String QUERY_PARAM_FINISH_ONLY = "finish_only";
	public static final String QUERY_PARAM_NEW_ITEMS_COUNT = "new_items_count";
	public static final String QUERY_PARAM_RECIPIENT_ID = "recipient_id";

	public static final String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

	public static final String OAUTH_CALLBACK_OOB = "oob";
	public static final String OAUTH_CALLBACK_URL = PROTOCOL_TWIDERE + "com.twitter.oauth/";

	public static final String FORMAT_PATTERN_TITLE = "[TITLE]";
	public static final String FORMAT_PATTERN_TEXT = "[TEXT]";
	public static final String FORMAT_PATTERN_NAME = "[NAME]";
	public static final String FORMAT_PATTERN_LINK = "[LINK]";

	public static final String LINK_HIGHLIGHT_OPTION_NONE = "none";
	public static final String LINK_HIGHLIGHT_OPTION_HIGHLIGHT = "highlight";
	public static final String LINK_HIGHLIGHT_OPTION_UNDERLINE = "underline";
	public static final String LINK_HIGHLIGHT_OPTION_BOTH = "both";
	public static final int LINK_HIGHLIGHT_OPTION_CODE_NONE = 0x0;
	public static final int LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT = 0x1;
	public static final int LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE = 0x2;
	public static final int LINK_HIGHLIGHT_OPTION_CODE_BOTH = LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT
			| LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE;

	public static final String FONT_FAMILY_REGULAR = "sans-serif";
	public static final String FONT_FAMILY_CONDENSED = "sans-serif-condensed";
	public static final String FONT_FAMILY_LIGHT = "sans-serif-light";
	public static final String FONT_FAMILY_THIN = "sans-serif-thin";

	public static final int NOTIFICATION_FLAG_NONE = 0x0;
	public static final int NOTIFICATION_FLAG_RINGTONE = 0x1;
	public static final int NOTIFICATION_FLAG_VIBRATION = 0x2;
	public static final int NOTIFICATION_FLAG_LIGHT = 0x4;

	public static final String COMPOSE_QUIT_ACTION_ASK = "ask";
	public static final String COMPOSE_QUIT_ACTION_SAVE = "save";
	public static final String COMPOSE_QUIT_ACTION_DISCARD = "discard";

	public static final String TAB_DIPLAY_OPTION_ICON = "icon";
	public static final String TAB_DIPLAY_OPTION_LABEL = "label";
	public static final String TAB_DIPLAY_OPTION_BOTH = "both";
	public static final int TAB_DIPLAY_OPTION_CODE_ICON = 0x1;
	public static final int TAB_DIPLAY_OPTION_CODE_LABEL = 0x2;
	public static final int TAB_DIPLAY_OPTION_CODE_BOTH = TAB_DIPLAY_OPTION_CODE_ICON | TAB_DIPLAY_OPTION_CODE_LABEL;

	@PreferenceType(PreferenceType.INT)
	public static final String PREFERENCE_KEY_DATABASE_ITEM_LIMIT = "database_item_limit";
	@PreferenceType(PreferenceType.INT)
	public static final String PREFERENCE_KEY_LOAD_ITEM_LIMIT = "load_item_limit";
	@PreferenceType(PreferenceType.INT)
	public static final String PREFERENCE_KEY_TEXT_SIZE = "text_size_int";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_THEME = "theme";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_THEME_BACKGROUND = "theme_background";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_THEME_DARK_ACTIONBAR = "theme_dark_actionbar";
	@PreferenceType(PreferenceType.INT)
	public static final String PREFERENCE_KEY_THEME_COLOR = "theme_color";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_THEME_FONT_FAMILY = "theme_font_family";
	@PreferenceType(PreferenceType.NULL)
	public static final String PREFERENCE_KEY_CLEAR_DATABASES = "clear_databases";
	@PreferenceType(PreferenceType.NULL)
	public static final String PREFERENCE_KEY_CLEAR_CACHE = "clear_cache";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_DISPLAY_IMAGE_PREVIEW = "display_image_preview";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_BOTTOM_COMPOSE_BUTTON = "bottom_compose_button";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON = "leftside_compose_button";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_BOTTOM_SEND_BUTTON = "bottom_send_button";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_ATTACH_LOCATION = "attach_location";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_GZIP_COMPRESSING = "gzip_compressing";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_IGNORE_SSL_ERROR = "ignore_ssl_error";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY = "load_more_automatically";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_CONSUMER_KEY = "consumer_key";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_CONSUMER_SECRET = "consumer_secret";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_QUOTE_FORMAT = "quote_format";
	@PreferenceType(PreferenceType.LONG)
	public static final String PREFERENCE_KEY_DEFAULT_ACCOUNT_ID = "default_account_id";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_REMEMBER_POSITION = "remember_position";
	@PreferenceType(PreferenceType.INT)
	public static final String PREFERENCE_KEY_SAVED_TAB_POSITION = "saved_tab_position";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_ENABLE_PROXY = "enable_proxy";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_PROXY_HOST = "proxy_host";
	@PreferenceType(PreferenceType.STRING)
	public static final String PREFERENCE_KEY_PROXY_PORT = "proxy_port";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_REFRESH_ON_START = "refresh_on_start";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_REFRESH_AFTER_TWEET = "refresh_after_tweet";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_AUTO_REFRESH = "auto_refresh";
	@PreferenceType(PreferenceType.INT)
	public static final String PREFERENCE_KEY_REFRESH_INTERVAL = "refresh_interval";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_AUTO_REFRESH_HOME_TIMELINE = "auto_refresh_home_timeline";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_AUTO_REFRESH_MENTIONS = "auto_refresh_mentions";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_AUTO_REFRESH_DIRECT_MESSAGES = "auto_refresh_direct_messages";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_AUTO_REFRESH_TRENDS = "auto_refresh_trends";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_HOME_TIMELINE_NOTIFICATION = "home_timeline_notification";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_MENTIONS_NOTIFICATION = "mentions_notification";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_DIRECT_MESSAGES_NOTIFICATION = "direct_messages_notification";
	public static final String PREFERENCE_KEY_LOCAL_TRENDS_WOEID = "local_trends_woeid";
	public static final String PREFERENCE_KEY_NOTIFICATION_RINGTONE = "notification_ringtone";
	public static final String PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR = "notification_light_color";
	public static final String PREFERENCE_KEY_SHARE_FORMAT = "share_format";
	public static final String PREFERENCE_KEY_IMAGE_UPLOADER = "image_uploader";
	public static final String PREFERENCE_KEY_HOME_REFRESH_MENTIONS = "home_refresh_mentions";
	public static final String PREFERENCE_KEY_HOME_REFRESH_DIRECT_MESSAGES = "home_refresh_direct_messages";
	public static final String PREFERENCE_KEY_HOME_REFRESH_TRENDS = "home_refresh_trends";
	public static final String PREFERENCE_KEY_IMAGE_UPLOAD_FORMAT = "image_upload_format";
	public static final String PREFERENCE_KEY_TWEET_SHORTENER = "tweet_shortener";
	public static final String PREFERENCE_KEY_SHOW_ABSOLUTE_TIME = "show_absolute_time";
	public static final String PREFERENCE_KEY_DUAL_PANE_IN_LANDSCAPE = "dual_pane_in_landscape";
	public static final String PREFERENCE_KEY_DUAL_PANE_IN_PORTRAIT = "dual_pane_in_portrait";
	public static final String PREFERENCE_KEY_QUICK_SEND = "quick_send";
	public static final String PREFERENCE_KEY_COMPOSE_ACCOUNTS = "compose_accounts";
	public static final String PREFERENCE_KEY_TCP_DNS_QUERY = "tcp_dns_query";
	public static final String PREFERENCE_KEY_DNS_SERVER = "dns_server";
	public static final String PREFERENCE_KEY_SEPARATE_RETWEET_ACTION = "separate_retweet_action";
	public static final String PREFERENCE_KEY_CONNECTION_TIMEOUT = "connection_timeout";
	public static final String PREFERENCE_KEY_NAME_FIRST = "name_first";
	public static final String PREFERENCE_KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW = "stop_auto_refresh_when_battery_low";
	public static final String PREFERENCE_KEY_UCD_DATA_PROFILING = "ucd_data_profiling";
	public static final String PREFERENCE_KEY_SHOW_UCD_DATA_PROFILING_REQUEST = "show_ucd_data_profiling_request";
	public static final String PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS = "display_sensitive_contents";
	public static final String PREFERENCE_KEY_PHISHING_LINK_WARNING = "phishing_link_warning";
	public static final String PREFERENCE_KEY_FAST_SCROLL_THUMB = "fast_scroll_thumb";
	public static final String PREFERENCE_KEY_LINK_HIGHLIGHT_OPTION = "link_highlight_option";
	public static final String PREFERENCE_KEY_INDICATE_MY_STATUS = "indicate_my_status";
	public static final String PREFERENCE_KEY_PRELOAD_PROFILE_IMAGES = "preload_profile_images";
	public static final String PREFERENCE_KEY_PRELOAD_PREVIEW_IMAGES = "preload_preview_images";
	public static final String PREFERENCE_KEY_PRELOAD_WIFI_ONLY = "preload_wifi_only";
	public static final String PREFERENCE_KEY_DISABLE_TAB_SWIPE = "disable_tab_swipe";
	public static final String PREFERENCE_KEY_DARK_THEME_COLOR = "dark_theme_color";
	public static final String PREFERENCE_KEY_LIGHT_THEME_COLOR = "light_theme_color";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_LINK_TO_QUOTED_TWEET = "link_to_quoted_tweet";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_BACKGROUND_TOAST_NOTIFICATION = "background_toast_notification";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_COMPOSE_QUIT_ACTION = "compose_quit_action";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_NO_CLOSE_AFTER_TWEET_SENT = "no_close_after_tweet_sent";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_FAST_IMAGE_LOADING = "fast_image_loading";
	public static final String PREFERENCE_KEY_REST_BASE_URL = "rest_base_url";
	public static final String PREFERENCE_KEY_OAUTH_BASE_URL = "oauth_base_url";
	public static final String PREFERENCE_KEY_SIGNING_REST_BASE_URL = "signing_rest_base_url";
	public static final String PREFERENCE_KEY_SIGNING_OAUTH_BASE_URL = "signing_oauth_base_url";
	public static final String PREFERENCE_KEY_AUTH_TYPE = "auth_type";
	public static final String PREFERENCE_KEY_API_LAST_CHANGE = "api_last_change";
	public static final String PREFERENCE_KEY_FILTERS_IN_HOME_TIMELINE = "filters_in_home_timeline";
	public static final String PREFERENCE_KEY_FILTERS_IN_MENTIONS = "filters_in_mentions";
	public static final String PREFERENCE_KEY_FILTERS_FOR_RTS = "filters_for_rts";
	public static final String PREFERENCE_KEY_NICKNAME_ONLY = "nickname_only";
	public static final String PREFERENCE_KEY_SETTINGS_WIZARD_COMPLETED = "settings_wizard_completed";
	public static final String PREFERENCE_KEY_CARD_ANIMATION = "card_animation";
	public static final String PREFERENCE_KEY_UNREAD_COUNT = "unread_count";
	public static final String PREFERENCE_KEY_NOTIFICATION = "notification";
	public static final String PREFERENCE_KEY_NOTIFICATION_TYPE_HOME = "notification_type_home";
	public static final String PREFERENCE_KEY_NOTIFICATION_TYPE_MENTIONS = "notification_type_mentions";
	public static final String PREFERENCE_KEY_NOTIFICATION_TYPE_DIRECT_MESSAGES = "notification_type_direct_messages";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_COMPACT_CARDS = "compact_cards";
	public static final String PREFERENCE_KEY_TAB_DISPLAY_OPTION = "tab_display_option";
	public static final String PREFERENCE_KEY_LIVE_WALLPAPER_SCALE = "live_wallpaper_scale";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_LONG_CLICK_TO_OPEN_MENU = "long_click_to_open_menu";
	@PreferenceType(PreferenceType.BOOLEAN)
	public static final String PREFERENCE_KEY_SWIPE_BACK = "swipe_back";

	public static final String PREFERENCE_DEFAULT_QUOTE_FORMAT = "RT @" + FORMAT_PATTERN_NAME + ": "
			+ FORMAT_PATTERN_TEXT;
	public static final String PREFERENCE_DEFAULT_SHARE_FORMAT = FORMAT_PATTERN_TITLE + " - " + FORMAT_PATTERN_TEXT;
	public static final String PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT = FORMAT_PATTERN_TEXT + " " + FORMAT_PATTERN_LINK;

	public static final String PREFERENCE_DEFAULT_REFRESH_INTERVAL = "15";
	public static final boolean PREFERENCE_DEFAULT_AUTO_REFRESH = true;
	public static final boolean PREFERENCE_DEFAULT_AUTO_REFRESH_HOME_TIMELINE = false;
	public static final boolean PREFERENCE_DEFAULT_AUTO_REFRESH_MENTIONS = true;
	public static final boolean PREFERENCE_DEFAULT_AUTO_REFRESH_DIRECT_MESSAGES = true;
	public static final boolean PREFERENCE_DEFAULT_AUTO_REFRESH_TRENDS = false;
	public static final boolean PREFERENCE_DEFAULT_NOTIFICATION = true;
	public static final int PREFERENCE_DEFAULT_NOTIFICATION_TYPE_HOME = NOTIFICATION_FLAG_NONE;
	public static final int PREFERENCE_DEFAULT_NOTIFICATION_TYPE_MENTIONS = NOTIFICATION_FLAG_VIBRATION
			| NOTIFICATION_FLAG_LIGHT;
	public static final int PREFERENCE_DEFAULT_NOTIFICATION_TYPE_DIRECT_MESSAGES = NOTIFICATION_FLAG_RINGTONE
			| NOTIFICATION_FLAG_VIBRATION | NOTIFICATION_FLAG_LIGHT;

	public static final boolean PREFERENCE_DEFAULT_HOME_TIMELINE_NOTIFICATION = false;
	public static final boolean PREFERENCE_DEFAULT_MENTIONS_NOTIFICATION = true;
	public static final boolean PREFERENCE_DEFAULT_DIRECT_MESSAGES_NOTIFICATION = true;

	public static final int PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT = 100;
	public static final int PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT = 20;
	public static final boolean PREFERENCE_DEFAULT_HARDWARE_ACCELERATION = true;
	public static final boolean PREFERENCE_DEFAULT_SEPARATE_RETWEET_ACTION = true;

	public static final String INTENT_PACKAGE_PREFIX = "org.mariotaku.twidere.";

	public static final String INTENT_ACTION_HOME = INTENT_PACKAGE_PREFIX + "HOME";
	public static final String INTENT_ACTION_COMPOSE = INTENT_PACKAGE_PREFIX + "COMPOSE";
	public static final String INTENT_ACTION_REPLY = INTENT_PACKAGE_PREFIX + "REPLY";
	public static final String INTENT_ACTION_QUOTE = INTENT_PACKAGE_PREFIX + "QUOTE";
	public static final String INTENT_ACTION_EDIT_DRAFT = INTENT_PACKAGE_PREFIX + "EDIT_DRAFT";
	public static final String INTENT_ACTION_MENTION = INTENT_PACKAGE_PREFIX + "MENTION";
	public static final String INTENT_ACTION_REPLY_MULTIPLE = INTENT_PACKAGE_PREFIX + "REPLY_MULTIPLE";
	public static final String INTENT_ACTION_SETTINGS = INTENT_PACKAGE_PREFIX + "SETTINGS";
	public static final String INTENT_ACTION_SELECT_ACCOUNT = INTENT_PACKAGE_PREFIX + "SELECT_ACCOUNT";
	public static final String INTENT_ACTION_VIEW_IMAGE = INTENT_PACKAGE_PREFIX + "VIEW_IMAGE";
	public static final String INTENT_ACTION_FILTERS = INTENT_PACKAGE_PREFIX + "FILTERS";
	public static final String INTENT_ACTION_TWITTER_LOGIN = INTENT_PACKAGE_PREFIX + "TWITTER_LOGIN";
	public static final String INTENT_ACTION_DRAFTS = INTENT_PACKAGE_PREFIX + "DRAFTS";
	public static final String INTENT_ACTION_PICK_FILE = INTENT_PACKAGE_PREFIX + "PICK_FILE";
	public static final String INTENT_ACTION_PICK_DIRECTORY = INTENT_PACKAGE_PREFIX + "PICK_DIRECTORY";
	public static final String INTENT_ACTION_VIEW_WEBPAGE = INTENT_PACKAGE_PREFIX + "VIEW_WEBPAGE";
	public static final String INTENT_ACTION_EXTENSIONS = INTENT_PACKAGE_PREFIX + "EXTENSIONS";
	public static final String INTENT_ACTION_CUSTOM_TABS = INTENT_PACKAGE_PREFIX + "CUSTOM_TABS";
	public static final String INTENT_ACTION_ADD_TAB = INTENT_PACKAGE_PREFIX + "ADD_TAB";
	public static final String INTENT_ACTION_EDIT_TAB = INTENT_PACKAGE_PREFIX + "EDIT_TAB";
	public static final String INTENT_ACTION_EDIT_USER_PROFILE = INTENT_PACKAGE_PREFIX + "EDIT_USER_PROFILE";
	public static final String INTENT_ACTION_SERVICE_COMMAND = INTENT_PACKAGE_PREFIX + "SERVICE_COMMAND";
	public static final String INTENT_ACTION_REQUEST_PERMISSIONS = INTENT_PACKAGE_PREFIX + "REQUEST_PERMISSIONS";
	public static final String INTENT_ACTION_SELECT_USER_LIST = INTENT_PACKAGE_PREFIX + "SELECT_USER_LIST";
	public static final String INTENT_ACTION_SELECT_USER = INTENT_PACKAGE_PREFIX + "SELECT_USER";

	public static final String INTENT_ACTION_EXTENSION_EDIT_IMAGE = INTENT_PACKAGE_PREFIX + "EXTENSION_EDIT_IMAGE";
	public static final String INTENT_ACTION_EXTENSION_UPLOAD = INTENT_PACKAGE_PREFIX + "EXTENSION_UPLOAD";
	public static final String INTENT_ACTION_EXTENSION_OPEN_STATUS = INTENT_PACKAGE_PREFIX + "EXTENSION_OPEN_STATUS";
	public static final String INTENT_ACTION_EXTENSION_OPEN_USER = INTENT_PACKAGE_PREFIX + "EXTENSION_OPEN_USER";
	public static final String INTENT_ACTION_EXTENSION_OPEN_USER_LIST = INTENT_PACKAGE_PREFIX
			+ "EXTENSION_OPEN_USER_LIST";
	public static final String INTENT_ACTION_EXTENSION_COMPOSE = INTENT_PACKAGE_PREFIX + "EXTENSION_COMPOSE";
	public static final String INTENT_ACTION_EXTENSION_UPLOAD_IMAGE = INTENT_PACKAGE_PREFIX + "EXTENSION_UPLOAD_IMAGE";
	public static final String INTENT_ACTION_EXTENSION_SHORTEN_TWEET = INTENT_PACKAGE_PREFIX
			+ "EXTENSION_SHORTEN_TWEET";
	public static final String INTENT_ACTION_EXTENSION_SETTINGS = INTENT_PACKAGE_PREFIX + "EXTENSION_SETTINGS";

	public static final String INTENT_ACTION_UPDATE_STATUS = INTENT_PACKAGE_PREFIX + "UPDATE_STATUS";
	public static final String INTENT_ACTION_SEND_DIRECT_MESSAGE = INTENT_PACKAGE_PREFIX + "SEND_DIRECT_MESSAGE";

	public static final String BROADCAST_HOME_TIMELINE_REFRESHED = INTENT_PACKAGE_PREFIX + "HOME_TIMELINE_REFRESHED";
	public static final String BROADCAST_MENTIONS_REFRESHED = INTENT_PACKAGE_PREFIX + "MENTIONS_REFRESHED";
	public static final String BROADCAST_TASK_STATE_CHANGED = INTENT_PACKAGE_PREFIX + "TASK_STATE_CHANGED";
	public static final String BROADCAST_NOTIFICATION_DELETED = INTENT_PACKAGE_PREFIX + "NOTIFICATION_DELETED";
	public static final String BROADCAST_FRIENDSHIP_CHANGED = INTENT_PACKAGE_PREFIX + "FRIENDSHIP_CHANGED";
	public static final String BROADCAST_BLOCKSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "BLOCKSTATE_CHANGED";
	public static final String BROADCAST_PROFILE_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_UPDATED";
	public static final String BROADCAST_PROFILE_IMAGE_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_IMAGE_UPDATED";
	public static final String BROADCAST_PROFILE_BANNER_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_BANNER_UPDATED";
	public static final String BROADCAST_USER_LIST_DETAILS_UPDATED = INTENT_PACKAGE_PREFIX
			+ "USER_LIST_DETAILS_UPDATED";

	public static final String BROADCAST_FAVORITE_CHANGED = INTENT_PACKAGE_PREFIX + "FAVORITE_CHANGED";
	public static final String BROADCAST_RETWEET_CHANGED = INTENT_PACKAGE_PREFIX + "RETWEET_CHANGED";
	public static final String BROADCAST_STATUS_DESTROYED = INTENT_PACKAGE_PREFIX + "STATUS_DESTROYED";
	public static final String BROADCAST_USER_LIST_MEMBERS_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_DELETED";
	public static final String BROADCAST_USER_LIST_MEMBERS_ADDED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_ADDED";
	public static final String BROADCAST_USER_LIST_SUBSCRIBED = INTENT_PACKAGE_PREFIX + "USER_LIST_SUBSRCIBED";
	public static final String BROADCAST_USER_LIST_UNSUBSCRIBED = INTENT_PACKAGE_PREFIX + "USER_LIST_UNSUBSCRIBED";
	public static final String BROADCAST_USER_LIST_CREATED = INTENT_PACKAGE_PREFIX + "USER_LIST_CREATED";
	public static final String BROADCAST_USER_LIST_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_DELETED";
	public static final String BROADCAST_FILTERS_UPDATED = INTENT_PACKAGE_PREFIX + "FILTERS_UPDATED";
	public static final String BROADCAST_REFRESH_HOME_TIMELINE = INTENT_PACKAGE_PREFIX + "REFRESH_HOME_TIMELINE";
	public static final String BROADCAST_REFRESH_MENTIONS = INTENT_PACKAGE_PREFIX + "REFRESH_MENTIONS";
	public static final String BROADCAST_REFRESH_DIRECT_MESSAGES = INTENT_PACKAGE_PREFIX + "REFRESH_DIRECT_MESSAGES";
	public static final String BROADCAST_REFRESH_TRENDS = INTENT_PACKAGE_PREFIX + "REFRESH_TRENDS";
	public static final String BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING = INTENT_PACKAGE_PREFIX
			+ "RESCHEDULE_HOME_TIMELINE_REFRESHING";
	public static final String BROADCAST_RESCHEDULE_MENTIONS_REFRESHING = INTENT_PACKAGE_PREFIX
			+ "RESCHEDULE_MENTIONS_REFRESHING";
	public static final String BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING = INTENT_PACKAGE_PREFIX
			+ "RESCHEDULE_DIRECT_MESSAGES_REFRESHING";
	public static final String BROADCAST_RESCHEDULE_TRENDS_REFRESHING = INTENT_PACKAGE_PREFIX
			+ "RESCHEDULE_TRENDS_REFRESHING";
	public static final String BROADCAST_MULTI_BLOCKSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "MULTI_BLOCKSTATE_CHANGED";
	public static final String BROADCAST_MULTI_MUTESTATE_CHANGED = INTENT_PACKAGE_PREFIX + "MULTI_MUTESTATE_CHANGED";
	public static final String BROADCAST_HOME_ACTIVITY_ONCREATE = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONCREATE";
	public static final String BROADCAST_HOME_ACTIVITY_ONSTART = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONSTART";
	public static final String BROADCAST_HOME_ACTIVITY_ONSTOP = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONSTOP";
	public static final String BROADCAST_HOME_ACTIVITY_ONDESTROY = INTENT_PACKAGE_PREFIX + "HOME_ACTIVITY_ONDESTROY";
	public static final String BROADCAST_UNREAD_COUNT_UPDATED = INTENT_PACKAGE_PREFIX + "UNREAD_COUNT_UPDATED";
	public static final String BROADCAST_DATABASE_READY = INTENT_PACKAGE_PREFIX + "DATABASE_READY";

	public static final String EXTRA_LATITUDE = "latitude";
	public static final String EXTRA_LONGITUDE = "longitude";
	public static final String EXTRA_URI = "uri";
	public static final String EXTRA_URI_ORIG = "uri_orig";
	public static final String EXTRA_MENTIONS = "mentions";
	public static final String EXTRA_ACCOUNT_ID = "account_id";
	public static final String EXTRA_ACCOUNT_IDS = "account_ids";
	public static final String EXTRA_PAGE = "page";
	public static final String EXTRA_DATA = "data";
	public static final String EXTRA_QUERY = "query";
	public static final String EXTRA_QUERY_TYPE = "query_type";
	public static final String EXTRA_USER_ID = "user_id";
	public static final String EXTRA_USER_IDS = "user_ids";
	public static final String EXTRA_LIST_ID = "list_id";
	public static final String EXTRA_MAX_ID = "max_id";
	public static final String EXTRA_MAX_IDS = "max_ids";
	public static final String EXTRA_SINCE_ID = "since_id";
	public static final String EXTRA_SINCE_IDS = "since_ids";
	public static final String EXTRA_STATUS_ID = "status_id";
	public static final String EXTRA_SCREEN_NAME = "screen_name";
	public static final String EXTRA_SCREEN_NAMES = "screen_names";
	public static final String EXTRA_LIST_NAME = "list_name";
	public static final String EXTRA_DESCRIPTION = "description";
	public static final String EXTRA_IN_REPLY_TO_ID = "in_reply_to_id";
	public static final String EXTRA_IN_REPLY_TO_NAME = "in_reply_to_name";
	public static final String EXTRA_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
	public static final String EXTRA_TEXT = "text";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_TYPE = "type";
	public static final String EXTRA_SUCCEED = "succeed";
	public static final String EXTRA_IDS = "ids";
	public static final String EXTRA_IS_SHARE = "is_share";
	public static final String EXTRA_STATUS = "status";
	public static final String EXTRA_STATUSES = "statuses";
	public static final String EXTRA_DRAFT = "draft";
	public static final String EXTRA_FAVORITED = "favorited";
	public static final String EXTRA_RETWEETED = "retweeted";
	public static final String EXTRA_FILENAME = "filename";
	public static final String EXTRA_FILE_SOURCE = "file_source";
	public static final String EXTRA_FILE_EXTENSIONS = "file_extensions";
	public static final String EXTRA_ITEMS_INSERTED = "items_inserted";
	public static final String EXTRA_INITIAL_TAB = "initial_tab";
	public static final String EXTRA_NOTIFICATION_ID = "notification_id";
	public static final String EXTRA_NOTIFICATION_ACCOUNT = "notification_account";
	public static final String EXTRA_FROM_NOTIFICATION = "from_notification";
	public static final String EXTRA_IS_PUBLIC = "is_public";
	public static final String EXTRA_USER = "user";
	public static final String EXTRA_USERS = "users";
	public static final String EXTRA_USER_LIST = "user_list";
	public static final String EXTRA_APPEND_TEXT = "append_text";
	public static final String EXTRA_NAME = "name";
	public static final String EXTRA_TEXT1 = "text1";
	public static final String EXTRA_TEXT2 = "text2";
	public static final String EXTRA_POSITION = "position";
	public static final String EXTRA_ARGUMENTS = "arguments";
	public static final String EXTRA_ICON = "icon";
	public static final String EXTRA_ID = "id";
	public static final String EXTRA_RESID = "resid";
	public static final String EXTRA_IMAGE_URI = "image_uri";
	public static final String EXTRA_ATTACHED_IMAGE_TYPE = "attached_image_type";
	public static final String EXTRA_ACTIVATED_ONLY = "activated_only";
	public static final String EXTRA_TAB_POSITION = "tab_position";
	public static final String EXTRA_HAS_RUNNING_TASK = "has_running_task";
	public static final String EXTRA_OAUTH_VERIFIER = "oauth_verifier";
	public static final String EXTRA_REQUEST_TOKEN = "request_token";
	public static final String EXTRA_REQUEST_TOKEN_SECRET = "request_token_secret";
	public static final String EXTRA_OMIT_INTENT_EXTRA = "omit_intent_extra";
	public static final String EXTRA_COMMAND = "command";
	public static final String EXTRA_WIDTH = "width";
	public static final String EXTRA_ALLOW_SELECT_NONE = "allow_select_none";
	public static final String EXTRA_OAUTH_ONLY = "oauth_only";
	public static final String EXTRA_PERMISSIONS = "permissions";
	public static final String EXTRA_LOCATION = "location";
	public static final String EXTRA_URL = "url";
	public static final String EXTRA_NEXT_CURSOR = "next_cursor";
	public static final String EXTRA_PREV_CURSOR = "prev_cursor";
	public static final String EXTRA_EXTRA_INTENT = "extra_intent";
	public static final String EXTRA_IS_MY_ACCOUNT = "is_my_account";
	public static final String EXTRA_TAB_TYPE = "tab_type";
	public static final String EXTRA_ACCOUNT = "account";
	public static final String EXTRA_ACTIVITY_SCREENSHOT_ID = "activity_screenshot_id";
	public static final String EXTRA_COLOR = "color";
	public static final String EXTRA_ALPHA_SLIDER = "alpha_slider";
	public static final String EXTRA_OPEN_ACCOUNTS_DRAWER = "open_accounts_drawer";
	public static final String EXTRA_RECIPIENT_ID = "recipient_id";
	public static final String EXTRA_OFFICIAL_KEY_ONLY = "official_key_only";
	public static final String EXTRA_SEARCH_ID = "search_id";
	public static final String EXTRA_CLEAR_BUTTON = "clear_button";

	public static final int MENU_GROUP_STATUS_EXTENSION = 10;
	public static final int MENU_GROUP_COMPOSE_EXTENSION = 11;
	public static final int MENU_GROUP_IMAGE_EXTENSION = 12;
	public static final int MENU_GROUP_STATUS_SHARE = 20;

	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_PICK_IMAGE = 2;
	public static final int REQUEST_SELECT_ACCOUNT = 3;
	public static final int REQUEST_COMPOSE = 4;
	public static final int REQUEST_EDIT_API = 5;
	public static final int REQUEST_BROWSER_SIGN_IN = 6;
	public static final int REQUEST_SET_COLOR = 7;
	public static final int REQUEST_SAVE_FILE = 8;
	public static final int REQUEST_EDIT_IMAGE = 9;
	public static final int REQUEST_EXTENSION_COMPOSE = 10;
	public static final int REQUEST_ADD_TAB = 11;
	public static final int REQUEST_EDIT_TAB = 12;
	public static final int REQUEST_PICK_FILE = 13;
	public static final int REQUEST_PICK_DIRECTORY = 14;
	public static final int REQUEST_ADD_TO_LIST = 15;
	public static final int REQUEST_SELECT_USER = 16;
	public static final int REQUEST_SELECT_USER_LIST = 17;
	public static final int REQUEST_SWIPEBACK_ACTIVITY = 101;

	public static final int TABLE_ID_ACCOUNTS = 1;
	public static final int TABLE_ID_STATUSES = 12;
	public static final int TABLE_ID_MENTIONS = 13;
	public static final int TABLE_ID_DIRECT_MESSAGES = 21;
	public static final int TABLE_ID_DIRECT_MESSAGES_INBOX = 22;
	public static final int TABLE_ID_DIRECT_MESSAGES_OUTBOX = 23;
	public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATION = 24;
	public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = 25;
	public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRIES = 26;
	public static final int TABLE_ID_FILTERED_USERS = 31;
	public static final int TABLE_ID_FILTERED_KEYWORDS = 32;
	public static final int TABLE_ID_FILTERED_SOURCES = 33;
	public static final int TABLE_ID_FILTERED_LINKS = 34;
	public static final int TABLE_ID_TRENDS_LOCAL = 41;
	public static final int TABLE_ID_DRAFTS = 51;
	public static final int TABLE_ID_TABS = 52;
	public static final int TABLE_ID_CACHED_USERS = 61;
	public static final int TABLE_ID_CACHED_STATUSES = 62;
	public static final int TABLE_ID_CACHED_HASHTAGS = 63;
	public static final int VIRTUAL_TABLE_ID_DATABASE_READY = 100;
	public static final int VIRTUAL_TABLE_ID_NOTIFICATIONS = 101;
	public static final int VIRTUAL_TABLE_ID_PREFERENCES = 102;
	public static final int VIRTUAL_TABLE_ID_ALL_PREFERENCES = 103;
	public static final int VIRTUAL_TABLE_ID_PERMISSIONS = 104;
	public static final int VIRTUAL_TABLE_ID_DNS = 105;
	public static final int VIRTUAL_TABLE_ID_CACHED_IMAGES = 106;
	public static final int VIRTUAL_TABLE_ID_CACHE_FILES = 107;
	public static final int VIRTUAL_TABLE_ID_UNREAD_COUNTS = 108;
	public static final int VIRTUAL_TABLE_ID_UNREAD_COUNTS_BY_TYPE = 109;

	public static final int NOTIFICATION_ID_HOME_TIMELINE = 1;
	public static final int NOTIFICATION_ID_MENTIONS = 2;
	public static final int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
	public static final int NOTIFICATION_ID_DRAFTS = 4;
	public static final int NOTIFICATION_ID_DATA_PROFILING = 5;
	public static final int NOTIFICATION_ID_UPDATE_STATUS = 101;
	public static final int NOTIFICATION_ID_SEND_DIRECT_MESSAGE = 102;

	public static final String ICON_SPECIAL_TYPE_CUSTOMIZE = "_customize";

	public static final String TASK_TAG_GET_HOME_TIMELINE = "get_home_tomeline";
	public static final String TASK_TAG_GET_MENTIONS = "get_mentions";
	public static final String TASK_TAG_GET_SENT_DIRECT_MESSAGES = "get_sent_direct_messages";
	public static final String TASK_TAG_GET_RECEIVED_DIRECT_MESSAGES = "get_received_direct_messages";
	public static final String TASK_TAG_GET_TRENDS = "get_trends";
	public static final String TASK_TAG_STORE_HOME_TIMELINE = "store_home_tomeline";
	public static final String TASK_TAG_STORE_MENTIONS = "store_mentions";
	public static final String TASK_TAG_STORE_SENT_DIRECT_MESSAGES = "store_sent_direct_messages";
	public static final String TASK_TAG_STORE_RECEIVED_DIRECT_MESSAGES = "store_received_direct_messages";
	public static final String TASK_TAG_STORE_TRENDS = "store_trends";

	public static final String SERVICE_COMMAND_REFRESH_ALL = "refresh_all";
	public static final String SERVICE_COMMAND_GET_HOME_TIMELINE = "get_home_timeline";
	public static final String SERVICE_COMMAND_GET_MENTIONS = "get_mentions";
	public static final String SERVICE_COMMAND_GET_SENT_DIRECT_MESSAGES = "get_sent_direct_messages";
	public static final String SERVICE_COMMAND_GET_RECEIVED_DIRECT_MESSAGES = "get_received_direct_messages";

	public static final String METADATA_KEY_PERMISSIONS = "org.mariotaku.twidere.extension.permissions";
	public static final String METADATA_KEY_SETTINGS = "org.mariotaku.twidere.extension.settings";
	public static final String METADATA_KEY_EXTENSION = "org.mariotaku.twidere.extension";

	public static final int PERMISSION_DENIED = -1;
	public static final int PERMISSION_INVALID = 0;
	public static final int PERMISSION_NONE = 1;
	public static final int PERMISSION_REFRESH = 2;
	public static final int PERMISSION_READ = 3;
	public static final int PERMISSION_WRITE = 5;
	public static final int PERMISSION_DIRECT_MESSAGES = 7;
	public static final int PERMISSION_ACCOUNTS = 11;
	public static final int PERMISSION_PREFERENCES = 13;

	public static final int ATTACHED_IMAGE_TYPE_NONE = 0;
	public static final int ATTACHED_IMAGE_TYPE_PHOTO = 1;
	public static final int ATTACHED_IMAGE_TYPE_IMAGE = 2;

	public static final String TAB_TYPE_HOME_TIMELINE = "home_timeline";
	public static final String TAB_TYPE_MENTIONS_TIMELINE = "mentions_timeline";
	public static final String TAB_TYPE_TRENDS_SUGGESTIONS = "trends_suggestions";
	public static final String TAB_TYPE_DIRECT_MESSAGES = "direct_messages";
	public static final String TAB_TYPE_FAVORITES = "favorites";
	public static final String TAB_TYPE_USER_TIMELINE = "user_timeline";
	public static final String TAB_TYPE_SEARCH_STATUSES = "search_statuses";
	public static final String TAB_TYPE_LIST_TIMELINE = "list_timeline";
	public static final String TAB_TYPE_ACTIVITIES_ABOUT_ME = "activities_about_me";
	public static final String TAB_TYPE_ACTIVITIES_BY_FRIENDS = "activities_by_friends";
	public static final String TAB_TYPE_STAGGERED_HOME_TIMELINE = "staggered_home_timeline";

	public static final int TWITTER_MAX_IMAGE_SIZE = 3145728;
	public static final int TWITTER_MAX_IMAGE_WIDTH = 1024;
	public static final int TWITTER_MAX_IMAGE_HEIGHT = 2048;

}
