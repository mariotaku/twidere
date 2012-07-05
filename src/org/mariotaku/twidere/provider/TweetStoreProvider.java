package org.mariotaku.twidere.provider;

import static org.mariotaku.twidere.util.DatabaseUpgradeHelper.safeUpgrade;
import static org.mariotaku.twidere.util.Utils.clearAccountColor;
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameForContentUri;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

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
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			result = database.delete(table, selection, selectionArgs);
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
		final long row_id = database.insert(table, null, values);
		onDatabaseUpdated(uri, true);
		return Uri.withAppendedPath(uri, String.valueOf(row_id));
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
		return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			result = database.update(table, values, selection, selectionArgs);
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
				if (!is_insert) {
					context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED).putExtra(
							INTENT_KEY_SUCCEED, true));
				}
				break;
			}
			case URI_MENTIONS: {
				if (!is_insert) {
					context.sendBroadcast(new Intent(BROADCAST_MENTIONS_DATABASE_UPDATED).putExtra(INTENT_KEY_SUCCEED,
							true));
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
			db.execSQL(createTable(TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, false));
			db.execSQL(createTable(TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, false));
			db.execSQL(createTable(TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, false));
			db.execSQL(createTable(TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, false));
			db.execSQL(createTable(TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, false));
			db.execSQL(createTable(TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, false));
			db.execSQL(createTable(TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, false));
			db.execSQL(createTable(TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, false));
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
			for (int n = 0, i = columns.length; n < i; n++) {
				if (n > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(columns[n]).append(' ').append(types[n]);
			}
			return stringBuilder.append(");").toString();
		}

		private void handleVersionChange(SQLiteDatabase db) {
			safeUpgrade(db, TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true);
			safeUpgrade(db, TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true);
			safeUpgrade(db, TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true);
			safeUpgrade(db, TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true);
			safeUpgrade(db, TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true);
			safeUpgrade(db, TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true);
			safeUpgrade(db, TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true);
			safeUpgrade(db, TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true);
		}

	}

}
