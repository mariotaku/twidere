package org.mariotaku.twidere.util;

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
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.service.UpdateService;

import twitter4j.GeoLocation;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CommonUtils implements Constants {

	private Context mContext;
	private HashMap<Context, ServiceBinder> mConnectionMap = new HashMap<Context, ServiceBinder>();

	private static UriMatcher URI_MATCHER;

	static {
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES, URI_STATUSES);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_ACCOUNTS, URI_ACCOUNTS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_MENTIONS, URI_MENTIONS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FAVORITES, URI_FAVORITES);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_USERS, URI_CACHED_USERS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_USERS, URI_FILTERED_USERS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_KEYWORDS, URI_FILTERED_KEYWORDS);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_SOURCES, URI_FILTERED_SOURCES);
		URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES + "/*", URI_USER_TIMELINE);
	}

	private static HashMap<Long, Integer> sAccountColors = new HashMap<Long, Integer>();

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

	public Bitmap getColorPreviewBitmap(int color) {
		return getColorPreviewBitmap(mContext, color);
	}

	public String getImagePathFromUri(Uri uri) {
		return getImagePathFromUri(mContext, uri);
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

	public static String buildActivatedStatsWhereClause(Context context, String selection) {
		long[] account_ids = getActivatedAccounts(context);
		if (account_ids.length <= 0) return null;
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
		builder.append(" WHERE " + table + "." + Statuses.SCREEN_NAME + " IN ( SELECT "
				+ TABLE_FILTERED_USERS + "." + Filters.Users.TEXT + " FROM " + TABLE_FILTERED_USERS
				+ " )");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table);
		builder.append(" WHERE " + table + "." + Statuses.NAME + " IN ( SELECT "
				+ TABLE_FILTERED_USERS + "." + Filters.Users.TEXT + " FROM " + TABLE_FILTERED_USERS
				+ " )");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
				+ TABLE_FILTERED_SOURCES);
		builder.append(" WHERE " + table + "." + Statuses.SOURCE + " LIKE '%'||"
				+ TABLE_FILTERED_SOURCES + "." + Filters.Sources.TEXT + "||'%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
				+ TABLE_FILTERED_KEYWORDS);
		builder.append(" WHERE " + table + "." + Statuses.TEXT + " LIKE '%'||"
				+ TABLE_FILTERED_KEYWORDS + "." + Filters.Keywords.TEXT + "||'%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" )");

		return builder.toString();
	}

	public static void clearAccountColor() {
		sAccountColors.clear();
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

	public static String formatStatusString(Status status) {
		final CharSequence TAG_START = "<p>";
		final CharSequence TAG_END = "</p>";
		if (status == null || status.getText() == null) return "";
		SpannableString text = new SpannableString(status.getText());
		// Format links.
		URLEntity[] urls = status.getURLEntities();
		if (urls != null) {
			for (URLEntity url : urls) {
				int start = url.getStart();
				int end = url.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				URL expanded_url = url.getExpandedURL();
				if (expanded_url != null) {
					text.setSpan(new URLSpan(expanded_url.toString()), start, end,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		// Format mentioned users.
		UserMentionEntity[] mentions = status.getUserMentionEntities();
		if (mentions != null) {
			for (UserMentionEntity mention : mentions) {
				int start = mention.getStart();
				int end = mention.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				String link = "https://twitter.com/#!/" + mention.getScreenName();
				text.setSpan(new URLSpan(link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		// Format hashtags.
		HashtagEntity[] hashtags = status.getHashtagEntities();
		if (hashtags != null) {
			for (HashtagEntity hashtag : hashtags) {
				int start = hashtag.getStart();
				int end = hashtag.getEnd();
				if (start < 0 || end > text.length()) {
					continue;
				}
				String link = "https://twitter.com/search/#" + hashtag.getText();
				text.setSpan(new URLSpan(link), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
					text.setSpan(new URLSpan(media_url.toString()), start, end,
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		String formatted = Html.toHtml(text);
		if (formatted != null && formatted.contains(TAG_START) && formatted.contains(TAG_END)) {
			int start = formatted.indexOf(TAG_START.toString()) + TAG_START.length();
			int end = formatted.lastIndexOf(TAG_END.toString());
			return formatted.substring(start, end);
		}
		return formatted;
	}

	public static String formatToLongTimeString(Context context, long timestamp) {
		Time then = new Time();
		then.set(timestamp);
		Time now = new Time();
		now.setToNow();

		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL
				| DateUtils.FORMAT_CAP_AMPM;

		format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

		return DateUtils.formatDateTime(context, timestamp, format_flags);
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

		Integer color = sAccountColors.get(account_id);
		if (color == null) {
			Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.USER_COLOR }, Accounts.USER_ID + "=" + account_id,
					null, null);
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

	public static long[] getAccounts(Context context) {
		long[] accounts = new long[] {};
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
				new String[] { Accounts.USER_ID }, null, null, null);
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
		float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0,
				height, width, height };
		canvas.drawLines(points, paint);

		return bm;
	}

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

	public static String[] getMentionedNames(CharSequence text, boolean at_sign) {
		Pattern pattern = Pattern.compile("(?<!\\w)(@(\\w+))", Pattern.MULTILINE);
		Matcher matcher = pattern.matcher(text);
		List<String> mentions = new ArrayList<String>();

		while (matcher.find()) {
			String mention = matcher.group(at_sign ? 1 : 2);
			if (mentions.contains(mention)) {
				continue;
			}
			mentions.add(mention);
		}
		return mentions.toArray(new String[mentions.size()]);
	}

	public static int getTableId(Uri uri) {
		return URI_MATCHER.match(uri);
	}

	public static String getTableNameForContentUri(Uri uri) {
		switch (CommonUtils.getTableId(uri)) {
			case URI_STATUSES:
				return TABLE_STATUSES;
			case URI_ACCOUNTS:
				return TABLE_ACCOUNTS;
			case URI_MENTIONS:
				return TABLE_MENTIONS;
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

	public static Twitter getTwitterInstance(Context context, long account_id) {
		final SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		final boolean enable_gzip_compressing = preferences.getBoolean(
				PREFERENCE_KEY_GZIP_COMPRESSING, false);
		Twitter twitter = null;
		StringBuilder where = new StringBuilder();
		where.append(Accounts.USER_ID + "=" + account_id);
		Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS,
				where.toString(), null, null);
		if (cur != null) {
			if (cur.getCount() == 1) {
				cur.moveToFirst();
				ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setGZIPEnabled(enable_gzip_compressing);
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

	public static boolean isUserLoggedIn(Context context, long account_id) {
		long[] ids = getAccounts(context);
		if (ids == null) return false;
		for (long id : ids) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static void limitDatabases(Context context) {
		ContentResolver resolver = context.getContentResolver();
		String[] cols = new String[0];
		Uri[] uris = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };
		int item_limit = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
				.getInt(PREFERENCE_KEY_ITEM_LIMIT, PREFERENCE_DEFAULT_ITEM_LIMIT);

		for (long account_id : getAccounts(context)) {
			// Clean statuses.
			for (Uri uri : uris) {
				Cursor cur = resolver.query(uri, cols, Statuses.ACCOUNT_ID + "=" + account_id,
						null, Statuses.DEFAULT_SORT_ORDER);
				if (cur != null && cur.getCount() > item_limit) {
					cur.moveToPosition(item_limit - 1);
					int _id = cur.getInt(cur.getColumnIndexOrThrow(Statuses._ID));
					resolver.delete(uri, Statuses._ID + "<" + _id, null);
				}
				if (cur != null) {
					cur.close();
				}
			}
		}
	}

	public static ContentValues makeAccountContentValues(int color, AccessToken access_token,
			User user, String rest_api_base, String search_api_base, String basic_password,
			int auth_type) {
		if (user == null) throw new IllegalArgumentException("User can't be null!");
		ContentValues values = new ContentValues();
		switch (auth_type) {
			case Accounts.AUTH_TYPE_BASIC:
				if (basic_password == null)
					throw new IllegalArgumentException("Password can't be null in Basic mode!");
				values.put(Accounts.BASIC_AUTH_PASSWORD, basic_password);
				break;
			case Accounts.AUTH_TYPE_OAUTH:
			case Accounts.AUTH_TYPE_XAUTH:
				if (access_token == null)
					throw new IllegalArgumentException(
							"Access Token can't be null in OAuth/xAuth mode!");
				if (user.getId() != access_token.getUserId())
					throw new IllegalArgumentException("User and Access Token not match!");
				values.put(Accounts.OAUTH_TOKEN, access_token.getToken());
				values.put(Accounts.TOKEN_SECRET, access_token.getTokenSecret());
				break;
		}
		values.put(Accounts.AUTH_TYPE, auth_type);
		values.put(Accounts.USER_ID, user.getId());
		values.put(Accounts.USERNAME, user.getScreenName());
		values.put(Accounts.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
		values.put(Accounts.USER_COLOR, color);
		values.put(Accounts.IS_ACTIVATED, 1);
		if (rest_api_base != null) {
			values.put(Accounts.REST_API_BASE, rest_api_base);
		}
		if (search_api_base != null) {
			values.put(Accounts.SEARCH_API_BASE, search_api_base);
		}

		return values;
	}

	public static ContentValues makeCachedUsersContentValues(User user) {
		ContentValues values = new ContentValues();
		values.put(CachedUsers.NAME, user.getName());
		values.put(CachedUsers.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
		values.put(CachedUsers.USER_ID, user.getId());

		return values;
	}

	public static ContentValues makeStatusesContentValues(Status status, long account_id) {
		ContentValues values = new ContentValues();
		User user = status.getUser();
		long status_id = status.getId(), user_id = user.getId();
		String profile_image_url = user.getProfileImageURL().toString();
		String name = user.getName(), screen_name = user.getScreenName();
		MediaEntity[] medias = status.getMediaEntities();
		int retweet_status = Math.abs(status.isRetweet() ? 1 : 0);
		retweet_status = status.isRetweetedByMe() ? -retweet_status : retweet_status;
		values.put(Statuses.STATUS_ID, status_id);
		values.put(Statuses.ACCOUNT_ID, account_id);
		values.put(Statuses.USER_ID, user_id);
		values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
		values.put(Statuses.TEXT, formatStatusString(status));
		values.put(Statuses.NAME, name);
		values.put(Statuses.SCREEN_NAME, screen_name);
		values.put(Statuses.PROFILE_IMAGE_URL, profile_image_url);
		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
		values.put(Statuses.IN_REPLY_TO_SCREEN_NAME, status.getInReplyToScreenName());
		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
		values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
		values.put(Statuses.SOURCE, status.getSource());
		values.put(Statuses.LOCATION, formatGeoLocationToString(status.getGeoLocation()));
		values.put(Statuses.IS_RETWEET, retweet_status);
		values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);
		values.put(Statuses.IS_PROTECTED, user.isProtected() ? 1 : 0);
		values.put(Statuses.HAS_MEDIA, medias != null && medias.length > 0 ? 1 : 0);

		return values;
	}

	public static void restartActivity(Activity activity) {
		int fade_in = android.R.anim.fade_in;
		int fade_out = android.R.anim.fade_out;
		activity.overridePendingTransition(fade_in, fade_out);
		activity.finish();
		activity.overridePendingTransition(fade_in, fade_out);
		activity.startActivity(activity.getIntent());
	}

	public static void setLayerType(View view, int layerType, Paint paint) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			new MethodsCompat().setLayerType(view, layerType, paint);
		}
	}

	public static void setMenuForStatus(Context context, Menu menu, long status_id, Uri uri) {
		int activated_color = context.getResources().getColor(R.color.holo_blue_bright);
		ContentResolver resolver = context.getContentResolver();
		String[] accounts_cols = new String[] { Accounts.USER_ID };
		Cursor accounts_cur = resolver.query(Accounts.CONTENT_URI, accounts_cols, null, null, null);
		ArrayList<Long> ids = new ArrayList<Long>();
		if (accounts_cur != null) {
			accounts_cur.moveToFirst();
			int idx = accounts_cur.getColumnIndexOrThrow(Accounts.USER_ID);
			while (!accounts_cur.isAfterLast()) {
				ids.add(accounts_cur.getLong(idx));
				accounts_cur.moveToNext();
			}
			accounts_cur.close();
		}
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + status_id;
		Cursor cur = resolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			long user_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));
			menu.findItem(R.id.delete_submenu).setVisible(ids.contains(user_id));
			menu.findItem(MENU_RETWEET).setVisible(!ids.contains(user_id) || ids.size() > 1);
			MenuItem itemFav = menu.findItem(MENU_FAV);
			if (cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1) {
				itemFav.getIcon().setColorFilter(activated_color, Mode.MULTIPLY);
				itemFav.setTitle(R.string.unfav);
			} else {
				itemFav.getIcon().clearColorFilter();
				itemFav.setTitle(R.string.fav);
			}
		}
		if (cur != null) {
			cur.close();
		}
	}

	public static void setUiOptions(Window window, int uiOptions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			new MethodsCompat().setUiOptions(window, uiOptions);
		}
	}

	public static void showErrorMessage(Context context, int error_code) {
		switch (error_code) {
			case RESULT_ALREADY_LOGGED_IN:
				Toast.makeText(context, R.string.error_already_logged_in, Toast.LENGTH_SHORT)
						.show();
				break;
			case RESULT_CONNECTIVITY_ERROR:
				Toast.makeText(context, R.string.error_connectivity_error, Toast.LENGTH_SHORT)
						.show();
				break;
			case RESULT_SERVER_ERROR:
				Toast.makeText(context, R.string.error_server_error, Toast.LENGTH_SHORT).show();
				break;
			case RESULT_BAD_ADDRESS:
				Toast.makeText(context, R.string.error_bad_address, Toast.LENGTH_SHORT).show();
				break;
			case RESULT_NO_PERMISSION:
				Toast.makeText(context, R.string.error_no_permission, Toast.LENGTH_SHORT).show();
				break;
			case RESULT_UNKNOWN_ERROR:
				Toast.makeText(context, R.string.error_unknown_error, Toast.LENGTH_SHORT).show();
				break;
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
