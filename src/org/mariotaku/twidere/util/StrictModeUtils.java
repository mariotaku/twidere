package org.mariotaku.twidere.util;

import android.util.Log;

public class StrictModeUtils {

	public static final String LOGTAG = "Twidere.StrictMode";
	public static final String CLASS_NAME = StrictModeUtils.class.getName();

	public static void checkDiskIO() {
		final Thread thread = Thread.currentThread();
		if (thread == null || thread.getId() != 1) return;
		final StackTraceElement[] framesArray = thread.getStackTrace();

		// look for the last stack frame from this class and then whatever is
		// next is the caller we want to know about
		int log_counter = -1;
		for (final StackTraceElement stackFrame : framesArray) {
			final String className = stackFrame.getClassName();
			if (log_counter >= 0 && log_counter < 3) {
				final String file = stackFrame.getFileName(), method = stackFrame.getMethodName();
				final int line = stackFrame.getLineNumber();
				final String nonEmptyFile = file != null ? file : "Unknown";
				final String template = (log_counter == 0 ? "Disk IO on main thread:\n\t" : "\t") + "at %s.%s(%s:%d)";
				Log.w(LOGTAG, String.format(template, className, method, nonEmptyFile, line));
				if (++log_counter == 3) return;
			} else if (CLASS_NAME.equals(className) && log_counter == -1) {
				log_counter = 0;
			}
		}
	}

}
