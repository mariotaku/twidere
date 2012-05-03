package org.mariotaku.twidere.activity;

import android.content.Context;
import android.content.SharedPreferences;

public class BaseDialogActivity extends BaseActivity {

	private int mThemeId;

	@Override
	public boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? android.R.style.Theme_Holo_Dialog
				: android.R.style.Theme_Holo_Light_Dialog;
		return new_theme_id != mThemeId;
	}

	@Override
	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? android.R.style.Theme_Holo_Dialog
				: android.R.style.Theme_Holo_Light_Dialog;
		setTheme(mThemeId);
	}
}
