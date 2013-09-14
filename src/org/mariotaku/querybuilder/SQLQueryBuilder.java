package org.mariotaku.querybuilder;

public class SQLQueryBuilder {

	private boolean buildCalled;
	private final SQLQuery query = new SQLQuery();

	public SQLQuery build() {
		buildCalled = true;
		return query;
	}

	public SQLQueryBuilder from(final Selectable from) {
		checkNotBuilt();
		query.setFrom(from);
		return this;
	}

	public SQLQueryBuilder groupBy(final Selectable groupBy) {
		checkNotBuilt();
		query.setGroupBy(groupBy);
		return this;
	}

	public SQLQueryBuilder having(final Where having) {
		checkNotBuilt();
		query.setHaving(having);
		return this;
	}

	public SQLQueryBuilder limit(final int limit) {
		checkNotBuilt();
		query.setLimit(limit);
		return this;
	}

	public SQLQueryBuilder offset(final int offset) {
		query.setOffset(offset);
		return this;
	}

	public SQLQueryBuilder orderBy(final OrderBy orderBy) {
		checkNotBuilt();
		query.setOrderBy(orderBy);
		return this;
	}

	public SQLQueryBuilder select(final boolean distinct, final Selectable select) {
		checkNotBuilt();
		query.setSelect(select);
		query.setDistinct(distinct);
		return this;
	}

	public SQLQueryBuilder select(final Selectable select) {
		checkNotBuilt();
		select(false, select);
		return this;
	}

	public SQLQueryBuilder union() {
		checkNotBuilt();
		query.union();
		return this;
	}

	public SQLQueryBuilder where(final Where where) {
		checkNotBuilt();
		query.setWhere(where);
		return this;
	}

	private void checkNotBuilt() {
		if (buildCalled) throw new IllegalStateException();
	}

}
