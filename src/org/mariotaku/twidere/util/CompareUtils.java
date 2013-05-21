package org.mariotaku.twidere.util;

import android.os.Bundle;
import java.util.Iterator;

public class CompareUtils {

	public static boolean bundleEquals(final Bundle bundle1, final Bundle bundle2) {
		if (bundle1 == null || bundle2 == null) return bundle1 == bundle2;
		final Iterator<String> keys = bundle1.keySet().iterator();
		while (keys.hasNext()) {
			final String key = keys.next();
			if (!objectEquals(bundle1.get(key), bundle2.get(key))) return false;
		}
		return true;
	}
	
	public static boolean classEquals(final Class<?> cls1, final Class<?> cls2) {
		if (cls1 == null || cls2 == null) return cls1 == cls2;
		return cls1.getName().equals(cls2.getName());
	}

	public static boolean objectEquals(final Object object1, final Object object2) {
		if (object1 == null || object2 == null) return object1 == object2;
		return object1.equals(object2);
	}
	
}
