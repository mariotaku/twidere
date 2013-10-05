package org.mariotaku.twidere.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.mariotaku.twidere.Constants;

public class BasePreferenceFragment extends PreferenceFragment implements Constants {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
	}
}
