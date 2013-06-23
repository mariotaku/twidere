/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public final class AccessibilityEventAccessor {

	public static void setSource(final AccessibilityEvent event, final View source) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
		AccessibilityAccessorICS.setSource(event, source);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class AccessibilityAccessorICS {

		private static void setSource(final AccessibilityEvent event, final View source) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
			event.setSource(source);
		}

	}
}
