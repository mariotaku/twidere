package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesWrapper {

	private final SharedPreferences mPreferences;

	private SharedPreferencesWrapper(final SharedPreferences preferences) {
		mPreferences = preferences;
	}

	public SharedPreferences.Editor edit() {
		return mPreferences.edit();
	}

	public boolean getBoolean(final String key, final boolean defValue) {
		try {
			return mPreferences.getBoolean(key, defValue);
		} catch (final ClassCastException e) {
			mPreferences.edit().remove(key).apply();
			return defValue;
		}
	}

	public int getInt(final String key, final int defValue) {
		try {
			return mPreferences.getInt(key, defValue);
		} catch (final ClassCastException e) {
			mPreferences.edit().remove(key).apply();
			return defValue;
		}
	}

	public long getLong(final String key, final long defValue) {
		try {
			return mPreferences.getLong(key, defValue);
		} catch (final ClassCastException e) {
			mPreferences.edit().remove(key).apply();
			return defValue;
		}
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public String getString(final String key, final String defValue) {
		try {
			return mPreferences.getString(key, defValue);
		} catch (final ClassCastException e) {
			mPreferences.edit().remove(key).apply();
			return defValue;
		}
	}

	public static SharedPreferencesWrapper getInstance(final Context context, final String name, final int mode) {
		final SharedPreferences prefs = context.getSharedPreferences(name, mode);
		return new SharedPreferencesWrapper(prefs);
	}

}
