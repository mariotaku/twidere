package org.mariotaku.querybuilder;

public class Columns implements Selectable {

	private final AbsColumn[] columns;

	public Columns(final AbsColumn... columns) {
		this.columns = columns;
	}

	@Override
	public String getSQL() {
		return Utils.toString(columns);
	}

	public abstract static class AbsColumn implements Selectable {

	}

	public static class AllColumn extends AbsColumn {

		private final String table;

		public AllColumn() {
			this(null);
		}

		public AllColumn(final String table) {
			this.table = table;
		}

		@Override
		public String getSQL() {
			return table != null ? table + ".*" : "*";
		}

	}

	public static class Column extends AbsColumn {

		private final String table, columnName, alias;

		public Column(final String columnName) {
			this(null, columnName, null);
		}

		public Column(final String columnName, final String alias) {
			this(null, columnName, alias);
		}

		public Column(final String table, final String columnName, final String alias) {
			if (columnName == null) throw new IllegalArgumentException("");
			this.table = table;
			this.columnName = columnName;
			this.alias = alias;
		}

		@Override
		public String getSQL() {
			final String col = table != null ? table + "." + columnName : columnName;
			return alias != null ? col + " AS " + alias : col;
		}
	}

}
