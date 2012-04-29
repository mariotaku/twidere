package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ActivityThemeChangeImpl;
import org.mariotaku.twidere.util.CommonUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BaseDialogActivity extends SherlockFragmentActivity implements Constants,
		ActivityThemeChangeImpl {

	private int mThemeId;

	@Override
	public boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? android.R.style.Theme_Holo_Dialog
				: android.R.style.Theme_Holo_Light_Dialog;
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

	@Override
	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? android.R.style.Theme_Holo_Dialog
				: android.R.style.Theme_Holo_Light_Dialog;
		setTheme(mThemeId);
	}
}
