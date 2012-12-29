package org.mariotaku.twidere.model;

import org.mariotaku.twidere.Constants;

import android.database.AbstractCursor;
import android.os.Bundle;

public class BundleCursor extends AbstractCursor implements Constants {

	private final Bundle extras;

	public BundleCursor(final Bundle extras) {
		this.extras = extras != null ? extras : Bundle.EMPTY;
	}

	@Override
	public String[] getColumnNames() {
		return new String[0];
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public double getDouble(final int column) {
		return 0;
	}

	@Override
	public Bundle getExtras() {
		return extras;
	}

	@Override
	public float getFloat(final int column) {
		return 0;
	}

	@Override
	public int getInt(final int column) {
		return 0;
	}

	@Override
	public long getLong(final int column) {
		return 0;
	}

	@Override
	public short getShort(final int column) {
		return 0;
	}

	@Override
	public String getString(final int column) {
		return null;
	}

	@Override
	public boolean isNull(final int column) {
		return true;
	}

}
