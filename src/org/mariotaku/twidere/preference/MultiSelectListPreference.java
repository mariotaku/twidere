package org.mariotaku.twidere.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;

abstract class MultiSelectListPreference extends DialogPreference implements OnMultiChoiceClickListener,
		OnClickListener {

	private final boolean[] mValues, mDefaultValues;
	private SharedPreferences prefs;
	private final String[] mNames, mKeys;

	public MultiSelectListPreference(final Context context) {
		this(context, null);
	}

	public MultiSelectListPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public MultiSelectListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mNames = getNames();
		mKeys = getKeys();
		mDefaultValues = getDefaults();
		final int length = mNames.length;
		if (length != mKeys.length || length != mDefaultValues.length) throw new IllegalArgumentException();
		mValues = new boolean[length];

	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (prefs == null) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final SharedPreferences.Editor editor = prefs.edit();
				final int length = mKeys.length;
				for (int i = 0; i < length; i++) {
					editor.putBoolean(mKeys[i], mValues[i]);
				}
				editor.commit();
				break;
		}

	}

	@Override
	public void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
		mValues[which] = isChecked;
	}

	@Override
	public void onPrepareDialogBuilder(final AlertDialog.Builder builder) {
		super.onPrepareDialogBuilder(builder);
		prefs = getSharedPreferences();
		if (prefs == null) return;
		final int length = mKeys.length;
		for (int i = 0; i < length; i++) {
			mValues[i] = prefs.getBoolean(mKeys[i], mDefaultValues[i]);
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setMultiChoiceItems(mNames, mValues, this);
	}

	protected abstract boolean[] getDefaults();

	protected abstract String[] getKeys();

	protected abstract String[] getNames();

}
