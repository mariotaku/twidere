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
import org.mariotaku.twidere.util.ArrayUtils;

import android.net.Uri;
import android.provider.BaseColumns;

public final class TweetStore implements Constants {

	public static final String AUTHORITY = "twidere";

	public static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI,
			CachedStatuses.CONTENT_URI };
	public static final Uri[] CACHE_URIS = new Uri[] { CachedUsers.CONTENT_URI, CachedStatuses.CONTENT_URI,
			CachedHashtags.CONTENT_URI };
	public static final Uri[] DIRECT_MESSAGES_URIS = new Uri[] { DirectMessages.Inbox.CONTENT_URI,
			DirectMessages.Outbox.CONTENT_URI };

	private static final String TYPE_PRIMARY_KEY = "INTEGER PRIMARY KEY AUTOINCREMENT";

	private static final String TYPE_TEXT = "TEXT";
	private static final String TYPE_INT = "INTEGER";

	private static final String TYPE_TEXT_NOT_NULL = "TEXT NOT NULL";

	private static final String TYPE_INT_UNIQUE = "INTEGER UNIQUE";

	private static final String TYPE_BOOLEAN = "INTEGER(1)";

	public static final String CONTENT_PATH_NULL = "null_content";

	public static final String CONTENT_PATH_NOTIFICATIONS = "notifications";

	public static final Uri CONTENT_URI_NULL = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
			CONTENT_PATH_NULL);

	public static final Uri CONTENT_URI_NOTOFICATIONS = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
			CONTENT_PATH_NOTIFICATIONS);

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
		public static final String SCREEN_NAME = "screen_name";

		public static final String NAME = "name";

		/**
		 * Unique ID of the account<br>
		 * Type: INTEGER (long)
		 */
		public static final String ACCOUNT_ID = "account_id";

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

		public static final String SIGNING_OAUTH_BASE_URL = "signing_oauth_base_url";

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

		public static final String[] COLUMNS = new String[] { _ID, NAME, SCREEN_NAME, ACCOUNT_ID, AUTH_TYPE,
				BASIC_AUTH_PASSWORD, OAUTH_TOKEN, TOKEN_SECRET, REST_BASE_URL, SIGNING_REST_BASE_URL, OAUTH_BASE_URL,
				SIGNING_OAUTH_BASE_URL, PROFILE_IMAGE_URL, USER_COLOR, IS_ACTIVATED };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_TEXT_NOT_NULL,
				TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN };

	}

	public static interface CachedHashtags extends CachedValues {

		public static final String[] COLUMNS = new String[] { _ID, NAME };
		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT };

		public static final String CONTENT_PATH = "cached_hashtags";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);
	}

	public static interface CachedStatuses extends Statuses {
		public static final String CONTENT_PATH = "cached_statuses";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);
	}

	public static interface CachedTrends extends CachedValues {

		public static final String TIMESTAMP = "timestamp";

		public static final String[] COLUMNS = new String[] { _ID, NAME, TIMESTAMP };
		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_INT };

		public static interface Local extends CachedTrends {
			public static final String CONTENT_PATH = "local_trends";

			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);

		}

	}

	public static interface CachedUsers extends CachedValues {

		public static final String CONTENT_PATH = "cached_users";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		/**
		 * User's ID of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String USER_ID = "user_id";

		public static final String CREATED_AT = "created_at";

		public static final String IS_PROTECTED = "is_protected";

		public static final String IS_VERIFIED = "is_verified";
		
		public static final String IS_FOLLOWING = "is_following";

		public static final String DESCRIPTION_PLAIN = "description_plain";
		
		public static final String DESCRIPTION_HTML = "description_html";
		
		public static final String DESCRIPTION_EXPANDED = "description_expanded";

		public static final String LOCATION = "location";

		public static final String URL = "url";
		
		public static final String URL_EXPANDED = "url_expanded";

		public static final String PROFILE_BANNER_URL = "profile_banner_url";

		public static final String FOLLOWERS_COUNT = "followers_count";

		public static final String FRIENDS_COUNT = "friends_count";

		public static final String STATUSES_COUNT = "statuses_count";

		public static final String FAVORITES_COUNT = "favorites_count";

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

		public static final String[] COLUMNS = new String[] { _ID, USER_ID, CREATED_AT, NAME, SCREEN_NAME, DESCRIPTION_PLAIN,
				LOCATION, URL, PROFILE_IMAGE_URL, PROFILE_BANNER_URL, IS_PROTECTED, IS_VERIFIED, IS_FOLLOWING, FOLLOWERS_COUNT,
				FRIENDS_COUNT, STATUSES_COUNT, FAVORITES_COUNT, DESCRIPTION_HTML, DESCRIPTION_EXPANDED, URL_EXPANDED };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT_UNIQUE, TYPE_INT, TYPE_TEXT,
				TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, 
				TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT };

	}

	public static interface CachedValues extends BaseColumns {

		public static final String NAME = "name";
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

		public static final String IS_OUTGOING = "is_outgoing";

		public static final String TEXT_HTML = "text_html";
		public static final String TEXT_PLAIN = "text_plain";
		public static final String SENDER_NAME = "sender_name";
		public static final String RECIPIENT_NAME = "recipient_name";
		public static final String SENDER_SCREEN_NAME = "sender_screen_name";
		public static final String RECIPIENT_SCREEN_NAME = "recipient_screen_name";
		public static final String SENDER_PROFILE_IMAGE_URL = "sender_profile_image_url";
		public static final String RECIPIENT_PROFILE_IMAGE_URL = "recipient_profile_image_url";

		public static final String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, MESSAGE_ID, MESSAGE_TIMESTAMP,
				SENDER_ID, RECIPIENT_ID, IS_OUTGOING, TEXT_HTML, TEXT_PLAIN, SENDER_NAME, RECIPIENT_NAME,
				SENDER_SCREEN_NAME, RECIPIENT_SCREEN_NAME, SENDER_PROFILE_IMAGE_URL, RECIPIENT_PROFILE_IMAGE_URL };
		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT,
				TYPE_INT, TYPE_BOOLEAN, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT };

		public static final String DEFAULT_SORT_ORDER = MESSAGE_ID + " DESC";

		public static interface Conversation extends DirectMessages {

			public static final String DEFAULT_SORT_ORDER = MESSAGE_TIMESTAMP + " ASC";

			public static final String CONTENT_PATH = "messages_conversation";

			public static final String CONTENT_PATH_SCREEN_NAME = "messages_conversation_screen_name";

			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);

			public static final Uri CONTENT_URI_SCREEN_NAME = Uri.withAppendedPath(
					Uri.parse(PROTOCOL_CONTENT + AUTHORITY), CONTENT_PATH_SCREEN_NAME);

			public static final class QueryBuilder {

				public static final String buildByConversationId(final String[] projection, final long account_id,
						final long conversation_id, final String selection, final String sortOrder) {
					final String projection_string = projection != null ? ArrayUtils.toString(projection, ',', false)
							: "*";
					final StringBuilder sql_builder = new StringBuilder();
					sql_builder.append("SELECT " + projection_string);
					sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_INBOX);
					sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + account_id);
					sql_builder.append(" AND " + DirectMessages.SENDER_ID + " = " + conversation_id);
					if (selection != null) {
						sql_builder.append(" AND " + selection);
					}
					sql_builder.append(" UNION ");
					sql_builder.append("SELECT " + projection_string);
					sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_OUTBOX);
					sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + account_id);
					sql_builder.append(" AND " + DirectMessages.RECIPIENT_ID + " = " + conversation_id);
					if (selection != null) {
						sql_builder.append(" AND " + selection);
					}
					sql_builder.append(" ORDER BY "
							+ (sortOrder != null ? sortOrder : DirectMessages.Conversation.DEFAULT_SORT_ORDER));
					return sql_builder.toString();
				}

				public static final String buildByScreenName(final String[] projection, final long account_id,
						final String screen_name, final String selection, final String sortOrder) {
					final String projection_string = projection != null ? ArrayUtils.toString(projection, ',', false)
							: "*";
					final StringBuilder sql_builder = new StringBuilder();
					sql_builder.append("SELECT " + projection_string);
					sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_INBOX);
					sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + account_id);
					sql_builder.append(" AND " + DirectMessages.SENDER_SCREEN_NAME + " = '" + screen_name + "'");
					if (selection != null) {
						sql_builder.append(" AND " + selection);
					}
					sql_builder.append(" UNION ");
					sql_builder.append("SELECT " + projection_string);
					sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_OUTBOX);
					sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + account_id);
					sql_builder.append(" AND " + DirectMessages.RECIPIENT_SCREEN_NAME + " = '" + screen_name + "'");
					if (selection != null) {
						sql_builder.append(" AND " + selection);
					}
					sql_builder.append(" ORDER BY "
							+ (sortOrder != null ? sortOrder : DirectMessages.Conversation.DEFAULT_SORT_ORDER));
					return sql_builder.toString();
				}

			}
		}

		public static interface ConversationsEntry extends BaseColumns {

			public static final String CONTENT_PATH = "messages_conversations_entry";

			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);

			public static final String MESSAGE_TIMESTAMP = "message_timestamp";
			public static final String NAME = "name";
			public static final String SCREEN_NAME = "screen_name";
			public static final String PROFILE_IMAGE_URL = "profile_image_url";
			public static final String TEXT_HTML = "text_html";
			public static final String CONVERSATION_ID = "conversation_id";

			public static final int IDX__ID = 0;
			public static final int IDX_MESSAGE_TIMESTAMP = 1;
			public static final int IDX_MESSAGE_ID = 2;
			public static final int IDX_ACCOUNT_ID = 3;
			public static final int IDX_IS_OUTGOING = 4;
			public static final int IDX_NAME = 5;
			public static final int IDX_SCREEN_NAME = 6;
			public static final int IDX_PROFILE_IMAGE_URL = 7;
			public static final int IDX_TEXT = 8;
			public static final int IDX_CONVERSATION_ID = 9;

			public static class QueryBuilder {
				public static String build(final String where) {
					final StringBuilder builder = new StringBuilder();
					builder.append("SELECT " + _ID + ", " + MESSAGE_TIMESTAMP + ", " + MESSAGE_ID + ", " + ACCOUNT_ID
							+ ", " + IS_OUTGOING + ", " + NAME + ", " + SCREEN_NAME + ", " + PROFILE_IMAGE_URL + ", "
							+ TEXT_HTML + ", " + CONVERSATION_ID);
					builder.append(" FROM(");
					builder.append("SELECT " + _ID + ", " + MESSAGE_TIMESTAMP + ", " + MESSAGE_ID + ", " + ACCOUNT_ID
							+ ", " + "0 AS " + IS_OUTGOING + ", " + SENDER_NAME + " AS " + NAME + ", "
							+ SENDER_SCREEN_NAME + " AS " + SCREEN_NAME + ", " + SENDER_PROFILE_IMAGE_URL + " AS "
							+ PROFILE_IMAGE_URL + ", " + TEXT_HTML + ", " + SENDER_ID + " AS " + CONVERSATION_ID);
					builder.append(" FROM " + TABLE_DIRECT_MESSAGES_INBOX);
					builder.append(" UNION ");
					builder.append("SELECT " + _ID + ", " + MESSAGE_TIMESTAMP + ", " + MESSAGE_ID + ", " + ACCOUNT_ID
							+ ", " + "1 AS " + IS_OUTGOING + ", " + RECIPIENT_NAME + " AS " + NAME + ", "
							+ RECIPIENT_SCREEN_NAME + " AS " + SCREEN_NAME + ", " + RECIPIENT_PROFILE_IMAGE_URL
							+ " AS " + PROFILE_IMAGE_URL + ", " + TEXT_HTML + ", " + RECIPIENT_ID + " AS "
							+ CONVERSATION_ID);
					builder.append(" FROM " + TABLE_DIRECT_MESSAGES_OUTBOX);
					builder.append(")");
					builder.append(" WHERE ");
					builder.append(MESSAGE_ID + " IN(");
					builder.append("SELECT " + MESSAGE_ID + " FROM (" + " SELECT " + MESSAGE_ID + ", " + SENDER_ID
							+ " AS " + CONVERSATION_ID + " FROM " + TABLE_DIRECT_MESSAGES_INBOX + " WHERE "
							+ MESSAGE_ID + " IN (SELECT " + "MAX(" + MESSAGE_ID + ") FROM "
							+ TABLE_DIRECT_MESSAGES_INBOX + " GROUP BY " + SENDER_ID + ")" + " UNION " + " SELECT "
							+ MESSAGE_ID + ", " + RECIPIENT_ID + " AS " + CONVERSATION_ID + " FROM "
							+ TABLE_DIRECT_MESSAGES_OUTBOX + " WHERE " + MESSAGE_ID + " IN (" + " SELECT " + "MAX("
							+ MESSAGE_ID + ") FROM " + TABLE_DIRECT_MESSAGES_OUTBOX + " GROUP BY " + RECIPIENT_ID
							+ ")) " + " GROUP BY " + CONVERSATION_ID);
					builder.append(")");
					if (where != null) {
						builder.append(" AND " + where);
					}
					builder.append(" GROUP BY " + CONVERSATION_ID + ", " + ACCOUNT_ID);
					builder.append(" ORDER BY " + MESSAGE_TIMESTAMP + " DESC");
					return builder.toString();
				}
			}
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

		public static final class QueryBuilder {
			public static final String build(final String[] projection, final String selection, final String sortOrder) {
				final String projection_string = projection != null ? ArrayUtils.toString(projection, ',', false) : "*";
				final StringBuilder sql_builder = new StringBuilder();
				sql_builder.append("SELECT " + projection_string);
				sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_INBOX);
				if (selection != null) {
					sql_builder.append(" WHERE " + selection);
				}
				sql_builder.append(" UNION ");
				sql_builder.append("SELECT " + projection_string);
				sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_OUTBOX);
				if (selection != null) {
					sql_builder.append(" WHERE " + selection);
				}
				sql_builder.append(" ORDER BY " + (sortOrder != null ? sortOrder : DirectMessages.DEFAULT_SORT_ORDER));
				return sql_builder.toString();
			}
		}

	}

	public static interface Drafts extends BaseColumns {

		public static final String CONTENT_PATH = "drafts";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		/**
		 * Status content.<br>
		 * Type: TEXT
		 */
		public static final String TEXT = "text";

		/**
		 * Account IDs of unsent status.<br>
		 * Type: TEXT
		 */
		public static final String ACCOUNT_IDS = "account_ids";

		public static final String IMAGE_URI = "image_uri";

		public static final String LOCATION = "location";

		public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

		public static final String IS_IMAGE_ATTACHED = "is_image_attached";

		public static final String IS_PHOTO_ATTACHED = "is_photo_attached";

		public static final String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

		public static final String[] COLUMNS = new String[] { _ID, TEXT, ACCOUNT_IDS, LOCATION, IMAGE_URI,
				IN_REPLY_TO_STATUS_ID, IS_IMAGE_ATTACHED, IS_PHOTO_ATTACHED, IS_POSSIBLY_SENSITIVE };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN };

	}

	public static interface Filters extends BaseColumns {

		public static final String VALUE = "value";

		public static final String[] COLUMNS = new String[] { _ID, VALUE };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL };

		public static interface Keywords extends Filters {

			public static final String CONTENT_PATH = "filtered_keywords";
			public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
					CONTENT_PATH);
		}

		public static interface Links extends Filters {

			public static final String CONTENT_PATH = "filtered_links";
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
		 * Status content, in HTML. Please note, this is not actually original
		 * text.<br>
		 * Type: TEXT
		 */
		public static final String TEXT_HTML = "text_html";

		/**
		 *
		 */
		public static final String TEXT_PLAIN = "text_plain";
		
		public static final String TEXT_UNESCAPED = "text_unescaped";

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

		public static final String IS_POSSIBLY_SENSITIVE = "is_possibly_sensitive";

		/**
		 * Set to 1 if the status is a gap.<br>
		 * Type: INTEGER (boolean)
		 */
		public static final String IS_GAP = "is_gap";

		public static final String LOCATION = "location";

		/**
		 * User's ID of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String USER_ID = "user_id";

		public static final String IN_REPLY_TO_STATUS_ID = "in_reply_to_status_id";

		public static final String IN_REPLY_TO_SCREEN_NAME = "in_reply_to_screen_name";

		public static final String SOURCE = "source";

		public static final String IS_PROTECTED = "is_protected";

		public static final String IS_VERIFIED = "is_verified";

		public static final String IS_FOLLOWING = "is_following";

		public static final String RETWEET_ID = "retweet_id";

		public static final String RETWEETED_BY_ID = "retweeted_by_id";

		public static final String RETWEETED_BY_NAME = "retweeted_by_name";

		public static final String RETWEETED_BY_SCREEN_NAME = "retweeted_by_screen_name";

		/**
		 * Timestamp of the status.<br>
		 * Type: INTEGER (long)
		 */
		public static final String STATUS_TIMESTAMP = "status_timestamp";

		public static final String MY_RETWEET_ID = "my_retweet_id";

		public static final String IMAGE_PREVIEW_URL = "image_preview_url";
		
		public static final String SORT_ORDER_TIMESTAMP_DESC = STATUS_TIMESTAMP + " DESC";

		public static final String SORT_ORDER_STATUS_ID_DESC = STATUS_ID + " DESC";

		public static final String DEFAULT_SORT_ORDER = SORT_ORDER_STATUS_ID_DESC;

		public static final String[] COLUMNS = new String[] { _ID, ACCOUNT_ID, STATUS_ID, USER_ID, STATUS_TIMESTAMP,
				TEXT_HTML, TEXT_PLAIN, TEXT_UNESCAPED, NAME, SCREEN_NAME, PROFILE_IMAGE_URL, IN_REPLY_TO_STATUS_ID,
				IN_REPLY_TO_SCREEN_NAME, SOURCE, LOCATION, RETWEET_COUNT, RETWEET_ID, RETWEETED_BY_ID,
				RETWEETED_BY_NAME, RETWEETED_BY_SCREEN_NAME, MY_RETWEET_ID, IS_RETWEET, IS_FAVORITE, IS_PROTECTED,
				IS_VERIFIED, IS_FOLLOWING, IS_GAP, IS_POSSIBLY_SENSITIVE, IMAGE_PREVIEW_URL };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_INT, TYPE_INT, TYPE_INT, TYPE_INT,
				TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
				TYPE_INT, TYPE_INT, TYPE_INT, TYPE_TEXT, TYPE_TEXT, TYPE_INT, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN,
				TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_BOOLEAN, TYPE_TEXT };

	}

	public static interface Tabs extends BaseColumns {
		public static final String CONTENT_PATH = "tabs";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		public static final String NAME = "name";

		public static final String ICON = "icon";

		public static final String TYPE = "type";

		public static final String ARGUMENTS = "arguments";

		public static final String POSITION = "position";

		public static final String[] COLUMNS = new String[] { _ID, NAME, ICON, TYPE, ARGUMENTS, POSITION };

		public static final String[] TYPES = new String[] { TYPE_PRIMARY_KEY, TYPE_TEXT_NOT_NULL, TYPE_TEXT, TYPE_TEXT,
				TYPE_TEXT, TYPE_INT };

		public static final String DEFAULT_SORT_ORDER = POSITION + " ASC";
	}
	
	public static interface Preferences extends BaseColumns {
		public static final String CONTENT_PATH = "preferences";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);
				
		public static final int TYPE_INVALID = -1;
		
		public static final int TYPE_NULL = 0;
				
		public static final int TYPE_BOOLEAN = 1;
		
		public static final int TYPE_INTEGER = 2;
		
		public static final int TYPE_LONG = 3;

		public static final int TYPE_FLOAT = 4;
		
		public static final int TYPE_STRING = 5;

		public static final String KEY = "key";

		public static final String VALUE = "value";

		public static final String TYPE = "type";

		public static final String[] MATRIX_COLUMNS = new String[] { KEY, VALUE, TYPE };
		
		public static final String[] COLUMNS = new String[] { _ID, KEY, VALUE, TYPE };
	}

	public static interface Permissions extends BaseColumns {
		public static final String CONTENT_PATH = "permissions";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		public static final String PERMISSION = "permissions";

		public static final String PACKAGE_NAME = "package_name";

		public static final String[] MATRIX_COLUMNS = new String[] { PACKAGE_NAME, PERMISSION };

		public static final String[] COLUMNS = new String[] { _ID, PACKAGE_NAME, PERMISSION };
	}
	

	public static interface DNS extends BaseColumns {
		public static final String CONTENT_PATH = "dns";

		public static final Uri CONTENT_URI = Uri.withAppendedPath(Uri.parse(PROTOCOL_CONTENT + AUTHORITY),
				CONTENT_PATH);

		public static final String HOST = "host";
		
		public static final String ADDRESS = "address";

		public static final String[] MATRIX_COLUMNS = new String[] { HOST, ADDRESS };

		public static final String[] COLUMNS = new String[] { _ID, HOST, ADDRESS };
	}
}
