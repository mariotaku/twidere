package twitter4j;

public interface TwitterConstants {

	public static final String DEFAULT_OAUTH_BASE_URL = "https://api.twitter.com/oauth/";
	public static final String DEFAULT_SIGNING_OAUTH_BASE_URL = DEFAULT_OAUTH_BASE_URL;

	public static final String PATH_SEGMENT_AUTHENTICATION = "authenticate";
	public static final String PATH_SEGMENT_REQUEST_TOKEN = "request_token";
	public static final String PATH_SEGMENT_ACCESS_TOKEN = "access_token";
	public static final String PATH_SEGMENT_AUTHORIZATION = "authorize";
	
	public static final String DEFAULT_OAUTH_REQUEST_TOKEN_URL = DEFAULT_OAUTH_BASE_URL + PATH_SEGMENT_REQUEST_TOKEN;
	public static final String DEFAULT_OAUTH_AUTHORIZATION_URL = DEFAULT_OAUTH_BASE_URL + PATH_SEGMENT_AUTHORIZATION;
	public static final String DEFAULT_OAUTH_ACCESS_TOKEN_URL = DEFAULT_OAUTH_BASE_URL + PATH_SEGMENT_ACCESS_TOKEN;
	public static final String DEFAULT_OAUTH_AUTHENTICATION_URL = DEFAULT_OAUTH_BASE_URL + PATH_SEGMENT_AUTHENTICATION;

	public static final String DEFAULT_SIGNING_OAUTH_REQUEST_TOKEN_URL = DEFAULT_SIGNING_OAUTH_BASE_URL + PATH_SEGMENT_REQUEST_TOKEN;
	public static final String DEFAULT_SIGNING_OAUTH_AUTHORIZATION_URL = DEFAULT_SIGNING_OAUTH_BASE_URL + PATH_SEGMENT_AUTHORIZATION;
	public static final String DEFAULT_SIGNING_OAUTH_ACCESS_TOKEN_URL = DEFAULT_SIGNING_OAUTH_BASE_URL + PATH_SEGMENT_ACCESS_TOKEN;
	public static final String DEFAULT_SIGNING_OAUTH_AUTHENTICATION_URL = DEFAULT_SIGNING_OAUTH_BASE_URL + PATH_SEGMENT_AUTHENTICATION;
	
	public static final String DEFAULT_REST_BASE_URL = "https://api.twitter.com/1/";
	public static final String DEFAULT_SIGNING_REST_BASE_URL = DEFAULT_REST_BASE_URL;
	public static final String DEFAULT_SEARCH_BASE_URL = "https://search.twitter.com/";
	public static final String DEFAULT_STREAM_BASE_URL = "https://stream.twitter.com/1/";
	public static final String DEFAULT_USER_STREAM_BASE_URL = "https://userstream.twitter.com/2/";
	public static final String DEFAULT_SITE_STREAM_BASE_URL = "https://sitestream.twitter.com/2b/";
	public static final String DEFAULT_UPLOAD_BASE_URL = "https://upload.twitter.com/1/";
}
