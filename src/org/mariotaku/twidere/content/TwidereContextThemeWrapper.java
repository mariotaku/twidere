/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.content;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;

import org.mariotaku.twidere.content.iface.ITwidereContextWrapper;
import org.mariotaku.twidere.util.theme.TwidereAccentHelper;

public class TwidereContextThemeWrapper extends ContextThemeWrapper implements ITwidereContextWrapper {

	private final TwidereAccentHelper mAccentHelper;

	private final int mThemeResourceId;
	private final int mAccentColor;

	private final boolean mIsActionBarContext;

	public TwidereContextThemeWrapper(final Context base, final int themeResource, final int accentColor) {
		this(base, themeResource, accentColor, false);
	}

	public TwidereContextThemeWrapper(final Context base, final int themeResource, final int accentColor,
			final boolean isActionBarContext) {
		super(base, themeResource);
		mThemeResourceId = themeResource;
		mAccentColor = accentColor;
		mAccentHelper = new TwidereAccentHelper(accentColor);
		mIsActionBarContext = isActionBarContext;
	}

	public int getAccentColor() {
		return mAccentColor;
	}

	@Override
	public Resources getResources() {
		return mAccentHelper.getResources(this, super.getResources());
	}

	@Override
	public int getThemeResourceId() {
		return mThemeResourceId;
	}

	public boolean isActionBarContext() {
		return mIsActionBarContext;
	}

	@Override
	public String toString() {
		return "TwidereContextThemeWrapper{isActionBarContext=" + isActionBarContext() + "}";
	}

}
