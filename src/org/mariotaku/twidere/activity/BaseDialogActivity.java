package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

public class BaseDialogActivity extends RoboSherlockFragmentActivity implements Constants {

	private int mThemeId;

	public boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Holo_Dialog
				: R.style.Theme_Holo_Light_Dialog;
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
			restartActivity();
		}
	}

	public final void restartActivity() {
		overridePendingTransition(0, 0);
		finish();
		overridePendingTransition(0, 0);
		startActivity(getIntent());
	}

	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Holo_Dialog
				: R.style.Theme_Holo_Light_Dialog;
		setTheme(mThemeId);
	}
}
