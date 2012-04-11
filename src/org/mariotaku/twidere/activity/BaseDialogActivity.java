package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

public class BaseDialogActivity extends RoboSherlockFragmentActivity implements Constants {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme();
		super.onCreate(savedInstanceState);
	}

	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		setTheme(preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Holo_Dialog
				: R.style.Theme_Holo_Light_Dialog);
	}
}
