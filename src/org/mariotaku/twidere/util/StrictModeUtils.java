/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.util.Log;

import java.util.Locale;

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
				Log.w(LOGTAG, String.format(Locale.US, template, className, method, nonEmptyFile, line));
				if (++log_counter == 3) return;
			} else if (CLASS_NAME.equals(className) && log_counter == -1) {
				log_counter = 0;
			}
		}
	}

	public static void detectAllThreadPolicy() {
		final ThreadPolicy.Builder threadPolicyBuilder = new ThreadPolicy.Builder();
		threadPolicyBuilder.detectAll();
		threadPolicyBuilder.penaltyLog();
		StrictMode.setThreadPolicy(threadPolicyBuilder.build());
	}

	public static void detectAllVmPolicy() {
		final VmPolicy.Builder vmPolicyBuilder = new VmPolicy.Builder();
		vmPolicyBuilder.detectAll();
		vmPolicyBuilder.penaltyLog();
		StrictMode.setVmPolicy(vmPolicyBuilder.build());
	}

}
