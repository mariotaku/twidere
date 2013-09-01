package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

public class SQLiteDatabaseAccessor {

	public static long insertWithOnConflict(final SQLiteDatabase db, final String table, final String nullColumnHack,
			final ContentValues initialValues, final int conflictAlgorithm) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) return db.insert(table, nullColumnHack, initialValues);
		return SQLiteDatabaseAccessorSDK8.insertWithOnConflict(db, table, nullColumnHack, initialValues,
				conflictAlgorithm);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private static class SQLiteDatabaseAccessorSDK8 {

		private static long insertWithOnConflict(final SQLiteDatabase db, final String table,
				final String nullColumnHack, final ContentValues initialValues, final int conflictAlgorithm) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO)
				return db.insert(table, nullColumnHack, initialValues);
			return db.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
		}

	}
}
