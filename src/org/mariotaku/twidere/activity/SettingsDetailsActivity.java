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

import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

public class SettingsDetailsActivity extends BasePreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
		final Bundle args = getIntent().getExtras();
		if (args != null) {
			if (args.containsKey(INTENT_KEY_RESID)) {
				addPreferencesFromResource(args.getInt(INTENT_KEY_RESID));
			}
		}
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
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
}
