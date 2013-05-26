package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

public final class AccessibilityEventAccessor {

	public static void setSource(final AccessibilityEvent event, final View source) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
		AccessibilityAccessorICS.setSource(event, source);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static class AccessibilityAccessorICS {

		private static void setSource(final AccessibilityEvent event, final View source) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
			event.setSource(source);
		}
		
	}
}
