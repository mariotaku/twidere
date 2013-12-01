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

package org.mariotaku.twidere.util.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import org.mariotaku.twidere.util.ArrayUtils;

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

	public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newCols,
			final String[] newTypes, final boolean fastUpgrade, final boolean dropDirectly,
			final HashMap<String, String> colAliases) {

		if (newCols == null || newTypes == null || newCols.length != newTypes.length)
			throw new IllegalArgumentException("Invalid parameters for upgrading table " + table
					+ ", length of columns and types not match.");

		final List<ContentValues> valuesList = new ArrayList<ContentValues>();

		// First, create the table if not exists.
		db.execSQL(createTable(table, newCols, newTypes, true));

		// We need to get all data from old table.
		final Cursor cur = db.query(table, null, null, null, null, null, null);
		cur.moveToFirst();
		final String[] oldCols = cur.getColumnNames();

		if (fastUpgrade) {
			final String[] oldTypes = getBatchTypeString(db, table, oldCols);

			if (!shouldUpgrade(oldCols, oldTypes, newCols, newTypes)) {
				if (cur != null) {
					cur.close();
				}
				return;
			}
		}

		// If drop_directly set to true, we will not backup any data actually.
		if (!dropDirectly) {

			while (!cur.isAfterLast()) {
				final ContentValues values = new ContentValues();
				final int length = newCols.length;
				for (int i = 0; i < length; i++) {
					final String newCol = newCols[i];
					final String colAlias = colAliases != null && colAliases.containsKey(newCol)
							&& ArrayUtils.contains(oldCols, colAliases.get(newCol)) ? colAliases.get(newCol) : newCol;
					final String newType = newTypes[i];
					if (BaseColumns._ID.equals(newCol)) {
						continue;
					}

					final int idx = cur.getColumnIndex(colAlias);

					if (ArrayUtils.contains(oldCols, colAlias)) {
						final String old_type = getTypeString(db, table, colAlias);
						final boolean compatible = isTypeCompatible(old_type, newType, false);
						if (compatible && idx > -1) {
							switch (getTypeInt(newType)) {
								case FIELD_TYPE_INTEGER:
									values.put(newCol, cur.getLong(idx));
									break;
								case FIELD_TYPE_FLOAT:
									values.put(newCol, cur.getFloat(idx));
									break;
								case FIELD_TYPE_STRING:
									values.put(newCol, cur.getString(idx));
									break;
								case FIELD_TYPE_BLOB:
									values.put(newCol, cur.getBlob(idx));
									break;
								case FIELD_TYPE_NULL:
								default:
									break;
							}
						}
					}
				}
				valuesList.add(values);
				cur.moveToNext();
			}
		}
		cur.close();

		// OK, now we got all data can be moved from old table, so we will
		// delete the old table and create a new one.
		db.execSQL("DROP TABLE IF EXISTS " + table);
		db.execSQL(createTable(table, newCols, newTypes, false));

		// Now, insert all data backuped into new table.
		db.beginTransaction();
		for (final ContentValues values : valuesList) {
			db.insert(table, null, values);
		}
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private static String createTable(final String tableName, final String[] columns, final String[] types,
			final boolean createIfNotExists) {
		if (tableName == null || columns == null || types == null || types.length != columns.length
				|| types.length == 0)
			throw new IllegalArgumentException("Invalid parameters for creating table " + tableName);
		final StringBuilder stringBuilder = new StringBuilder(createIfNotExists ? "CREATE TABLE IF NOT EXISTS "
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

	private static boolean isTypeCompatible(final String oldType, final String newType,
			final boolean treatNullAsCompatible) {
		if (oldType != null && newType != null) {
			final int old_idx = oldType.contains("(") ? oldType.indexOf("(") : oldType.indexOf(" ");
			final int new_idx = newType.contains("(") ? newType.indexOf("(") : newType.indexOf(" ");
			final String old_type_main = old_idx > -1 ? oldType.substring(0, old_idx) : oldType;
			final String new_type_main = new_idx > -1 ? newType.substring(0, new_idx) : newType;
			if (treatNullAsCompatible)
				return "NULL".equalsIgnoreCase(old_type_main) || "NULL".equalsIgnoreCase(new_type_main)
						|| old_type_main.equalsIgnoreCase(new_type_main);
			return old_type_main.equalsIgnoreCase(new_type_main);
		}
		return false;
	}

	private static boolean shouldUpgrade(final String[] oldCols, final String[] oldTypes, final String[] newCols,
			final String[] newTypes) {
		if (oldCols == null || oldTypes == null || newCols == null || newTypes == null)
			throw new IllegalArgumentException("All arguments cannot be null!");
		if (oldCols.length != oldTypes.length || newCols.length != newTypes.length)
			throw new IllegalArgumentException("Length of columns and types not match!");
		if (oldCols.length != newCols.length) return true;
		if (!ArrayUtils.contentMatch(oldCols, newCols)) return true;
		final HashMap<String, String> oldMap = new HashMap<String, String>(), newMap = new HashMap<String, String>();
		// I'm sure the length of four arrays are equal.
		final int length = oldCols.length;
		for (int i = 0; i < length; i++) {
			oldMap.put(oldCols[i], oldTypes[i]);
			newMap.put(newCols[i], newTypes[i]);
		}
		final Set<String> oldKeySet = oldMap.keySet();
		for (final String colName : oldKeySet) {
			if (!isTypeCompatible(oldMap.get(colName), newMap.get(colName), true)) return true;
		}
		return false;
	}

}
