package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.SQLLang;
import org.mariotaku.querybuilder.Where;

public class SQLDeleteQuery implements SQLLang {

	private String table;
	private Where where;

	@Override
	public String getSQL() {
		if (where != null) return String.format("DELETE FROM %s", table);
		return String.format("DELETE FROM %S WHERE %s", table, where.getSQL());
	}

	void setFrom(final String table) {
		this.table = table;
	}

	void setWhere(final Where where) {
		this.where = where;
	}

	public static final class Builder {

		private boolean buildCalled;
		private final SQLDeleteQuery query = new SQLDeleteQuery();

		public SQLDeleteQuery build() {
			buildCalled = true;
			return query;
		}

		public Builder from(final String table) {
			checkNotBuilt();
			query.setFrom(table);
			return this;
		}

		public Builder where(final Where where) {
			checkNotBuilt();
			query.setWhere(where);
			return this;
		}

		private void checkNotBuilt() {
			if (buildCalled) throw new IllegalStateException();
		}
	}

}
