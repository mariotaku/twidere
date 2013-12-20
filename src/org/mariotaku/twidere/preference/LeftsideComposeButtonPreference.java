package org.mariotaku.twidere.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import org.mariotaku.twidere.util.SmartBarUtils;

public class LeftsideComposeButtonPreference extends CheckBoxPreference {

	public LeftsideComposeButtonPreference(final Context context) {
		super(context);
	}

	public LeftsideComposeButtonPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public LeftsideComposeButtonPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onDependencyChanged(final Preference dependency, final boolean disableDependent) {
		super.onDependencyChanged(dependency, disableDependent || SmartBarUtils.hasSmartBar());
	}

}
