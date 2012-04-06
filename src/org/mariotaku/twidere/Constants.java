package org.mariotaku.twidere;

public interface Constants {

	public final static String LOGTAG = "Twidere";

	public final static String DATABASES_NAME = "twidere.sqlite";
	public final static int DATABASES_VERSION = 2;

	public final static String CONSUMER_KEY = "uAFVpMhBntJutfVj6abfA";
	public final static String CONSUMER_SECRET = "JARXkJTfxo0F8MyctYy9bUmrLISjo8vXAHsZHYuk2E";

	public final static String PROTOCOL_HTTP = "http://";
	public final static String PROTOCOL_HTTPS = "https://";
	public final static String PROTOCOL_TWIDERE = "twidere://";

	public final static String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

	public final static String DEFAULT_OAUTH_CALLBACK = PROTOCOL_TWIDERE + "com.twitter.oauth/";

	public final static String DEFAULT_REST_API_BASE = DEFAULT_PROTOCOL + "api.twitter.com/1/";
	public final static String DEFAULT_SEARCH_API_BASE = DEFAULT_PROTOCOL + "search.twitter.com/";

	public final static String BROADCAST_HOME_TIMELINE_REFRESHED = "org.mariotaku.twidere.HOME_TIMELINE_REFRESHED";
	public final static String BROADCAST_MENTIONS_REFRESHED = "org.mariotaku.twidere.MENTIONS_REFRESHED";

	public final static String OAUTH_VERIFIER = "oauth_verifier";

}
