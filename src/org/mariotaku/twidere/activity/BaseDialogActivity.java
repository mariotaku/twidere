package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ActivityThemeChangeInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.app.FragmentActivity;

public class BaseDialogActivity extends FragmentActivity implements Constants, ActivityThemeChangeInterface {

	private int mThemeId;

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	@Override
	public boolean isThemeChanged() {
		SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere_Dialog
				: R.style.Theme_Twidere_Light_Dialog;
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
			boolean show_anim = false;
			try {
				float transition_animation = Settings.System.getFloat(getContentResolver(),
						Settings.System.TRANSITION_ANIMATION_SCALE);
				show_anim = transition_animation > 0.0;
			} catch (SettingNotFoundException e) {
				e.printStackTrace();
			}
			restartActivity(this, show_anim);
			return;
		}
	}

	@Override
	public void setTheme() {
		SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere_Dialog
				: R.style.Theme_Twidere_Light_Dialog;
		setTheme(mThemeId);
	}
}
