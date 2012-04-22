package org.mariotaku.twidere.provider;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class TweetStoreProvider extends ContentProvider implements Constants {

	private static UriMatcher URI_MATCHER;

	private static final String TABLE_ACCOUNTS = Accounts.CONTENT_PATH;
	private static final String TABLE_STATUSES = Statuses.CONTENT_PATH;
	private static final String TABLE_MENTIONS = Mentions.CONTENT_PATH;

	private static final int URI_ACCOUNTS = 1;
	private static final int URI_STATUSES = 2;
	private static final int URI_MENTIONS = 3;
	private static final int URI_USER_TIMELINE = 4;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, Statuses.CONTENT_PATH, URI_STATUSES);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, Accounts.CONTENT_PATH, URI_ACCOUNTS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, Mentions.CONTENT_PATH, URI_MENTIONS);
		// URI_MATCHER.addURI(TweetStore.AUTHORITY, "user/*",
		// URI_USER_TIMELINE);
	}

	private SQLiteDatabase database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = getTableName(uri);
		int result = 0;
		if (table != null) {
			result = database.delete(table, selection, selectionArgs);
		}
		if (result > 0) {
			sendBroadcastForOperatedUri(uri);
		}
		return result;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = getTableName(uri);
		if (table == null) return null;
		long row_id = database.insert(table, null, values);
		sendBroadcastForOperatedUri(uri);
		return Uri.withAppendedPath(uri, String.valueOf(row_id));
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext(), DATABASES_NAME, DATABASES_VERSION)
				.getWritableDatabase();
		return database != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {

		String table = getTableName(uri);
		if (table == null) return null;

		return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String table = getTableName(uri);
		int result = 0;
		if (table != null) {
			result = database.update(table, values, selection, selectionArgs);
		}
		if (result > 0) {
			sendBroadcastForOperatedUri(uri);
		}
		return result;
	}

	private String getTableName(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case URI_STATUSES:
				return TABLE_STATUSES;
			case URI_ACCOUNTS:
				return TABLE_ACCOUNTS;
			case URI_MENTIONS:
				return TABLE_MENTIONS;
			default:
				return null;
		}
	}

	private void sendBroadcastForOperatedUri(Uri uri) {
		Context context = getContext();
		switch (URI_MATCHER.match(uri)) {
			case URI_STATUSES:
				context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_UPDATED));
				break;
			case URI_ACCOUNTS:
				context.sendBroadcast(new Intent(BROADCAST_ACCOUNTS_LIST_UPDATED));
				break;
			case URI_MENTIONS:
				context.sendBroadcast(new Intent(BROADCAST_MENTIONS_UPDATED));
				break;
			default:
				return;
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		private static final int FIELD_TYPE_NULL = 0;

		private static final int FIELD_TYPE_INTEGER = 1;

		private static final int FIELD_TYPE_FLOAT = 2;

		private static final int FIELD_TYPE_STRING = 3;

		private static final int FIELD_TYPE_BLOB = 4;

		public DatabaseHelper(Context context, String name, int version) {
			super(context, name, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.beginTransaction();
			db.execSQL(createTable(TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES));
			db.execSQL(createTable(TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES));
			db.execSQL(createTable(TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES));
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

		private String createTable(String tableName, String[] columns, String[] types) {
			if (tableName == null || columns == null || types == null
					|| types.length != columns.length || types.length == 0)
				throw new IllegalArgumentException("Invalid parameters for creating table "
						+ tableName);
			else {
				StringBuilder stringBuilder = new StringBuilder("CREATE TABLE ");

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
		}

		private int getTypeInt(String type) {
			int idx = type.contains("(") ? type.indexOf("(") : type.indexOf(" ");
			String type_main = idx > -1 ? type.substring(0, idx) : type;
			if ("NULL".equalsIgnoreCase(type_main))
				return FIELD_TYPE_NULL;
			else if ("INTEGER".equalsIgnoreCase(type_main))
				return FIELD_TYPE_INTEGER;
			else if ("FLOAT".equalsIgnoreCase(type_main))
				return FIELD_TYPE_FLOAT;
			else if ("TEXT".equalsIgnoreCase(type_main))
				return FIELD_TYPE_STRING;
			else if ("BLOB".equalsIgnoreCase(type_main)) return FIELD_TYPE_BLOB;
			throw new IllegalStateException("Unknown field type " + type + " !");
		}

		private String getTypeString(SQLiteDatabase db, String table, String column) {

			String sql = "SELECT typeof(" + column + ") FROM " + table;
			Cursor cur = db.rawQuery(sql, null);
			if (cur == null) return null;

			cur.moveToFirst();
			String type = cur.getString(0);
			cur.close();
			return type;

		}

		private void handleVersionChange(SQLiteDatabase db) {
			safeVersionChange(db, TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES);
			safeVersionChange(db, TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES);
			safeVersionChange(db, TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES);
		}

		private boolean isColumnContained(String[] cols, String col) {
			for (String tmp_col : cols) {
				if (col.equals(tmp_col)) return true;
			}
			return false;
		}

		private boolean isTypeCompatible(String old_type, String new_type) {
			if (old_type != null && new_type != null) {
				int old_idx = old_type.contains("(") ? old_type.indexOf("(") : old_type
						.indexOf(" ");
				int new_idx = new_type.contains("(") ? new_type.indexOf("(") : new_type
						.indexOf(" ");
				String old_type_main = old_idx > -1 ? old_type.substring(0, old_idx) : old_type;
				String new_type_main = new_idx > -1 ? new_type.substring(0, new_idx) : new_type;
				return old_type_main.equalsIgnoreCase(new_type_main);
			}
			return false;
		}

		private void safeVersionChange(SQLiteDatabase db, String table, String[] new_cols,
				String[] new_types) {

			if (new_cols == null || new_types == null || new_cols.length != new_types.length)
				throw new IllegalArgumentException(
						"Invalid parameters, length of columns and types not match.");

			// First we need to get all data from old table.
			Cursor cur = db.query(table, null, null, null, null, null, null);
			cur.moveToFirst();
			String[] old_cols = cur.getColumnNames();

			List<ContentValues> values_list = new ArrayList<ContentValues>();

			while (!cur.isAfterLast()) {
				ContentValues values = new ContentValues();
				for (int i = 0; i < new_cols.length; i++) {
					String new_col = new_cols[i];
					String new_type = new_types[i];
					if (BaseColumns._ID.equals(new_col)) {
						continue;
					}

					int idx = cur.getColumnIndex(new_col);

					if (isColumnContained(old_cols, new_col)) {
						String old_type = getTypeString(db, table, new_col);
						boolean compatible = isTypeCompatible(old_type, new_type);
						if (compatible && idx > -1) {
							switch (getTypeInt(new_type)) {
								case FIELD_TYPE_INTEGER:
									values.put(new_col, cur.getLong(idx));
									break;
								case FIELD_TYPE_FLOAT:
									values.put(new_col, cur.getFloat(idx));
									break;
								case FIELD_TYPE_STRING:
									values.put(new_col, cur.getString(idx));
									break;
								case FIELD_TYPE_BLOB:
									values.put(new_col, cur.getBlob(idx));
									break;
								case FIELD_TYPE_NULL:
								default:
									break;
							}
						}
					}

				}
				values_list.add(values);
				cur.moveToNext();
			}
			cur.close();
			// OK, now we got all data can be moved from old table, so we will
			// delete the old table and create a new one.
			db.execSQL("DROP TABLE IF EXISTS " + table);
			db.execSQL(createTable(table, new_cols, new_types));

			// Now, insert all data backuped into new table.
			for (ContentValues values : values_list) {
				db.insert(table, null, values);
			}
		}

	}

}
