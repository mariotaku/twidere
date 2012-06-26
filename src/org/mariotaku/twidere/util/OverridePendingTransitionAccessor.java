package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.app.Activity;

public class OverridePendingTransitionAccessor {

	@TargetApi(5)
	public static void overridePendingTransition(Activity activity, int enter_anim, int exit_anim) {
		activity.overridePendingTransition(enter_anim, exit_anim);
	}

}
