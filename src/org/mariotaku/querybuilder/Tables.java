package org.mariotaku.querybuilder;

import org.mariotaku.twidere.util.ArrayUtils;

public class Tables implements Selectable {

	private final String[] tables;

	public Tables(final String... tables) {
		this.tables = tables;
	}

	@Override
	public String getSQL() {
		return ArrayUtils.toString(tables, ',', false);
	}

}
