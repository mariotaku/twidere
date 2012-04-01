package org.mariotaku.twidere.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class TweetStore {

	public static final String PROTOCOL_CONTENT = "content://";

	public static final String AUTHORITY = "org.mariotaku.twidere.provider.TweetStore";

	private static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";

	private static final String TYPE_TEXT = "TEXT";

	private static final String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";

	private static final String TYPE_TEXT_UNIQUE = "TEXT UNIQUE";

	private static final String TYPE_DATETIME = "DATETIME";

	private static final String TYPE_INT = "INTEGER";

	private static final String TYPE_INT_UNIQUE = "INTEGER UNIQUE";

	private static final String TYPE_BOOLEAN = "INTEGER(1)";

	public static class Statuses implements BaseColumns {

		public final static String CONTENT_PATH = "statuses";
		public final static Uri CONTENT_URI = Uri.withAppendedPath(
				Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH);
		/**
		 * Status content. <br>
		 * Type: TEXT
		 */
		public final static String TEXT = "text";

		/**
		 * User's name of the status. <br>
		 * Type: TEXT
		 */
		public final static String USER_NAME = "user_name";

		/**
		 * User's screen name of the status. <br>
		 * Type: TEXT
		 */
		public final static String SCREEN_NAME = "screen_name";

		/**
		 * Unique id of status. Type: INTEGER (long)
		 */
		public final static String STATUS_ID = "status_id";

		/**
		 * Non-zero if the status is a retweet <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_RETWEET = "is_retweet";

		/**
		 * Non-zero if the status is a favorite <br>
		 * Type: INTEGER (boolean)
		 */
		public final static String IS_FAVORITE = "is_favorite";

		public final static String[] COLUMNS = new String[] { _ID, STATUS_ID, TEXT, USER_NAME,
				SCREEN_NAME, IS_RETWEET, IS_FAVORITE };

		public final static String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE,
				TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL, TYPE_BOOLEAN,
				TYPE_BOOLEAN };

	}
}
