package org.mariotaku.twidere;

public interface Constants {

	public static final String LOGTAG = "Twidere";

	public static final boolean MULTIPLE_ACCOUNTS_ENABLED = true;

	public static final String DATABASES_NAME = "twidere.sqlite";
	public static final int DATABASES_VERSION = 2;

	public static final String CONSUMER_KEY = "uAFVpMhBntJutfVj6abfA";
	public static final String CONSUMER_SECRET = "JARXkJTfxo0F8MyctYy9bUmrLISjo8vXAHsZHYuk2E";

	public static final String PROTOCOL_HTTP = "http://";
	public static final String PROTOCOL_HTTPS = "https://";
	public static final String PROTOCOL_TWIDERE = "twidere://";

	public static final String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

	public static final String DEFAULT_OAUTH_CALLBACK = PROTOCOL_TWIDERE + "com.twitter.oauth/";

	public static final String DEFAULT_REST_API_BASE = DEFAULT_PROTOCOL + "api.twitter.com/1/";
	public static final String DEFAULT_SEARCH_API_BASE = DEFAULT_PROTOCOL + "search.twitter.com/";

	public static final String BROADCAST_HOME_TIMELINE_REFRESHED = "org.mariotaku.twidere.HOME_TIMELINE_REFRESHED";
	public static final String BROADCAST_MENTIONS_REFRESHED = "org.mariotaku.twidere.MENTIONS_REFRESHED";

	public static final String BROADCAST_REFRESHSTATE_CHANGED = "org.mariotaku.twidere.REFRESHSTATE_CHANGED";

	public static final String SHUFFIX_SCROLL_TO_TOP = ".SCROLL_TO_TOP";

	public static final String OAUTH_VERIFIER = "oauth_verifier";

	public static final String INTENT_KEY_REFRESH_ALL = "refresh_all";

	public static final String PREFERENCE_NAME = "preference";

	public static final String PREFERENCE_KEY_ITEM_LIMIT = "item_limit";
	public static final String PREFERENCE_KEY_DARK_THEME = "dark_theme";
	public static final String PREFERENCE_KEY_CLEAR_DATABASES = "clear_databases";
	public static final String PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE = "display_profile_image";

	public static final String INTENT_ACTION_PREFIX = "org.mariotaku.twidere.";

	public static final String INTENT_ACTION_COMPOSE = INTENT_ACTION_PREFIX + "COMPOSE";
	public static final String INTENT_ACTION_GLOBAL_SETTINGS = INTENT_ACTION_PREFIX
			+ "GLOBAL_SETTINGS";
	public static final String INTENT_ACTION_SELECT_ACCOUNT = INTENT_ACTION_PREFIX
			+ "SELECT_ACCOUNT";
	public static final String INTENT_ACTION_VIEW_STATUS = INTENT_ACTION_PREFIX + "VIEW_STATUS";
	public static final String INTENT_ACTION_FILTERS = INTENT_ACTION_PREFIX + "FILTERS";
	public static final String INTENT_ACTION_ABOUT = INTENT_ACTION_PREFIX + "ABOUT";

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
	public static final int MENU_PICK_FROM_GALLERY = R.id.pick_from_gallery;
	public static final int MENU_PICK_FROM_MAP = R.id.pick_from_map;

	public final static int REQUEST_TAKE_PHOTO = 1;
	public final static int REQUEST_ADD_IMAGE = 2;
	public final static int REQUEST_SELECT_ACCOUNT = 3;

}
