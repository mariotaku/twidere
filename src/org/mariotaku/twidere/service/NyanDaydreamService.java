package org.mariotaku.twidere.service;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.service.dreams.DreamService;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.NyanDaydreamView;

public class NyanDaydreamService extends DreamService implements Constants, OnSharedPreferenceChangeListener {

	private NyanDaydreamView mNyanDaydreamView;
	private SharedPreferences mPreferences;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setContentView(R.layout.nyan_daydream);
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
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (PREFERENCE_KEY_LIVE_WALLPAPER_SCALE.equals(key)) {
			updateView();
		}
	}

	private void updateView() {
		if (mPreferences == null) return;
		final Resources res = getResources();
		final int def = res.getInteger(R.integer.default_live_wallpaper_scale);
		mNyanDaydreamView.setScale(mPreferences.getInt(PREFERENCE_KEY_LIVE_WALLPAPER_SCALE, def));
	}

}
