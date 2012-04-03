package org.mariotaku.twidere.provider;

import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class TweetStoreProvider extends ContentProvider {

	private static UriMatcher URI_MATCHER;

	private static final String TABLE_ACCOUNTS = "accounts";
	private static final String TABLE_STATUSES = "statuses";

	private static final int URI_ACCOUNTS = 1;

	private static final int URI_STATUSES = 2;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, Statuses.CONTENT_PATH, URI_STATUSES);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, Accounts.CONTENT_PATH, URI_ACCOUNTS);
	}

	private SQLiteDatabase database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = getTableName(uri);
		if (table == null) return 0;
		database.delete(table, selection, selectionArgs);
		return 0;
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

		return Uri.withAppendedPath(uri, String.valueOf(row_id));
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext(), "databases.sqlite", 1).getWritableDatabase();
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
		if (table == null) return 0;
		return database.update(table, values, selection, selectionArgs);
	}

	private String getTableName(Uri uri) {
		switch (URI_MATCHER.match(uri)) {
			case URI_STATUSES:
				return TABLE_STATUSES;
			case URI_ACCOUNTS:
				return TABLE_ACCOUNTS;
			default:
				return null;
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name, int version) {
			super(context, name, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			database.beginTransaction();
			database.execSQL(createTable(TABLE_STATUSES, TweetStore.Statuses.COLUMNS,
					TweetStore.Statuses.TYPES));
			database.execSQL(createTable(TABLE_ACCOUNTS, TweetStore.Accounts.COLUMNS,
					TweetStore.Accounts.TYPES));
			database.setTransactionSuccessful();
			database.endTransaction();
		}

		@Override
		public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

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

	}

}
