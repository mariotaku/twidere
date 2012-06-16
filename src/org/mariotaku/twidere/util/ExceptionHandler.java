package org.mariotaku.twidere.util;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler {

	private static ExceptionHandler sInstance;

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("Twidere", "Uncaught exception", ex);
	}

	public static ExceptionHandler getInstance() {
		if (sInstance == null) {
			sInstance = new ExceptionHandler();
		}
		return sInstance;
	}
}
