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

package org.mariotaku.twidere.theme;

import android.content.Context;
import android.view.Window;

import com.negusoft.holoaccent.AccentHelper;
import com.negusoft.holoaccent.dialog.DividerPainter;

public class TwidereAccentHelper extends AccentHelper {

	private DividerPainter mDividerPainter;
	private final int mAccentColor;

	public TwidereAccentHelper() {
		mAccentColor = 0;
	}

	public TwidereAccentHelper(final int color) {
		super(color);
		mAccentColor = color;
	}

	@Override
	public void prepareDialog(final Context c, final Window window) {
		if (mDividerPainter == null) {
			if (mAccentColor != 0) {
				mDividerPainter = new DividerPainter(mAccentColor);
			} else {
				mDividerPainter = new DividerPainter(c);
			}
		}
		mDividerPainter.paint(window);
	}

}
