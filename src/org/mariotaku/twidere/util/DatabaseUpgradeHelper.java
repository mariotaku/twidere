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

package org.mariotaku.twidere.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class DatabaseUpgradeHelper {

	private static final String TYPE_NULL = "NULL";
	private static final String TYPE_INT = "INTEGER";
	private static final String TYPE_FLOAT = "FLOAT";
	private static final String TYPE_TEXT = "TEXT";
	private static final String TYPE_BLOB = "BLOB";

	private static final int FIELD_TYPE_NULL = 0;
	private static final int FIELD_TYPE_INTEGER = 1;
	private static final int FIELD_TYPE_FLOAT = 2;
	private static final int FIELD_TYPE_STRING = 3;
	private static final int FIELD_TYPE_BLOB = 4;

	public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] new_cols,
			final String[] new_types, final boolean fast_upgrade, final boolean drop_directly,
			final HashMap<String, String> column_alias) {

		if (new_cols == null || new_types == null || new_cols.length != new_types.length)
			throw new IllegalArgumentException("Invalid parameters for upgrading table " + table
					+ ", length of columns and types not match.");

		final List<ContentValues> values_list = new ArrayList<ContentValues>();

		// First, create the table if not exists.
		db.execSQL(createTable(table, new_cols, new_types, true));

		// We need to get all data from old table.
		final Cursor cur = db.query(table, null, null, null, null, null, null);
		cur.moveToFirst();
		final String[] old_cols = cur.getColumnNames();

		if (fast_upgrade) {
			final String[] old_types = getBatchTypeString(db, table, old_cols);

			if (!shouldUpgrade(old_cols, old_types, new_cols, new_types)) {
				if (cur != null) {
					cur.close();
				}
				return;
			}
		}

		// If drop_directly set to true, we will not backup any data actually.
		if (!drop_directly) {

			while (!cur.isAfterLast()) {
				final ContentValues values = new ContentValues();
				final int length = new_cols.length;
				for (int i = 0; i < length; i++) {
					final String new_col = new_cols[i];
					final String col_alias = column_alias != null && column_alias.containsKey(new_col)
							&& ArrayUtils.contains(old_cols, column_alias.get(new_col)) ? column_alias.get(new_col)
							: new_col;
					final String new_type = new_types[i];
					if (BaseColumns._ID.equals(new_col)) {
						continue;
					}

					final int idx = cur.getColumnIndex(col_alias);

					if (ArrayUtils.contains(old_cols, col_alias)) {
						final String old_type = getTypeString(db, table, col_alias);
						final boolean compatible = isTypeCompatible(old_type, new_type, false);
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
		}
		cur.close();

		// OK, now we got all data can be moved from old table, so we will
		// delete the old table and create a new one.
		db.execSQL("DROP TABLE IF EXISTS " + table);
		db.execSQL(createTable(table, new_cols, new_types, false));

		// Now, insert all data backuped into new table.
		db.beginTransaction();
		for (final ContentValues values : values_list) {
			db.insert(table, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private static String createTable(final String tableName, final String[] columns, final String[] types,
			final boolean create_if_not_exists) {
		if (tableName == null || columns == null || types == null || types.length != columns.length
				|| types.length == 0)
			throw new IllegalArgumentException("Invalid parameters for creating table " + tableName);
		final StringBuilder stringBuilder = new StringBuilder(create_if_not_exists ? "CREATE TABLE IF NOT EXISTS "
				: "CREATE TABLE ");

		stringBuilder.append(tableName);
		stringBuilder.append(" (");
		final int length = columns.length;
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(columns[i]).append(' ').append(types[i]);
		}
		return stringBuilder.append(");").toString();
	}

	private static String[] getBatchTypeString(final SQLiteDatabase db, final String table, final String[] columns) {

		if (columns == null || columns.length == 0) return new String[0];
		final String[] types = new String[columns.length];
		final StringBuilder builder = new StringBuilder();
		builder.append("SELECT ");
		final int columns_length = columns.length;
		for (int i = 0, len = columns_length; i < len; i++) {
			builder.append("typeof(" + columns[i] + ")");
			if (i != columns.length - 1) {
				builder.append(", ");
			}
		}
		builder.append(" FROM " + table);
		final Cursor cur = db.rawQuery(builder.toString(), null);
		if (cur == null) return null;

		if (cur.getCount() > 0) {
			cur.moveToFirst();
			final int types_length = types.length;
			for (int i = 0; i < types_length; i++) {
				types[i] = cur.getString(i);
			}
		} else {
			Arrays.fill(types, "NULL");
		}
		cur.close();
		return types;

	}

	private static int getTypeInt(final String type) {
		final int idx = type.contains("(") ? type.indexOf("(") : type.indexOf(" ");
		final String type_main = idx > -1 ? type.substring(0, idx) : type;
		if (TYPE_NULL.equalsIgnoreCase(type_main))
			return FIELD_TYPE_NULL;
		else if (TYPE_INT.equalsIgnoreCase(type_main))
			return FIELD_TYPE_INTEGER;
		else if (TYPE_FLOAT.equalsIgnoreCase(type_main))
			return FIELD_TYPE_FLOAT;
		else if (TYPE_TEXT.equalsIgnoreCase(type_main))
			return FIELD_TYPE_STRING;
		else if (TYPE_BLOB.equalsIgnoreCase(type_main)) return FIELD_TYPE_BLOB;
		throw new IllegalStateException("Unknown field type " + type);
	}

	private static String getTypeString(final SQLiteDatabase db, final String table, final String column) {

		final String sql = "SELECT typeof(" + column + ") FROM " + table;
		final Cursor cur = db.rawQuery(sql, null);
		if (cur == null) return null;
		cur.moveToFirst();
		try {
			return cur.getString(0);
		} finally {
			cur.close();
		}
	}

	private static boolean isTypeCompatible(final String old_type, final String new_type,
			final boolean treat_null_as_compatible) {
		if (old_type != null && new_type != null) {
			final int old_idx = old_type.contains("(") ? old_type.indexOf("(") : old_type.indexOf(" ");
			final int new_idx = new_type.contains("(") ? new_type.indexOf("(") : new_type.indexOf(" ");
			final String old_type_main = old_idx > -1 ? old_type.substring(0, old_idx) : old_type;
			final String new_type_main = new_idx > -1 ? new_type.substring(0, new_idx) : new_type;
			if (treat_null_as_compatible)
				return "NULL".equalsIgnoreCase(old_type_main) || "NULL".equalsIgnoreCase(new_type_main)
						|| old_type_main.equalsIgnoreCase(new_type_main);
			return old_type_main.equalsIgnoreCase(new_type_main);
		}
		return false;
	}

	private static boolean shouldUpgrade(final String[] old_cols, final String[] old_types, final String[] new_cols,
			final String[] new_types) {
		if (old_cols == null || old_types == null || new_cols == null || new_types == null)
			throw new IllegalArgumentException("All arguments cannot be null!");
		if (old_cols.length != old_types.length || new_cols.length != new_types.length)
			throw new IllegalArgumentException("Length of columns and types not match!");
		if (old_cols.length != new_cols.length) return true;
		if (!ArrayUtils.contentMatch(old_cols, new_cols)) return true;
		final HashMap<String, String> old_map = new HashMap<String, String>(), new_map = new HashMap<String, String>();
		// I'm sure the length of four arrays are equal.
		final int length = old_cols.length;
		for (int i = 0; i < length; i++) {
			old_map.put(old_cols[i], old_types[i]);
			new_map.put(new_cols[i], new_types[i]);
		}
		final Set<String> old_keyset = old_map.keySet();
		for (final String col_name : old_keyset) {
			if (!isTypeCompatible(old_map.get(col_name), new_map.get(col_name), true)) return true;
		}
		return false;
	}

}
