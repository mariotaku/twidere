package org.mariotaku.twidere;

import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Favorites;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

public interface Constants {

	public static final String LOGTAG = "Twidere";

	public static final String CLIENT_URL = "https://github.com/mariotaku/twidere";

	public static final boolean MULTIPLE_ACCOUNTS_ENABLED = true;

	public static final String DATABASES_NAME = "twidere.sqlite";
	public static final int DATABASES_VERSION = 8;

	public static final String CONSUMER_KEY = "uAFVpMhBntJutfVj6abfA";
	public static final String CONSUMER_SECRET = "JARXkJTfxo0F8MyctYy9bUmrLISjo8vXAHsZHYuk2E";

	public static final String SCHEME_HTTP = "http";
	public static final String SCHEME_HTTPS = "https";
	public static final String SCHEME_TWIDERE = "twidere";

	public static final String PROTOCOL_HTTP = SCHEME_HTTP + "://";
	public static final String PROTOCOL_HTTPS = SCHEME_HTTPS + "://";
	public static final String PROTOCOL_TWIDERE = SCHEME_TWIDERE + "://";

	public static final String HOST_USER = "user";

	public static final String QUERY_PARAM_ACCOUNT_ID = "account_id";
	public static final String QUERY_PARAM_SCREEN_NAME = "screen_name";

	public static final String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

	public static final String DEFAULT_OAUTH_CALLBACK = PROTOCOL_TWIDERE + "com.twitter.oauth/";

	public static final String DEFAULT_REST_API_BASE = DEFAULT_PROTOCOL + "api.twitter.com/1/";
	public static final String DEFAULT_SEARCH_API_BASE = DEFAULT_PROTOCOL + "search.twitter.com/";

	public static final String BROADCAST_HOME_TIMELINE_DATABASE_UPDATED = "org.mariotaku.twidere.HOME_TIMELINE_DATABASE_UPDATED";
	public static final String BROADCAST_MENTIONS_DATABASE_UPDATED = "org.mariotaku.twidere.MENTIONS_DATABASE_UPDATED";
	public static final String BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED = "org.mariotaku.twidere.ACCOUNT_LIST_DATABASE_UPDATED";

	public static final String BROADCAST_HOME_TIMELINE_REFRESHED = "org.mariotaku.twidere.HOME_TIMELINE_REFRESHED";
	public static final String BROADCAST_MENTIONS_REFRESHED = "org.mariotaku.twidere.MENTIONS_REFRESHED";

	public static final String BROADCAST_REFRESHSTATE_CHANGED = "org.mariotaku.twidere.REFRESHSTATE_CHANGED";
	public static final String BROADCAST_DATABASE_UPDATED = "org.mariotaku.twidere.DATABASE_UPDATED";

	public static final String SHUFFIX_SCROLL_TO_TOP = ".SCROLL_TO_TOP";

	public static final String OAUTH_VERIFIER = "oauth_verifier";

	public static final String PREFERENCE_NAME = "preference";

	public static final String PREFERENCE_KEY_DATABASE_ITEM_LIMIT = "database_item_limit";
	public static final String PREFERENCE_KEY_LOAD_ITEM_LIMIT = "load_item_limit";
	public static final String PREFERENCE_KEY_TEXT_SIZE = "text_size";
	public static final String PREFERENCE_KEY_DARK_THEME = "dark_theme";
	public static final String PREFERENCE_KEY_CLEAR_DATABASES = "clear_databases";
	public static final String PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";
	public static final String PREFERENCE_KEY_DISPLAY_NAME = "display_name";
	public static final String PREFERENCE_KEY_COMPOSE_BUTTON = "bottom_compose_button";
	public static final String PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON = "leftside_compose_button";
	public static final String PREFERENCE_KEY_ATTACH_LOCATION = "attach_location";
	public static final String PREFERENCE_KEY_ENABLE_FILTER = "enable_filter";
	public static final String PREFERENCE_KEY_GZIP_COMPRESSING = "gzip_compressing";
	public static final String PREFERENCE_LOAD_MORE_AUTOMATICALLY = "load_more_automatically";

	public static final int PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT = 100;
	public static final int PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT = 20;
	public static final int PREFERENCE_DEFAULT_TEXT_SIZE = 12;

	public static final String INTENT_ACTION_PREFIX = "org.mariotaku.twidere.";

	public static final String INTENT_ACTION_HOME = INTENT_ACTION_PREFIX + "HOME";
	public static final String INTENT_ACTION_COMPOSE = INTENT_ACTION_PREFIX + "COMPOSE";
	public static final String INTENT_ACTION_GLOBAL_SETTINGS = INTENT_ACTION_PREFIX + "GLOBAL_SETTINGS";
	public static final String INTENT_ACTION_SELECT_ACCOUNT = INTENT_ACTION_PREFIX + "SELECT_ACCOUNT";
	public static final String INTENT_ACTION_VIEW_STATUS = INTENT_ACTION_PREFIX + "VIEW_STATUS";
	public static final String INTENT_ACTION_VIEW_USER_PROFILE = INTENT_ACTION_PREFIX + "VIEW_USER_PROFILE";
	public static final String INTENT_ACTION_VIEW_CONVERSATION = INTENT_ACTION_PREFIX + "VIEW_CONVERSATION";
	public static final String INTENT_ACTION_VIEW_MAP = INTENT_ACTION_PREFIX + "VIEW_MAP";
	public static final String INTENT_ACTION_FILTERS = INTENT_ACTION_PREFIX + "FILTERS";
	public static final String INTENT_ACTION_ABOUT = INTENT_ACTION_PREFIX + "ABOUT";
	public static final String INTENT_ACTION_EDIT_API = INTENT_ACTION_PREFIX + "EDIT_API";
	public static final String INTENT_ACTION_SET_COLOR = INTENT_ACTION_PREFIX + "SET_COLOR";
	public static final String INTENT_ACTION_DEBUG = INTENT_ACTION_PREFIX + "DEBUG";
	public static final String INTENT_ACTION_TWITTER_LOGIN = INTENT_ACTION_PREFIX + "TWITTER_LOGIN";

	public static final String INTENT_KEY_LATITUDE = "latitude";
	public static final String INTENT_KEY_LONGITUDE = "longitude";
	public static final String INTENT_KEY_URI = "uri";
	public static final String INTENT_KEY_MENTIONS = "mentions";
	public static final String INTENT_KEY_ACCOUNT_ID = "account_id";
	public static final String INTENT_KEY_QUERY = "query";
	public static final String INTENT_KEY_USER_ID = "user_id";
	public static final String INTENT_KEY_STATUS_ID = "status_id";
	public static final String INTENT_KEY_SCREEN_NAME = "screen_name";
	public static final String INTENT_KEY_IN_REPLY_TO_ID = "in_reply_to_id";
	public static final String INTENT_KEY_TEXT = "text";
	public static final String INTENT_KEY_SUCCEED = "succeed";
	public static final String INTENT_KEY_REFRESH_ALL = "refresh_all";
	public static final String INTENT_KEY_IDS = "ids";
	public static final String INTENT_KEY_IS_QUOTE = "is_quote";

	public static final int MENU_HOME = android.R.id.home;
	public static final int MENU_COMPOSE = R.id.compose;
	public static final int MENU_SEND = R.id.send;
	public static final int MENU_SELECT_ACCOUNT = R.id.select_account;
	public static final int MENU_SETTINGS = R.id.settings;
	public static final int MENU_ADD_LOCATION = R.id.add_location;
	public static final int MENU_TAKE_PHOTO = R.id.take_photo;
	public static final int MENU_ADD_IMAGE = R.id.add_image;
	public static final int MENU_LOCATION = R.id.location;
	public static final int MENU_IMAGE = R.id.image;
	public static final int MENU_VIEW = R.id.view;
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
	public static final int MENU_FAV = R.id.fav;
	public static final int MENU_RETWEET = R.id.retweet;
	public static final int MENU_QUOTE = R.id.quote;
	public static final int MENU_SHARE = R.id.share;
	public static final int MENU_DEBUG = R.id.debug;

	public static final int REQUEST_TAKE_PHOTO = 1;
	public static final int REQUEST_ADD_IMAGE = 2;
	public static final int REQUEST_SELECT_ACCOUNT = 3;
	public static final int REQUEST_EDIT_API = 4;
	public static final int REQUEST_GOTO_AUTHORIZATION = 5;
	public static final int REQUEST_SET_COLOR = 6;

	public static final int RESULT_UNKNOWN_ERROR = -1;
	public static final int RESULT_SUCCESS = 0;
	public static final int RESULT_ALREADY_LOGGED_IN = 1;
	public static final int RESULT_CONNECTIVITY_ERROR = 2;
	public static final int RESULT_SERVER_ERROR = 3;
	public static final int RESULT_BAD_ADDRESS = 4;
	public static final int RESULT_NO_PERMISSION = 5;
	public static final int RESULT_OPEN_BROWSER = 6;

	public static final String TABLE_ACCOUNTS = Accounts.CONTENT_PATH;
	public static final String TABLE_STATUSES = Statuses.CONTENT_PATH;
	public static final String TABLE_MENTIONS = Mentions.CONTENT_PATH;
	public static final String TABLE_FAVORITES = Favorites.CONTENT_PATH;
	public static final String TABLE_CACHED_USERS = CachedUsers.CONTENT_PATH;
	public static final String TABLE_FILTERED_USERS = Filters.Users.CONTENT_PATH;
	public static final String TABLE_FILTERED_KEYWORDS = Filters.Keywords.CONTENT_PATH;
	public static final String TABLE_FILTERED_SOURCES = Filters.Sources.CONTENT_PATH;

	public static final int URI_ACCOUNTS = 1;
	public static final int URI_STATUSES = 2;
	public static final int URI_MENTIONS = 3;
	public static final int URI_FAVORITES = 4;
	public static final int URI_CACHED_USERS = 5;
	public static final int URI_FILTERED_USERS = 6;
	public static final int URI_FILTERED_KEYWORDS = 7;
	public static final int URI_FILTERED_SOURCES = 8;
	public static final int URI_USER_TIMELINE = 9;

}
