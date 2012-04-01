package org.mariotaku.twidere.provider;

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

	private static final int URI_ACCOUNT = 2;

	private static final int URI_STATUSES_FOR_ALL_ACCOUNTS = 3;

	private static final int URI_STATUSES = 4;

	private static final int URI_ALLENTRIES = 5;

	private static final int URI_ALLENTRIES_ENTRY = 6;

	private static final int URI_FAVORITES = 7;

	private static final int URI_FAVORITES_ENTRY = 8;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TweetStore.Statuses.CONTENT_PATH,
				URI_STATUSES_FOR_ALL_ACCOUNTS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, "statuses/#", URI_STATUSES);
	}

	private SQLiteDatabase database;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		database = new DatabaseHelper(getContext(), "databases.sqlite", 1).getWritableDatabase();
		return database != null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {

		String table = null;

		switch (URI_MATCHER.match(uri)) {
			case URI_STATUSES_FOR_ALL_ACCOUNTS:
			case URI_STATUSES:
				table = TABLE_STATUSES;
				break;
			default:
				return null;
		}

		return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
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
