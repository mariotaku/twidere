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
