package org.mariotaku.actionbarcompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

final class MethodsCompat {

	private MethodsCompat() {
		throw new IllegalArgumentException("You cannot create instance for this class");
	}

	@TargetApi(11)
	public static void invalidateOptionsMenu(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			activity.invalidateOptionsMenu();
		}
	}

}
