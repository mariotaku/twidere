package org.mariotaku.querybuilder;

import org.mariotaku.twidere.util.ArrayUtils;

public class OrderBy implements SQLLang {

	private final String[] orderBy;

	public OrderBy(final String... orderBy) {
		this.orderBy = orderBy;
	}

	@Override
	public String getSQL() {
		return ArrayUtils.toString(orderBy, ',', false);
	}

}
