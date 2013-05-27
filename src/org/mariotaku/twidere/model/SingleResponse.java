package org.mariotaku.twidere.model;

import android.os.Bundle;

public class SingleResponse<Data> {
	public final Exception exception;
	public final Data data;
	public final Bundle extras;

	public SingleResponse(final Data data, final Exception exception) {
		this(data, exception, null);
	}

	public SingleResponse(final Data data, final Exception exception, final Bundle extras) {
		this.data = data;
		this.exception = exception;
		this.extras = extras != null ? extras : new Bundle();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof SingleResponse)) return false;
		final SingleResponse<?> other = (SingleResponse<?>) obj;
		if (data == null) {
			if (other.data != null) return false;
		} else if (!data.equals(other.data)) return false;
		if (exception == null) {
			if (other.exception != null) return false;
		} else if (!exception.equals(other.exception)) return false;
		if (extras == null) {
			if (other.extras != null) return false;
		} else if (!extras.equals(other.extras)) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (data == null ? 0 : data.hashCode());
		result = prime * result + (exception == null ? 0 : exception.hashCode());
		result = prime * result + (extras == null ? 0 : extras.hashCode());
		return result;
	}
	
	public static <T> SingleResponse<T> nullInstance() {
		return new SingleResponse<T>(null, null);
	}
}
