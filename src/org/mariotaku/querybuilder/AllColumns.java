package org.mariotaku.querybuilder;

public class AllColumns implements Selectable {

	private final String table;

	public AllColumns() {
		this(null);
	}

	public AllColumns(final String table) {
		this.table = table;
	}

	@Override
	public String getSQL() {
		return table != null ? table + ".*" : "*";
	}

}
