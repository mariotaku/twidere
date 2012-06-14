package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Window;

public class MethodsCompat {

	@TargetApi(5)
	public void overridePendingTransition(Activity activity, int enter_anim, int exit_anim) {
		activity.overridePendingTransition(enter_anim, exit_anim);

	}

	@TargetApi(11)
	public void recreate(Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			activity.recreate();
		}
	}

	@TargetApi(14)
	public void setUiOptions(Window window, int uiOptions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			window.setUiOptions(uiOptions);
		}
	}
}
