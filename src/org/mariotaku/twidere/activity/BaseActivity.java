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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.ThemeUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

@SuppressLint("Registered")
public class BaseActivity extends BaseThemedActivity implements Constants, IThemedActivity {

	private boolean mIsSolidColorBackground;

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

	public boolean isVisible() {
		return mIsVisible;
	}

	@Override
	protected int getThemeResource() {
		return ThemeUtils.getThemeResource(this);
	}

	protected boolean isSolidColorBackground() {
		return mIsSolidColorBackground;
	}

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
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
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false) != mIsSolidColorBackground) {
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
		// final ActionBar ab = getActionBar();
		// final TypedArray a = obtainStyledAttributes(new int[] {
		// R.attr.actionBarBackground });
		// final int color = getThemeColor(this);
		// final Drawable d = a.getDrawable(0);
		// if (d == null) return;
		// if (mIsDarkTheme) {
		// final Drawable mutated = d.mutate();
		// mutated.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		// ab.setBackgroundDrawable(mutated);
		// } else if (d instanceof LayerDrawable) {
		// final LayerDrawable ld = (LayerDrawable) d.mutate();
		// ld.findDrawableByLayerId(R.id.color_layer).setColorFilter(color,
		// PorterDuff.Mode.MULTIPLY);
		// ab.setBackgroundDrawable(ld);
		// }
	}

	protected boolean shouldSetBackground() {
		return true;
	}

	private void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = ThemeUtils.isDarkTheme(getCurrentThemeResource());
		mIsSolidColorBackground = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		if (mIsSolidColorBackground && shouldSetBackground()) {
			getWindow().setBackgroundDrawableResource(is_dark_theme ? android.R.color.black : android.R.color.white);
		}
	}

}
