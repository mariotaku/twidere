/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.UserBlocksFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserFollowersFragment;
import org.mariotaku.twidere.fragment.UserFriendsFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.fragment.ViewConversationFragment;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.StatusesCursorIndices;

public final class Utils implements Constants {

	private static UriMatcher CONTENT_PROVIDER_URI_MATCHER;

	private static final HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new HostnameVerifier() {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	} };

	private static final SSLSocketFactory IGNORE_ERROR_SSL_FACTORY;

	static {
		SSLSocketFactory factory = null;
		try {
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
			factory = sc.getSocketFactory();
		} catch (final KeyManagementException e) {
		} catch (final NoSuchAlgorithmException e) {
		}
		IGNORE_ERROR_SSL_FACTORY = factory;
	}

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

	private Utils() {

	}

	public static String buildActivatedStatsWhereClause(Context context, String selection) {
		final long[] account_ids = getActivatedAccountIds(context);
		final StringBuilder builder = new StringBuilder();
		if (selection != null) {
			builder.append(selection);
			builder.append(" AND ");
		}

		builder.append(Statuses.ACCOUNT_ID + " IN ( ");
		builder.append(ArrayUtils.buildString(account_ids, ',', true));
		builder.append(" )");

		return builder.toString();
	}

	public static String buildFilterWhereClause(String table, String selection) {
		final StringBuilder builder = new StringBuilder();
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
		final Uri.Builder uribuilder = uri.buildUpon();
		uribuilder.appendQueryParameter(QUERY_PARAM_NOTIFY, String.valueOf(notify));
		return uribuilder.build();
	}

	public static synchronized void cleanDatabasesByItemLimit(Context context) {
		final ContentResolver resolver = context.getContentResolver();
		final int item_limit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);

		for (final long account_id : getAccountIds(context)) {
			// Clean statuses.
			for (final Uri uri : STATUSES_URIS) {
				final String table = getTableNameForContentUri(uri);
				final StringBuilder where = new StringBuilder();
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
			final Uri uri = CachedUsers.CONTENT_URI;
			final String table = getTableNameForContentUri(uri);
			final StringBuilder where = new StringBuilder();
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
		final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = "
				+ status_id;
		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = resolver.query(uri, Statuses.COLUMNS, where, null, null);
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

	public static String formatStatusText(Status status) {
		if (status == null) return null;
		final String text = status.getText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, DEBUG);
		final URLEntity[] urls = status.getURLEntities();
		if (urls != null) {
			for (final URLEntity url_entity : urls) {
				final int start = url_entity.getStart();
				final int end = url_entity.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				final URL expanded_url = url_entity.getExpandedURL();
				if (expanded_url != null) {
					builder.addLink(expanded_url.toString(), start, end);
				}
			}
		}
		// Format media.
		final MediaEntity[] medias = status.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity media_item : medias) {
				final int start = media_item.getStart();
				final int end = media_item.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				final URL media_url = media_item.getMediaURL();
				if (media_url != null) {
					builder.addLink(media_url.toString(), start, end);
				}
			}
		}
		return builder.build();
	}

	public static String formatTimeStampString(Context context, long timestamp) {
		final Time then = new Time();
		then.set(timestamp);
		final Time now = new Time();
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
		if (context == null) return null;
		final Time then = new Time();
		then.set(timestamp);
		final Time now = new Time();
		now.setToNow();

		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

		format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

		return DateUtils.formatDateTime(context, timestamp, format_flags);
	}

	public static String formatToShortTimeString(Context context, long timestamp) {
		if (context == null) return null;
		final Resources res = context.getResources();
		final Time then = new Time(), now = new Time();
		then.set(timestamp);
		now.setToNow();
		if (then.before(now)) {

			final int year_diff = now.year - then.year;

			final int month_diff = (year_diff > 0 ? 12 : 0) + now.month - then.month;
			if (year_diff < 1) {
				final int day_diff = (month_diff > 0 ? then.getActualMaximum(Time.MONTH_DAY) : 0) + now.monthDay
						- then.monthDay;
				if (month_diff < 1) {
					if (day_diff >= then.getActualMaximum(Time.MONTH_DAY))
						return res.getQuantityString(R.plurals.Nmonths, month_diff, month_diff);
					final int hour_diff = (day_diff > 0 ? 24 : 0) + now.hour - then.hour;
					if (day_diff < 1) {
						if (hour_diff >= 24) return res.getQuantityString(R.plurals.Ndays, day_diff, day_diff);
						final int minute_diff = (hour_diff > 0 ? 60 : 0) + now.minute - then.minute;
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

	public static String formatTweetText(Tweet tweet) {
		if (tweet == null) return null;
		final String text = tweet.getText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, DEBUG);
		final URLEntity[] urls = tweet.getURLEntities();
		if (urls != null) {
			for (final URLEntity url_entity : urls) {
				final int start = url_entity.getStart();
				final int end = url_entity.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				final URL expanded_url = url_entity.getExpandedURL();
				if (expanded_url != null) {
					builder.addLink(expanded_url.toString(), start, end);
				}
			}
		}
		// Format media.
		final MediaEntity[] medias = tweet.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity media_item : medias) {
				final int start = media_item.getStart();
				final int end = media_item.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				final URL media_url = media_item.getMediaURL();
				if (media_url != null) {
					builder.addLink(media_url.toString(), start, end);
				}
			}
		}
		return builder.build();
	}

	public static int getAccountColor(Context context, long account_id) {

		Integer color = sAccountColors.get(account_id);
		if (color == null) {
			final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.USER_COLOR }, Accounts.USER_ID + "=" + account_id, null, null);
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

		final String[] cols = new String[] { Statuses.ACCOUNT_ID };
		final String where = Statuses.STATUS_ID + " = " + status_id;

		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				final long id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.ACCOUNT_ID));
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
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.USER_ID },
				null, null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
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
		final String[] cols = new String[] { Accounts.USERNAME };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, null, null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.USERNAME);
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
		final String[] cols = new String[] { Accounts.USER_ID };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
				null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
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
		final String[] cols = new String[] { Accounts.USERNAME };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
				null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.USERNAME);
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

		final float density = context.getResources().getDisplayMetrics().density;
		final int width = (int) (32 * density), height = (int) (32 * density);

		final Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		final Canvas canvas = new Canvas(bm);

		final int rectrangle_size = (int) (density * 5);
		final int numRectanglesHorizontal = (int) Math.ceil(width / rectrangle_size);
		final int numRectanglesVertical = (int) Math.ceil(height / rectrangle_size);
		final Rect r = new Rect();
		boolean verticalStartWhite = true;
		for (int i = 0; i <= numRectanglesVertical; i++) {

			boolean isWhite = verticalStartWhite;
			for (int j = 0; j <= numRectanglesHorizontal; j++) {

				r.top = i * rectrangle_size;
				r.left = j * rectrangle_size;
				r.bottom = r.top + rectrangle_size;
				r.right = r.left + rectrangle_size;
				final Paint paint = new Paint();
				paint.setColor(isWhite ? Color.WHITE : Color.GRAY);

				canvas.drawRect(r, paint);

				isWhite = !isWhite;
			}

			verticalStartWhite = !verticalStartWhite;

		}
		canvas.drawColor(color);
		final Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(2.0f);
		final float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0, height,
				width, height };
		canvas.drawLines(points, paint);

		return bm;
	}

	public static long getDefaultAccountId(Context context) {
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		return preferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
	}

	public static Twitter getDefaultTwitterInstance(Context context, boolean include_entities) {
		return getDefaultTwitterInstance(context, include_entities, true);
	}

	public static Twitter getDefaultTwitterInstance(Context context, boolean include_entities, boolean include_rts) {
		return getTwitterInstance(context, getDefaultAccountId(context), include_entities, include_rts);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static int getErrorCode(TwitterException e) {
		if (e == null) return RESULT_UNKNOWN_ERROR;
		final int status_code = e.getStatusCode();
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
		final String[] longlat = location.split(",");
		if (longlat == null || longlat.length != 2) return null;
		try {
			return new GeoLocation(Double.valueOf(longlat[0]), Double.valueOf(longlat[1]));
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static URL[] getImageLinksForText(CharSequence text) {

		final Pattern pattern = Pattern.compile(IMAGE_URL_PATTERN);
		final Matcher matcher = pattern.matcher(text);
		final List<URL> image_links = new ArrayList<URL>();
		while (matcher.find()) {
			final String link_string = matcher.group(1);
			if (link_string == null) {
				continue;
			}
			URL link = null;
			try {
				link = new URL(link_string);
			} catch (final MalformedURLException e) {

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

		final String media_uri_start = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString();

		if (uri.toString().startsWith(media_uri_start)) {

			final String[] proj = { MediaStore.Images.Media.DATA };
			final Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);

			if (cursor == null || cursor.getCount() <= 0) return null;

			final int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

			cursor.moveToFirst();

			final String path = cursor.getString(column_index);
			cursor.close();
			return path;
		} else if (uri.getScheme().equals("file")) return uri.getPath();
		return null;
	}

	public static long[] getLastSortIds(Context context, Uri uri) {
		final long[] account_ids = getActivatedAccountIds(context);
		final String[] cols = new String[] { Statuses.STATUS_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = resolver.query(uri, cols, where, null, Statuses.STATUS_ID);
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

	/**
	 * @deprecated
	 */
	@Deprecated
	public static String getNameForStatusId(Context context, long status_id) {

		final String[] cols = new String[] { Statuses.NAME };
		final String where = Statuses.STATUS_ID + " = " + status_id;

		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				final String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.NAME));
				cur.close();
				return name;
			}
			cur.close();
		}
		return null;
	}

	public static Proxy getProxy(Context context) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean enable_proxy = prefs.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		if (!enable_proxy) return Proxy.NO_PROXY;
		final String proxy_host = prefs.getString(PREFERENCE_KEY_PROXY_HOST, null);
		final int proxy_port = parseInt(prefs.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
		if (!isNullOrEmpty(proxy_host) && proxy_port > 0) {
			final SocketAddress addr = InetSocketAddress.createUnresolved(proxy_host, proxy_port);
			return new Proxy(Proxy.Type.HTTP, addr);
		}
		return Proxy.NO_PROXY;
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
		final String[] cols = new String[] { Statuses.RETWEETED_BY_ID };
		final String where = Statuses.STATUS_ID + "=" + status_id;

		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				final long retweeted_by_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEETED_BY_ID));
				cur.close();
				return retweeted_by_id;
			}
			cur.close();
		}
		return -1;
	}

	public static long getRetweetId(Context context, long status_id) {
		final String[] cols = new String[] { Statuses.RETWEET_ID };
		final String where = Statuses.STATUS_ID + "=" + status_id;
		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				final long retweet_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.RETWEET_ID));
				cur.close();
				return retweet_id;
			}
			cur.close();
		}
		return -1;
	}

	@Deprecated
	public static String getScreenNameForStatusId(Context context, long status_id) {
		final String[] cols = new String[] { Statuses.SCREEN_NAME };
		final String where = Statuses.STATUS_ID + " = " + status_id;

		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = context.getContentResolver().query(uri, cols, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				final String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
				cur.close();
				return name;
			}
			cur.close();
		}
		return null;
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
		final boolean enable_proxy = preferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		final String consumer_key = preferences.getString(PREFERENCE_KEY_CONSUMER_KEY, CONSUMER_KEY);
		final String consumer_secret = preferences.getString(PREFERENCE_KEY_CONSUMER_SECRET, CONSUMER_SECRET);

		Twitter twitter = null;
		final StringBuilder where = new StringBuilder();
		where.append(Accounts.USER_ID + "=" + account_id);
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS, where.toString(),
				null, null);
		if (cur != null) {
			if (cur.getCount() == 1) {
				cur.moveToFirst();
				final ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setGZIPEnabled(enable_gzip_compressing);
				cb.setIgnoreSSLError(ignore_ssl_error);
				if (enable_proxy) {
					final String proxy_host = preferences.getString(PREFERENCE_KEY_PROXY_HOST, null);
					final int proxy_port = parseInt(preferences.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
					if (!isNullOrEmpty(proxy_host) && proxy_port > 0) {
						cb.setHttpProxyHost(proxy_host);
						cb.setHttpProxyPort(proxy_port);
					}

				}
				final String rest_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.REST_BASE_URL));
				final String signing_rest_base_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.SIGNING_REST_BASE_URL));
				final String search_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.SEARCH_BASE_URL));
				final String upload_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.UPLOAD_BASE_URL));
				final String oauth_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.OAUTH_BASE_URL));
				final String signing_oauth_base_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.SIGNING_OAUTH_BASE_URL));
				if (!isNullOrEmpty(rest_base_url)) {
					cb.setRestBaseURL(rest_base_url);
				}
				if (!isNullOrEmpty(search_base_url)) {
					cb.setSearchBaseURL(search_base_url);
				}
				if (!isNullOrEmpty(upload_base_url)) {
					cb.setUploadBaseURL(upload_base_url);
				}
				if (!isNullOrEmpty(signing_rest_base_url)) {
					cb.setSigningRestBaseURL(signing_rest_base_url);
				}
				if (!isNullOrEmpty(oauth_base_url)) {
					cb.setOAuthBaseURL(oauth_base_url);
				}
				if (!isNullOrEmpty(signing_oauth_base_url)) {
					cb.setSigningOAuthBaseURL(signing_oauth_base_url);
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

	public static int getTypeIcon(boolean is_fav, boolean has_location, boolean has_media) {
		if (is_fav)
			return R.drawable.ic_tweet_stat_starred;
		else if (has_media)
			return R.drawable.ic_tweet_stat_has_media;
		else if (has_location) return R.drawable.ic_tweet_stat_has_location;
		return 0;
	}

	public static boolean isMyAccount(Context context, long account_id) {
		for (final long id : getAccountIds(context)) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isMyActivatedAccount(Context context, long account_id) {
		if (account_id <= 0) return false;
		for (final long id : getActivatedAccountIds(context)) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isMyActivatedUserName(Context context, String screen_name) {
		if (screen_name == null) return false;
		for (final String account_user_name : getActivatedAccountScreenNames(context)) {
			if (account_user_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isMyRetweet(Context context, long account_id, long status_id) {
		return account_id == getRetweetedByUserId(context, status_id);
	}

	public static boolean isMyUserName(Context context, String screen_name) {
		for (final String account_screen_name : getAccountScreenNames(context)) {
			if (account_screen_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isNullOrEmpty(CharSequence text) {
		return text == null || "".equals(text);
	}

	public static boolean isUserLoggedIn(Context context, long account_id) {
		final long[] ids = getAccountIds(context);
		if (ids == null) return false;
		for (final long id : ids) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static ContentValues makeAccountContentValues(int color, AccessToken access_token, User user,
			String rest_api_base, String search_api_base, String basic_password, int auth_type) {
		if (user == null) throw new IllegalArgumentException("User can't be null!");
		final ContentValues values = new ContentValues();
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
		values.put(Accounts.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
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
		final ContentValues values = new ContentValues();
		values.put(CachedUsers.NAME, user.getName());
		values.put(CachedUsers.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
		values.put(CachedUsers.USER_ID, user.getId());

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
			final String profile_image_url = user.getProfileImageURL().toString();
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
		values.put(Statuses.TEXT, formatStatusText(status));
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

	public static void openConversation(Activity activity, long account_id, long status_id) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new ViewConversationFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_STATUS_ID, status_id);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_CONVERSATION);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openTweetSearch(Activity activity, long account_id, String query) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new SearchTweetsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (query != null) {
				args.putString(INTENT_KEY_QUERY, query);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_SEARCH);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_TYPE, QUERY_PARAM_VALUE_TWEETS);
			if (query != null) {
				builder.appendQueryParameter(QUERY_PARAM_QUERY, query);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserBlocks(Activity activity, long account_id) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserBlocksFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_BLOCKS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserFavorites(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserFavoritesFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id != -1) {
				args.putLong(INTENT_KEY_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_FAVORITES);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id != -1) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static void openUserFollowers(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserFollowersFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id != -1) {
				args.putLong(INTENT_KEY_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_FOLLOWERS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id != -1) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static void openUserFollowing(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserFriendsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id != -1) {
				args.putLong(INTENT_KEY_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_FOLLOWING);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id != -1) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static void openUserProfile(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserProfileFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id != -1) {
				args.putLong(INTENT_KEY_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_RIGHT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id != -1) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserTimeline(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserTimelineFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id != -1) {
				args.putLong(INTENT_KEY_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_TIMELINE);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id != -1) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static int parseInt(String string) {
		try {
			return Integer.valueOf(string);
		} catch (final NumberFormatException e) {
			return -1;
		}
	}

	public static long parseLong(String source) {
		if (source == null) return -1;
		try {
			return Long.parseLong(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static URL parseURL(String url_string) {
		try {
			return new URL(url_string);
		} catch (final MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}

	public static void restartActivity(Activity activity, boolean animation) {
		final int enter_anim = animation ? android.R.anim.fade_in : 0;
		final int exit_anim = animation ? android.R.anim.fade_out : 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			OverridePendingTransitionAccessor.overridePendingTransition(activity, enter_anim, exit_anim);
		} else {
			activity.getWindow().setWindowAnimations(0);
		}
		activity.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			OverridePendingTransitionAccessor.overridePendingTransition(activity, enter_anim, exit_anim);
		} else {
			activity.getWindow().setWindowAnimations(0);
		}
		activity.startActivity(activity.getIntent());
	}

	public static void setIgnoreSSLError(URLConnection conn) {
		if (conn instanceof HttpsURLConnection) {
			((HttpsURLConnection) conn).setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
			if (IGNORE_ERROR_SSL_FACTORY != null) {
				((HttpsURLConnection) conn).setSSLSocketFactory(IGNORE_ERROR_SSL_FACTORY);
			}
		}
	}

	public static void setMenuForStatus(Context context, Menu menu, ParcelableStatus status) {
		if (context == null || menu == null || status == null) return;
		final int activated_color = context.getResources().getColor(R.color.holo_blue_bright);
		menu.findItem(R.id.delete_submenu).setVisible(isMyActivatedAccount(context, status.user_id));
		final MenuItem itemRetweet = menu.findItem(MENU_RETWEET);
		if (itemRetweet != null) {
			itemRetweet.setVisible(!status.is_protected
					&& (!isMyActivatedAccount(context, status.user_id) || getActivatedAccountIds(context).length > 1));
			final Drawable iconRetweetSubMenu = menu.findItem(R.id.retweet_submenu).getIcon();
			if (isMyActivatedAccount(context, status.retweeted_by_id)) {
				iconRetweetSubMenu.setColorFilter(activated_color, Mode.MULTIPLY);
				itemRetweet.setTitle(R.string.cancel_retweet);
			} else {
				iconRetweetSubMenu.clearColorFilter();
				itemRetweet.setTitle(R.string.retweet);
			}
		}
		final MenuItem itemFav = menu.findItem(MENU_FAV);
		if (itemFav != null) {
			final Drawable iconFav = itemFav.getIcon();
			if (status.is_favorite) {
				iconFav.setColorFilter(activated_color, Mode.MULTIPLY);
				itemFav.setTitle(R.string.unfav);
			} else {
				iconFav.clearColorFilter();
				itemFav.setTitle(R.string.fav);
			}
		}
	}

	public static void showErrorToast(Context context, Exception e, boolean long_message) {
		final String message = e != null ? context.getString(R.string.error_message, e.getLocalizedMessage()) : context
				.getString(R.string.error_unknown_error);
		final int length = long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(context, message, length);
		toast.show();
	}

}
