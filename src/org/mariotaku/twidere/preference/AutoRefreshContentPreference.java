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

public class AutoRefreshContentPreference extends Preference implements Constants, OnPreferenceClickListener,
		OnMultiChoiceClickListener, OnClickListener {

	private boolean[] checked_items;
	private SharedPreferences prefs;

	public AutoRefreshContentPreference(final Context context) {
		this(context, null);
	}

	public AutoRefreshContentPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public AutoRefreshContentPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		checked_items = new boolean[3];
		setOnPreferenceClickListener(this);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (prefs == null) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final SharedPreferences.Editor editor = prefs.edit();
				editor.putBoolean(PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE, checked_items[0]);
				editor.putBoolean(PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS, checked_items[1]);
				editor.putBoolean(PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES, checked_items[2]);
				editor.commit();
				break;
		}

	}

	@Override
	public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
		checked_items[which] = isChecked;
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		prefs = getSharedPreferences();
		if (prefs == null) return false;
		checked_items = new boolean[] { prefs.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE, false),
				prefs.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS, false),
				prefs.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES, false) };

		final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

		builder.setTitle(getTitle());
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		builder.setMultiChoiceItems(R.array.entries_refresh_notification_content, checked_items, this);
		builder.show();

		return true;
	}

}
