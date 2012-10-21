package org.mariotaku.twidere.preference;

import android.content.Context;
import android.util.AttributeSet;

public final class SlientNotificationsPreference extends MultiSelectListPreference {

	public SlientNotificationsPreference(final Context context) {
		super(context);
	}

	public SlientNotificationsPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SlientNotificationsPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean[] getDefaults() {
		return new boolean[24];
	}

	@Override
	protected String[] getKeys() {
		final String[] keys = new String[24];
		for (int i = 0; i < 24; i++) {
			keys[i] = "slient_notifications_at_" + i;
		}
		return keys;
	}

	@Override
	protected String[] getNames() {
		final String[] names = new String[24];
		for (int i = 0; i < 24; i++) {
			final String value_1 = i + ":00";
			final String value_2 = (i == 23 ? 0 : i + 1) + ":00";
			names[i] = value_1 + " - " + value_2;
		}
		return names;
	}

}
