/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getThemeColor;
import static org.mariotaku.twidere.util.Utils.restartActivity;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.actionbarcompat.ActionBarFragmentActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MessagesManager;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("Registered")
public class BaseActivity extends ActionBarFragmentActivity implements Constants, IThemedActivity {

	private boolean mIsDarkTheme, mIsSolidColorBackground, mHardwareAccelerated;

	private boolean mInstanceStateSaved, mIsVisible, mIsOnTop;

	public MessagesManager getMessagesManager() {
		return getTwidereApplication() != null ? getTwidereApplication().getMessagesManager() : null;
	}

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return getTwidereApplication() != null ? getTwidereApplication().getTwitterWrapper() : null;
	}

	public boolean isOnTop() {
		return mIsOnTop;
	}

	public boolean isThemeChanged() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		final boolean solid_color_background = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		return is_dark_theme != mIsDarkTheme || solid_color_background != mIsSolidColorBackground;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	public void restart() {
		restartActivity(this);
	}

	protected int getDarkThemeRes() {
		return R.style.Theme_Twidere;
	}

	protected int getLightThemeRes() {
		return R.style.Theme_Twidere_Light;
	}

	protected boolean isDarkTheme() {
		return mIsDarkTheme;
	}

	protected boolean isHardwareAccelerationChanged() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return false;
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean hardware_acceleration = preferences.getBoolean(PREFERENCE_KEY_HARDWARE_ACCELERATION,
				PREFERENCE_DEFAULT_HARDWARE_ACCELERATION);
		return mHardwareAccelerated != hardware_acceleration;
	}

	protected boolean isSolidColorBackground() {
		return mIsSolidColorBackground;
	}

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setHardwareAcceleration();
		setTheme();
		super.onCreate(savedInstanceState);
		setActionBarBackground();
	}

	@Override
	protected void onPause() {
		mIsOnTop = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mInstanceStateSaved = false;
		mIsOnTop = true;
		if (isThemeChanged() || isHardwareAccelerationChanged()) {
			restart();
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mInstanceStateSaved = true;
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mIsVisible = true;
		final MessagesManager croutons = getMessagesManager();
		if (croutons != null) {
			croutons.addMessageCallback(this);
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			postDelayed(new SetActionBarBackgroundRunnable(), 50);
		}
	}

	@Override
	protected void onStop() {
		mIsVisible = false;
		final MessagesManager croutons = getMessagesManager();
		if (croutons != null) {
			croutons.removeMessageCallback(this);
		}
		super.onStop();
	}

	protected void setActionBarBackground() {
		final ActionBar ab = getSupportActionBar();
		final TypedArray a = obtainStyledAttributes(new int[] { R.attr.actionBarBackground });
		final int color = getThemeColor(this);
		final Drawable d = a.getDrawable(0);
		if (d == null) return;
		if (mIsDarkTheme) {
			final Drawable mutated = d.mutate();
			mutated.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			ab.setBackgroundDrawable(mutated);
		} else if (d instanceof LayerDrawable) {
			final LayerDrawable ld = (LayerDrawable) d.mutate();
			ld.findDrawableByLayerId(R.id.color_layer).setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			ab.setBackgroundDrawable(ld);
		}
	}

	protected boolean shouldSetBackground() {
		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setHardwareAcceleration() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return;
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mHardwareAccelerated = preferences.getBoolean(PREFERENCE_KEY_HARDWARE_ACCELERATION,
				PREFERENCE_DEFAULT_HARDWARE_ACCELERATION);
		final Window w = getWindow();
		if (mHardwareAccelerated) {
			w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
					WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		}
	}

	private void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		mIsDarkTheme = preferences.getBoolean(PREFERENCE_KEY_DARK_THEME, false);
		mIsSolidColorBackground = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		setTheme(is_dark_theme ? getDarkThemeRes() : getLightThemeRes());
		if (mIsSolidColorBackground && shouldSetBackground()) {
			getWindow().setBackgroundDrawableResource(is_dark_theme ? android.R.color.black : android.R.color.white);
		}
	}

	private class SetActionBarBackgroundRunnable implements Runnable {

		@Override
		public void run() {
			setActionBarBackground();
		}

	}

}
