package org.mariotaku.twidere.model;

import org.mariotaku.twidere.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.AbstractCursor;
import android.database.CursorIndexOutOfBoundsException;

public class ConsumerKeySecretCursor extends AbstractCursor implements Constants {

	private final String consumer_secret;
	private final String consumer_key;

	public ConsumerKeySecretCursor(final Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		consumer_key = prefs.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
		consumer_secret = prefs.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
	}

	@Override
	public String[] getColumnNames() {
		return new String[] { PREFERENCE_KEY_CONSUMER_KEY, PREFERENCE_KEY_CONSUMER_SECRET };
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public double getDouble(final int arg0) {
		throw new IllegalArgumentException("not supported.");
	}

	@Override
	public float getFloat(final int column) {
		throw new IllegalArgumentException("not supported.");
	}

	@Override
	public int getInt(final int column) {
		throw new IllegalArgumentException("not supported.");
	}

	@Override
	public long getLong(final int column) {
		throw new IllegalArgumentException("not supported.");
	}

	@Override
	public short getShort(final int column) {
		throw new IllegalArgumentException("not supported.");
	}

	@Override
	public String getString(final int column) {
		final String col = getColumnName(column);
		if (PREFERENCE_KEY_CONSUMER_KEY.equals(col))
			return consumer_key;
		else if (PREFERENCE_KEY_CONSUMER_SECRET.equals(col)) return consumer_secret;
		throw new CursorIndexOutOfBoundsException("wrong column");
	}

	@Override
	public boolean isNull(final int column) {
		return false;
	}

}
