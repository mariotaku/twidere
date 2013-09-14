package org.mariotaku.querybuilder;

public class SQLQueryException extends RuntimeException {

	private static final long serialVersionUID = 910158450604676104L;

	public SQLQueryException() {
	}

	public SQLQueryException(final String detailMessage) {
		super(detailMessage);
	}

	public SQLQueryException(final String detailMessage, final Throwable throwable) {
		super(detailMessage, throwable);
	}

	public SQLQueryException(final Throwable throwable) {
		super(throwable);
	}

}
