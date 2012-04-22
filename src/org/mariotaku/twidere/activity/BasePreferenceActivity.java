package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.CommonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class BasePreferenceActivity extends SherlockPreferenceActivity implements Constants {

	private int mThemeId;

	public boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere
				: R.style.Theme_Twidere_Light;
		return new_theme_id != mThemeId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme();
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isThemeChanged()) {
			CommonUtils.restartActivity(this);
		}
	}

	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere
				: R.style.Theme_Twidere_Light;
		setTheme(mThemeId);
	}
}
