package org.mariotaku.twidere.model;

import java.util.List;

import android.os.Bundle;

public class ListResponse<Data> extends SingleResponse<List<Data>> {

	public final List<Data> list;

	public ListResponse(final List<Data> list, final Exception exception) {
		super(list, exception);
		this.list = list;
	}

	public ListResponse(final List<Data> list, final Exception exception, final Bundle extras) {
		super(list, exception, extras);
		this.list = list;
	}

}