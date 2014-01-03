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

	public static final String DEFAULT_SIGNING_OAUTH_REQUEST_TOKEN_URL = DEFAULT_SIGNING_OAUTH_BASE_URL
			+ PATH_SEGMENT_REQUEST_TOKEN;
	public static final String DEFAULT_SIGNING_OAUTH_AUTHORIZATION_URL = DEFAULT_SIGNING_OAUTH_BASE_URL
			+ PATH_SEGMENT_AUTHORIZATION;
	public static final String DEFAULT_SIGNING_OAUTH_ACCESS_TOKEN_URL = DEFAULT_SIGNING_OAUTH_BASE_URL
			+ PATH_SEGMENT_ACCESS_TOKEN;
	public static final String DEFAULT_SIGNING_OAUTH_AUTHENTICATION_URL = DEFAULT_SIGNING_OAUTH_BASE_URL
			+ PATH_SEGMENT_AUTHENTICATION;

	public static final String DEFAULT_REST_BASE_URL = "https://api.twitter.com/1.1/";
	public static final String DEFAULT_SIGNING_REST_BASE_URL = DEFAULT_REST_BASE_URL;

	static final String ENDPOINT_ACCOUNT_REMOVE_PROFILE_BANNER = "account/remove_profile_banner.json";
	static final String ENDPOINT_ACCOUNT_SETTINGS = "account/settings.json";
	static final String ENDPOINT_ACCOUNT_UPDATE_PROFILE = "account/update_profile.json";
	static final String ENDPOINT_ACCOUNT_UPDATE_PROFILE_BACKGROUND_IMAGE = "account/update_profile_background_image.json";
	static final String ENDPOINT_ACCOUNT_UPDATE_PROFILE_BANNER = "account/update_profile_banner.json";
	static final String ENDPOINT_ACCOUNT_UPDATE_PROFILE_COLORS = "account/update_profile_colors.json";
	static final String ENDPOINT_ACCOUNT_UPDATE_PROFILE_IMAGE = "account/update_profile_image.json";

	static final String ENDPOINT_ACTIVITY_ABOUT_ME = "activity/about_me.json";
	static final String ENDPOINT_ACTIVITY_BY_FRIENDS = "activity/by_friends.json";

	static final String ENDPOINT_CONVERSATION_SHOW = "conversation/show.json";

	static final String ENDPOINT_TRANSLATIONS_SHOW = "translations/show.json";

	static final String ENDPOINT_BLOCKS_CREATE = "blocks/create.json";
	static final String ENDPOINT_BLOCKS_DESTROY = "blocks/destroy.json";
	static final String ENDPOINT_BLOCKS_LIST = "blocks/list.json";
	static final String ENDPOINT_BLOCKS_IDS = "blocks/ids.json";

	static final String ENDPOINT_DIRECT_MESSAGES = "direct_messages.json";
	static final String ENDPOINT_DIRECT_MESSAGES_DESTROY = "direct_messages/destroy.json";
	static final String ENDPOINT_DIRECT_MESSAGES_NEW = "direct_messages/new.json";
	static final String ENDPOINT_DIRECT_MESSAGES_SENT = "direct_messages/sent.json";
	static final String ENDPOINT_DIRECT_MESSAGES_SHOW = "direct_messages/show.json";

	static final String ENDPOINT_FAVORITES_LIST = "favorites/list.json";
	static final String ENDPOINT_FAVORITES_CREATE = "favorites/create.json";
	static final String ENDPOINT_FAVORITES_DESTROY = "favorites/destroy.json";

	static final String ENDPOINT_GEO_PLACE = "geo/place.json";
	static final String ENDPOINT_GEO_REVERSE_GEOCODE = "geo/reverse_geocode.json";
	static final String ENDPOINT_GEO_SEARCH = "geo/search.json";
	static final String ENDPOINT_GEO_SIMILAR_PLACES = "geo/similar_places.json";

	static final String ENDPOINT_FOLLOWERS_IDS = "followers/ids.json";
	static final String ENDPOINT_FRIENDS_IDS = "friends/ids.json";
	static final String ENDPOINT_FOLLOWERS_LIST = "followers/list.json";
	static final String ENDPOINT_FRIENDS_LIST = "friends/list.json";
	static final String ENDPOINT_FRIENDSHIPS_CREATE = "friendships/create.json";
	static final String ENDPOINT_FRIENDSHIPS_DESTROY = "friendships/destroy.json";
	static final String ENDPOINT_FRIENDSHIPS_INCOMING = "friendships/incoming.json";
	static final String ENDPOINT_FRIENDSHIPS_LOOKUP = "friendships/lookup.json";
	static final String ENDPOINT_FRIENDSHIPS_OUTGOING = "friendships/outgoing.json";
	static final String ENDPOINT_FRIENDSHIPS_SHOW = "friendships/show.json";
	static final String ENDPOINT_FRIENDSHIPS_UPDATE = "friendships/update.json";
	static final String ENDPOINT_FRIENDSHIPS_ACCEPT = "friendships/accept.json";
	static final String ENDPOINT_FRIENDSHIPS_DENY = "friendships/deny.json";

	static final String ENDPOINT_HELP_CONFIGURATION = "help/configuration.json";
	static final String ENDPOINT_HELP_LANGUAGES = "help/languages.json";
	static final String ENDPOINT_LEGAL_PRIVACY = "legal/privacy.json";
	static final String ENDPOINT_LEGAL_TOS = "legal/tos.json";

	static final String ENDPOINT_LISTS_CREATE = "lists/create.json";
	static final String ENDPOINT_LISTS_DESTROY = "lists/destroy.json";
	static final String ENDPOINT_LISTS_LIST = "lists/list.json";
	static final String ENDPOINT_LISTS_MEMBERSHIPS = "lists/memberships.json";
	static final String ENDPOINT_LISTS_MEMBERS = "lists/members.json";
	static final String ENDPOINT_LISTS_MEMBERS_CREATE = "lists/members/create.json";
	static final String ENDPOINT_LISTS_MEMBERS_CREATE_ALL = "lists/members/create_all.json";
	static final String ENDPOINT_LISTS_MEMBERS_DESTROY = "lists/members/destroy.json";
	static final String ENDPOINT_LISTS_MEMBERS_DESTROY_ALL = "lists/members/destroy_all.json";
	static final String ENDPOINT_LISTS_MEMBERS_SHOW = "lists/members/show.json";
	static final String ENDPOINT_LISTS_SHOW = "lists/show.json";
	static final String ENDPOINT_LISTS_STATUSES = "lists/statuses.json";
	static final String ENDPOINT_LISTS_SUBSCRIPTIONS = "lists/subscriptions.json";
	static final String ENDPOINT_LISTS_SUBSCRIBERS = "lists/subscribers.json";
	static final String ENDPOINT_LISTS_SUBSCRIBERS_CREATE = "lists/subscribers/create.json";
	static final String ENDPOINT_LISTS_SUBSCRIBERS_DESTROY = "lists/subscribers/destroy.json";
	static final String ENDPOINT_LISTS_SUBSCRIBERS_SHOW = "lists/subscribers/show.json";
	static final String ENDPOINT_LISTS_UPDATE = "lists/update.json";
	static final String ENDPOINT_LISTS_OWNERSHIPS = "lists/ownerships.json";

	static final String ENDPOINT_RATE_LIMIT_STATUS = "application/rate_limit_status.json";

	static final String ENDPOINT_SAVED_SEARCHES_CREATE = "saved_searches/create.json";
	static final String ENDPOINT_SAVED_SEARCHES_LIST = "saved_searches/list.json";

	static final String ENDPOINT_SEARCH_TWEETS = "search/tweets.json";

	static final String ENDPOINT_STATUSES_HOME_TIMELINE = "statuses/home_timeline.json";
	static final String ENDPOINT_STATUSES_MENTIONS_TIMELINE = "statuses/mentions_timeline.json";
	static final String ENDPOINT_STATUSES_OEMBED = "statuses/oembed.json";
	static final String ENDPOINT_STATUSES_RETWEETS_OF_ME = "statuses/retweets_of_me.json";
	static final String ENDPOINT_STATUSES_RETWEETERS_IDS = "statuses/retweeters/ids.json";
	static final String ENDPOINT_STATUSES_SHOW = "statuses/show.json";
	static final String ENDPOINT_STATUSES_UPDATE = "statuses/update.json";
	static final String ENDPOINT_STATUSES_UPDATE_WITH_MEDIA = "statuses/update_with_media.json";
	static final String ENDPOINT_STATUSES_USER_TIMELINE = "statuses/user_timeline.json";
	static final String ENDPOINT_STATUSES_MEDIA_TIMELINE = "statuses/media_timeline.json";

	static final String ENDPOINT_TRENDS_AVAILABLE = "trends/available.json";
	static final String ENDPOINT_TRENDS_CLOSEST = "trends/closest.json";
	static final String ENDPOINT_TRENDS_PLACE = "trends/place.json";

	static final String ENDPOINT_USERS_LOOKUP = "users/lookup.json";
	static final String ENDPOINT_USERS_REPORT_SPAM = "users/report_spam.json";
	static final String ENDPOINT_USERS_SEARCH = "users/search.json";
	static final String ENDPOINT_USERS_SHOW = "users/show.json";
	static final String ENDPOINT_USERS_SUGGESTIONS = "users/suggestions.json";

}
