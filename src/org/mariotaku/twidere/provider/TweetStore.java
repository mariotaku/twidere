package org.mariotaku.twidere.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TweetStore {

	public static final String PROTOCOL_CONTENT = "content://";

	public static final String AUTHORITY = "org.mariotaku.twidere.provider.TweetStore";

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

		public final static String[] COLUMNS = new String[] { _ID, USERNAME, USER_ID, AUTH_TYPE,
				BASIC_AUTH_PASSWORD, OAUTH_TOKEN, TOKEN_SECRET, REST_API_BASE, SEARCH_API_BASE };

		public final static String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT };

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
		 * 推文发送者的用户名（@username 格式） <br>
		 * Type: TEXT
		 */
		public final static String SCREEN_NAME = "screen_name";

		/**
		 * 推文的唯一ID <br>
		 * Type: INTEGER UNIQUE(long)
		 */
		public final static String STATUS_ID = "status_id";

		/**
		 * 如果这条推文是转发则数值不为0 <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_RETWEET = "is_retweet";

		/**
		 * 如果这条推文是收藏则数值不为0 <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_FAVORITE = "is_favorite";

		/**
		 * 推文发送者的头像的URL <br>
		 * Type: TEXT NOT NULL
		 */
		public final static String PROFILE_IMAGE_URL = "profile_image_url";

		/**
		 * 推文发送者的ID <br>
		 * Type: INTEGER (long)
		 */
		public final static String USER_ID = "user_id";

		/**
		 * 推文的时间戳 <br>
		 * Type: INTEGER
		 */
		public final static String STATUS_TIMESTAMP = "status_timestamp";

		public final static String DEFAULT_SORT_ORDER = STATUS_TIMESTAMP + " DESC";

		public final static String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, STATUS_ID, USER_ID,
				STATUS_TIMESTAMP, TEXT, NAME, SCREEN_NAME, PROFILE_IMAGE_URL, IS_RETWEET,
				IS_FAVORITE };

		public final static String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_INT, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL,
				TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL, TYPE_BOOLEAN, TYPE_BOOLEAN };

	}
}
