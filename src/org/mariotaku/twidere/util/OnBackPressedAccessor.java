package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

public final class OnBackPressedAccessor {

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public static void onBackPressed(final Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			activity.onBackPressed();
		}
	}
}
