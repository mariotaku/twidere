package org.mariotaku.twidere.util;

import java.util.HashMap;

import org.mariotaku.twidere.Constants;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

public final class ServiceUtils implements Constants {

	private static HashMap<Context, ServiceUtils.ServiceBinder> sConnectionMap = new HashMap<Context, ServiceUtils.ServiceBinder>();

	public static ServiceToken bindToService(final Context context, final Intent intent) {

		return bindToService(context, intent, null);
	}

	public static ServiceToken bindToService(final Context context, final Intent intent,
			final ServiceConnection callback) {

		final ContextWrapper cw = new ContextWrapper(context);
		final ComponentName cn = cw.startService(intent);
		if (cn != null) {
			final ServiceUtils.ServiceBinder sb = new ServiceBinder(callback);
			if (cw.bindService(intent, sb, 0)) {
				sConnectionMap.put(cw, sb);
				return new ServiceToken(cw);
			}
		}
		Log.e(LOGTAG, "Failed to bind to service");
		return null;
	}

	public static class ServiceToken {

		ContextWrapper wrapped_context;

		ServiceToken(final ContextWrapper context) {

			wrapped_context = context;
		}
	}

	static class ServiceBinder implements ServiceConnection {

		private final ServiceConnection mCallback;

		public ServiceBinder(final ServiceConnection callback) {

			mCallback = callback;
		}

		@Override
		public void onServiceConnected(final ComponentName className, final android.os.IBinder service) {

			if (mCallback != null) {
				mCallback.onServiceConnected(className, service);
			}
		}

		@Override
		public void onServiceDisconnected(final ComponentName className) {

			if (mCallback != null) {
				mCallback.onServiceDisconnected(className);
			}
		}
	}
}
