/**
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */

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
