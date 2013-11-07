package org.mariotaku.twidere.util;

import android.os.Build;

import java.lang.reflect.Method;

public final class SmartBarUtils {

	public static boolean hasSmartBar() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Class.forName("android.os.Build").getMethod("hasSmartBar");
			return ((Boolean) method.invoke(null)).booleanValue();
		} catch (final Exception e) {
		}
		// Detect by Build.DEVICE
		if (Build.DEVICE.equals("mx2"))
			return true;
		else if (Build.DEVICE.equals("mx") || Build.DEVICE.equals("m9")) return false;
		return false;
	}

}
