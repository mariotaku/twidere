package org.mariotaku.twidere.util;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;

public class SetHomeButtonEnabledAccessor {

	public static void setHomeButtonEnabled(Activity activity, boolean enabled) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final ActionBar action_bar = activity.getActionBar();
			action_bar.setHomeButtonEnabled(enabled);
		}
	}
}
