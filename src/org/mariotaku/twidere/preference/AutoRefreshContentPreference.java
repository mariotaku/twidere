package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.util.AttributeSet;

public class AutoRefreshContentPreference extends MultiSelectListPreference implements Constants {

	public AutoRefreshContentPreference(final Context context) {
		this(context, null);
	}

	public AutoRefreshContentPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public AutoRefreshContentPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean[] getDefaults() {
		return new boolean[] { false, false, false };
	}

	@Override
	protected String[] getKeys() {
		return new String[] { PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE, PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS,
				PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES };
	}

	@Override
	protected String[] getNames() {
		return getContext().getResources().getStringArray(R.array.entries_refresh_notification_content);
	}

}
