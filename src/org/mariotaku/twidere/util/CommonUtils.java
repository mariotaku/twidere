package org.mariotaku.twidere.util;

import java.util.Date;
import java.util.HashMap;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.service.UpdateService;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;

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
		return formatToShortTimeString(mContext, timestamp);
	}

	public String getImagePathFromUri(Uri uri) {
		return getImagePathFromUri(mContext, uri);
	}

	public Bitmap getPreviewBitmap(int color) {
		return getPreviewBitmap(mContext, color);
	}

	public Twitter getTwitterInstance(long account_id) {
		return getTwitterInstance(mContext, account_id);
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

	public static String formatToShortTimeString(Context context, long timestamp) {
		Time then = new Time();
		then.set(timestamp);
		Time now = new Time();
		now.setToNow();

		if (then.year < now.year) {
			int diff = now.year - then.year;
			return context.getResources().getQuantityString(R.plurals.Nyears, diff, diff);
		} else if (then.month < now.month) {
			int diff = now.month - then.month;
			return context.getResources().getQuantityString(R.plurals.Nmonths, diff, diff);
		} else if (then.yearDay < now.yearDay) {
			int diff = now.yearDay - then.yearDay;
			return context.getResources().getQuantityString(R.plurals.Ndays, diff, diff);
		} else if (then.hour < now.hour) {
			int diff = now.hour - then.hour;
			return context.getResources().getQuantityString(R.plurals.Nhours, diff, diff);
		} else if (then.minute < now.minute) {
			int diff = now.minute - then.minute;
			return context.getResources().getQuantityString(R.plurals.Nminutes, diff, diff);
		} else if (then.minute == now.minute) return context.getString(R.string.just_now);
		return then.format3339(true);
	}

	public static int getAccountColor(Context context, long account_id) {
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
				new String[] { Accounts.USER_COLOR }, Accounts.USER_ID + "=" + account_id, null,
				null);
		if (cur == null || cur.getCount() <= 0) return Color.TRANSPARENT;
		cur.moveToFirst();
		int color = cur.getInt(cur.getColumnIndexOrThrow(Accounts.USER_COLOR));
		if (cur != null) {
			cur.close();
		}
		return color;
	}

	public static long[] getActivatedAccounts(Context context) {
		long[] accounts = new long[] {};
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
				new String[] { Accounts.USER_ID }, Accounts.IS_ACTIVATED + "=1", null, null);
		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
			cur.moveToFirst();
			accounts = new long[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				accounts[i] = cur.getLong(idx);
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static String getImagePathFromUri(Context context, Uri uri) {
		if (uri == null) return null;

		String media_uri_start = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();

		if (uri.toString().startsWith(media_uri_start)) {

			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);

			if (cursor == null || cursor.getCount() <= 0) return null;

			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

			cursor.moveToFirst();

			String path = cursor.getString(column_index);
			cursor.close();
			return path;
		} else if (uri.getScheme().equals("file")) return uri.getPath();
		return null;
	}

	public static Bitmap getPreviewBitmap(Context context, int color) {

		float density = context.getResources().getDisplayMetrics().density;
		int width = (int) (32 * density), height = (int) (32 * density);

		Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bm);

		int rectrangle_size = (int) (density * 5);
		int numRectanglesHorizontal = (int) Math.ceil(width / rectrangle_size);
		int numRectanglesVertical = (int) Math.ceil(height / rectrangle_size);
		Rect r = new Rect();
		boolean verticalStartWhite = true;
		for (int i = 0; i <= numRectanglesVertical; i++) {

			boolean isWhite = verticalStartWhite;
			for (int j = 0; j <= numRectanglesHorizontal; j++) {

				r.top = i * rectrangle_size;
				r.left = j * rectrangle_size;
				r.bottom = r.top + rectrangle_size;
				r.right = r.left + rectrangle_size;
				Paint paint = new Paint();
				paint.setColor(isWhite ? Color.WHITE : Color.GRAY);

				canvas.drawRect(r, paint);

				isWhite = !isWhite;
			}

			verticalStartWhite = !verticalStartWhite;

		}
		canvas.drawColor(color);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(2.0f);
		float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0,
				height, width, height };
		canvas.drawLines(points, paint);

		return bm;
	}

	public static Twitter getTwitterInstance(Context context, long account_id) {
		Twitter twitter = null;
		StringBuilder where = new StringBuilder();
		where.append(Accounts.USER_ID + "=" + account_id);
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS,
				where.toString(), null, null);
		if (cur != null) {
			if (cur.getCount() == 1) {
				cur.moveToFirst();
				ConfigurationBuilder cb = new ConfigurationBuilder();
				String rest_api_base = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.REST_API_BASE));
				String search_api_base = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.SEARCH_API_BASE));
				if (rest_api_base == null || "".equals(rest_api_base)) {
					rest_api_base = DEFAULT_REST_API_BASE;
				}
				if (search_api_base == null || "".equals(search_api_base)) {
					search_api_base = DEFAULT_SEARCH_API_BASE;
				}
				cb.setRestBaseURL(rest_api_base);
				cb.setSearchBaseURL(search_api_base);

				switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
					case Accounts.AUTH_TYPE_OAUTH:
					case Accounts.AUTH_TYPE_XAUTH:
						cb.setOAuthConsumerKey(CONSUMER_KEY);
						cb.setOAuthConsumerSecret(CONSUMER_SECRET);
						twitter = new TwitterFactory(cb.build()).getInstance(new AccessToken(cur
								.getString(cur.getColumnIndexOrThrow(Accounts.OAUTH_TOKEN)), cur
								.getString(cur.getColumnIndexOrThrow(Accounts.TOKEN_SECRET))));
						break;
					case Accounts.AUTH_TYPE_BASIC:
						twitter = new TwitterFactory(cb.build())
								.getInstance(new BasicAuthorization(
										cur.getString(cur.getColumnIndexOrThrow(Accounts.USERNAME)),
										cur.getString(cur
												.getColumnIndexOrThrow(Accounts.BASIC_AUTH_PASSWORD))));
						break;
					default:
				}
			}
			cur.close();
		}
		return twitter;
	}

	public static int getTypeIcon(boolean is_retweet, boolean is_fav, boolean has_location,
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

	public static void setLayerType(View view, int layerType, Paint paint) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new MethodsCompat().setLayerType(view, layerType, paint);
		}
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
