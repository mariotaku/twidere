package org.mariotaku.twidere.util;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler {

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.e("Twidere", "Uncaught exception", ex);
	}

	
	private static ExceptionHandler sInstance;
	
	public static ExceptionHandler getInstance() {
		if (sInstance == null) sInstance = new ExceptionHandler();
		return sInstance;
	}
}
