package org.mariotaku.querybuilder;

import org.mariotaku.querybuilder.Columns.Column;

public class Where implements SQLLang {
	private final String expr;
	private Where and, or;

	public Where(final String expr) {
		this.expr = expr;
	}

	public Where and(final Where another) {
		checkNotSetAndOr();
		and = another;
		return this;
	}

	@Override
	public String getSQL() {
		if (and != null) return "(" + expr + " AND " + and.getSQL() + ")";
		if (or != null) return "(" + expr + " OR " + or.getSQL() + ")";
		return expr;
	}

	public Where or(final Where another) {
		checkNotSetAndOr();
		or = another;
		return this;
	}

	private void checkNotSetAndOr() {
		if (and != null) throw new SQLQueryException("AND expr is set!");
		if (or != null) throw new SQLQueryException("OR expr is set!");
	}

	public static Where in(final Column column, final Selectable in) {
		return new Where(String.format("%s IN(%s)", column.getSQL(), in.getSQL()));
	}

	public static Where notIn(final Column column, final Selectable in) {
		return new Where(String.format("%s NOT IN(%s)", column.getSQL(), in.getSQL()));
	}
}
