package org.mariotaku.twidere.view;

import android.os.Build;
import android.webkit.WebSettings;

public class WebSettingsAccessor {
	public static void setAllowUniversalAccessFromFileURLs(final WebSettings settings, final boolean flag) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
		WebSettingsAccessorSDK16.setAllowUniversalAccessFromFileURLs(settings, flag);
	}

	private static class WebSettingsAccessorSDK16 {
		private static void setAllowUniversalAccessFromFileURLs(final WebSettings settings, final boolean flag) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
			settings.setAllowUniversalAccessFromFileURLs(flag);
		}
	}

}
