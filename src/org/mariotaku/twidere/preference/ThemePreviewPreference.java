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

package org.mariotaku.twidere.preference;

import static org.mariotaku.twidere.util.ThemeUtils.getThemeResource;
import static org.mariotaku.twidere.util.ThemeUtils.setPreviewView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

public class ThemePreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

	private final LayoutInflater mInflater;

	public ThemePreviewPreference(final Context context) {
		this(context, null);
	}

	public ThemePreviewPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ThemePreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mInflater = LayoutInflater.from(context);
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
		if (PREFERENCE_KEY_THEME.equals(key) || PREFERENCE_KEY_SOLID_COLOR_BACKGROUND.equals(key)) {
			notifyChanged();
		}
	}

	@Override
	protected void onBindView(final View view) {
		final Context context = getContext();
		setPreviewView(context, view.findViewById(R.id.theme_preview_content), getThemeResource(context));
		super.onBindView(view);
	}

	@Override
	protected View onCreateView(final ViewGroup parent) {
		return mInflater.inflate(R.layout.theme_preview, null);
	}

}
