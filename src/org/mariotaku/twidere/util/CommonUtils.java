package org.mariotaku.twidere.util;

import java.util.Date;
import java.util.HashMap;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.service.UpdateService;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

public class CommonUtils implements Constants {

	private Context mContext;
	private HashMap<Context, ServiceBinder> mConnectionMap = new HashMap<Context, ServiceBinder>();

	public CommonUtils(Context context) {
		mContext = context;
	}

	public ServiceToken bindToService() {

		return bindToService(null);
	}

	public ServiceToken bindToService(ServiceConnection callback) {

		ContextWrapper cw = new ContextWrapper(mContext);
		cw.startService(new Intent(cw, UpdateService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService(new Intent(cw, UpdateService.class), sb, 0)) {
			mConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		Log.e(LOGTAG, "Failed to bind to service");
		return null;
	}

	public String formatTimeStampString(long timestamp) {
		Time then = new Time();
		then.set(timestamp);
		Time now = new Time();
		now.setToNow();

		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL
				| DateUtils.FORMAT_CAP_AMPM;

		if (then.year != now.year) {
			format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
		} else if (then.yearDay != now.yearDay) {
			format_flags |= DateUtils.FORMAT_SHOW_DATE;
		} else {
			format_flags |= DateUtils.FORMAT_SHOW_TIME;
		}

		return DateUtils.formatDateTime(mContext, timestamp, format_flags);
	}

	@SuppressWarnings("deprecation")
	public String formatTimeStampString(String date_time) {
		return formatTimeStampString(Date.parse(date_time));
	}

	public void unbindFromService(ServiceToken token) {

		if (token == null) {
			Log.e(LOGTAG, "Trying to unbind with null token");
			return;
		}
		ContextWrapper wrapper = token.mWrappedContext;
		ServiceBinder binder = mConnectionMap.remove(wrapper);
		if (binder == null) {
			Log.e(LOGTAG, "Trying to unbind for unknown Context");
			return;
		}
		wrapper.unbindService(binder);
	}

	private class ServiceBinder implements ServiceConnection {

		private ServiceConnection mCallback;

		public ServiceBinder(ServiceConnection callback) {

			mCallback = callback;
		}

		@Override
		public void onServiceConnected(ComponentName className, android.os.IBinder service) {

			if (mCallback != null) {
				mCallback.onServiceConnected(className, service);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {

			if (mCallback != null) {
				mCallback.onServiceDisconnected(className);
			}
		}
	}

}
