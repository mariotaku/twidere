package org.mariotaku.twidere.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Messages;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.TwipOModeAuthorization;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public final class Utils implements Constants {

	private static UriMatcher CONTENT_PROVIDER_URI_MATCHER;

	static {
		CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES, URI_STATUSES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_ACCOUNTS, URI_ACCOUNTS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_MENTIONS, URI_MENTIONS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DRAFTS, URI_DRAFTS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FAVORITES, URI_FAVORITES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_USERS, URI_CACHED_USERS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_USERS, URI_FILTERED_USERS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_KEYWORDS, URI_FILTERED_KEYWORDS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_SOURCES, URI_FILTERED_SOURCES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES + "/*", URI_USER_TIMELINE);
	}
	private static HashMap<Long, Integer> sAccountColors = new HashMap<Long, Integer>();

	private static final String IMAGE_URL_PATTERN = "href=\\s*[\\\"'](http(s?):\\/\\/.+?(?i)(png|jpeg|jpg|gif|bmp))[\\\"']\\s*";

	private static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };

	private static Drawable ICON_STARRED, ICON_HAS_MEDIA, ICON_HAS_LOCATION;

	public static String buildActivatedStatsWhereClause(Context context, String selection) {
		long[] account_ids = getActivatedAccountIds(context);
		StringBuilder builder = new StringBuilder();
		if (selection != null) {
			builder.append(selection);
			builder.append(" AND ");
		}

		builder.append(Statuses.ACCOUNT_ID + " IN ( ");
		for (int i = 0; i < account_ids.length; i++) {
			String id_string = String.valueOf(account_ids[i]);
			if (id_string != null) {
				if (i > 0) {
					builder.append(", ");
				}
				builder.append(id_string);
			}
		}
		builder.append(" )");

		return builder.toString();
	}

	public static String buildFilterWhereClause(String table, String selection) {
		StringBuilder builder = new StringBuilder();
		if (selection != null) {
			builder.append(selection);
			builder.append(" AND ");
		}
		builder.append(Statuses._ID + " NOT IN ( ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table);
		builder.append(" WHERE " + table + "." + Statuses.SCREEN_NAME + " IN ( SELECT " + TABLE_FILTERED_USERS + "."
				+ Filters.Users.TEXT + " FROM " + TABLE_FILTERED_USERS + " )");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
				+ TABLE_FILTERED_SOURCES);
		builder.append(" WHERE " + table + "." + Statuses.SOURCE + " LIKE '%>'||" + TABLE_FILTERED_SOURCES + "."
				+ Filters.Sources.TEXT + "||'</a>%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
				+ TABLE_FILTERED_KEYWORDS);
		builder.append(" WHERE " + table + "." + Statuses.TEXT_PLAIN + " LIKE '%'||" + TABLE_FILTERED_KEYWORDS + "."
				+ Filters.Keywords.TEXT + "||'%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" )");

		return builder.toString();
	}

	public static Uri buildQueryUri(Uri uri, boolean notify) {
		Uri.Builder uribuilder = uri.buildUpon();
		uribuilder.appendQueryParameter(QUERY_PARAM_NOTIFY, String.valueOf(notify));
		return uribuilder.build();
	}

	public static synchronized void cleanDatabasesByItemLimit(Context context) {
		final ContentResolver resolver = context.getContentResolver();
		final int item_limit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);

		for (long account_id : getAccountIds(context)) {
			// Clean statuses.
			for (Uri uri : STATUSES_URIS) {
				String table = getTableNameForContentUri(uri);
				StringBuilder where = new StringBuilder();
				where.append(Statuses.ACCOUNT_ID + " = " + account_id);
				where.append(" AND ");
				where.append(Statuses._ID + " NOT IN (");
				where.append(" SELECT " + Statuses._ID + " FROM " + table);
				where.append(" WHERE " + Statuses.ACCOUNT_ID + " = " + account_id);
				where.append(" ORDER BY " + Statuses.STATUS_ID + " DESC");
				where.append(" LIMIT " + item_limit + ")");
				resolver.delete(uri, where.toString(), null);
			}
		}
		// Clean cached users.
		{
			Uri uri = CachedUsers.CONTENT_URI;
			String table = getTableNameForContentUri(uri);
			StringBuilder where = new StringBuilder();
			where.append(Statuses._ID + " NOT IN (");
			where.append(" SELECT " + CachedUsers._ID + " FROM " + table);
			where.append(" LIMIT " + item_limit * 8 + ")");
			resolver.delete(uri, where.toString(), null);
		}
	}

	public static void clearAccountColor() {
		sAccountColors.clear();
	}

	public static ParcelableStatus findStatusInDatabases(Context context, long account_id, long status_id) {
		final ContentResolver resolver = context.getContentResolver();
		ParcelableStatus status = null;
		String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = " + status_id;
		for (Uri uri : STATUSES_URIS) {
			Cursor cur = resolver.query(uri, Statuses.COLUMNS, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				status = new ParcelableStatus(cur, new StatusesCursorIndices(cur));
			}
			cur.close();
		}
		return status;
	}

	/**
	 * 
	 * @param location
	 * 
	 * @return Location in "[longitude],[latitude]" format.
	 */
	public static String formatGeoLocationToString(GeoLocation location) {
		if (location == null) return null;
		return location.getLatitude() + "," + location.getLongitude();
	}

	public static String formatTimeStampString(Context context, long timestamp) {
		Time then = new Time();
		then.set(timestamp);
		Time now = new Time();
		now.setToNow();

		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

		if (then.year != now.year) {
			format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
		} else if (then.yearDay != now.yearDay) {
			format_flags |= DateUtils.FORMAT_SHOW_DATE;
		} else {
			format_flags |= DateUtils.FORMAT_SHOW_TIME;
		}

		return DateUtils.formatDateTime(context, timestamp, format_flags);
	}

	@SuppressWarnings("deprecation")
	public static String formatTimeStampString(Context context, String date_time) {
		return formatTimeStampString(context, Date.parse(date_time));
	}

	public static String formatToLongTimeString(Context context, long timestamp) {
		Time then = new Time();
		then.set(timestamp);
		Time now = new Time();
		now.setToNow();

		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

		format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

		return DateUtils.formatDateTime(context, timestamp, format_flags);
	}

	public static String formatToShortTimeString(Context context, long timestamp) {
		final Resources res = context.getResources();
		final Time then = new Time(), now = new Time();
		then.set(timestamp);
		now.setToNow();
		if (then.before(now)) {

			int year_diff = now.year - then.year;

			int month_diff = (year_diff > 0 ? 12 : 0) + now.month - then.month;
			if (year_diff < 1) {
				int day_diff = (month_diff > 0 ? then.getActualMaximum(Time.MONTH_DAY) : 0) + now.monthDay
						- then.monthDay;
				if (month_diff < 1) {
					if (day_diff >= then.getActualMaximum(Time.MONTH_DAY))
						return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
					int hour_diff = (day_diff > 0 ? 24 : 0) + now.hour - then.hour;
					if (day_diff < 1) {
						if (hour_diff >= 24) return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
						int minute_diff = (hour_diff > 0 ? 60 : 0) + now.minute - then.minute;
						if (hour_diff < 1) {
							if (minute_diff >= 60)
								return res.getQuantityString(R.plurals.Nhours, hour_diff, hour_diff);
							if (minute_diff <= 1) return context.getString(R.string.just_now);
							return res.getQuantityString(R.plurals.Nminutes, minute_diff, minute_diff);
						} else if (hour_diff == 1) {
							if (minute_diff < 60)
								return res.getQuantityString(R.plurals.Nminutes, minute_diff, minute_diff);
						}
						return res.getQuantityString(R.plurals.Nhours, hour_diff, hour_diff);
					} else if (day_diff == 1) {
						if (hour_diff < 24) return res.getQuantityString(R.plurals.Nhours, hour_diff, hour_diff);
					}
					return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
				} else if (month_diff == 1) {
					if (day_diff < then.getActualMaximum(Time.MONTH_DAY))
						return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
				}
				return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
			} else if (year_diff == 1) {
				if (month_diff < 12) return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
			}
			return res.getQuantityString(R.plurals.Nyears, year_diff, year_diff);
		}
		return then.format3339(true);
	}

	public static int getAccountColor(Context context, long account_id) {

		Integer color = sAccountColors.get(account_id);
		if (color == null) {
			Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_COLOR },
					Accounts.USER_ID + "=" + account_id, null, null);
			if (cur == null) return Color.TRANSPARENT;
			if (cur.getCount() <= 0) {
				cur.close();
				return Color.TRANSPARENT;
			}
			cur.moveToFirst();
			color = cur.getInt(cur.getColumnIndexOrThrow(Accounts.USER_COLOR));
			cur.close();
			sAccountColors.put(account_id, color);
		}
		return color;
	}

	public static long getAccountId(Context context, String username) {
		long user_id = -1;

		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_ID },
				Accounts.USERNAME + " = ?", new String[] { username }, null);
		if (cur == null) return user_id;

		if (cur.getCount() > 0) {
			cur.moveToFirst();
			user_id = cur.getLong(cur.getColumnIndexOrThrow(Accounts.USER_ID));
		}
		cur.close();
		return user_id;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static long getAccountIdForStatusId(Context context, long status_id) {

		String[] cols = new String[] { Statuses.ACCOUNT_ID };
		String where = Statuses.STATUS_ID + " = " + status_id;

		for (Uri uri : STATUSES_URIS) {
			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				long id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.ACCOUNT_ID));
				cur.close();
				return id;
			}
			cur.close();
		}
		return -1;
	}

	public static long[] getAccountIds(Context context) {
		long[] accounts = new long[] {};
		if (context == null) return accounts;
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_ID }, null,
				null, null);
		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
			cur.moveToFirst();
			accounts = new long[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				accounts[i] = cur.getLong(idx);
				i++;
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static String[] getAccountScreenNames(Context context) {
		String[] accounts = new String[0];
		String[] cols = new String[] { Accounts.USERNAME };
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, null, null, null);
		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USERNAME);
			cur.moveToFirst();
			accounts = new String[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				accounts[i] = cur.getString(idx);
				i++;
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static String getAccountUsername(Context context, long account_id) {

		String username = null;

		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USERNAME },
				Accounts.USER_ID + " = " + account_id, null, null);
		if (cur == null) return username;

		if (cur.getCount() > 0) {
			cur.moveToFirst();
			username = cur.getString(cur.getColumnIndex(Accounts.USERNAME));
		}
		cur.close();
		return username;
	}

	public static long[] getActivatedAccountIds(Context context) {
		long[] accounts = new long[] {};
		String[] cols = new String[] { Accounts.USER_ID };
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1", null,
				null);
		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
			cur.moveToFirst();
			accounts = new long[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				accounts[i] = cur.getLong(idx);
				i++;
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static String[] getActivatedAccountScreenNames(Context context) {
		String[] accounts = new String[0];
		String[] cols = new String[] { Accounts.USERNAME };
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1", null,
				null);
		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USERNAME);
			cur.moveToFirst();
			accounts = new String[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				accounts[i] = cur.getString(idx);
				i++;
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static Bitmap getColorPreviewBitmap(Context context, int color) {

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
		float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0, height, width,
				height };
		canvas.drawLines(points, paint);

		return bm;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static int getErrorCode(TwitterException e) {
		if (e == null) return RESULT_UNKNOWN_ERROR;
		int status_code = e.getStatusCode();
		if (status_code == -1)
			return RESULT_CONNECTIVITY_ERROR;
		else if (status_code >= 401 && status_code < 404)
			return RESULT_NO_PERMISSION;
		else if (status_code >= 404 && status_code < 500)
			return RESULT_BAD_ADDRESS;
		else if (status_code >= 500 && status_code < 600)
			return RESULT_SERVER_ERROR;
		else
			return RESULT_UNKNOWN_ERROR;
	}

	public static GeoLocation getGeoLocationFromString(String location) {
		if (location == null) return null;
		String[] longlat = location.split(",");
		if (longlat == null || longlat.length != 2) return null;
		try {
			return new GeoLocation(Double.valueOf(longlat[0]), Double.valueOf(longlat[1]));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static URL[] getImageLinksForText(CharSequence text) {

		final Pattern pattern = Pattern.compile(IMAGE_URL_PATTERN);
		final Matcher matcher = pattern.matcher(text);
		final List<URL> image_links = new ArrayList<URL>();
		while (matcher.find()) {
			String link_string = matcher.group(1);
			if (link_string == null) {
				continue;
			}
			URL link = null;
			try {
				link = new URL(link_string);
			} catch (MalformedURLException e) {

			}
			if (link == null) {
				continue;
			}
			if (!image_links.contains(link)) {
				image_links.add(link);
			}
		}
		return image_links.toArray(new URL[image_links.size()]);
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

	public static long[] getLastSortIds(Context context, Uri uri) {
		long[] account_ids = getActivatedAccountIds(context);
		String[] cols = new String[] { Statuses.STATUS_ID };
		ContentResolver resolver = context.getContentResolver();
		long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (long account_id : account_ids) {
			String where = Statuses.ACCOUNT_ID + " = " + account_id;
			Cursor cur = resolver.query(uri, cols, where, null, Statuses.STATUS_ID);
			if (cur == null) {
				continue;
			}

			if (cur.getCount() > 0) {
				cur.moveToFirst();
				status_ids[idx] = cur.getLong(cur.getColumnIndexOrThrow(Statuses.STATUS_ID));
			}
			cur.close();
			idx++;
		}
		return status_ids;
	}

	public static String[] getMentionedNames(CharSequence user_name, CharSequence text, boolean at_sign,
			boolean include_author) {
		Pattern pattern = Pattern.compile("(?<!\\w)(@(\\w+))", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(text);
		List<String> mentions = new ArrayList<String>();

		if (include_author) {
			mentions.add((at_sign ? "@" : "") + user_name);
		}

		while (matcher.find()) {
			String mention = matcher.group(at_sign ? 1 : 2);
			if (mentions.contains(mention)) {
				continue;
			}
			mentions.add(mention);
		}
		return mentions.toArray(new String[mentions.size()]);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static String getNameForStatusId(Context context, long status_id) {

		String[] cols = new String[] { Statuses.NAME };
		String where = Statuses.STATUS_ID + " = " + status_id;

		for (Uri uri : STATUSES_URIS) {
			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.NAME));
				cur.close();
				return name;
			}
			cur.close();
		}
		return null;
	}

	public static String getQuoteStatus(Context context, String screen_name, String text) {
		String quote_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
				PREFERENCE_KEY_QUOTE_FORMAT, PREFERENCE_DEFAULT_QUOTE_FORMAT);
		if (isNullOrEmpty(quote_format)) {
			quote_format = PREFERENCE_DEFAULT_QUOTE_FORMAT;
		}
		return quote_format.replace(QUOTE_FORMAT_TEXT_PATTERN, '@' + screen_name + ':' + text);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static long getRetweetedByUserId(Context context, long status_id) {
		String[] cols = new String[] { Statuses.RETWEETED_BY_ID };
		String where = Statuses.STATUS_ID + "=" + status_id;

		for (Uri uri : STATUSES_URIS) {
			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				long retweeted_by_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEETED_BY_ID));
				cur.close();
				return retweeted_by_id;
			}
			cur.close();
		}
		return -1;
	}

	public static long getRetweetId(Context context, long status_id) {
		String[] cols = new String[] { Statuses.RETWEET_ID };
		String where = Statuses.STATUS_ID + "=" + status_id;
		for (Uri uri : STATUSES_URIS) {
			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				long retweet_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEET_ID));
				cur.close();
				return retweet_id;
			}
			cur.close();
		}
		return -1;
	}

	@Deprecated
	public static String getScreenNameForStatusId(Context context, long status_id) {
		String[] cols = new String[] { Statuses.SCREEN_NAME };
		String where = Statuses.STATUS_ID + " = " + status_id;

		for (Uri uri : STATUSES_URIS) {
			Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
				cur.close();
				return name;
			}
			cur.close();
		}
		return null;
	}

	public static String getSpannedStatusString(Spanned text) {
		if (text == null) return "";
		final CharSequence TAG_START = "<p>";
		final CharSequence TAG_END = "</p>";
		String formatted = Html.toHtml(text);
		if (formatted != null && formatted.contains(TAG_START) && formatted.contains(TAG_END)) {
			int start = formatted.indexOf(TAG_START.toString()) + TAG_START.length();
			int end = formatted.lastIndexOf(TAG_END.toString());
			return formatted.substring(start, end);
		}
		return formatted;
	}

	public static Spanned getSpannedStatusText(Status status, long account_id) {
		if (status == null || status.getText() == null) return new SpannableString("");
		SpannableString text = new SpannableString(status.getText());
		// Format links.
		URLEntity[] urls = status.getURLEntities();
		if (urls != null) {
			for (URLEntity url_entity : urls) {
				int start = url_entity.getStart();
				int end = url_entity.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				URL expanded_url = url_entity.getExpandedURL();
				URL url = url_entity.getURL();
				if (expanded_url != null) {
					text.setSpan(new URLSpan(expanded_url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (url != null) {
					text.setSpan(new URLSpan(url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		// Format media.
		MediaEntity[] media = status.getMediaEntities();
		if (media != null) {
			for (MediaEntity media_item : media) {
				int start = media_item.getStart();
				int end = media_item.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				URL media_url = media_item.getMediaURL();
				if (media_url != null) {
					text.setSpan(new URLSpan(media_url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		return text;
	}

	public static Spanned getSpannedTweetText(Tweet tweet, long account_id) {
		if (tweet == null || tweet.getText() == null) return new SpannableString("");
		SpannableString text = new SpannableString(tweet.getText());
		// Format links.
		URLEntity[] urls = tweet.getURLEntities();
		if (urls != null) {
			for (URLEntity url_entity : urls) {
				int start = url_entity.getStart();
				int end = url_entity.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				URL expanded_url = url_entity.getExpandedURL();
				URL url = url_entity.getURL();
				if (expanded_url != null) {
					text.setSpan(new URLSpan(expanded_url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else if (url != null) {
					text.setSpan(new URLSpan(url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		// Format media.
		MediaEntity[] media = tweet.getMediaEntities();
		if (media != null) {
			for (MediaEntity media_item : media) {
				int start = media_item.getStart();
				int end = media_item.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				URL media_url = media_item.getMediaURL();
				if (media_url != null) {
					text.setSpan(new URLSpan(media_url.toString()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		return text;
	}

	public static int getTableId(Uri uri) {
		return CONTENT_PROVIDER_URI_MATCHER.match(uri);
	}

	public static String getTableNameForContentUri(Uri uri) {
		switch (getTableId(uri)) {
			case URI_ACCOUNTS:
				return TABLE_ACCOUNTS;
			case URI_STATUSES:
				return TABLE_STATUSES;
			case URI_MENTIONS:
				return TABLE_MENTIONS;
			case URI_DRAFTS:
				return TABLE_DRAFTS;
			case URI_CACHED_USERS:
				return TABLE_CACHED_USERS;
			case URI_FILTERED_USERS:
				return TABLE_FILTERED_USERS;
			case URI_FILTERED_KEYWORDS:
				return TABLE_FILTERED_KEYWORDS;
			case URI_FILTERED_SOURCES:
				return TABLE_FILTERED_SOURCES;
			default:
				return null;
		}
	}

	public static Twitter getTwitterInstance(Context context, long account_id, boolean include_entities) {
		return getTwitterInstance(context, account_id, include_entities, true);
	}

	public static Twitter getTwitterInstance(Context context, long account_id, boolean include_entities,
			boolean include_rts) {
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
		final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
		final String consumer_key = preferences.getString(PREFERENCE_KEY_CONSUMER_KEY, CONSUMER_KEY);
		final String consumer_secret = preferences.getString(PREFERENCE_KEY_CONSUMER_SECRET, CONSUMER_SECRET);
		Twitter twitter = null;
		StringBuilder where = new StringBuilder();
		where.append(Accounts.USER_ID + "=" + account_id);
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS, where.toString(), null,
				null);
		if (cur != null) {
			if (cur.getCount() == 1) {
				cur.moveToFirst();
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setGZIPEnabled(enable_gzip_compressing);
				cb.setIgnoreSSLError(ignore_ssl_error);
				String rest_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.REST_BASE_URL));
				String search_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.SEARCH_BASE_URL));
				String upload_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.UPLOAD_BASE_URL));
				String oauth_access_token_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.OAUTH_ACCESS_TOKEN_URL));
				String oauth_authentication_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.OAUTH_AUTHENTICATION_URL));
				String oauth_authorization_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.OAUTH_AUTHORIZATION_URL));
				String oauth_request_token_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.OAUTH_REQUEST_TOKEN_URL));
				if (!isNullOrEmpty(rest_base_url)) {
					cb.setRestBaseURL(rest_base_url);
				}
				if (!isNullOrEmpty(search_base_url)) {
					cb.setSearchBaseURL(search_base_url);
				}
				if (!isNullOrEmpty(upload_base_url)) {
					// Do nothing.
				}
				if (!isNullOrEmpty(oauth_access_token_url)) {
					cb.setOAuthAccessTokenURL(oauth_access_token_url);
				}
				if (!isNullOrEmpty(oauth_authentication_url)) {
					cb.setOAuthAuthenticationURL(oauth_authentication_url);
				}
				if (!isNullOrEmpty(oauth_authorization_url)) {
					cb.setOAuthAuthorizationURL(oauth_authorization_url);
				}
				if (!isNullOrEmpty(oauth_request_token_url)) {
					cb.setOAuthRequestTokenURL(oauth_request_token_url);
				}
				cb.setIncludeEntitiesEnabled(include_entities);
				cb.setIncludeRTsEnabled(include_rts);

				switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
					case Accounts.AUTH_TYPE_OAUTH:
					case Accounts.AUTH_TYPE_XAUTH:
						if (isNullOrEmpty(consumer_key) || isNullOrEmpty(consumer_secret)) {
							cb.setOAuthConsumerKey(CONSUMER_KEY);
							cb.setOAuthConsumerSecret(CONSUMER_SECRET);
						} else {
							cb.setOAuthConsumerKey(consumer_key);
							cb.setOAuthConsumerSecret(consumer_secret);
						}
						twitter = new TwitterFactory(cb.build()).getInstance(new AccessToken(cur.getString(cur
								.getColumnIndexOrThrow(Accounts.OAUTH_TOKEN)), cur.getString(cur
								.getColumnIndexOrThrow(Accounts.TOKEN_SECRET))));
						break;
					case Accounts.AUTH_TYPE_BASIC:
						twitter = new TwitterFactory(cb.build()).getInstance(new BasicAuthorization(cur.getString(cur
								.getColumnIndexOrThrow(Accounts.USERNAME)), cur.getString(cur
								.getColumnIndexOrThrow(Accounts.BASIC_AUTH_PASSWORD))));
						break;
					case Accounts.AUTH_TYPE_TWIP_O_MODE:
						twitter = new TwitterFactory(cb.build()).getInstance(new TwipOModeAuthorization());
						break;
					default:
				}
			}
			cur.close();
		}
		return twitter;
	}

	public static Twitter getTwitterInstance(Context context, String account_username, boolean include_entities) {
		return getTwitterInstance(context, account_username, include_entities, true);
	}

	public static Twitter getTwitterInstance(Context context, String account_username, boolean include_entities,
			boolean include_rts) {
		final StringBuilder where = new StringBuilder();
		where.append(Accounts.USERNAME + " = " + account_username);
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_ID },
				where.toString(), null, null);
		long account_id = -1;
		if (cur != null) {
			if (cur.getCount() == 1) {
				cur.moveToFirst();
				account_id = cur.getLong(cur.getColumnIndex(Accounts.USER_ID));
			}
			cur.close();
		}
		if (account_id != -1) return getTwitterInstance(context, account_id, include_entities, include_rts);
		return null;
	}

	public static Drawable getTypeIcon(Context context, boolean is_fav, boolean has_location, boolean has_media) {
		Resources res = context.getResources();
		if (is_fav) {
			if (ICON_STARRED == null) {
				ICON_STARRED = res.getDrawable(R.drawable.ic_tweet_stat_starred);
			}
			return ICON_STARRED;
		} else if (has_media) {
			if (ICON_HAS_MEDIA == null) {
				ICON_HAS_MEDIA = res.getDrawable(R.drawable.ic_tweet_stat_has_media);
			}
			return ICON_HAS_MEDIA;
		} else if (has_location) {
			if (ICON_HAS_LOCATION == null) {
				ICON_HAS_LOCATION = res.getDrawable(R.drawable.ic_tweet_stat_has_location);
			}
			return ICON_HAS_LOCATION;
		}
		return null;
	}

	public static boolean isMyAccount(Context context, long account_id) {
		for (long id : getAccountIds(context)) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isMyActivatedAccount(Context context, long account_id) {
		if (account_id <= 0) return false;
		for (long id : getActivatedAccountIds(context)) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isMyActivatedUserName(Context context, String screen_name) {
		if (screen_name == null) return false;
		for (String account_user_name : getActivatedAccountScreenNames(context)) {
			if (account_user_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isMyRetweet(Context context, long account_id, long status_id) {
		return account_id == getRetweetedByUserId(context, status_id);
	}

	public static boolean isMyUserName(Context context, String screen_name) {
		for (String account_screen_name : getAccountScreenNames(context)) {
			if (account_screen_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isNullOrEmpty(CharSequence text) {
		return text == null || "".equals(text);
	}

	public static boolean isUserLoggedIn(Context context, long account_id) {
		long[] ids = getAccountIds(context);
		if (ids == null) return false;
		for (long id : ids) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static ContentValues makeAccountContentValues(int color, AccessToken access_token, User user,
			String rest_api_base, String search_api_base, String basic_password, int auth_type) {
		if (user == null) throw new IllegalArgumentException("User can't be null!");
		ContentValues values = new ContentValues();
		switch (auth_type) {
			case Accounts.AUTH_TYPE_TWIP_O_MODE: {
				break;
			}
			case Accounts.AUTH_TYPE_BASIC: {
				if (basic_password == null)
					throw new IllegalArgumentException("Password can't be null in Basic mode!");
				values.put(Accounts.BASIC_AUTH_PASSWORD, basic_password);
				break;
			}
			case Accounts.AUTH_TYPE_OAUTH:
			case Accounts.AUTH_TYPE_XAUTH: {
				if (access_token == null)
					throw new IllegalArgumentException("Access Token can't be null in OAuth/xAuth mode!");
				if (user.getId() != access_token.getUserId())
					throw new IllegalArgumentException("User and Access Token not match!");
				values.put(Accounts.OAUTH_TOKEN, access_token.getToken());
				values.put(Accounts.TOKEN_SECRET, access_token.getTokenSecret());
				break;
			}
		}
		values.put(Accounts.AUTH_TYPE, auth_type);
		values.put(Accounts.USER_ID, user.getId());
		values.put(Accounts.USERNAME, user.getScreenName());
		values.put(Accounts.PROFILE_IMAGE_URL, user.getProfileImageUrlHttps().toString());
		values.put(Accounts.USER_COLOR, color);
		values.put(Accounts.IS_ACTIVATED, 1);
		if (rest_api_base != null) {
			values.put(Accounts.REST_BASE_URL, rest_api_base);
		}
		if (search_api_base != null) {
			values.put(Accounts.SEARCH_BASE_URL, search_api_base);
		}

		return values;
	}

	public static ContentValues makeCachedUsersContentValues(User user) {
		ContentValues values = new ContentValues();
		values.put(CachedUsers.NAME, user.getName());
		values.put(CachedUsers.PROFILE_IMAGE_URL, user.getProfileImageUrlHttps().toString());
		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
		values.put(CachedUsers.USER_ID, user.getId());

		return values;
	}

	public static ContentValues makeMessagesContentValues(DirectMessage message, long account_id) {
		ContentValues values = new ContentValues();
		values.put(Messages.ACCOUNT_ID, account_id);
		values.put(Messages.TEXT, message.getText());
		values.put(Messages.STATE, message.getSenderId() == account_id ? Messages.STATE_OUTGOING
				: Messages.STATE_INCOMING);
		return values;
	}

	public static ContentValues makeStatusesContentValues(Status status, long account_id) {
		if (status == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Statuses.STATUS_ID, status.getId());
		final int is_retweet = status.isRetweet() ? 1 : 0;
		final Status retweeted_status = status.getRetweetedStatus();
		if (is_retweet == 1 && retweeted_status != null) {
			final User retweet_user = status.getUser();
			values.put(Statuses.RETWEET_ID, status.getId());
			values.put(Statuses.RETWEETED_BY_ID, retweet_user.getId());
			values.put(Statuses.RETWEETED_BY_NAME, retweet_user.getName());
			values.put(Statuses.RETWEETED_BY_SCREEN_NAME, retweet_user.getScreenName());
			status = retweeted_status;
		}
		final User user = status.getUser();
		if (user != null) {
			final long user_id = user.getId();
			final String profile_image_url = user.getProfileImageUrlHttps().toString();
			final String name = user.getName(), screen_name = user.getScreenName();
			values.put(Statuses.USER_ID, user_id);
			values.put(Statuses.NAME, name);
			values.put(Statuses.SCREEN_NAME, screen_name);
			values.put(Statuses.IS_PROTECTED, user.isProtected() ? 1 : 0);
			values.put(Statuses.PROFILE_IMAGE_URL, profile_image_url);
		}
		final MediaEntity[] medias = status.getMediaEntities();
		values.put(Statuses.ACCOUNT_ID, account_id);
		if (status.getCreatedAt() != null) {
			values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
		}
		values.put(Statuses.TEXT, getSpannedStatusString(getSpannedStatusText(status, account_id)));
		values.put(Statuses.TEXT_PLAIN, status.getText());
		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
		values.put(Statuses.IN_REPLY_TO_SCREEN_NAME, status.getInReplyToScreenName());
		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
		values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
		values.put(Statuses.SOURCE, status.getSource());
		values.put(Statuses.LOCATION, formatGeoLocationToString(status.getGeoLocation()));
		values.put(Statuses.IS_RETWEET, is_retweet);
		values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);
		values.put(Statuses.HAS_MEDIA, medias != null && medias.length > 0 ? 1 : 0);
		return values;
	}

	public static void notifyForUpdatedUri(Context context, Uri uri) {
		switch (getTableId(uri)) {
			case URI_STATUSES: {
				context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED).putExtra(INTENT_KEY_SUCCEED,
						true));
				break;
			}
			case URI_MENTIONS: {
				context.sendBroadcast(new Intent(BROADCAST_MENTIONS_DATABASE_UPDATED)
						.putExtra(INTENT_KEY_SUCCEED, true));
				break;
			}
			default: {
				return;
			}
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	public static URL parseURL(String url_string) {
		try {
			return new URL(url_string);
		} catch (MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}

	public static void restartActivity(Activity activity, boolean animation) {
		int enter_anim = animation ? android.R.anim.fade_in : 0;
		int exit_anim = animation ? android.R.anim.fade_out : 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			new MethodsCompat().overridePendingTransition(activity, enter_anim, exit_anim);
		} else {
			activity.getWindow().setWindowAnimations(0);
		}
		activity.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			new MethodsCompat().overridePendingTransition(activity, enter_anim, exit_anim);
		} else {
			activity.getWindow().setWindowAnimations(0);
		}
		activity.startActivity(activity.getIntent());
	}

	public static void setMenuForStatus(Context context, Menu menu, ParcelableStatus status) {
		if (status == null) return;
		int activated_color = context.getResources().getColor(R.color.holo_blue_bright);
		menu.findItem(R.id.delete_submenu).setVisible(isMyActivatedAccount(context, status.user_id));
		MenuItem itemRetweet = menu.findItem(MENU_RETWEET);
		if (itemRetweet != null) {
			itemRetweet.setVisible(!status.is_protected
					&& (!isMyActivatedAccount(context, status.user_id) || getActivatedAccountIds(context).length > 1));
			Drawable iconRetweetSubMenu = menu.findItem(R.id.retweet_submenu).getIcon();
			if (isMyActivatedAccount(context, status.retweeted_by_id)) {
				iconRetweetSubMenu.setColorFilter(activated_color, Mode.MULTIPLY);
				itemRetweet.setTitle(R.string.cancel_retweet);
			} else {
				iconRetweetSubMenu.clearColorFilter();
				itemRetweet.setTitle(R.string.retweet);
			}
		}
		MenuItem itemFav = menu.findItem(MENU_FAV);
		if (itemFav != null) {
			Drawable iconFav = itemFav.getIcon();
			if (status.is_favorite) {
				iconFav.setColorFilter(activated_color, Mode.MULTIPLY);
				itemFav.setTitle(R.string.unfav);
			} else {
				iconFav.clearColorFilter();
				itemFav.setTitle(R.string.fav);
			}
		}
	}

	public static void setWindowUiOptions(Window window, int uiOptions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			new MethodsCompat().setUiOptions(window, uiOptions);
		}
	}

	public static void showErrorToast(Context context, Exception e, boolean long_message) {
		String message = e != null ? context.getString(R.string.error_message, e.getMessage()) : context
				.getString(R.string.error_unknown_error);
		int length = long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, message, length);
		toast.show();
	}

	public static void showErrorToast(Context context, long account_id, String operation, Exception e,
			boolean long_message) {
		String message = e != null ? context.getString(R.string.error_message, e.getMessage()) : context
				.getString(R.string.error_unknown_error);
		int length = long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(context, message, length);
		toast.show();
	}

}