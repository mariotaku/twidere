package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.SharedPreferences;

public class BaseDialogActivity extends BaseActivity {

	private int mThemeId;

	@Override
	public boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Holo_Dialog
				: R.style.Theme_Holo_Light_Dialog;
		return new_theme_id != mThemeId;
	}

	@Override
	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Holo_Dialog
				: R.style.Theme_Holo_Light_Dialog;
		setTheme(mThemeId);
	}
}
