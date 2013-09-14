package org.mariotaku.querybuilder;

import java.util.ArrayList;
import java.util.List;

public class SQLQuery implements Selectable {

	private final List<InternalQuery> internalQueries = new ArrayList<InternalQuery>();

	private InternalQuery currentInternalQuery;
	private OrderBy orderBy;
	private Integer limit = null, offset = null;

	SQLQuery() {
		initCurrentQuery();
	}

	@Override
	public String getSQL() {
		final StringBuilder sb = new StringBuilder();
		final int size = internalQueries.size();
		for (int i = 0; i < size; i++) {
			if (i != 0) {
				sb.append("UNION ");
			}
			final InternalQuery query = internalQueries.get(i);
			sb.append(query.getSQL());

		}
		if (orderBy != null) {
			sb.append(String.format("ORDER BY %s ", orderBy.getSQL()));
		}
		if (limit != null) {
			sb.append(String.format("LIMIT %s ", limit));
			if (offset != null) {
				sb.append(String.format("OFFSET %s ", offset));
			}
		}
		return sb.toString();
	}

	private void initCurrentQuery() {
		currentInternalQuery = new InternalQuery();
		internalQueries.add(currentInternalQuery);
	}

	void setDistinct(final boolean distinct) {
		currentInternalQuery.setDistinct(distinct);
	}

	void setFrom(final Selectable from) {
		currentInternalQuery.setFrom(from);
	}

	void setGroupBy(final Selectable groupBy) {
		currentInternalQuery.setGroupBy(groupBy);
	}

	void setHaving(final Where having) {
		currentInternalQuery.setHaving(having);
	}

	void setLimit(final int limit) {
		this.limit = limit;
	}

	void setOffset(final int offset) {
		this.offset = offset;
	}

	void setOrderBy(final OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	void setSelect(final Selectable select) {
		currentInternalQuery.setSelect(select);
	}

	void setWhere(final Where where) {
		currentInternalQuery.setWhere(where);
	}

	void union() {
		initCurrentQuery();
	}

	private static class InternalQuery implements SQLLang {

		private boolean distinct;
		private Selectable select, from, groupBy;
		private Where where, having;

		@Override
		public String getSQL() {
			if (select == null) throw new SQLQueryException("selectable is null");
			final StringBuilder sb = new StringBuilder("SELECT ");
			if (distinct) {
				sb.append("DISTINCT ");
			}
			sb.append(String.format("%s ", select.getSQL()));
			if (!(select instanceof SQLQuery) && from == null)
				throw new SQLQueryException("FROM not specified");
			else if (from != null) {
				if (from instanceof SQLQuery) {
					sb.append(String.format("FROM (%s) ", from.getSQL()));
				} else {
					sb.append(String.format("FROM %s ", from.getSQL()));
				}
			}
			if (where != null) {
				sb.append(String.format("WHERE %s ", where.getSQL()));
			}
			if (groupBy != null) {
				sb.append(String.format("GROUP BY %s ", groupBy.getSQL()));
				if (having != null) {
					sb.append(String.format("HAVING %s ", having.getSQL()));
				}
			}
			return sb.toString();
		}

		void setDistinct(final boolean distinct) {
			this.distinct = distinct;
		}

		void setFrom(final Selectable from) {
			this.from = from;
		}

		void setGroupBy(final Selectable groupBy) {
			this.groupBy = groupBy;
		}

		void setHaving(final Where having) {
			this.having = having;
		}

		void setSelect(final Selectable select) {
			this.select = select;
		}

		void setWhere(final Where where) {
			this.where = where;
		}
	}
}
