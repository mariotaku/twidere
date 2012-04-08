package org.mariotaku.twidere.util;

import java.util.Date;
import java.util.HashMap;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
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

	public String formatToShortTimeString(long timestamp) {
		Time then = new Time();
		then.set(timestamp);
		Time now = new Time();
		now.setToNow();

		if (then.year < now.year) {
			int diff = now.year - then.year;
			return mContext.getResources().getQuantityString(R.plurals.Nyears, diff, diff);
		} else if (then.month < now.month) {
			int diff = now.month - then.month;
			return mContext.getResources().getQuantityString(R.plurals.Nmonths, diff, diff);
		} else if (then.yearDay < now.yearDay) {
			int diff = now.yearDay - then.yearDay;
			return mContext.getResources().getQuantityString(R.plurals.Ndays, diff, diff);
		} else if (then.hour < now.hour) {
			int diff = now.hour - then.hour;
			return mContext.getResources().getQuantityString(R.plurals.Nhours, diff, diff);
		} else if (then.minute < now.minute) {
			int diff = now.minute - then.minute;
			return mContext.getResources().getQuantityString(R.plurals.Nminutes, diff, diff);
		} else if (then.minute == now.minute) return mContext.getString(R.string.just_now);
		return then.format3339(true);
	}

	public int getTypeIcon(boolean is_retweet, boolean is_fav, boolean has_location,
			boolean has_media) {
		if (is_fav)
			return R.drawable.ic_tweet_stat_starred;
		else if (is_retweet)
			return R.drawable.ic_tweet_stat_retweet;
		else if (has_media)
			return R.drawable.ic_tweet_stat_has_media;
		else if (has_location) return R.drawable.ic_tweet_stat_has_location;
		return 0;
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
