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
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameForContentUri;
import static org.mariotaku.twidere.util.Utils.parseInt;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;
import org.mariotaku.twidere.util.ArrayUtils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

public final class TweetStoreProvider extends ContentProvider implements Constants {

	private SQLiteDatabase database;

	private final Handler mErrorToastHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.obj instanceof Exception) {
				showErrorToast(getContext(), msg.obj, false);
			}
			super.handleMessage(msg);
		}

	};

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			try {
				result = database.delete(table, selection, selectionArgs);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		}
		if (result > 0) {
			onDatabaseUpdated(uri, false);
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
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
		onDatabaseUpdated(uri, true);
		try {
			return Uri.withAppendedPath(uri, String.valueOf(row_id));
		} catch (final SQLiteException e) {
			mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext(), DATABASES_NAME, DATABASES_VERSION).getWritableDatabase();
		return database != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final String table = getTableNameForContentUri(uri);
		if (table == null) return null;
		final String projection_string = projection != null ? ArrayUtils.buildString(projection, ',', false) : "*";
		if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table)) {
			// read-only here.
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 3) return null;
			final StringBuilder sql_builder = new StringBuilder();
			sql_builder.append("SELECT " + projection_string);
			sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_INBOX);
			sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + segments.get(1));
			sql_builder.append(" AND " + DirectMessages.SENDER_ID + " = " + segments.get(2));
			if (selection != null) {
				sql_builder.append(" AND " + selection);
			}
			sql_builder.append(" UNION ");
			sql_builder.append("SELECT " + projection_string);
			sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_OUTBOX);
			sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + segments.get(1));
			sql_builder.append(" AND " + DirectMessages.RECIPIENT_ID + " = " + segments.get(2));
			if (selection != null) {
				sql_builder.append(" AND " + selection);
			}
			sql_builder.append(" ORDER BY "
					+ (sortOrder != null ? sortOrder : DirectMessages.Conversation.DEFAULT_SORT_ORDER));
			try {
				return database.rawQuery(sql_builder.toString(), selectionArgs);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		} else if (TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME.equals(table)) {
			// read-only here.
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 3) return null;
			final StringBuilder sql_builder = new StringBuilder();
			sql_builder.append("SELECT " + projection_string);
			sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_INBOX);
			sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + segments.get(1));
			sql_builder.append(" AND " + DirectMessages.SENDER_SCREEN_NAME + " = '" + segments.get(2) + "'");
			if (selection != null) {
				sql_builder.append(" AND " + selection);
			}
			sql_builder.append(" UNION ");
			sql_builder.append("SELECT " + projection_string);
			sql_builder.append(" FROM " + TABLE_DIRECT_MESSAGES_OUTBOX);
			sql_builder.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + segments.get(1));
			sql_builder.append(" AND " + DirectMessages.RECIPIENT_SCREEN_NAME + " = '" + segments.get(2) + "'");
			if (selection != null) {
				sql_builder.append(" AND " + selection);
			}
			sql_builder.append(" ORDER BY "
					+ (sortOrder != null ? sortOrder : DirectMessages.Conversation.DEFAULT_SORT_ORDER));
			try {
				return database.rawQuery(sql_builder.toString(), selectionArgs);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		} else if (TABLE_DIRECT_MESSAGES.equals(table)) {
			// read-only here.
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
			try {
				return database.rawQuery(sql_builder.toString(), selectionArgs);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		} else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table)) {
			try {
				return database.rawQuery(
						DirectMessages.ConversationsEntry.buildSQL(parseInt(uri.getLastPathSegment())), null);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		} else {
			try {
				return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table))
				// read-only here.
				return 0;
			else if (TABLE_DIRECT_MESSAGES.equals(table)) // read-only here.
				return 0;
			else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table)) // read-only
																				// here.
				return 0;
			try {
				result = database.update(table, values, selection, selectionArgs);
			} catch (final SQLiteException e) {
				mErrorToastHandler.sendMessage(mErrorToastHandler.obtainMessage(0, e));
			}
		}
		if (result > 0) {
			onDatabaseUpdated(uri, false);
		}
		return result;
	}

	private void onDatabaseUpdated(Uri uri, boolean is_insert) {
		if (uri == null || "false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) return;
		final Context context = getContext();
		switch (getTableId(uri)) {
			case URI_ACCOUNTS: {
				clearAccountColor();
				context.sendBroadcast(new Intent(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED));
				break;
			}
			case URI_DRAFTS: {
				context.sendBroadcast(new Intent(BROADCAST_DRAFTS_DATABASE_UPDATED));
				break;
			}
			case URI_STATUSES: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED).putExtra(
							INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			case URI_MENTIONS: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_MENTIONS_DATABASE_UPDATED).putExtra(INTENT_KEY_SUCCEED,
							true));
				}
				break;
			}
			case URI_DIRECT_MESSAGES_INBOX: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED).putExtra(
							INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			case URI_DIRECT_MESSAGES_OUTBOX: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED).putExtra(
							INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			case URI_TRENDS_DAILY: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_TRENDS_UPDATED).putExtra(INTENT_KEY_SUCCEED, true));
					break;
				}
			}
			case URI_TRENDS_WEEKLY: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_TRENDS_UPDATED).putExtra(INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			case URI_TRENDS_LOCAL: {
				if (!is_insert || "true".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_TRENDS_UPDATED).putExtra(INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			case URI_TABS: {
				if (!"false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					context.sendBroadcast(new Intent(BROADCAST_TABS_UPDATED).putExtra(INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			default:
				return;
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name, int version) {
			super(context, name, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
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
			db.execSQL(createTable(TABLE_TRENDS_DAILY, CachedTrends.Daily.COLUMNS, CachedTrends.Daily.TYPES, true));
			db.execSQL(createTable(TABLE_TRENDS_WEEKLY, CachedTrends.Weekly.COLUMNS, CachedTrends.Weekly.TYPES, true));
			db.execSQL(createTable(TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true));
			db.execSQL(createTable(TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, false));
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			handleVersionChange(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			handleVersionChange(db);
		}

		private String createTable(String tableName, String[] columns, String[] types, boolean create_if_not_exists) {
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

		private void handleVersionChange(SQLiteDatabase db) {
			safeUpgrade(db, TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true, false);
			safeUpgrade(db, TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true, true);
			safeUpgrade(db, TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true, true);
			safeUpgrade(db, TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true, false);
			safeUpgrade(db, TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true, true);
			safeUpgrade(db, TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true, false);
			safeUpgrade(db, TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true, false);
			safeUpgrade(db, TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true, false);
			safeUpgrade(db, TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES,
					true, true);
			safeUpgrade(db, TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS, DirectMessages.Outbox.TYPES,
					true, true);
			safeUpgrade(db, TABLE_TRENDS_DAILY, CachedTrends.Daily.COLUMNS, CachedTrends.Daily.TYPES, true, true);
			safeUpgrade(db, TABLE_TRENDS_WEEKLY, CachedTrends.Weekly.COLUMNS, CachedTrends.Weekly.TYPES, true, true);
			safeUpgrade(db, TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true, true);
			safeUpgrade(db, TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true, false);
		}

	}

}
