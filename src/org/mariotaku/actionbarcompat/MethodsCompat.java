package org.mariotaku.actionbarcompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

final class MethodsCompat {

	private MethodsCompat() {
		throw new IllegalArgumentException("You cannot create instance for this class");
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static void invalidateOptionsMenu(final Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			activity.invalidateOptionsMenu();
		}
	}

}
