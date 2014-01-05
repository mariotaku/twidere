/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util.content;

import static org.mariotaku.querybuilder.SQLQueryBuilder.alterTable;
import static org.mariotaku.querybuilder.SQLQueryBuilder.createTable;
import static org.mariotaku.querybuilder.SQLQueryBuilder.dropTable;
import static org.mariotaku.querybuilder.SQLQueryBuilder.insertInto;
import static org.mariotaku.querybuilder.SQLQueryBuilder.select;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.NewColumn;
import org.mariotaku.querybuilder.Tables;
import org.mariotaku.querybuilder.query.SQLInsertIntoQuery;
import org.mariotaku.querybuilder.query.SQLInsertIntoQuery.OnConflict;
import org.mariotaku.querybuilder.query.SQLSelectQuery;
import org.mariotaku.twidere.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class DatabaseUpgradeHelper {

	public static void safeUpgrade(final SQLiteDatabase db, final String table, final String[] newColNames,
			final String[] newColTypes, final boolean dropDirectly, final Map<String, String> colAliases) {

		if (newColNames == null || newColTypes == null || newColNames.length != newColTypes.length)
			throw new IllegalArgumentException("Invalid parameters for upgrading table " + table
					+ ", length of columns and types not match.");

		// First, create the table if not exists.
		final NewColumn[] newCols = NewColumn.createNewColumns(newColNames, newColTypes);
		db.execSQL(createTable(true, table).columns(newCols).buildSQL());

		// We need to get all data from old table.
		final String[] oldCols = getColumnNames(db, table);
		if (oldCols == null || ArrayUtils.contentMatch(newColNames, oldCols)) return;
		if (dropDirectly) {
			db.beginTransaction();
			db.execSQL(dropTable(true, table).getSQL());
			db.execSQL(createTable(false, table).columns(newCols).buildSQL());
			db.setTransactionSuccessful();
			db.endTransaction();
			return;
		}
		final String tempTable = String.format(Locale.US, "temp_%s_%d", table, System.currentTimeMillis());
		db.beginTransaction();
		db.execSQL(alterTable(table).renameTo(tempTable).buildSQL());
		db.execSQL(createTable(true, table).columns(newCols).buildSQL());
		final String[] notNullCols = getNotNullColumns(newCols);
		final String insertQuery = createInsertDataQuery(table, tempTable, newColNames, oldCols, colAliases,
				notNullCols);
		if (insertQuery != null) {
			db.execSQL(insertQuery);
		}
		db.execSQL(dropTable(true, tempTable).getSQL());
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	private static String createInsertDataQuery(final String table, final String tempTable, final String[] newCols,
			final String[] oldCols, final Map<String, String> colAliases, final String[] notNullCols) {
		final SQLInsertIntoQuery.Builder qb = insertInto(OnConflict.REPLACE, table);
		final List<String> newInsertColsList = new ArrayList<String>();
		for (final String newCol : newCols) {
			final String oldAliasedCol = colAliases != null ? colAliases.get(newCol) : null;
			if (ArrayUtils.contains(oldCols, newCol) || oldAliasedCol != null
					&& ArrayUtils.contains(oldCols, oldAliasedCol)) {
				newInsertColsList.add(newCol);
			}
		}
		final String[] newInsertCols = newInsertColsList.toArray(new String[newInsertColsList.size()]);
		if (!ArrayUtils.contains(newInsertCols, notNullCols)) return null;
		qb.columns(newInsertCols);
		final Columns.Column[] oldDataCols = new Columns.Column[newInsertCols.length];
		for (int i = 0, j = oldDataCols.length; i < j; i++) {
			final String newCol = newInsertCols[i];
			final String oldAliasedCol = colAliases != null ? colAliases.get(newCol) : null;
			if (oldAliasedCol != null && ArrayUtils.contains(oldCols, oldAliasedCol)) {
				oldDataCols[i] = new Columns.Column(oldAliasedCol, newCol);
			} else {
				oldDataCols[i] = new Columns.Column(newCol);
			}
		}
		final SQLSelectQuery.Builder selectOldBuilder = select(new Columns(oldDataCols));
		selectOldBuilder.from(new Tables(tempTable));
		qb.select(selectOldBuilder.build());
		return qb.buildSQL();
	}

	private static String[] getColumnNames(final SQLiteDatabase db, final String table) {
		final Cursor cur = db.query(table, null, null, null, null, null, null, "1");
		if (cur == null) return null;
		try {
			return cur.getColumnNames();
		} finally {
			cur.close();
		}
	}

	private static String[] getNotNullColumns(final NewColumn[] newCols) {
		if (newCols == null) return null;
		final String[] notNullCols = new String[newCols.length];
		int count = 0;
		for (final NewColumn column : newCols) {
			if (column.getType().endsWith(" NOT NULL")) {
				notNullCols[count++] = column.getName();
			}
		}
		return ArrayUtils.subArray(notNullCols, 0, count);
	}

}
