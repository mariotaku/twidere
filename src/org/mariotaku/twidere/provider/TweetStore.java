package org.mariotaku.twidere.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TweetStore {

	public static final String PROTOCOL_CONTENT = "content://";

	public static final String AUTHORITY = "org.mariotaku.twidere.provider.TweetStore";

	public static final String KEY_TYPE = "type";

	public static final int VALUE_TYPE_STATUS = 1;
	public static final int VALUE_TYPE_MENTION = 2;

	private static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";

	private static final String TYPE_TEXT = "TEXT";

	private static final String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";

	private static final String TYPE_INT = "INTEGER";

	private static final String TYPE_INT_UNIQUE = "INTEGER UNIQUE";

	private static final String TYPE_BOOLEAN = "INTEGER(1)";

	public static class Accounts implements BaseColumns {

		public final static int AUTH_TYPE_OAUTH = 0;
		public final static int AUTH_TYPE_XAUTH = 1;
		public final static int AUTH_TYPE_BASIC = 2;

		public final static String CONTENT_PATH = "accounts";
		public final static Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);

		/**
		 * 帐户的登录名 <br>
		 * Type: TEXT NOT NULL
		 */
		public final static String USERNAME = "username";

		/**
		 * 账户的ID <br>
		 * Type: INTEGER (long)
		 */
		public final static String USER_ID = "user_id";

		/**
		 * 账户ID的列表，仅用作Intent中使用的key，不会出现在数据库中。 <br>
		 * Type: INTEGER (long)
		 */
		public final static String USER_IDS = "user_ids";

		/**
		 * 帐户的认证模式 Type: INTEGER
		 */
		public final static String AUTH_TYPE = "auth_type";

		/**
		 * 账户的密码（仅作为一个常量存在，不会保存进数据库） <br>
		 * Type: TEXT
		 */
		public final static String PASSWORD = "password";

		/**
		 * 账户的密码（仅在 Basic 认证模式下有效） <br>
		 * Type: TEXT
		 */
		public final static String BASIC_AUTH_PASSWORD = "basic_auth_password";

		/**
		 * 账户的 OAuth Token （仅在 OAuth/xAuth 认证模式下有效）<br>
		 * Type: TEXT
		 */
		public final static String OAUTH_TOKEN = "oauth_token";

		/**
		 * 账户的 Token Secret （仅在 OAuth/xAuth 认证模式下有效）<br>
		 * Type: TEXT
		 */
		public final static String TOKEN_SECRET = "token_secret";

		/**
		 * 帐户的 Rest API Base Type: TEXT
		 */
		public final static String REST_API_BASE = "rest_api_base";

		/**
		 * 帐户的 Search API Base Type: TEXT
		 */
		public final static String SEARCH_API_BASE = "search_api_base";

		public final static String USER_COLOR = "user_color";

		/**
		 * Set to a non-zero integer if the account is activated. <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_ACTIVATED = "is_activated";

		/**
		 * User's profile image URL of the status. <br>
		 * Type: TEXT NOT NULL
		 */
		public final static String PROFILE_IMAGE_URL = "profile_image_url";

		public final static String[] COLUMNS = new String[] { _ID, USERNAME, USER_ID, AUTH_TYPE,
				BASIC_AUTH_PASSWORD, OAUTH_TOKEN, TOKEN_SECRET, REST_API_BASE, SEARCH_API_BASE,
				PROFILE_IMAGE_URL, USER_COLOR, IS_ACTIVATED };

		public final static String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN };

	}

	public static class Mentions extends Statuses {

		public final static String CONTENT_PATH = "mentions";

		public final static Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);

	}

	public static class Statuses implements BaseColumns {

		public final static String CONTENT_PATH = "statuses";

		public final static Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);
		/**
		 * 已保存推文所属的帐户的ID <br>
		 * Type: TEXT
		 */
		public final static String ACCOUNT_ID = "account_id";

		/**
		 * 推文的内容 <br>
		 * Type: TEXT
		 */
		public final static String TEXT = "text";

		/**
		 * 推文发送者的名字 <br>
		 * Type: TEXT
		 */
		public final static String NAME = "name";

		/**
		 * User's screen name of the status. <br>
		 * Type: TEXT
		 */
		public final static String SCREEN_NAME = "screen_name";

		/**
		 * Unique id of the status. <br>
		 * Type: INTEGER UNIQUE(long)
		 */
		public final static String STATUS_ID = "status_id";

		/**
		 * Set to a non-zero integer if the status was a retweet. <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_RETWEET = "is_retweet";

		/**
		 * Set to a non-zero integer if the status was a favorite. <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_FAVORITE = "is_favorite";

		public final static String HAS_MEDIA = "has_media";
		public final static String HAS_LOCATION = "has_location";

		/**
		 * 如果数值非0，则这是一个间隔 <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_GAP = "is_gap";

		/**
		 * User's profile image URL of the status. <br>
		 * Type: TEXT NOT NULL
		 */
		public final static String PROFILE_IMAGE_URL = "profile_image_url";

		/**
		 * User's ID of the status. <br>
		 * Type: INTEGER (long)
		 */
		public final static String USER_ID = "user_id";

		public final static String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

		public final static String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";

		public final static String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";

		public final static String SOURCE = "source";

		/**
		 * Timestamp of the status. <br>
		 * Type: INTEGER
		 */
		public final static String STATUS_TIMESTAMP = "status_timestamp";

		public final static String DEFAULT_SORT_ORDER = STATUS_TIMESTAMP + " DESC";

		public final static String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, STATUS_ID, USER_ID,
				STATUS_TIMESTAMP, TEXT, NAME, SCREEN_NAME, PROFILE_IMAGE_URL,
				IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID, IN_REPLY_TO_SCREEN_NAME, SOURCE,
				IS_RETWEET, IS_FAVORITE, HAS_MEDIA, HAS_LOCATION, IS_GAP };

		public final static String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN,
				TYPE_BOOLEAN, TYPE_BOOLEAN };

	}
}
