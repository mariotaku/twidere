package org.mariotaku.twidere.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TweetStore {

	public static final String PROTOCOL_CONTENT = "content://";

	public static final String AUTHORITY = "org.mariotaku.twidere.provider.TweetStore";

	public static final String KEY_TYPE = "type";
	public static final String KEY_ACCOUNT_ID = "account_id";

	public static final int VALUE_TYPE_STATUS = 1;
	public static final int VALUE_TYPE_MENTION = 2;

	public static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI,
			Mentions.CONTENT_URI };

	private static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";

	private static final String TYPE_TEXT = "TEXT";

	private static final String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";

	private static final String TYPE_INT = "INTEGER";

	private static final String TYPE_INT_UNIQUE = "INTEGER UNIQUE";

	private static final String TYPE_BOOLEAN = "INTEGER(1)";

	public static class Accounts implements BaseColumns {

		public static final int AUTH_TYPE_OAUTH = 0;
		public static final int AUTH_TYPE_XAUTH = 1;
		public static final int AUTH_TYPE_BASIC = 2;

		public static final String CONTENT_PATH = "accounts";
		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);

		/**
		 * Login name of the account<br>
		 * Type: TEXT NOT NULL
		 */
		public static final String USERNAME = "username";

		/**
		 * Unique ID of the account<br>
		 * Type: INTEGER (long)
		 */
		public static final String USER_ID = "user_id";

		/**
		 * User's IDs in long array. Only used for Intent.<br>
		 */
		public static final String USER_IDS = "user_ids";

		/**
		 * Auth type of the account.</br> Type: INTEGER
		 */
		public static final String AUTH_TYPE = "auth_type";

		/**
		 * Password of the account. (It will not stored)<br>
		 * Type: TEXT
		 */
		public static final String PASSWORD = "password";

		/**
		 * Password of the account for basic auth.<br>
		 * Type: TEXT
		 */
		public static final String BASIC_AUTH_PASSWORD = "basic_auth_password";

		/**
		 * OAuth Token of the account.<br>
		 * Type: TEXT
		 */
		public static final String OAUTH_TOKEN = "oauth_token";

		/**
		 * Token Secret of the account.<br>
		 * Type: TEXT
		 */
		public static final String TOKEN_SECRET = "token_secret";

		/**
		 * Rest API Base of the account </br> Type: TEXT
		 */
		public static final String REST_API_BASE = "rest_api_base";

		/**
		 * Search API Base of the account </br> Type: TEXT
		 */
		public static final String SEARCH_API_BASE = "search_api_base";

		public static final String USER_COLOR = "user_color";

		/**
		 * Set to a non-zero integer if the account is activated. <br>
		 * Type: INTEGER (boolean)
		 */
		public static final String IS_ACTIVATED = "is_activated";

		/**
		 * User's profile image URL of the status. <br>
		 * Type: TEXT
		 */
		public static final String PROFILE_IMAGE_URL = "profile_image_url";

		public static final String[] COLUMNS = new String[] { _ID, USERNAME, USER_ID, AUTH_TYPE,
				BASIC_AUTH_PASSWORD, OAUTH_TOKEN, TOKEN_SECRET, REST_API_BASE, SEARCH_API_BASE,
				PROFILE_IMAGE_URL, USER_COLOR, IS_ACTIVATED };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN };

	}

	public static class Drafts implements BaseColumns {

		public static final String CONTENT_PATH = "drafts";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);

		public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

		/**
		 * Account IDs of unsent status.<br>
		 * Type: TEXT
		 */
		public static final String ACCOUNT_IDS = "account_ids";

		/**
		 * Status content.<br>
		 * Type: TEXT
		 */
		public static final String TEXT = "text";
	}

	public static class Mentions extends Statuses {

		public static final String CONTENT_PATH = "mentions";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);

	}

	public static class Statuses implements BaseColumns {

		public static final String CONTENT_PATH = "statuses";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);
		/**
		 * Account ID of the status.<br>
		 * Type: TEXT
		 */
		public static final String ACCOUNT_ID = "account_id";

		/**
		 * Status content.<br>
		 * Type: TEXT
		 */
		public static final String TEXT = "text";

		/**
		 * User name of the status.<br>
		 * Type: TEXT
		 */
		public static final String NAME = "name";

		/**
		 * User's screen name of the status.<br>
		 * Type: TEXT
		 */
		public static final String SCREEN_NAME = "screen_name";

		/**
		 * Unique id of the status.<br>
		 * Type: INTEGER UNIQUE(long)
		 */
		public static final String STATUS_ID = "status_id";

		/**
		 * Retweet count of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String RETWEET_COUNT = "retweet_count";

		/**
		 * Set to an non-zero integer if the status is a retweet, set to
		 * negative value if the status is retweeted by user.<br>
		 * Type: INTEGER
		 */
		public static final String IS_RETWEET = "is_retweet";

		/**
		 * Set to 1 if the status is a favorite.<br>
		 * Type: INTEGER (boolean)
		 */
		public static final String IS_FAVORITE = "is_favorite";

		public static final String HAS_MEDIA = "has_media";
		public static final String LOCATION = "location";

		/**
		 * Set to 1 if the status is a gap.<br>
		 * Type: INTEGER (boolean)
		 */
		public static final String IS_GAP = "is_gap";

		/**
		 * User's profile image URL of the status.<br>
		 * Type: TEXT NOT NULL
		 */
		public static final String PROFILE_IMAGE_URL = "profile_image_url";

		/**
		 * User's ID of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String USER_ID = "user_id";

		public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

		public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";

		public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";

		public static final String SOURCE = "source";

		public static final String IS_PROTECTED = "is_protected";

		/**
		 * Timestamp of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String STATUS_TIMESTAMP = "status_timestamp";

		public static final String DEFAULT_SORT_ORDER = STATUS_TIMESTAMP + " DESC";

		public static final String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, STATUS_ID, USER_ID,
				STATUS_TIMESTAMP, TEXT, NAME, SCREEN_NAME, PROFILE_IMAGE_URL,
				IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID, IN_REPLY_TO_SCREEN_NAME, SOURCE,
				LOCATION, RETWEET_COUNT, IS_RETWEET, IS_FAVORITE, HAS_MEDIA, IS_PROTECTED, IS_GAP };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_INT,
				TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN };

	}
}
