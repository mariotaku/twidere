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
import org.mariotaku.twidere.fragment.ActivityHostFragment;
import org.mariotaku.twidere.util.ThemeUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

public class InternalSettingsDetailsActivity extends PreferenceActivity implements Constants,
		ActivityHostFragment.FragmentCallback<InternalSettingsDetailsActivity> {

	private ActivityHostFragment<InternalSettingsDetailsActivity> mCallbackFragment;

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK: {
				final Activity activity = getParent();
				if (activity instanceof SettingsActivity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
					activity.onBackPressed();
					return true;
				}
				break;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onTitleChanged(final CharSequence title, final int color) {
		super.onTitleChanged(title, color);
		if (mCallbackFragment == null) return;
		final Activity activity = mCallbackFragment.getActivity();
		if (activity instanceof SettingsDetailsActivity) {
			activity.setTitle(title);
		}
	}

	@Override
	public void setCallbackFragment(final ActivityHostFragment<InternalSettingsDetailsActivity> fragment) {
		mCallbackFragment = fragment;
		if (mCallbackFragment == null) return;
		final Activity activity = mCallbackFragment.getActivity();
		if (activity instanceof SettingsDetailsActivity) {
			activity.setTitle(getTitle());
		}
	}

	@Override
	public void setContentView(final int layoutRes) {
		setContentView(null);
	}

	@Override
	public void setContentView(final View view) {
		setContentView(null, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
	}

	@Override
	public void setContentView(final View view, final ViewGroup.LayoutParams params) {
		final ListView lv = new ListView(this);
		final Resources res = getResources();
		final float density = res.getDisplayMetrics().density;
		final int padding = (int) density * 16;
		lv.setId(android.R.id.list);
		lv.setPadding(padding, 0, padding, 0);
		super.setContentView(lv, params);
	}

	public void setTheme() {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean is_dark_theme = ThemeUtils.isDarkTheme(this);
		final boolean solid_color_background = preferences.getBoolean(PREFERENCE_KEY_SOLID_COLOR_BACKGROUND, false);
		setTheme(ThemeUtils.getThemeResource(this));
		if (solid_color_background) {
			getWindow().setBackgroundDrawableResource(is_dark_theme ? android.R.color.black : android.R.color.white);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		setTheme();
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
		final Bundle args = getIntent().getExtras();
		if (args != null) {
			if (args.containsKey(INTENT_KEY_RESID)) {
				addPreferencesFromResource(args.getInt(INTENT_KEY_RESID));
			}
		}
	}
}
