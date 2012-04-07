package org.mariotaku.twidere;

public interface Constants {

	public static final String LOGTAG = "Twidere";

	public static final String DATABASES_NAME = "twidere.sqlite";
	public static final int DATABASES_VERSION = 1;

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
	
	public static final String SHUFFIX_SCROLL_TO_TOP = ".SCROLL_TO_TOP";

	public static final String OAUTH_VERIFIER = "oauth_verifier";

	public static final int MENU_HOME = android.R.id.home;
	public static final int MENU_ADD_LOCATION = R.id.add_location;
	public static final int MENU_LOCATION = R.id.location;
	public static final int MENU_TAKE_PHOTO = R.id.take_photo;
	public static final int MENU_ADD_IMAGE = R.id.add_image;
	public static final int MENU_IMAGE = R.id.image;
	public static final int MENU_VIEW = R.id.view;
	public static final int MENU_DELETE = R.id.delete;
	public static final int MENU_PICK_FROM_GALLERY = R.id.pick_from_gallery;
	public static final int MENU_PICK_FROM_MAP = R.id.pick_from_map;

}
