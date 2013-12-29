package org.mariotaku.twidere.util;

import android.app.ActionBar;
import android.os.Build;

import java.lang.reflect.Method;

public final class SmartBarUtils {

	private static String[] SMARTBAR_SUPPORTED_DEVICES = { "mx2", "mx3" };

	public static boolean hasSmartBar() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (final Exception e) {
		}
		// Detect by Build.DEVICE
		if (isDeviceWithSmartBar(Build.DEVICE)) return true;
		return false;
	}

	public static boolean isDeviceWithSmartBar(final String buildDevice) {
		for (final String dev : SMARTBAR_SUPPORTED_DEVICES) {
			if (dev.equals(buildDevice)) return true;
		}
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
