package org.mariotaku.twidere.util;

import android.os.Build;
import android.os.StrictMode;

public class StrictModeAccessor {

	public static final boolean ENABLED = false;

	public static void detectAll() {
		if (!ENABLED || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) return;
		StrictModeAccessorGingerbread.detectAll();
	}

	static class StrictModeAccessorGingerbread {

		static void detectAll() {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) return;
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
		}
	}

}
