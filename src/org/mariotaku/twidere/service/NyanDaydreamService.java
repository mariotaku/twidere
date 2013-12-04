package org.mariotaku.twidere.service;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.service.dreams.DreamService;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.NyanSurfaceHelper;

public class NyanDaydreamService extends DreamService implements Constants, OnSharedPreferenceChangeListener {

	private SurfaceView mSurfaceView;
	private SharedPreferences mPreferences;
	private NyanSurfaceHelper mHelper;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		setContentView(R.layout.surface_view);
		final SurfaceHolder holder = mSurfaceView.getHolder();
		mHelper = new NyanSurfaceHelper(this, holder);
		updateSurface();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (PREFERENCE_KEY_LIVE_WALLPAPER_SCALE.equals(key)) {
			updateSurface();
		}
	}

	private void updateSurface() {
		if (mPreferences == null) return;
		final Resources res = getResources();
		final int def = res.getInteger(R.integer.default_live_wallpaper_scale);
		mHelper.setScale(mPreferences.getInt(PREFERENCE_KEY_LIVE_WALLPAPER_SCALE, def));
	}

}
