/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

public interface Constants {

	public static final String APP_NAME = "Twidere";

	public static final String APP_PROJECT_URL = "https://github.com/mariotaku/twidere";

	public static final String LOGTAG = APP_NAME;

	public static final String USER_NICKNAME_PREFERENCES_NAME = "user_nicknames";
	public static final String USER_COLOR_PREFERENCES_NAME = "user_colors";
	public static final String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
	public static final String SHARED_PREFERENCES_NAME = "preferences";
	public static final String PERMISSION_PREFERENCES_NAME = "permissions";
	public static final String SILENT_NOTIFICATIONS_PREFERENCE_NAME = "silent_notifications";
	public static final String TIMELINE_POSITIONS_PREFERENCES_NAME = "timeline_positions";

	public static final String DATABASES_NAME = "twidere.sqlite";
	public static final int DATABASES_VERSION = 47;

	public static final String TWITTER_CONSUMER_KEY = "uAFVpMhBntJutfVj6abfA";
	public static final String TWITTER_CONSUMER_SECRET = "JARXkJTfxo0F8MyctYy9bUmrLISjo8vXAHsZHYuk2E";
	public static final String TWITTER_CONSUMER_KEY_2 = "UyaS0xmUQXKiJ48vZP4dXQ";
	public static final String TWITTER_CONSUMER_SECRET_2 = "QlYVMWA751Dl5yNve41CNEN46GV4nxk57FmLeAXAV0";

	public static final String GOOGLE_MAPS_API_KEY_RELEASE = "0kjPwJOe_zwYjzGc9uYak7vhm_Sf3eob-2L3Xzw";
	public static final String GOOGLE_MAPS_API_KEY_DEBUG = "0kjPwJOe_zwY9p6kT-kygu4mxwysyOOpfkaXqTA";

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
	public static final String AUTHORITY_LIST_DETAILS = "list_details";
	public static final String AUTHORITY_LIST_TYPES = "list_types";
	public static final String AUTHORITY_LIST_TIMELINE = "list_timeline";
	public static final String AUTHORITY_LIST_MEMBERS = "list_members";
	public static final String AUTHORITY_LIST_MEMBERSHIPS = "list_memberships";
	public static final String AUTHORITY_LISTS = "lists";
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

	public static final String QUERY_PARAM_ACCOUNT_ID = "account_id";
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
	public static final String QUERY_PARAM_CONVERSATION_ID = "conversation_id";
	public static final String QUERY_PARAM_URL = "url";
	public static final String QUERY_PARAM_NAME = "name";
	public static final String QUERY_PARAM_FINISH_ONLY = "finish_only";
	public static final String QUERY_PARAM_NEW_ITEMS_COUNT = "new_items_count";

	public static final String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

	public static final String OAUTH_CALLBACK_OOB = "oob";
	public static final String OAUTH_CALLBACK_URL = PROTOCOL_TWIDERE + "com.twitter.oauth/";

	public static final String FORMAT_PATTERN_TITLE = "[TITLE]";
	public static final String FORMAT_PATTERN_TEXT = "[TEXT]";
	public static final String FORMAT_PATTERN_NAME = "[NAME]";
	public static final String FORMAT_PATTERN_LINK = "[LINK]";

	public static final String PREFERENCE_KEY_DATABASE_ITEM_LIMIT = "database_item_limit";
	public static final String PREFERENCE_KEY_LOAD_ITEM_LIMIT = "load_item_limit";
	public static final String PREFERENCE_KEY_TEXT_SIZE = "text_size_int";
	public static final String PREFERENCE_KEY_THEME = "theme";
	public static final String PREFERENCE_KEY_SOLID_COLOR_BACKGROUND = "solid_color_background";
	public static final String PREFERENCE_KEY_CLEAR_DATABASES = "clear_databases";
	public static final String PREFERENCE_KEY_CLEAR_CACHE = "clear_cache";
	public static final String PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";
	public static final String PREFERENCE_KEY_DISPLAY_IMAGE_PREVIEW = "display_image_preview";
	public static final String PREFERENCE_KEY_BOTTOM_COMPOSE_BUTTON = "bottom_compose_button";
	public static final String PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON = "leftside_compose_button";
	public static final String PREFERENCE_KEY_BOTTOM_SEND_BUTTON = "bottom_send_button";
	public static final String PREFERENCE_KEY_ATTACH_LOCATION = "attach_location";
	public static final String PREFERENCE_KEY_GZIP_COMPRESSING = "gzip_compressing";
	public static final String PREFERENCE_KEY_IGNORE_SSL_ERROR = "ignore_ssl_error";
	public static final String PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY = "load_more_automatically";
	public static final String PREFERENCE_KEY_CONSUMER_KEY = "consumer_key";
	public static final String PREFERENCE_KEY_CONSUMER_SECRET = "consumer_secret";
	public static final String PREFERENCE_KEY_QUOTE_FORMAT = "quote_format";
	public static final String PREFERENCE_KEY_DEFAULT_ACCOUNT_ID = "default_account_id";
	public static final String PREFERENCE_KEY_REMEMBER_POSITION = "remember_position";
	public static final String PREFERENCE_KEY_SAVED_TAB_POSITION = "saved_tab_position";
	public static final String PREFERENCE_KEY_ENABLE_PROXY = "enable_proxy";
	public static final String PREFERENCE_KEY_PROXY_HOST = "proxy_host";
	public static final String PREFERENCE_KEY_PROXY_PORT = "proxy_port";
	public static final String PREFERENCE_KEY_REFRESH_ON_START = "refresh_on_start";
	public static final String PREFERENCE_KEY_REFRESH_AFTER_TWEET = "refresh_after_tweet";
	public static final String PREFERENCE_KEY_AUTO_REFRESH = "auto_refresh";
	public static final String PREFERENCE_KEY_REFRESH_INTERVAL = "refresh_interval";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE = "refresh_enable_home_timeline";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS = "refresh_enable_mentions";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES = "refresh_enable_direct_messages";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_TRENDS = "refresh_enable_trends";
	public static final String PREFERENCE_KEY_NOTIFICATION_ENABLE_HOME_TIMELINE = "notification_enable_home_timeline";
	public static final String PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS = "notification_enable_mentions";
	public static final String PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES = "notification_enable_direct_messages";
	public static final String PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND = "notification_have_sound";
	public static final String PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION = "notification_have_vibration";
	public static final String PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS = "notification_have_lights";
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
	public static final String PREFERENCE_KEY_LINK_TO_QUOTED_TWEET = "link_to_quoted_tweet";
	public static final String PREFERENCE_KEY_BACKGROUND_TOAST_NOTIFICATION = "background_toast_notification";
	public static final String PREFERENCE_KEY_COMPOSE_QUIT_ACTION = "compose_quit_action";
	public static final String PREFERENCE_KEY_NO_CLOSE_AFTER_TWEET_SENT = "no_close_after_tweet_sent";
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

	public static final String PREFERENCE_DEFAULT_QUOTE_FORMAT = "RT @" + FORMAT_PATTERN_NAME + ": "
			+ FORMAT_PATTERN_TEXT;
	public static final String PREFERENCE_DEFAULT_SHARE_FORMAT = FORMAT_PATTERN_TITLE + " - " + FORMAT_PATTERN_TEXT;
	public static final String PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT = FORMAT_PATTERN_TEXT + " " + FORMAT_PATTERN_LINK;

	public static final int PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT = 100;
	public static final int PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT = 20;
	public static final boolean PREFERENCE_DEFAULT_HARDWARE_ACCELERATION = true;

	public static final String LINK_HIGHLIGHT_OPTION_NONE = "none";
	public static final String LINK_HIGHLIGHT_OPTION_HIGHLIGHT = "highlight";
	public static final String LINK_HIGHLIGHT_OPTION_UNDERLINE = "underline";
	public static final String LINK_HIGHLIGHT_OPTION_BOTH = "both";
	public static final int LINK_HIGHLIGHT_OPTION_CODE_NONE = 0;
	public static final int LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT = 1;
	public static final int LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE = 2;
	public static final int LINK_HIGHLIGHT_OPTION_CODE_BOTH = 3;

	public static final String COMPOSE_QUIT_ACTION_ASK = "ask";
	public static final String COMPOSE_QUIT_ACTION_SAVE = "save";
	public static final String COMPOSE_QUIT_ACTION_DISCARD = "discard";

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
	public static final String INTENT_ACTION_EDIT_HOST_MAPPING = INTENT_PACKAGE_PREFIX + "EDIT_HOST_MAPPING";
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

	public static final String BROADCAST_HOME_TIMELINE_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX
			+ "HOME_TIMELINE_DATABASE_UPDATED";
	public static final String BROADCAST_MENTIONS_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX
			+ "MENTIONS_DATABASE_UPDATED";
	public static final String BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX
			+ "ACCOUNT_LIST_DATABASE_UPDATED";
	public static final String BROADCAST_DRAFTS_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX + "DRAFTS_DATABASE_UPDATED";
	public static final String BROADCAST_HOME_TIMELINE_REFRESHED = INTENT_PACKAGE_PREFIX + "HOME_TIMELINE_REFRESHED";
	public static final String BROADCAST_MENTIONS_REFRESHED = INTENT_PACKAGE_PREFIX + "MENTIONS_REFRESHED";
	public static final String BROADCAST_TASK_STATE_CHANGED = INTENT_PACKAGE_PREFIX + "TASK_STATE_CHANGED";
	public static final String BROADCAST_NOTIFICATION_CLEARED = INTENT_PACKAGE_PREFIX + "NOTIFICATION_CLEARED";
	public static final String BROADCAST_FRIENDSHIP_CHANGED = INTENT_PACKAGE_PREFIX + "FRIENDSHIP_CHANGED";
	public static final String BROADCAST_BLOCKSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "BLOCKSTATE_CHANGED";
	public static final String BROADCAST_PROFILE_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_UPDATED";
	public static final String BROADCAST_PROFILE_IMAGE_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_IMAGE_UPDATED";
	public static final String BROADCAST_PROFILE_BANNER_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_BANNER_UPDATED";
	public static final String BROADCAST_USER_LIST_DETAILS_UPDATED = INTENT_PACKAGE_PREFIX
			+ "USER_LIST_DETAILS_UPDATED";
	public static final String BROADCAST_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX + "DATABASE_UPDATED";
	public static final String BROADCAST_FAVORITE_CHANGED = INTENT_PACKAGE_PREFIX + "FAVORITE_CHANGED";
	public static final String BROADCAST_RETWEET_CHANGED = INTENT_PACKAGE_PREFIX + "RETWEET_CHANGED";
	public static final String BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED = INTENT_PACKAGE_PREFIX
			+ "RECEIVED_DIRECT_MESSAGES_REFRESHED";
	public static final String BROADCAST_SENT_DIRECT_MESSAGES_REFRESHED = INTENT_PACKAGE_PREFIX
			+ "SENT_DIRECT_MESSAGES_REFRESHED";
	public static final String BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX
			+ "RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED";
	public static final String BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED = INTENT_PACKAGE_PREFIX
			+ "SENT_DIRECT_MESSAGES_DATABASE_UPDATED";
	public static final String BROADCAST_STATUS_DESTROYED = INTENT_PACKAGE_PREFIX + "STATUS_DESTROYED";
	public static final String BROADCAST_TRENDS_UPDATED = INTENT_PACKAGE_PREFIX + "TRENDS_UPDATED";
	public static final String BROADCAST_USER_LIST_MEMBERS_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_DELETED";
	public static final String BROADCAST_USER_LIST_MEMBERS_ADDED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_ADDED";
	public static final String BROADCAST_USER_LIST_SUBSCRIBED = INTENT_PACKAGE_PREFIX + "USER_LIST_SUBSRCIBED";
	public static final String BROADCAST_USER_LIST_UNSUBSCRIBED = INTENT_PACKAGE_PREFIX + "USER_LIST_UNSUBSCRIBED";
	public static final String BROADCAST_USER_LIST_CREATED = INTENT_PACKAGE_PREFIX + "USER_LIST_CREATED";
	public static final String BROADCAST_USER_LIST_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_DELETED";
	public static final String BROADCAST_TABS_UPDATED = INTENT_PACKAGE_PREFIX + "TABS_UPDATED";
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

	public static final String EXTRA_LATITUDE = "latitude";
	public static final String EXTRA_LONGITUDE = "longitude";
	public static final String EXTRA_URI = "uri";
	public static final String EXTRA_URI_ORIG = "uri_orig";
	public static final String EXTRA_MENTIONS = "mentions";
	public static final String EXTRA_ACCOUNT_ID = "account_id";
	public static final String EXTRA_ACCOUNT_IDS = "account_ids";
	public static final String EXTRA_CONVERSATION_ID = "conversation_id";
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

	public static final int MENU_HOME = android.R.id.home;
	public static final int MENU_SEARCH = R.id.search;
	public static final int MENU_ACTIONS = R.id.actions;
	public static final int MENU_COMPOSE = R.id.compose;
	public static final int MENU_SEND = R.id.send;
	public static final int MENU_EDIT = R.id.edit;
	public static final int MENU_SELECT_ACCOUNT = R.id.select_account;
	public static final int MENU_SETTINGS = R.id.settings;
	public static final int MENU_ADD_LOCATION = R.id.add_location;
	public static final int MENU_TAKE_PHOTO = R.id.take_photo;
	public static final int MENU_ADD_IMAGE = R.id.add_image;
	public static final int MENU_LOCATION = R.id.location;
	public static final int MENU_IMAGE = R.id.image;
	public static final int MENU_VIEW = R.id.view;
	public static final int MENU_VIEW_PROFILE = R.id.view_profile;
	public static final int MENU_DELETE = R.id.delete;
	public static final int MENU_DELETE_SUBMENU = R.id.delete_submenu;
	public static final int MENU_TOGGLE = R.id.toggle;
	public static final int MENU_ADD = R.id.add;
	public static final int MENU_PICK_FROM_GALLERY = R.id.pick_from_gallery;
	public static final int MENU_PICK_FROM_MAP = R.id.pick_from_map;
	public static final int MENU_EDIT_API = R.id.edit_api;
	public static final int MENU_OPEN_IN_BROWSER = R.id.open_in_browser;
	public static final int MENU_SET_COLOR = R.id.set_color;
	public static final int MENU_ADD_ACCOUNT = R.id.add_account;
	public static final int MENU_REPLY = R.id.reply;
	public static final int MENU_FAVORITE = R.id.favorite;
	public static final int MENU_RETWEET = R.id.retweet;
	public static final int MENU_QUOTE = R.id.quote;
	public static final int MENU_SHARE = R.id.share;
	public static final int MENU_DRAFTS = R.id.drafts;
	public static final int MENU_DELETE_ALL = R.id.delete_all;
	public static final int MENU_SET_AS_DEFAULT = R.id.set_as_default;
	public static final int MENU_SAVE = R.id.save;
	public static final int MENU_CANCEL = R.id.cancel;
	public static final int MENU_BLOCK = R.id.block;
	public static final int MENU_REPORT_SPAM = R.id.report_spam;
	public static final int MENU_MUTE_SOURCE = R.id.mute_source;
	public static final int MENU_MUTE_USER = R.id.mute_user;
	public static final int MENU_REFRESH = R.id.refresh;
	public static final int MENU_MENTION = R.id.mention;
	public static final int MENU_SEND_DIRECT_MESSAGE = R.id.send_direct_message;
	public static final int MENU_VIEW_USER_LIST = R.id.view_user_list;
	public static final int MENU_UP = R.id.up;
	public static final int MENU_DOWN = R.id.down;
	public static final int MENU_MULTI_SELECT = R.id.multi_select;
	public static final int MENU_CLEAR_COLOR = R.id.clear_color;
	public static final int MENU_COPY = R.id.copy;
	public static final int MENU_TOGGLE_SENSITIVE = R.id.toggle_sensitive;
	public static final int MENU_REVOKE = R.id.revoke;
	public static final int MENU_IMPORT_FROM = R.id.import_from;
	public static final int MENU_ADD_TO_LIST = R.id.add_to_list;
	public static final int MENU_STATUSES = R.id.statuses;
	public static final int MENU_FAVORITES = R.id.favorites;
	public static final int MENU_LISTS = R.id.lists;
	public static final int MENU_LIST_MEMBERSHIPS = R.id.list_memberships;
	public static final int MENU_CENTER = R.id.center;
	public static final int MENU_FILTERS = R.id.filters;
	public static final int MENU_SET_NICKNAME = R.id.set_nickname;
	public static final int MENU_CLEAR_NICKNAME = R.id.clear_nickname;

	public static final int MENU_GROUP_STATUS_EXTENSION = 10;
	public static final int MENU_GROUP_COMPOSE_EXTENSION = 11;
	public static final int MENU_GROUP_IMAGE_EXTENSION = 12;

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

	public static final String TABLE_ACCOUNTS = Accounts.TABLE_NAME;
	public static final String TABLE_STATUSES = Statuses.TABLE_NAME;
	public static final String TABLE_MENTIONS = Mentions.TABLE_NAME;
	public static final String TABLE_DRAFTS = Drafts.TABLE_NAME;
	public static final String TABLE_CACHED_HASHTAGS = CachedHashtags.TABLE_NAME;
	public static final String TABLE_CACHED_USERS = CachedUsers.TABLE_NAME;
	public static final String TABLE_CACHED_STATUSES = CachedStatuses.TABLE_NAME;
	public static final String TABLE_FILTERED_USERS = Filters.Users.TABLE_NAME;
	public static final String TABLE_FILTERED_KEYWORDS = Filters.Keywords.TABLE_NAME;
	public static final String TABLE_FILTERED_SOURCES = Filters.Sources.TABLE_NAME;
	public static final String TABLE_FILTERED_LINKS = Filters.Links.TABLE_NAME;
	public static final String TABLE_DIRECT_MESSAGES = DirectMessages.TABLE_NAME;
	public static final String TABLE_DIRECT_MESSAGES_INBOX = DirectMessages.Inbox.TABLE_NAME;
	public static final String TABLE_DIRECT_MESSAGES_OUTBOX = DirectMessages.Outbox.TABLE_NAME;
	public static final String TABLE_DIRECT_MESSAGES_CONVERSATION = DirectMessages.Conversation.TABLE_NAME;
	public static final String TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = DirectMessages.Conversation.TABLE_NAME_SCREEN_NAME;
	public static final String TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY = DirectMessages.ConversationsEntry.TABLE_NAME;
	public static final String TABLE_TRENDS_LOCAL = CachedTrends.Local.TABLE_NAME;
	public static final String TABLE_TABS = Tabs.TABLE_NAME;
	public static final String TABLE_NOTIFICATIONS = TweetStore.Notifications.TABLE_NAME;
	public static final String TABLE_PREFERENCES = TweetStore.Preferences.TABLE_NAME;
	public static final String TABLE_PERMISSIONS = TweetStore.Permissions.TABLE_NAME;
	public static final String TABLE_DNS = TweetStore.DNS.TABLE_NAME;
	public static final String TABLE_CACHED_IMAGES = TweetStore.CachedImages.TABLE_NAME;
	public static final String TABLE_CACHE_FILES = TweetStore.CacheFiles.TABLE_NAME;

	public static final int TABLE_ID_ACCOUNTS = 1;
	public static final int TABLE_ID_STATUSES = 2;
	public static final int TABLE_ID_MENTIONS = 3;
	public static final int TABLE_ID_DIRECT_MESSAGES = 11;
	public static final int TABLE_ID_DIRECT_MESSAGES_INBOX = 12;
	public static final int TABLE_ID_DIRECT_MESSAGES_OUTBOX = 13;
	public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATION = 14;
	public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = 15;
	public static final int TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY = 16;
	public static final int TABLE_ID_FILTERED_USERS = 21;
	public static final int TABLE_ID_FILTERED_KEYWORDS = 22;
	public static final int TABLE_ID_FILTERED_SOURCES = 23;
	public static final int TABLE_ID_FILTERED_LINKS = 24;
	public static final int TABLE_ID_TRENDS_LOCAL = 31;
	public static final int TABLE_ID_DRAFTS = 41;
	public static final int TABLE_ID_TABS = 42;
	public static final int TABLE_ID_CACHED_USERS = 51;
	public static final int TABLE_ID_CACHED_STATUSES = 52;
	public static final int TABLE_ID_CACHED_HASHTAGS = 53;
	public static final int VIRTUAL_TABLE_ID_NOTIFICATIONS = 101;
	public static final int VIRTUAL_TABLE_ID_PREFERENCES = 102;
	public static final int VIRTUAL_TABLE_ID_ALL_PREFERENCES = 103;
	public static final int VIRTUAL_TABLE_ID_PERMISSIONS = 104;
	public static final int VIRTUAL_TABLE_ID_DNS = 105;
	public static final int VIRTUAL_TABLE_ID_CACHED_IMAGES = 106;
	public static final int VIRTUAL_TABLE_ID_CACHE_FILES = 107;

	public static final int LINK_ID_STATUS = 1;
	public static final int LINK_ID_USER = 2;
	public static final int LINK_ID_USER_TIMELINE = 3;
	public static final int LINK_ID_USER_FAVORITES = 4;
	public static final int LINK_ID_USER_FOLLOWERS = 5;
	public static final int LINK_ID_USER_FRIENDS = 6;
	public static final int LINK_ID_USER_BLOCKS = 7;
	public static final int LINK_ID_DIRECT_MESSAGES_CONVERSATION = 9;
	public static final int LINK_ID_LIST_DETAILS = 10;
	public static final int LINK_ID_LISTS = 11;
	public static final int LINK_ID_LIST_TIMELINE = 12;
	public static final int LINK_ID_LIST_MEMBERS = 13;
	public static final int LINK_ID_LIST_SUBSCRIBERS = 14;
	public static final int LINK_ID_LIST_MEMBERSHIPS = 15;
	public static final int LINK_ID_SAVED_SEARCHES = 19;
	public static final int LINK_ID_USER_MENTIONS = 21;
	public static final int LINK_ID_INCOMING_FRIENDSHIPS = 22;
	public static final int LINK_ID_USERS = 23;
	public static final int LINK_ID_STATUSES = 24;
	public static final int LINK_ID_STATUS_RETWEETERS = 25;
	public static final int LINK_ID_SEARCH = 26;

	public static final String DIR_NAME_IMAGE_CACHE = "image_cache";

	public static final int PANE_LEFT = R.id.fragment_container_left;
	public static final int PANE_RIGHT = R.id.fragment_container_right;

	public static final int NOTIFICATION_ID_HOME_TIMELINE = 1;
	public static final int NOTIFICATION_ID_MENTIONS = 2;
	public static final int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
	public static final int NOTIFICATION_ID_DRAFTS = 4;
	public static final int NOTIFICATION_ID_DATA_PROFILING = 5;
	public static final int NOTIFICATION_ID_UPDATE_STATUS = 6;

	public static final String ICON_SPECIAL_TYPE_CUSTOMIZE = "_customize";

	public static final String FRAGMENT_TAG_API_UPGRADE_NOTICE = "api_upgrade_notice";

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

	public static final String TWIDERE_PREVIEW_NICKNAME = "Twidere";
	public static final String TWIDERE_PREVIEW_NAME = "Twidere Project";
	public static final String TWIDERE_PREVIEW_SCREEN_NAME = "TwidereProject";
	public static final String TWIDERE_PREVIEW_TEXT_HTML = "Twidere is an open source twitter client for Android, see <a href='https://github.com/mariotaku/twidere'>github.com/mariotak&#8230;<a/>";
	public static final String TWIDERE_PREVIEW_SOURCE = "Twidere for Android";

}
