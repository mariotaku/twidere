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

import static org.mariotaku.twidere.util.DatabaseUpgradeHelper.safeUpgrade;
import static org.mariotaku.twidere.util.Utils.clearAccountColor;
import static org.mariotaku.twidere.util.Utils.clearAccountName;
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameForContentUri;

import java.util.HashMap;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public final class TweetStoreProvider extends ContentProvider implements Constants {

	private SQLiteDatabase database;

	@Override
	public int bulkInsert(final Uri uri, final ContentValues[] values) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			database.beginTransaction();
			for (final ContentValues contentValues : values) {
				database.insert(table, null, contentValues);
				result++;
			}
			database.setTransactionSuccessful();
			database.endTransaction();
		}
		if (result > 0) {
			onDatabaseUpdated(uri);
		}
		return result;
	};

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			result = database.delete(table, selection, selectionArgs);
		}
		if (result > 0) {
			onDatabaseUpdated(uri);
		}
		return result;
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		final String table = getTableNameForContentUri(uri);
		if (table == null) return null;
		if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table))
			// read-only here.
			return null;
		else if (TABLE_DIRECT_MESSAGES.equals(table)) // read-only here.
			return null;
		else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table)) // read-only
																			// here.
			return null;
		final long row_id = database.insert(table, null, values);
		onDatabaseUpdated(uri);
		return Uri.withAppendedPath(uri, String.valueOf(row_id));
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext(), DATABASES_NAME, DATABASES_VERSION).getWritableDatabase();
		return database != null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		final String table = getTableNameForContentUri(uri);
		if (table == null) return null;
		if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table)) {
			// read-only here.
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 3) return null;
			final String query = Conversation.QueryBuilder.buildByConversationId(projection,
					Long.parseLong(segments.get(1)), Long.parseLong(segments.get(2)), selection, sortOrder);
			return database.rawQuery(query, selectionArgs);
		} else if (TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME.equals(table)) {
			// read-only here.
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 3) return null;
			final String query = Conversation.QueryBuilder.buildByScreenName(projection,
					Long.parseLong(segments.get(1)), segments.get(2), selection, sortOrder);
			return database.rawQuery(query, selectionArgs);
		} else if (TABLE_DIRECT_MESSAGES.equals(table)) {
			// read-only here.
			final String query = DirectMessages.QueryBuilder.build(projection, selection, sortOrder);
			return database.rawQuery(query, selectionArgs);
		} else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table))
			return database.rawQuery(ConversationsEntry.QueryBuilder.build(selection), null);
		return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table))
				return 0;
			else if (TABLE_DIRECT_MESSAGES.equals(table))
				return 0;
			else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table))
				return 0;
			result = database.update(table, values, selection, selectionArgs);
		}
		if (result > 0) {
			onDatabaseUpdated(uri);
		}
		return result;
	}

	private void onDatabaseUpdated(final Uri uri) {
		if (uri == null) return;
		final Context context = getContext();
		switch (getTableId(uri)) {
			case URI_ACCOUNTS: {
				clearAccountColor();
				clearAccountName();
				context.sendBroadcast(new Intent(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED));
				break;
			}
			case URI_DRAFTS: {
				context.sendBroadcast(new Intent(BROADCAST_DRAFTS_DATABASE_UPDATED));
				break;
			}
			case URI_STATUSES: {
				context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED));
				break;
			}
			case URI_MENTIONS: {
				context.sendBroadcast(new Intent(BROADCAST_MENTIONS_DATABASE_UPDATED));
				break;
			}
			case URI_DIRECT_MESSAGES_INBOX: {
				context.sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED));
				break;
			}
			case URI_DIRECT_MESSAGES_OUTBOX: {
				context.sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED));
				break;
			}
			case URI_TRENDS_LOCAL: {
				context.sendBroadcast(new Intent(BROADCAST_TRENDS_UPDATED));
				break;
			}
			case URI_TABS: {
				context.sendBroadcast(new Intent(BROADCAST_TABS_UPDATED));		
				break;
			}
			case URI_FILTERED_USERS:
			case URI_FILTERED_KEYWORDS:
			case URI_FILTERED_SOURCES: {
				context.sendBroadcast(new Intent(BROADCAST_FILTERS_UPDATED));
				break;
			}
			default:
				return;
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(final Context context, final String name, final int version) {
			super(context, name, null, version);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.beginTransaction();
			db.execSQL(createTable(TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true));
			db.execSQL(createTable(TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true));
			db.execSQL(createTable(TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true));
			db.execSQL(createTable(TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true));
			db.execSQL(createTable(TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true));
			db.execSQL(createTable(TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true));
			db.execSQL(createTable(TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true));
			db.execSQL(createTable(TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
			db.execSQL(createTable(TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
			db.execSQL(createTable(TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS,
					DirectMessages.Inbox.TYPES, true));
			db.execSQL(createTable(TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS,
					DirectMessages.Outbox.TYPES, true));
			db.execSQL(createTable(TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true));
			db.execSQL(createTable(TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true));
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		@Override
		public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			handleVersionChange(db);
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
			handleVersionChange(db);
		}

		private String createTable(final String tableName, final String[] columns, final String[] types,
				final boolean create_if_not_exists) {
			if (tableName == null || columns == null || types == null || types.length != columns.length
					|| types.length == 0)
				throw new IllegalArgumentException("Invalid parameters for creating table " + tableName);
			final StringBuilder stringBuilder = new StringBuilder(create_if_not_exists ? "CREATE TABLE IF NOT EXISTS "
					: "CREATE TABLE ");

			stringBuilder.append(tableName);
			stringBuilder.append(" (");
			final int length = columns.length;
			for (int n = 0, i = length; n < i; n++) {
				if (n > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(columns[n]).append(' ').append(types[n]);
			}
			return stringBuilder.append(");").toString();
		}

		private void handleVersionChange(final SQLiteDatabase db) {
			final HashMap<String, String> account_db_table_alias = new HashMap<String, String>();
			account_db_table_alias.put(Accounts.SCREEN_NAME, "username");
			account_db_table_alias.put(Accounts.NAME, "username");
			account_db_table_alias.put(Accounts.ACCOUNT_ID, "user_id");
			safeUpgrade(db, TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true, false, account_db_table_alias);
			safeUpgrade(db, TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true, true, null);
			safeUpgrade(db, TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true, true, null);
			safeUpgrade(db, TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true, false, null);
			safeUpgrade(db, TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true, true, null);
			safeUpgrade(db, TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true, false, null);
			safeUpgrade(db, TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true, false,
					null);
			safeUpgrade(db, TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true, false, null);
			safeUpgrade(db, TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES,
					true, true, null);
			safeUpgrade(db, TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS, DirectMessages.Outbox.TYPES,
					true, true, null);
			safeUpgrade(db, TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true, true, null);
			safeUpgrade(db, TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true, false, null);
		}

	}

}
