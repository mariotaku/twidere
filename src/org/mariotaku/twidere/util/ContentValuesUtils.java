package org.mariotaku.twidere.util;

import android.content.ContentValues;

public class ContentValuesUtils {

	public static boolean getAsBoolean(final ContentValues values, final String key, final boolean def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Boolean.valueOf(value.toString());
	}

	public static long getAsInteger(final ContentValues values, final String key, final int def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Integer.valueOf(value.toString());
	}

	public static long getAsLong(final ContentValues values, final String key, final long def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Long.valueOf(value.toString());
	}
}
