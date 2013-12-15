package org.mariotaku.twidere.util;

import android.app.ActionBar;
import android.os.Build;

import java.lang.reflect.Method;

public final class SmartBarUtils {

	public static boolean hasSmartBar() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (final Exception e) {
		}
		// Detect by Build.DEVICE
		if (Build.DEVICE.equals("mx2"))
			return true;
		else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) return false;
		return false;
	}

	public static void setActionModeHeaderHidden(final ActionBar actionbar, final boolean hidden) {
		try {
			final Method method = ActionBar.class.getMethod("setActionModeHeaderHidden", new Class[] { boolean.class });
			method.invoke(actionbar, hidden);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
