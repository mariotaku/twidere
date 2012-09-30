package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.util.AttributeSet;

public class NotificationTypePreference extends MultiSelectListPreference implements Constants {

	public NotificationTypePreference(final Context context) {
		this(context, null);
	}

	public NotificationTypePreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public NotificationTypePreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean[] getDefaults() {
		return new boolean[] { false, false, false };
	}

	@Override
	protected String[] getKeys() {
		return new String[] { PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION,
				PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS };
	}

	@Override
	protected String[] getNames() {
		return getContext().getResources().getStringArray(R.array.entries_notification_type);
	}

}
