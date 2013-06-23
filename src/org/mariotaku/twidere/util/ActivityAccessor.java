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

package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;

public final class ActivityAccessor {

	public static void onBackPressed(final Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) return;
		ActivityAccessorSDK5.onBackPressed(activity);
	}

	public static void overridePendingTransition(final Activity activity, final int enter_anim, final int exit_anim) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) return;
		ActivityAccessorSDK5.overridePendingTransition(activity, enter_anim, exit_anim);
	}

	public static void setFinishOnTouchOutside(final Activity activity, final boolean finish) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return;
		ActivityAccessorSDK11.setFinishOnTouchOutside(activity, finish);
	}

	public static void setHomeButtonEnabled(final Activity activity, final boolean enabled) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
		ActivityAccessorSDK14.setHomeButtonEnabled(activity, enabled);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class ActivityAccessorSDK11 {

		public static void setFinishOnTouchOutside(final Activity activity, final boolean finish) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) return;
			activity.setFinishOnTouchOutside(finish);
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class ActivityAccessorSDK14 {

		private static void setHomeButtonEnabled(final Activity activity, final boolean enabled) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
			final ActionBar actionBar = activity.getActionBar();
			if (actionBar == null) return;
			actionBar.setHomeButtonEnabled(enabled);
		}
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private static class ActivityAccessorSDK5 {

		private static void onBackPressed(final Activity activity) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) return;
			activity.onBackPressed();
		}

		private static void overridePendingTransition(final Activity activity, final int enter_anim, final int exit_anim) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) return;
			activity.overridePendingTransition(enter_anim, exit_anim);
		}
	}
}
