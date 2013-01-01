package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.content.res.Configuration;
import android.os.Build;

public class ConfigurationAccessor {

	public static int getLayoutDirection(final Configuration conf) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return GetLayoutDirectionAccessorJB.getLayoutDirection(conf);
		final String lang = conf.locale.getLanguage();
		return "ar".equalsIgnoreCase(lang) ? Configuration.SCREENLAYOUT_LAYOUTDIR_RTL
				: Configuration.SCREENLAYOUT_LAYOUTDIR_LTR;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private static class GetLayoutDirectionAccessorJB {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		private static int getLayoutDirection(final Configuration conf) {
			return conf.getLayoutDirection();
		}
	}
}
