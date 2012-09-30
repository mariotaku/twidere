package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.util.AttributeSet;

public class HomeRefreshContentPreference extends MultiSelectListPreference implements Constants {

	public HomeRefreshContentPreference(final Context context) {
		this(context, null);
	}

	public HomeRefreshContentPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public HomeRefreshContentPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean[] getDefaults() {
		return new boolean[] { false, false };
	}

	@Override
	protected String[] getKeys() {
		return new String[] { PREFERENCE_KEY_HOME_REFRESH_MENTIONS, PREFERENCE_KEY_HOME_REFRESH_DIRECT_MESSAGES };
	}

	@Override
	protected String[] getNames() {
		return getContext().getResources().getStringArray(R.array.entries_home_refresh_content);
	}

}
