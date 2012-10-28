package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.parseString;

import org.apache.http.NameValuePair;

public class NameValuePairImpl implements NameValuePair {

	private final String name, value;

	public NameValuePairImpl(final String name, final Object value) {
		this.name = name;
		this.value = parseString(value);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

}
