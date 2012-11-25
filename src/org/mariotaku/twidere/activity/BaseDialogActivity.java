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
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ActivityThemeChangeInterface;

import android.annotation.SuppressLint;

@SuppressLint("Registered")
class BaseDialogActivity extends BaseActivity implements Constants, ActivityThemeChangeInterface {

	@Override
	protected int getDarkThemeRes() {
		return R.style.Theme_Twidere_Dialog;
	}

	@Override
	protected int getLightThemeRes() {
		return R.style.Theme_Twidere_Light_Dialog;
	}

	@Override
	protected boolean isSetBackgroundEnabled() {
		return false;
	}

}
