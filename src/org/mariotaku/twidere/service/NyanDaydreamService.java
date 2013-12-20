package org.mariotaku.twidere.service;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.service.dreams.DreamService;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.NyanDaydreamView;

public class NyanDaydreamService extends DreamService implements Constants, OnSharedPreferenceChangeListener,
		OnSystemUiVisibilityChangeListener {

	private NyanDaydreamView mNyanDaydreamView;
	private SharedPreferences mPreferences;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setContentView(R.layout.nyan_daydream);
		mNyanDaydreamView.setOnSystemUiVisibilityChangeListener(this);
		updateView();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mNyanDaydreamView = (NyanDaydreamView) findViewById(R.id.nyan);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		setInteractive(false);
		setFullscreen(true);
		setScreenBright(false);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (PREFERENCE_KEY_LIVE_WALLPAPER_SCALE.equals(key)) {
			updateView();
		}
	}

	@Override
	public void onSystemUiVisibilityChange(final int visibility) {
		if ((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
			finish();
		}
	}

	private void updateView() {
		if (mPreferences == null) return;
		final Resources res = getResources();
		final int def = res.getInteger(R.integer.default_live_wallpaper_scale);
		mNyanDaydreamView.setScale(mPreferences.getInt(PREFERENCE_KEY_LIVE_WALLPAPER_SCALE, def));
	}

}
