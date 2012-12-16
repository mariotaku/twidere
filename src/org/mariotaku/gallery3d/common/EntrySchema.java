/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.common;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public final class EntrySchema {
	@SuppressWarnings("unused")
	private static final String TAG = "EntrySchema";

	public static final int TYPE_STRING = 0;
	public static final int TYPE_BOOLEAN = 1;
	public static final int TYPE_SHORT = 2;
	public static final int TYPE_INT = 3;
	public static final int TYPE_LONG = 4;
	public static final int TYPE_FLOAT = 5;
	public static final int TYPE_DOUBLE = 6;
	public static final int TYPE_BLOB = 7;
	private static final String SQLITE_TYPES[] = { "TEXT", "INTEGER", "INTEGER", "INTEGER", "INTEGER", "REAL", "REAL",
			"NONE" };

	private static final String FULL_TEXT_INDEX_SUFFIX = "_fulltext";

	private final String mTableName;
	private final ColumnInfo[] mColumnInfo;
	private final String[] mProjection;
	private final boolean mHasFullTextIndex;

	public EntrySchema(final Class<? extends Entry> clazz) {
		// Get table and column metadata from reflection.
		final ColumnInfo[] columns = parseColumnInfo(clazz);
		mTableName = parseTableName(clazz);
		mColumnInfo = columns;

		// Cache the list of projection columns and check for full-text columns.
		String[] projection = {};
		boolean hasFullTextIndex = false;
		if (columns != null) {
			projection = new String[columns.length];
			for (int i = 0; i != columns.length; ++i) {
				final ColumnInfo column = columns[i];
				projection[i] = column.name;
				if (column.fullText) {
					hasFullTextIndex = true;
				}
			}
		}
		mProjection = projection;
		mHasFullTextIndex = hasFullTextIndex;
	}

	public void createTables(final SQLiteDatabase db) {
		// Wrapped class must have a @Table.Definition.
		final String tableName = mTableName;
		Utils.assertTrue(tableName != null);

		// Add the CREATE TABLE statement for the main table.
		final StringBuilder sql = new StringBuilder("CREATE TABLE ");
		sql.append(tableName);
		sql.append(" (_id INTEGER PRIMARY KEY AUTOINCREMENT");
		final StringBuilder unique = new StringBuilder();
		for (final ColumnInfo column : mColumnInfo) {
			if (!column.isId()) {
				sql.append(',');
				sql.append(column.name);
				sql.append(' ');
				sql.append(SQLITE_TYPES[column.type]);
				if (!TextUtils.isEmpty(column.defaultValue)) {
					sql.append(" DEFAULT ");
					sql.append(column.defaultValue);
				}
				if (column.unique) {
					if (unique.length() == 0) {
						unique.append(column.name);
					} else {
						unique.append(',').append(column.name);
					}
				}
			}
		}
		if (unique.length() > 0) {
			sql.append(",UNIQUE(").append(unique).append(')');
		}
		sql.append(");");
		logExecSql(db, sql.toString());
		sql.setLength(0);

		// Create indexes for all indexed columns.
		for (final ColumnInfo column : mColumnInfo) {
			// Create an index on the indexed columns.
			if (column.indexed) {
				sql.append("CREATE INDEX ");
				sql.append(tableName);
				sql.append("_index_");
				sql.append(column.name);
				sql.append(" ON ");
				sql.append(tableName);
				sql.append(" (");
				sql.append(column.name);
				sql.append(");");
				logExecSql(db, sql.toString());
				sql.setLength(0);
			}
		}

		if (mHasFullTextIndex) {
			// Add an FTS virtual table if using full-text search.
			final String ftsTableName = tableName + FULL_TEXT_INDEX_SUFFIX;
			sql.append("CREATE VIRTUAL TABLE ");
			sql.append(ftsTableName);
			sql.append(" USING FTS3 (_id INTEGER PRIMARY KEY");
			for (final ColumnInfo column : mColumnInfo) {
				if (column.fullText) {
					// Add the column to the FTS table.
					final String columnName = column.name;
					sql.append(',');
					sql.append(columnName);
					sql.append(" TEXT");
				}
			}
			sql.append(");");
			logExecSql(db, sql.toString());
			sql.setLength(0);

			// Build an insert statement that will automatically keep the FTS
			// table in sync.
			final StringBuilder insertSql = new StringBuilder("INSERT OR REPLACE INTO ");
			insertSql.append(ftsTableName);
			insertSql.append(" (_id");
			for (final ColumnInfo column : mColumnInfo) {
				if (column.fullText) {
					insertSql.append(',');
					insertSql.append(column.name);
				}
			}
			insertSql.append(") VALUES (new._id");
			for (final ColumnInfo column : mColumnInfo) {
				if (column.fullText) {
					insertSql.append(",new.");
					insertSql.append(column.name);
				}
			}
			insertSql.append(");");
			final String insertSqlString = insertSql.toString();

			// Add an insert trigger.
			sql.append("CREATE TRIGGER ");
			sql.append(tableName);
			sql.append("_insert_trigger AFTER INSERT ON ");
			sql.append(tableName);
			sql.append(" FOR EACH ROW BEGIN ");
			sql.append(insertSqlString);
			sql.append("END;");
			logExecSql(db, sql.toString());
			sql.setLength(0);

			// Add an update trigger.
			sql.append("CREATE TRIGGER ");
			sql.append(tableName);
			sql.append("_update_trigger AFTER UPDATE ON ");
			sql.append(tableName);
			sql.append(" FOR EACH ROW BEGIN ");
			sql.append(insertSqlString);
			sql.append("END;");
			logExecSql(db, sql.toString());
			sql.setLength(0);

			// Add a delete trigger.
			sql.append("CREATE TRIGGER ");
			sql.append(tableName);
			sql.append("_delete_trigger AFTER DELETE ON ");
			sql.append(tableName);
			sql.append(" FOR EACH ROW BEGIN DELETE FROM ");
			sql.append(ftsTableName);
			sql.append(" WHERE _id = old._id; END;");
			logExecSql(db, sql.toString());
			sql.setLength(0);
		}
	}

	public <T extends Entry> T cursorToObject(final Cursor cursor, final T object) {
		try {
			for (final ColumnInfo column : mColumnInfo) {
				final int columnIndex = column.projectionIndex;
				final Field field = column.field;
				switch (column.type) {
					case TYPE_STRING:
						field.set(object, cursor.isNull(columnIndex) ? null : cursor.getString(columnIndex));
						break;
					case TYPE_BOOLEAN:
						field.setBoolean(object, cursor.getShort(columnIndex) == 1);
						break;
					case TYPE_SHORT:
						field.setShort(object, cursor.getShort(columnIndex));
						break;
					case TYPE_INT:
						field.setInt(object, cursor.getInt(columnIndex));
						break;
					case TYPE_LONG:
						field.setLong(object, cursor.getLong(columnIndex));
						break;
					case TYPE_FLOAT:
						field.setFloat(object, cursor.getFloat(columnIndex));
						break;
					case TYPE_DOUBLE:
						field.setDouble(object, cursor.getDouble(columnIndex));
						break;
					case TYPE_BLOB:
						field.set(object, cursor.isNull(columnIndex) ? null : cursor.getBlob(columnIndex));
						break;
				}
			}
			return object;
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteAll(final SQLiteDatabase db) {
		final StringBuilder sql = new StringBuilder("DELETE FROM ");
		sql.append(mTableName);
		sql.append(";");
		logExecSql(db, sql.toString());
	}

	public boolean deleteWithId(final SQLiteDatabase db, final long id) {
		return db.delete(mTableName, "_id=?", new String[] { Long.toString(id) }) == 1;
	}

	public void dropTables(final SQLiteDatabase db) {
		final String tableName = mTableName;
		final StringBuilder sql = new StringBuilder("DROP TABLE IF EXISTS ");
		sql.append(tableName);
		sql.append(';');
		logExecSql(db, sql.toString());
		sql.setLength(0);

		if (mHasFullTextIndex) {
			sql.append("DROP TABLE IF EXISTS ");
			sql.append(tableName);
			sql.append(FULL_TEXT_INDEX_SUFFIX);
			sql.append(';');
			logExecSql(db, sql.toString());
		}

	}

	public ColumnInfo getColumn(final String columnName) {
		final int index = getColumnIndex(columnName);
		return index < 0 ? null : mColumnInfo[index];
	}

	public int getColumnIndex(final String columnName) {
		for (final ColumnInfo column : mColumnInfo) {
			if (column.name.equals(columnName)) return column.projectionIndex;
		}
		return -1;
	}

	public ColumnInfo[] getColumnInfo() {
		return mColumnInfo;
	}

	public String[] getProjection() {
		return mProjection;
	}

	public String getTableName() {
		return mTableName;
	}

	public long insertOrReplace(final SQLiteDatabase db, final Entry entry) {
		final ContentValues values = new ContentValues();
		objectToValues(entry, values);
		if (entry.id == 0) {
			values.remove("_id");
		}
		final long id = db.replace(mTableName, "_id", values);
		entry.id = id;
		return id;
	}

	public void objectToValues(final Entry object, final ContentValues values) {
		try {
			for (final ColumnInfo column : mColumnInfo) {
				final String columnName = column.name;
				final Field field = column.field;
				switch (column.type) {
					case TYPE_STRING:
						values.put(columnName, (String) field.get(object));
						break;
					case TYPE_BOOLEAN:
						values.put(columnName, field.getBoolean(object));
						break;
					case TYPE_SHORT:
						values.put(columnName, field.getShort(object));
						break;
					case TYPE_INT:
						values.put(columnName, field.getInt(object));
						break;
					case TYPE_LONG:
						values.put(columnName, field.getLong(object));
						break;
					case TYPE_FLOAT:
						values.put(columnName, field.getFloat(object));
						break;
					case TYPE_DOUBLE:
						values.put(columnName, field.getDouble(object));
						break;
					case TYPE_BLOB:
						values.put(columnName, (byte[]) field.get(object));
						break;
				}
			}
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public Cursor queryAll(final SQLiteDatabase db) {
		return db.query(mTableName, mProjection, null, null, null, null, null);
	}

	public boolean queryWithId(final SQLiteDatabase db, final long id, final Entry entry) {
		final Cursor cursor = db.query(mTableName, mProjection, "_id=?", new String[] { Long.toString(id) }, null,
				null, null);
		boolean success = false;
		if (cursor.moveToFirst()) {
			cursorToObject(cursor, entry);
			success = true;
		}
		cursor.close();
		return success;
	}

	public String toDebugString(final Entry entry) {
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append("ID=").append(entry.id);
			for (final ColumnInfo column : mColumnInfo) {
				final String columnName = column.name;
				final Field field = column.field;
				final Object value = field.get(entry);
				sb.append(" ").append(columnName).append("=").append(value == null ? "null" : value.toString());
			}
			return sb.toString();
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String toDebugString(final Entry entry, final String... columnNames) {
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append("ID=").append(entry.id);
			for (final String columnName : columnNames) {
				final ColumnInfo column = getColumn(columnName);
				final Field field = column.field;
				final Object value = field.get(entry);
				sb.append(" ").append(columnName).append("=").append(value == null ? "null" : value.toString());
			}
			return sb.toString();
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts the ContentValues to the object. The ContentValues may not
	 * contain values for all the fields in the object.
	 */
	public <T extends Entry> T valuesToObject(final ContentValues values, final T object) {
		try {
			for (final ColumnInfo column : mColumnInfo) {
				final String columnName = column.name;
				final Field field = column.field;
				switch (column.type) {
					case TYPE_STRING:
						setIfNotNull(field, object, values.getAsString(columnName));
						break;
					case TYPE_BOOLEAN:
						setIfNotNull(field, object, values.getAsBoolean(columnName));
						break;
					case TYPE_SHORT:
						setIfNotNull(field, object, values.getAsShort(columnName));
						break;
					case TYPE_INT:
						setIfNotNull(field, object, values.getAsInteger(columnName));
						break;
					case TYPE_LONG:
						setIfNotNull(field, object, values.getAsLong(columnName));
						break;
					case TYPE_FLOAT:
						setIfNotNull(field, object, values.getAsFloat(columnName));
						break;
					case TYPE_DOUBLE:
						setIfNotNull(field, object, values.getAsDouble(columnName));
						break;
					case TYPE_BLOB:
						setIfNotNull(field, object, values.getAsByteArray(columnName));
						break;
				}
			}
			return object;
		} catch (final IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void logExecSql(final SQLiteDatabase db, final String sql) {
		db.execSQL(sql);
	}

	private ColumnInfo[] parseColumnInfo(Class<? extends Object> clazz) {
		final ArrayList<ColumnInfo> columns = new ArrayList<ColumnInfo>();
		while (clazz != null) {
			parseColumnInfo(clazz, columns);
			clazz = clazz.getSuperclass();
		}

		// Return a list.
		final ColumnInfo[] columnList = new ColumnInfo[columns.size()];
		columns.toArray(columnList);
		return columnList;
	}

	private void parseColumnInfo(final Class<? extends Object> clazz, final ArrayList<ColumnInfo> columns) {
		// Gather metadata from each annotated field.
		final Field[] fields = clazz.getDeclaredFields(); // including
															// non-public fields
		for (int i = 0; i != fields.length; ++i) {
			// Get column metadata from the annotation.
			final Field field = fields[i];
			final Entry.Column info = ((AnnotatedElement) field).getAnnotation(Entry.Column.class);
			if (info == null) {
				continue;
			}

			// Determine the field type.
			int type;
			final Class<?> fieldType = field.getType();
			if (fieldType == String.class) {
				type = TYPE_STRING;
			} else if (fieldType == boolean.class) {
				type = TYPE_BOOLEAN;
			} else if (fieldType == short.class) {
				type = TYPE_SHORT;
			} else if (fieldType == int.class) {
				type = TYPE_INT;
			} else if (fieldType == long.class) {
				type = TYPE_LONG;
			} else if (fieldType == float.class) {
				type = TYPE_FLOAT;
			} else if (fieldType == double.class) {
				type = TYPE_DOUBLE;
			} else if (fieldType == byte[].class) {
				type = TYPE_BLOB;
			} else
				throw new IllegalArgumentException("Unsupported field type for column: " + fieldType.getName());

			// Add the column to the array.
			final int index = columns.size();
			columns.add(new ColumnInfo(info.value(), type, info.indexed(), info.unique(), info.fullText(), info
					.defaultValue(), field, index));
		}
	}

	private String parseTableName(final Class<? extends Object> clazz) {
		// Check for a table annotation.
		final Entry.Table table = clazz.getAnnotation(Entry.Table.class);
		if (table == null) return null;

		// Return the table name.
		return table.value();
	}

	private void setIfNotNull(final Field field, final Object object, final Object value) throws IllegalAccessException {
		if (value != null) {
			field.set(object, value);
		}
	}

	public static final class ColumnInfo {
		private static final String ID_KEY = "_id";

		public final String name;
		public final int type;
		public final boolean indexed;
		public final boolean unique;
		public final boolean fullText;
		public final String defaultValue;
		public final Field field;
		public final int projectionIndex;

		public ColumnInfo(final String name, final int type, final boolean indexed, final boolean unique,
				final boolean fullText, final String defaultValue, final Field field, final int projectionIndex) {
			this.name = name.toLowerCase();
			this.type = type;
			this.indexed = indexed;
			this.unique = unique;
			this.fullText = fullText;
			this.defaultValue = defaultValue;
			this.field = field;
			this.projectionIndex = projectionIndex;

			field.setAccessible(true); // in order to set non-public fields
		}

		public boolean isId() {
			return ID_KEY.equals(name);
		}
	}
}
