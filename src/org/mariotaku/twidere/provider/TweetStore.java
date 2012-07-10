/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.provider;

import org.mariotaku.twidere.Constants;

import android.net.Uri;
import android.provider.BaseColumns;

public final class TweetStore implements Constants {

	public static final String AUTHORITY = "org.mariotaku.twidere.provider.TweetStore";

	public static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };

	private static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";

	private static final String TYPE_TEXT = "TEXT";

	private static final String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";

	private static final String TYPE_INT = "INTEGER";

	private static final String TYPE_INT_UNIQUE = "INTEGER UNIQUE";

	private static final String TYPE_BOOLEAN = "INTEGER(1)";

	public static interface Accounts extends BaseColumns {

		public static final int AUTH_TYPE_OAUTH = 0;
		public static final int AUTH_TYPE_XAUTH = 1;
		public static final int AUTH_TYPE_BASIC = 2;
		public static final int AUTH_TYPE_TWIP_O_MODE = 3;

		public static final String CONTENT_PATH = "accounts";
		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

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

		public static final String REST_BASE_URL = "rest_base_url";

		public static final String SIGNING_REST_BASE_URL = "signing_rest_base_url";

		public static final String OAUTH_BASE_URL = "oauth_base_url";

		public static final String SIGNING_OAUTH_BASE_URL = "oauth_rest_base_url";

		/**
		 * Search Base URL of the account </br> Type: TEXT
		 */
		public static final String SEARCH_BASE_URL = "search_base_url";

		public static final String UPLOAD_BASE_URL = "upload_base_url";

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

		public static final String[] COLUMNS = new String[] { _ID, USERNAME, USER_ID, AUTH_TYPE, BASIC_AUTH_PASSWORD,
				OAUTH_TOKEN, TOKEN_SECRET, REST_BASE_URL, SIGNING_REST_BASE_URL, SEARCH_BASE_URL, UPLOAD_BASE_URL,
				OAUTH_BASE_URL, SIGNING_OAUTH_BASE_URL, PROFILE_IMAGE_URL, USER_COLOR, IS_ACTIVATED };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_INT_UNIQUE,
				TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN };

	}

	public static interface CachedUsers extends BaseColumns {

		public static final String CONTENT_PATH = "cached_users";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		/**
		 * User's ID of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String USER_ID = "user_id";

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
		 * User's profile image URL of the status.<br>
		 * Type: TEXT NOT NULL
		 */
		public static final String PROFILE_IMAGE_URL = "profile_image_url";

		public static final String[] COLUMNS = new String[] { _ID, USER_ID, NAME, SCREEN_NAME, PROFILE_IMAGE_URL };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT };

	}

	public static interface DirectMessages extends BaseColumns {

		public static final String CONTENT_PATH = "messages";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		public static final String ACCOUNT_ID = "account_id";
		public static final String MESSAGE_ID = "message_id";
		public static final String MESSAGE_TIMESTAMP = "message_timestamp";
		public static final String SENDER_ID = "sender_id";
		public static final String RECIPIENT_ID = "recipient_id";

		public static final String IS_GAP = "is_gap";

		public static final String TEXT = "text";
		public static final String SENDER_NAME = "sender_name";
		public static final String RECIPIENT_NAME = "recipient_name";
		public static final String SENDER_SCREEN_NAME = "sender_screen_name";
		public static final String RECIPIENT_SCREEN_NAME = "recipient_screen_name";
		public static final String SENDER_PROFILE_IMAGE_URL = "sender_profile_image_url";
		public static final String RECIPIENT_PROFILE_IMAGE_URL = "recipient_profile_image_url";

		public static final String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, MESSAGE_ID, MESSAGE_TIMESTAMP,
				SENDER_ID, RECIPIENT_ID, IS_GAP, TEXT, SENDER_NAME, RECIPIENT_NAME, SENDER_SCREEN_NAME,
				RECIPIENT_SCREEN_NAME, SENDER_PROFILE_IMAGE_URL, RECIPIENT_PROFILE_IMAGE_URL };
		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT,
				TYPE_INT, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT };

		public static final String DEFAULT_SORT_ORDER = MESSAGE_ID + " DESC";

		public static interface Conversation extends DirectMessages {

			public static final String CONTENT_PATH = "messages_conversation";

			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);
		}

		public static interface Inbox extends DirectMessages {

			public static final String CONTENT_PATH = "messages_inbox";

			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);

		}

		public static interface Outbox extends DirectMessages {

			public static final String CONTENT_PATH = "messages_outbox";

			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);

		}

	}

	public static interface Drafts extends BaseColumns {

		public static final String CONTENT_PATH = "drafts";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

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

		public static final String MEDIA_URI = "media_uri";

		public static final String[] COLUMNS = new String[] { _ID, IN_REPLY_TO_STATUS_ID, ACCOUNT_IDS, TEXT, MEDIA_URI };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT };
	}

	public static interface Filters extends BaseColumns {

		public static final String TEXT = "text";

		public static final String[] COLUMNS = new String[] { _ID, TEXT };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL };

		public static interface Keywords extends Filters {

			public static final String CONTENT_PATH = "filtered_keywords";
			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);
		}

		public static interface Sources extends Filters {

			public static final String CONTENT_PATH = "filtered_sources";
			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);
		}

		public static interface Users extends Filters {

			public static final String CONTENT_PATH = "filtered_users";
			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);
		}
	}

	public static interface Mentions extends Statuses {

		public static final String CONTENT_PATH = "mentions";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

	}

	public static interface Statuses extends BaseColumns {

		public static final String CONTENT_PATH = "statuses";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);
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
		public static final String TEXT_PLAIN = "text_plain";

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
		 * User's profile image URL of the status.<br>
		 * Type: TEXT NOT NULL
		 */
		public static final String PROFILE_IMAGE_URL = "profile_image_url";

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
		 * User's ID of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String USER_ID = "user_id";

		public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

		public static final String IN_REPLY_TO_USER_ID = "in_reply_to_user_id";

		public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";

		public static final String SOURCE = "source";

		public static final String IS_PROTECTED = "is_protected";

		public static final String RETWEET_ID = "retweet_id";

		public static final String RETWEETED_BY_ID = "retweeted_by_id";

		public static final String RETWEETED_BY_NAME = "retweeted_by_name";

		public static final String RETWEETED_BY_SCREEN_NAME = "retweeted_by_screen_name";

		/**
		 * Timestamp of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String STATUS_TIMESTAMP = "status_timestamp";

		public static final String DEFAULT_SORT_ORDER = STATUS_ID + " DESC";

		public static final String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, STATUS_ID, USER_ID, STATUS_TIMESTAMP,
				TEXT, TEXT_PLAIN, NAME, SCREEN_NAME, PROFILE_IMAGE_URL, IN_REPLY_TO_STATUS_ID, IN_REPLY_TO_USER_ID,
				IN_REPLY_TO_SCREEN_NAME, SOURCE, LOCATION, RETWEET_COUNT, RETWEET_ID, RETWEETED_BY_ID,
				RETWEETED_BY_NAME, RETWEETED_BY_SCREEN_NAME, IS_RETWEET, IS_FAVORITE, HAS_MEDIA, IS_PROTECTED, IS_GAP };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT,
				TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN, TYPE_BOOLEAN,
				TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN };

	}
}
