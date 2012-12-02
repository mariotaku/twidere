package org.mariotaku.twidere.model;

import java.util.List;

public class ListResponse<Data> {

	public final long account_id;
	public final List<Data> list;
	public final Exception exception;

	public ListResponse(final long account_id, final List<Data> list, final Exception exception) {
		this.account_id = account_id;
		this.list = list;
		this.exception = exception;
	}

}