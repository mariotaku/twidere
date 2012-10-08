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

import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import twitter4j.TwitterConstants;

public interface Constants extends TwitterConstants {

	public static final String APP_NAME = "Twidere";

	public static final String APP_PROJECT_URL = "https://github.com/mariotaku/twidere";

	public static final boolean DEBUG = BuildConfig.DEBUG;

	public static final String LOGTAG = APP_NAME;

	public static final String USER_COLOR_PREFERENCES_NAME = "user_colors";
	public static final String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
	public static final String SHARED_PREFERENCES_NAME = "preferences";

	public static final String DATABASES_NAME = "twidere.sqlite";
	public static final int DATABASES_VERSION = 28;

	// Following 4 consants are not included in source code.
	public static final String TWITTER_CONSUMER_KEY = PrivateConstants.TWITTER_CONSUMER_KEY;
	public static final String TWITTER_CONSUMER_SECRET = PrivateConstants.TWITTER_CONSUMER_SECRET;
	public static final String GOOGLE_MAPS_API_KEY_RELEASE = PrivateConstants.GOOGLE_MAPS_API_KEY_RELEASE;
	public static final String GOOGLE_MAPS_API_KEY_DEBUG = PrivateConstants.GOOGLE_MAPS_API_KEY_DEBUG;

	public static final String GOOGLE_MAPS_API_KEY = DEBUG ? GOOGLE_MAPS_API_KEY_DEBUG : GOOGLE_MAPS_API_KEY_RELEASE;

	public static final String SCHEME_HTTP = "http";
	public static final String SCHEME_HTTPS = "https";
	public static final String SCHEME_CONTENT = "content";
	public static final String SCHEME_TWIDERE = "twidere";

	public static final String PROTOCOL_HTTP = SCHEME_HTTP + "://";
	public static final String PROTOCOL_HTTPS = SCHEME_HTTPS + "://";
	public static final String PROTOCOL_CONTENT = SCHEME_CONTENT + "://";
	public static final String PROTOCOL_TWIDERE = SCHEME_TWIDERE + "://";

	public static final String AUTHORITY_USER = "user";
	public static final String AUTHORITY_USER_TIMELINE = "user_timeline";
	public static final String AUTHORITY_USER_FAVORITES = "user_favorites";
	public static final String AUTHORITY_USER_FOLLOWERS = "user_followers";
	public static final String AUTHORITY_USER_FRIENDS = "user_friends";
	public static final String AUTHORITY_USER_BLOCKS = "user_blocks";
	public static final String AUTHORITY_STATUS = "status";
	public static final String AUTHORITY_CONVERSATION = "conversation";
	public static final String AUTHORITY_DIRECT_MESSAGES_CONVERSATION = "direct_messages_conversation";
	public static final String AUTHORITY_SEARCH = "search";
	public static final String AUTHORITY_MAP = "map";
	public static final String AUTHORITY_LIST_DETAILS = "list_details";
	public static final String AUTHORITY_LIST_TYPES = "list_types";
	public static final String AUTHORITY_LIST_TIMELINE = "list_timeline";
	public static final String AUTHORITY_LIST_MEMBERS = "list_members";
	public static final String AUTHORITY_LIST_SUBSCRIBERS = "list_subscribers";
	public static final String AUTHORITY_LIST_CREATED = "list_created";
	public static final String AUTHORITY_LIST_SUBSCRIPTIONS = "list_subscriptions";
	public static final String AUTHORITY_LIST_MEMBERSHIPS = "list_memberships";
	public static final String AUTHORITY_USERS_RETWEETED_STATUS = "users_retweeted_status";
	public static final String AUTHORITY_SAVED_SEARCHES = "saved_searches";
	public static final String AUTHORITY_SEARCH_USERS = "search_users";
	public static final String AUTHORITY_SEARCH_TWEETS = "search_tweets";
	public static final String AUTHORITY_DIRECT_MESSAGES = "direct_messages";
	public static final String AUTHORITY_TRENDS = "trends";
	public static final String AUTHORITY_USER_MENTIONS = "user_mentions";
	public static final String AUTHORITY_ACTIVITIES_ABOUT_ME = "activities_about_me";
	public static final String AUTHORITY_INCOMING_FRIENDSHIPS = "incoming_friendships";

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

	public static final String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

	public static final String DEFAULT_OAUTH_CALLBACK = PROTOCOL_TWIDERE + "com.twitter.oauth/";

	public static final String SHUFFIX_SCROLL_TO_TOP = ".SCROLL_TO_TOP";

	public static final String FORMAT_PATTERN_TITLE = "[TITLE]";
	public static final String FORMAT_PATTERN_TEXT = "[TEXT]";
	public static final String FORMAT_PATTERN_NAME = "[NAME]";
	public static final String FORMAT_PATTERN_LINK = "[LINK]";

	public static final String PREFERENCE_KEY_DATABASE_ITEM_LIMIT = "database_item_limit";
	public static final String PREFERENCE_KEY_LOAD_ITEM_LIMIT = "load_item_limit";
	public static final String PREFERENCE_KEY_TEXT_SIZE = "text_size_int";
	public static final String PREFERENCE_KEY_DARK_THEME = "dark_theme";
	public static final String PREFERENCE_KEY_SOLID_COLOR_BACKGROUND = "solid_color_background";
	public static final String PREFERENCE_KEY_CLEAR_DATABASES = "clear_databases";
	public static final String PREFERENCE_KEY_CLEAR_CACHE = "clear_cache";
	public static final String PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";
	public static final String PREFERENCE_KEY_INLINE_IMAGE_PREVIEW = "inline_image_preview";
	public static final String PREFERENCE_KEY_COMPOSE_BUTTON = "bottom_compose_button";
	public static final String PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON = "leftside_compose_button";
	public static final String PREFERENCE_KEY_ATTACH_LOCATION = "attach_location";
	public static final String PREFERENCE_KEY_ENABLE_FILTER = "enable_filter";
	public static final String PREFERENCE_KEY_GZIP_COMPRESSING = "gzip_compressing";
	public static final String PREFERENCE_KEY_IGNORE_SSL_ERROR = "ignore_ssl_error";
	public static final String PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY = "load_more_automatically";
	public static final String PREFERENCE_KEY_CONSUMER_KEY = "consumer_key";
	public static final String PREFERENCE_KEY_CONSUMER_SECRET = "consumer_secret";
	public static final String PREFERENCE_KEY_QUOTE_FORMAT = "quote_format";
	public static final String PREFERENCE_KEY_DEFAULT_ACCOUNT_ID = "default_account_id";
	public static final String PREFERENCE_KEY_REMEMBER_POSITION = "remember_position";
	public static final String PREFERENCE_KEY_SAVED_HOME_TIMELINE_ID = "saved_home_timeline_id";
	public static final String PREFERENCE_KEY_SAVED_MENTIONS_LIST_ID = "saved_mentions_list_id";
	public static final String PREFERENCE_KEY_SAVED_TAB_POSITION = "saved_tab_position";
	public static final String PREFERENCE_KEY_ENABLE_PROXY = "enable_proxy";
	public static final String PREFERENCE_KEY_PROXY_HOST = "proxy_host";
	public static final String PREFERENCE_KEY_PROXY_PORT = "proxy_port";
	public static final String PREFERENCE_KEY_STOP_SERVICE_AFTER_CLOSED = "stop_service_after_closed";
	public static final String PREFERENCE_KEY_SORT_TIMELINE_BY_TIME = "sort_timeline_by_time";
	public static final String PREFERENCE_KEY_REFRESH_ON_START = "refresh_on_start";
	public static final String PREFERENCE_KEY_REFRESH_AFTER_TWEET = "refresh_after_tweet";
	public static final String PREFERENCE_KEY_AUTO_REFRESH = "auto_refresh";
	public static final String PREFERENCE_KEY_REFRESH_INTERVAL = "refresh_interval";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE = "refresh_enable_home_timeline";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS = "refresh_enable_mentions";
	public static final String PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES = "refresh_enable_direct_messages";
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
	public static final String PREFERENCE_KEY_IMAGE_UPLOAD_FORMAT = "image_upload_format";
	public static final String PREFERENCE_KEY_TWEET_SHORTENER = "tweet_shortener";
	public static final String PREFERENCE_KEY_SHOW_ABSOLUTE_TIME = "show_absolute_time";
	public static final String PREFERENCE_KEY_DUAL_PANE_IN_LANDSCAPE = "dual_pane_in_landscape";
	public static final String PREFERENCE_KEY_DUAL_PANE_IN_PORTRAIT = "dual_pane_in_portrait";
	public static final String PREFERENCE_KEY_QUICK_SEND = "quick_send";
	public static final String PREFERENCE_KEY_COMPOSE_ACCOUNTS = "compose_accounts";
	public static final String PREFERENCE_KEY_HARDWARE_ACCELERATION = "hardware_acceleration";
	public static final String PREFERENCE_KEY_TCP_DNS_QUERY = "tcp_dns_query";
	public static final String PREFERENCE_KEY_DNS_SERVER = "dns_server";
	public static final String PREFERENCE_KEY_CLICK_TO_OPEN_MENU = "click_to_open_menu";
	public static final String PREFERENCE_KEY_KEEP_IN_BACKGROUND = "keep_in_background";
	public static final String PREFERENCE_KEY_SEPRATE_RETWEET_ACTION = "seprate_retweet_action";
	public static final String PREFERENCE_KEY_API_UPGRADE_CONFIRMED = "api_upgrade_confirmed";
	public static final String PREFERENCE_KEY_CONNECTION_TIMEOUT = "connection_timeout";
	public static final String PREFERENCE_KEY_NAME_DISPLAY_OPTION = "name_display_option";
	public static final String PREFERENCE_KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW = "stop_auto_refresh_when_battery_low";

	public static final String PREFERENCE_DEFAULT_QUOTE_FORMAT = "RT @" + FORMAT_PATTERN_NAME + ": "
			+ FORMAT_PATTERN_TEXT;
	public static final String PREFERENCE_DEFAULT_SHARE_FORMAT = FORMAT_PATTERN_TITLE + " - " + FORMAT_PATTERN_TEXT;
	public static final String PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT = FORMAT_PATTERN_TEXT + " " + FORMAT_PATTERN_LINK;

	public static final int PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT = 100;
	public static final int PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT = 20;
	public static final int PREFERENCE_DEFAULT_TEXT_SIZE = 15;

	public static final String NAME_DISPLAY_OPTION_BOTH = "both";
	public static final String NAME_DISPLAY_OPTION_NAME = "name";
	public static final String NAME_DISPLAY_OPTION_SCREEN_NAME = "screen_name";
	public static final int NAME_DISPLAY_OPTION_CODE_NAME = 1;
	public static final int NAME_DISPLAY_OPTION_CODE_SCREEN_NAME = 2;

	public static final String INTENT_PACKAGE_PREFIX = "org.mariotaku.twidere.";

	public static final String INTENT_ACTION_SERVICE = INTENT_PACKAGE_PREFIX + "SERVICE";
	public static final String INTENT_ACTION_HOME = INTENT_PACKAGE_PREFIX + "HOME";
	public static final String INTENT_ACTION_COMPOSE = INTENT_PACKAGE_PREFIX + "COMPOSE";
	public static final String INTENT_ACTION_SETTINGS = INTENT_PACKAGE_PREFIX + "SETTINGS";
	public static final String INTENT_ACTION_SELECT_ACCOUNT = INTENT_PACKAGE_PREFIX + "SELECT_ACCOUNT";
	public static final String INTENT_ACTION_VIEW_IMAGE = INTENT_PACKAGE_PREFIX + "VIEW_IMAGE";
	public static final String INTENT_ACTION_FILTERS = INTENT_PACKAGE_PREFIX + "FILTERS";
	public static final String INTENT_ACTION_ABOUT = INTENT_PACKAGE_PREFIX + "ABOUT";
	public static final String INTENT_ACTION_TWITTER_LOGIN = INTENT_PACKAGE_PREFIX + "TWITTER_LOGIN";
	public static final String INTENT_ACTION_DRAFTS = INTENT_PACKAGE_PREFIX + "DRAFTS";
	public static final String INTENT_ACTION_SAVE_FILE = INTENT_PACKAGE_PREFIX + "SAVE_FILE";
	public static final String INTENT_ACTION_PICK_FILE = INTENT_PACKAGE_PREFIX + "PICK_FILE";
	public static final String INTENT_ACTION_VIEW_WEBPAGE = INTENT_PACKAGE_PREFIX + "VIEW_WEBPAGE";
	public static final String INTENT_ACTION_EXTENSIONS = INTENT_PACKAGE_PREFIX + "EXTENSIONS";
	public static final String INTENT_ACTION_CUSTOM_TABS = INTENT_PACKAGE_PREFIX + "CUSTOM_TABS";
	public static final String INTENT_ACTION_NEW_CUSTOM_TAB = INTENT_PACKAGE_PREFIX + "NEW_CUSTOM_TAB";
	public static final String INTENT_ACTION_EDIT_CUSTOM_TAB = INTENT_PACKAGE_PREFIX + "EDIT_CUSTOM_TAB";
	public static final String INTENT_ACTION_EDIT_HOST_MAPPING = INTENT_PACKAGE_PREFIX + "EDIT_HOST_MAPPING";

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
	public static final String BROADCAST_REFRESHSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "REFRESHSTATE_CHANGED";
	public static final String BROADCAST_NOTIFICATION_CLEARED = INTENT_PACKAGE_PREFIX + "NOTIFICATION_CLEARED";
	public static final String BROADCAST_FRIENDSHIP_CHANGED = INTENT_PACKAGE_PREFIX + "FRIENDSHIP_CHANGED";
	public static final String BROADCAST_BLOCKSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "BLOCKSTATE_CHANGED";
	public static final String BROADCAST_PROFILE_UPDATED = INTENT_PACKAGE_PREFIX + "PROFILE_UPDATED";
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
	public static final String BROADCAST_USER_LIST_MEMBER_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_MEMBER_DELETED";
	public static final String BROADCAST_USER_LIST_SUBSCRIPTION_CHANGED = INTENT_PACKAGE_PREFIX
			+ "USER_LIST_SUBSCRIPTION_CHANGED";
	public static final String BROADCAST_USER_LIST_CREATED = INTENT_PACKAGE_PREFIX + "USER_LIST_CREATED";
	public static final String BROADCAST_USER_LIST_DELETED = INTENT_PACKAGE_PREFIX + "USER_LIST_DELETED";
	public static final String BROADCAST_TABS_UPDATED = INTENT_PACKAGE_PREFIX + "TABS_UPDATED";
	public static final String BROADCAST_FILTERS_UPDATED = INTENT_PACKAGE_PREFIX + "FILTERS_UPDATED";
	public static final String BROADCAST_REFRESH_HOME_TIMELINE = INTENT_PACKAGE_PREFIX + "REFRESH_HOME_TIMELINE";
	public static final String BROADCAST_REFRESH_MENTIONS = INTENT_PACKAGE_PREFIX + "REFRESH_MENTIONS";
	public static final String BROADCAST_REFRESH_DIRECT_MESSAGES = INTENT_PACKAGE_PREFIX + "REFRESH_DIRECT_MESSAGES";
	public static final String BROADCAST_MULTI_SELECT_ITEM_CHANGED = INTENT_PACKAGE_PREFIX
			+ "MULTI_SELECT_ITEM_CHANGED";
	public static final String BROADCAST_MULTI_SELECT_STATE_CHANGED = INTENT_PACKAGE_PREFIX
			+ "MULTI_SELECT_STATE_CHANGED";
	public static final String BROADCAST_MULTI_BLOCKSTATE_CHANGED = INTENT_PACKAGE_PREFIX + "MULTI_BLOCKSTATE_CHANGED";
	public static final String BROADCAST_APPLICATION_LAUNCHED = INTENT_PACKAGE_PREFIX + "APPLICATION_LAUNCHED";
	public static final String BROADCAST_APPLICATION_QUITTED = INTENT_PACKAGE_PREFIX + "APPLICATION_QUITTED";

	public static final String INTENT_KEY_LATITUDE = "latitude";
	public static final String INTENT_KEY_LONGITUDE = "longitude";
	public static final String INTENT_KEY_URI = "uri";
	public static final String INTENT_KEY_MENTIONS = "mentions";
	public static final String INTENT_KEY_ACCOUNT_ID = "account_id";
	public static final String INTENT_KEY_CONVERSATION_ID = "conversation_id";
	public static final String INTENT_KEY_PAGE = "page";
	public static final String INTENT_KEY_DATA = "data";
	public static final String INTENT_KEY_QUERY = "query";
	public static final String INTENT_KEY_QUERY_TYPE = "query_type";
	public static final String INTENT_KEY_USER_ID = "user_id";
	public static final String INTENT_KEY_LIST_ID = "list_id";
	public static final String INTENT_KEY_MAX_ID = "max_id";
	public static final String INTENT_KEY_SINCE_ID = "since_id";
	public static final String INTENT_KEY_MIN_ID = "min_id";
	public static final String INTENT_KEY_STATUS_ID = "status_id";
	public static final String INTENT_KEY_SCREEN_NAME = "screen_name";
	public static final String INTENT_KEY_LIST_NAME = "list_name";
	public static final String INTENT_KEY_DESCRIPTION = "description";
	public static final String INTENT_KEY_IN_REPLY_TO_ID = "in_reply_to_id";
	public static final String INTENT_KEY_IN_REPLY_TO_NAME = "in_reply_to_name";
	public static final String INTENT_KEY_IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";
	public static final String INTENT_KEY_TEXT = "text";
	public static final String INTENT_KEY_TITLE = "title";
	public static final String INTENT_KEY_TYPE = "type";
	public static final String INTENT_KEY_SUCCEED = "succeed";
	public static final String INTENT_KEY_IDS = "ids";
	public static final String INTENT_KEY_IS_QUOTE = "is_quote";
	public static final String INTENT_KEY_IS_SHARE = "is_share";
	public static final String INTENT_KEY_STATUS = "status";
	public static final String INTENT_KEY_FAVORITED = "favorited";
	public static final String INTENT_KEY_RETWEETED = "retweeted";
	public static final String INTENT_KEY_FILENAME = "filename";
	public static final String INTENT_KEY_FILE_SOURCE = "file_source";
	public static final String INTENT_KEY_FILE_EXTENSIONS = "file_extensions";
	public static final String INTENT_KEY_ITEMS_INSERTED = "items_inserted";
	public static final String INTENT_KEY_INITIAL_TAB = "initial_tab";
	public static final String INTENT_KEY_NOTIFICATION_ID = "notification_id";
	public static final String INTENT_KEY_FROM_NOTIFICATION = "from_notification";
	public static final String INTENT_KEY_IS_PUBLIC = "is_public";
	public static final String INTENT_KEY_USER = "user";
	public static final String INTENT_KEY_USER_LIST = "user_list";
	public static final String INTENT_KEY_APPEND_TEXT = "append_text";
	public static final String INTENT_KEY_NAME = "name";
	public static final String INTENT_KEY_TEXT1 = "text1";
	public static final String INTENT_KEY_TEXT2 = "text2";
	public static final String INTENT_KEY_POSITION = "position";
	public static final String INTENT_KEY_ARGUMENTS = "arguments";
	public static final String INTENT_KEY_ICON = "icon";
	public static final String INTENT_KEY_ID = "id";
	public static final String INTENT_KEY_RESID = "resid";
	public static final String INTENT_KEY_IMAGE_URI = "image_uri";
	public static final String INTENT_KEY_IS_PHOTO_ATTACHED = "is_photo_attached";
	public static final String INTENT_KEY_IS_IMAGE_ATTACHED = "is_image_attached";
	public static final String INTENT_KEY_ACTIVATED_ONLY = "activated_only";
	public static final String INTENT_KEY_IS_HOME_TAB = "is_home_tab";
	public static final String INTENT_KEY_HAS_RUNNING_TASK = "has_running_task";
	public static final String INTENT_KEY_OAUTH_VERIFIER = "oauth_verifier";
	public static final String INTENT_KEY_REQUEST_TOKEN = "request_token";
	public static final String INTENT_KEY_REQUEST_TOKEN_SECRET = "request_token_secret";

	public static final int MENU_HOME = android.R.id.home;
	public static final int MENU_SEARCH = R.id.search;
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
	public static final int MENU_CONVERSATION = R.id.conversation;
	public static final int MENU_MENTION = R.id.mention;
	public static final int MENU_SEND_DIRECT_MESSAGE = R.id.send_direct_message;
	public static final int MENU_EXTENSIONS = R.id.extensions;
	public static final int MENU_VIEW_USER_LIST = R.id.view_user_list;
	public static final int MENU_UP = R.id.up;
	public static final int MENU_DOWN = R.id.down;
	public static final int MENU_MULTI_SELECT = R.id.multi_select;
	public static final int MENU_CLEAR_COLOR = R.id.clear_color;

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

	public static final String TABLE_ACCOUNTS = Accounts.CONTENT_PATH;
	public static final String TABLE_STATUSES = Statuses.CONTENT_PATH;
	public static final String TABLE_MENTIONS = Mentions.CONTENT_PATH;
	public static final String TABLE_DRAFTS = Drafts.CONTENT_PATH;
	public static final String TABLE_CACHED_USERS = CachedUsers.CONTENT_PATH;
	public static final String TABLE_FILTERED_USERS = Filters.Users.CONTENT_PATH;
	public static final String TABLE_FILTERED_KEYWORDS = Filters.Keywords.CONTENT_PATH;
	public static final String TABLE_FILTERED_SOURCES = Filters.Sources.CONTENT_PATH;
	public static final String TABLE_DIRECT_MESSAGES = DirectMessages.CONTENT_PATH;
	public static final String TABLE_DIRECT_MESSAGES_INBOX = DirectMessages.Inbox.CONTENT_PATH;
	public static final String TABLE_DIRECT_MESSAGES_OUTBOX = DirectMessages.Outbox.CONTENT_PATH;
	public static final String TABLE_DIRECT_MESSAGES_CONVERSATION = DirectMessages.Conversation.CONTENT_PATH;
	public static final String TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = DirectMessages.Conversation.CONTENT_PATH_SCREEN_NAME;
	public static final String TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY = DirectMessages.ConversationsEntry.CONTENT_PATH;
	public static final String TABLE_TRENDS_LOCAL = CachedTrends.Local.CONTENT_PATH;
	public static final String TABLE_TABS = Tabs.CONTENT_PATH;

	public static final int URI_ACCOUNTS = 1;
	public static final int URI_STATUSES = 2;
	public static final int URI_MENTIONS = 3;
	public static final int URI_DRAFTS = 4;
	public static final int URI_CACHED_USERS = 5;
	public static final int URI_FILTERED_USERS = 6;
	public static final int URI_FILTERED_KEYWORDS = 7;
	public static final int URI_FILTERED_SOURCES = 8;
	public static final int URI_DIRECT_MESSAGES = 9;
	public static final int URI_DIRECT_MESSAGES_INBOX = 10;
	public static final int URI_DIRECT_MESSAGES_OUTBOX = 11;
	public static final int URI_DIRECT_MESSAGES_CONVERSATION = 12;
	public static final int URI_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME = 13;
	public static final int URI_DIRECT_MESSAGES_CONVERSATIONS_ENTRY = 14;
	public static final int URI_TRENDS_DAILY = 15;
	public static final int URI_TRENDS_WEEKLY = 16;
	public static final int URI_TRENDS_LOCAL = 17;
	public static final int URI_TABS = 18;

	public static final int LINK_ID_STATUS = 1;
	public static final int LINK_ID_USER = 2;
	public static final int LINK_ID_USER_TIMELINE = 3;
	public static final int LINK_ID_USER_FAVORITES = 4;
	public static final int LINK_ID_USER_FOLLOWERS = 5;
	public static final int LINK_ID_USER_FRIENDS = 6;
	public static final int LINK_ID_USER_BLOCKS = 7;
	public static final int LINK_ID_CONVERSATION = 8;
	public static final int LINK_ID_DIRECT_MESSAGES_CONVERSATION = 9;
	public static final int LINK_ID_LIST_DETAILS = 10;
	public static final int LINK_ID_LIST_TYPES = 11;
	public static final int LINK_ID_LIST_TIMELINE = 12;
	public static final int LINK_ID_LIST_MEMBERS = 13;
	public static final int LINK_ID_LIST_SUBSCRIBERS = 14;
	public static final int LINK_ID_LIST_CREATED = 15;
	public static final int LINK_ID_LIST_SUBSCRIPTIONS = 16;
	public static final int LINK_ID_LIST_MEMBERSHIPS = 17;
	public static final int LINK_ID_SAVED_SEARCHES = 19;
	public static final int LINK_ID_USER_MENTIONS = 21;
	public static final int LINK_ID_INCOMING_FRIENDSHIPS = 22;

	public static final String DIR_NAME_PROFILE_IMAGES = "profile_images";
	public static final String DIR_NAME_CACHED_THUMBNAILS = "cached_thumbnails";

	public static final int PANE_LEFT = R.id.left_pane;
	public static final int PANE_LEFT_CONTAINER = R.id.left_pane_container;
	public static final int PANE_RIGHT = R.id.right_pane;
	public static final int PANE_RIGHT_CONTAINER = R.id.right_pane_container;

	public static final int NOTIFICATION_ID_HOME_TIMELINE = 1;
	public static final int NOTIFICATION_ID_MENTIONS = 2;
	public static final int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
	public static final int NOTIFICATION_ID_DRAFTS = 4;

	public static final String ICON_SPECIAL_TYPE_CUSTOMIZE = "_customize";

	public static final String FRAGMENT_TAG_API_UPGRADE_NOTICE = "api_upgrade_notice";
}
