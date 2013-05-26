package org.mariotaku.twidere.util;
import android.os.Bundle;

public class BundleAccessor {

	public static String getString(final Bundle bundle, final String key, final String def) {
		return bundle.containsKey(key) ? bundle.getString(key) : def;
	}
}
