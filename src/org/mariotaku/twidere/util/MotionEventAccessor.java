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
import android.view.InputDevice;
import android.view.MotionEvent;

public class MotionEventAccessor {

	public static float getAxisValue(final MotionEvent event, final int axis) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			return GetAxisValueAccessorHoneyComb.getAxisValue(event, axis);
		return 0;
	}

	public static int getSource(final MotionEvent event) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			return GetSourceAccessorGingerbread.getSource(event);
		return InputDevice.SOURCE_TOUCHSCREEN;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private static class GetAxisValueAccessorHoneyComb {
		private static float getAxisValue(final MotionEvent event, final int axis) {
			return event.getAxisValue(axis);
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class GetSourceAccessorGingerbread {
		private static int getSource(final MotionEvent event) {
			return event.getSource();
		}
	}
}
