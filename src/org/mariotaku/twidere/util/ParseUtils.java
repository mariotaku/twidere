package org.mariotaku.twidere.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.Constants;

import android.os.Bundle;
import android.util.Log;

public final class ParseUtils implements Constants {

	public static Bundle parseArguments(final String string) {
		final Bundle bundle = new Bundle();
		if (string != null) {
			try {
				final JSONObject json = new JSONObject(string);
				final Iterator<?> it = json.keys();
				while (it.hasNext()) {
					final Object key_obj = it.next();
					if (key_obj == null) {
						continue;
					}
					final String key = key_obj.toString();
					final Object value = json.get(key);
					if (value instanceof Boolean) {
						bundle.putBoolean(key, json.optBoolean(key));
					} else if (value instanceof Integer) {
						// Simple workaround for account_id
						if (INTENT_KEY_ACCOUNT_ID.equals(key)) {
							bundle.putLong(key, json.optLong(key));
						} else {
							bundle.putInt(key, json.optInt(key));
						}
					} else if (value instanceof Long) {
						bundle.putLong(key, json.optLong(key));
					} else if (value instanceof String) {
						bundle.putString(key, json.optString(key));
					} else {
						Log.w(LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
					}
				}
			} catch (final JSONException e) {
				e.printStackTrace();
			} catch (final ClassCastException e) {
				e.printStackTrace();
			}
		}
		return bundle;
	}

	public static double parseDouble(final String source) {
		if (source == null) return -1;
		try {
			return Double.parseDouble(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static int parseInt(final String source) {
		return parseInt(source, -1);
	}

	public static int parseInt(final String source, final int def) {
		if (source == null) return def;
		try {
			return Integer.valueOf(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return def;
	}

	public static long parseLong(final String source) {
		if (source == null) return -1;
		try {
			return Long.parseLong(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static String parseString(final Object object) {
		return parseString(object, null);
	}

	public static String parseString(final Object object, final String def) {
		if (object == null) return def;
		return String.valueOf(object);
	}

	public static URL parseURL(final String url_string) {
		if (url_string == null) return null;
		try {
			return new URL(url_string);
		} catch (final MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}

}
