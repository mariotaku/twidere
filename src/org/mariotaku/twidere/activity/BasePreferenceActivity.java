package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.restartActivity;

import org.mariotaku.actionbarcompat.ActionBarPreferenceActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.ActivityThemeChangeInterface;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

class BasePreferenceActivity extends ActionBarPreferenceActivity implements Constants, ActivityThemeChangeInterface {

	private int mThemeId;

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	@Override
	public boolean isThemeChanged() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int new_theme_id = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere
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
			boolean show_anim = false;
			try {
				final float transition_animation = Settings.System.getFloat(getContentResolver(),
						Settings.System.TRANSITION_ANIMATION_SCALE);
				show_anim = transition_animation > 0.0;
			} catch (final SettingNotFoundException e) {
				e.printStackTrace();
			}
			restartActivity(this, show_anim);
		}
	}

	@Override
	public void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mThemeId = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false) ? R.style.Theme_Twidere
				: R.style.Theme_Twidere_Light;
		setTheme(mThemeId);
	}
}
