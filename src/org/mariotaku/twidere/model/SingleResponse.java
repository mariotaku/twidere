package org.mariotaku.twidere.model;

public final class SingleResponse<Data> {
	public final Exception exception;
	public final Data data;
	public final long account_id;

	public SingleResponse(final long account_id, final Data data, final Exception exception) {
		this.exception = exception;
		this.data = data;
		this.account_id = account_id;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof SingleResponse)) return false;
		final SingleResponse<?> other = (SingleResponse<?>) obj;
		if (account_id != other.account_id) return false;
		if (data == null) {
			if (other.data != null) return false;
		} else if (!data.equals(other.data)) return false;
		if (exception == null) {
			if (other.exception != null) return false;
		} else if (!exception.equals(other.exception)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (account_id ^ account_id >>> 32);
		result = prime * result + (data == null ? 0 : data.hashCode());
		result = prime * result + (exception == null ? 0 : exception.hashCode());
		return result;
	}
}