package org.mariotaku.twidere.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class AutoInvalidateListPreference extends ListPreference {

	public AutoInvalidateListPreference(final Context context) {
		super(context);
	}

	public AutoInvalidateListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean shouldPersist() {
		if (!super.shouldPersist()) return false;
		notifyChanged();
		return true;
	}

}
