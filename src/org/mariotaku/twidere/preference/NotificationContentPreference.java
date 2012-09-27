package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;

public class NotificationContentPreference extends MultiSelectListPreference implements Constants {

	protected String[] getNames() {
		return getContext().getResources().getStringArray(R.array.entries_refresh_notification_content);
	}

	protected String[] getKeys() {
		return new String[]{ PREFERENCE_KEY_NOTIFICATION_ENABLE_HOME_TIMELINE, 
			PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS, PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES};
	}

	protected boolean[] getDefaults() {
		return new boolean[]{ false, false, false };
	}

	public NotificationContentPreference(final Context context) {
		this(context, null);
	}

	public NotificationContentPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public NotificationContentPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

}
