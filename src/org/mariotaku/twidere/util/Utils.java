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

import static org.mariotaku.twidere.util.HtmlUnescapeHelper.unescapeHTML;
import static org.mariotaku.twidere.util.TwidereLinkify.IMGLY_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.INSTAGRAM_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.MOBYPICTURE_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_ALL_AVALIABLE_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_IMGLY;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_INSTAGRAM;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_LOCKERZ_AND_PLIXI;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_MOBYPICTURE;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_SINA_WEIBO_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITGOO;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITPIC;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_YFROG;
import static org.mariotaku.twidere.util.TwidereLinkify.SINA_WEIBO_IMAGES_AVALIABLE_SIZES;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_IMGLY;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_INSTAGRAM;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_LOCKERZ_AND_PLIXI;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_MOBYPICTURE;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_SINA_WEIBO_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_TWITGOO;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_TWITPIC;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_TWITTER_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_TWITTER_PROFILE_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.STRING_PATTERN_YFROG;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITGOO_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITPIC_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITTER_PROFILE_IMAGES_AVALIABLE_SIZES;
import static org.mariotaku.twidere.util.TwidereLinkify.YFROG_GROUP_ID;

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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;

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
import org.mariotaku.twidere.fragment.ConversationFragment;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.UserBlocksFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserFollowersFragment;
import org.mariotaku.twidere.fragment.UserFriendsFragment;
import org.mariotaku.twidere.fragment.UserListCreatedFragment;
import org.mariotaku.twidere.fragment.UserListDetailsFragment;
import org.mariotaku.twidere.fragment.UserListMembersFragment;
import org.mariotaku.twidere.fragment.UserListMembershipsFragment;
import org.mariotaku.twidere.fragment.UserListSubscribersFragment;
import org.mariotaku.twidere.fragment.UserListSubscriptionsFragment;
import org.mariotaku.twidere.fragment.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.UserListTypesFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.UserRetweetedStatusFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.model.DirectMessageCursorIndices;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.PreviewImage;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import twitter4j.DirectMessage;
import twitter4j.MediaEntity;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserList;
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
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_USERS, URI_CACHED_USERS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_USERS, URI_FILTERED_USERS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_KEYWORDS, URI_FILTERED_KEYWORDS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_SOURCES, URI_FILTERED_SOURCES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES, URI_DIRECT_MESSAGES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_INBOX,
				URI_DIRECT_MESSAGES_INBOX);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_OUTBOX,
				URI_DIRECT_MESSAGES_OUTBOX);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATION + "/#/#",
				URI_DIRECT_MESSAGES_CONVERSATION);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME
				+ "/#/*", URI_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY + "/#",
				URI_DIRECT_MESSAGES_CONVERSATIONS_ENTRY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_DAILY, URI_TRENDS_DAILY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_WEEKLY, URI_TRENDS_WEEKLY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_LOCAL, URI_TRENDS_LOCAL);

	}

	private static HashMap<Long, Integer> sAccountColors = new HashMap<Long, Integer>();

	private static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };

	private static final Uri[] DIRECT_MESSAGES_URIS = new Uri[] { DirectMessages.Inbox.CONTENT_URI,
			DirectMessages.Outbox.CONTENT_URI };

	private Utils() {
		throw new IllegalArgumentException("You are trying to create an instance for this utility class!");
	}

	public static String buildActivatedStatsWhereClause(Context context, String selection) {
		if (context == null) return null;
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

	public static Uri buildDirectMessageConversationsEntryUri(long account_id) {
		if (account_id <= 0) return TweetStore.NULL_CONTENT_URI;
		final Uri.Builder builder = DirectMessages.ConversationsEntry.CONTENT_URI.buildUpon();
		builder.appendPath(String.valueOf(account_id));
		return builder.build();
	}

	public static Uri buildDirectMessageConversationUri(long account_id, long conversation_id, String screen_name) {
		if (conversation_id <= 0 && screen_name == null) return TweetStore.NULL_CONTENT_URI;
		final Uri.Builder builder = conversation_id > 0 ? DirectMessages.Conversation.CONTENT_URI.buildUpon()
				: DirectMessages.Conversation.CONTENT_URI_SCREEN_NAME.buildUpon();
		builder.appendPath(String.valueOf(account_id));
		builder.appendPath(conversation_id > 0 ? String.valueOf(conversation_id) : screen_name);
		return builder.build();
	}

	public static String buildFilterWhereClause(String table, String selection) {
		if (table == null) return null;
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
		if (uri == null) return null;
		final Uri.Builder uribuilder = uri.buildUpon();
		uribuilder.appendQueryParameter(QUERY_PARAM_NOTIFY, String.valueOf(notify));
		return uribuilder.build();
	}

	public static synchronized void cleanDatabasesByItemLimit(Context context) {
		if (context == null) return;
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
			for (final Uri uri : DIRECT_MESSAGES_URIS) {
				final String table = getTableNameForContentUri(uri);
				final StringBuilder where = new StringBuilder();
				where.append(DirectMessages.ACCOUNT_ID + " = " + account_id);
				where.append(" AND ");
				where.append(DirectMessages._ID + " NOT IN (");
				where.append(" SELECT " + DirectMessages._ID + " FROM " + table);
				where.append(" WHERE " + DirectMessages.ACCOUNT_ID + " = " + account_id);
				where.append(" ORDER BY " + DirectMessages.MESSAGE_ID + " DESC");
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

	public static ParcelableDirectMessage findDirectMessageInDatabases(Context context, long account_id, long message_id) {
		if (context == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		ParcelableDirectMessage message = null;
		final String where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND " + DirectMessages.MESSAGE_ID
				+ " = " + message_id;
		for (final Uri uri : DIRECT_MESSAGES_URIS) {
			final Cursor cur = resolver.query(uri, DirectMessages.COLUMNS, where, null, null);
			if (cur == null) {
				continue;
			}
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				message = new ParcelableDirectMessage(cur, new DirectMessageCursorIndices(cur));
			}
			cur.close();
		}
		return message;
	}

	public static ParcelableStatus findStatusInDatabases(Context context, long account_id, long status_id) {
		if (context == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final boolean force_ssl_connection = preferences != null ? preferences.getBoolean(
				PREFERENCE_KEY_FORCE_SSL_CONNECTION, false) : false;
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
				status = new ParcelableStatus(cur, new StatusCursorIndices(cur), force_ssl_connection);
			}
			cur.close();
		}
		return status;
	}

	public static UserList findUserList(Twitter twitter, long user_id, String list_name) throws TwitterException {
		if (twitter == null || user_id <= 0 || list_name == null) return null;
		final ResponseList<UserList> response = twitter.getUserLists(user_id, -1);
		for (final UserList list : response) {
			if (list_name.equals(list.getName())) return list;
		}
		return null;
	}

	public static UserList findUserList(Twitter twitter, long user_id, String screen_name, String list_name)
			throws TwitterException {
		if (user_id > 0)
			return findUserList(twitter, user_id, list_name);
		else if (screen_name != null) return findUserList(twitter, screen_name, list_name);
		return null;
	}

	public static UserList findUserList(Twitter twitter, String screen_name, String list_name) throws TwitterException {
		if (twitter == null || screen_name == null || list_name == null) return null;
		final ResponseList<UserList> response = twitter.getUserLists(screen_name, -1);
		for (final UserList list : response) {
			if (list_name.equals(list.getName())) return list;
		}
		return null;
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
					builder.addLink(expanded_url.toString(), url_entity.getDisplayURL(), start, end);
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
					builder.addLink(media_url.toString(), media_item.getDisplayURL(), start, end);
				}
			}
		}
		return builder.build();
	}

	public static String formatTimeStampString(Context context, long timestamp) {
		if (context == null) return null;
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
		if (context == null) return null;
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
					builder.addLink(expanded_url.toString(), url_entity.getDisplayURL(), start, end);
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
					builder.addLink(media_url.toString(), media_item.getDisplayURL(), start, end);
				}
			}
		}
		return builder.build();
	}

	public static int getAccountColor(Context context, long account_id) {
		if (context == null) return Color.TRANSPARENT;
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
		if (context == null) return -1;
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

		if (context == null) return -1;

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
		long[] accounts = new long[0];
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
		if (context == null) return accounts;
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
		if (context == null) return null;
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
		long[] accounts = new long[0];
		if (context == null) return accounts;
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
		if (context == null) return accounts;
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

	public static ImageSpec getAllAvailableImage(String link, boolean force_ssl) {
		if (link == null) return null;
		if (link.matches(STRING_PATTERN_IMGLY)) {
			final Matcher m = PATTERN_IMGLY.matcher(link);
			if (m.find()) return getImglyImage(matcherGroup(m, IMGLY_GROUP_ID), force_ssl);
		} else if (link.matches(STRING_PATTERN_INSTAGRAM)) {
			final Matcher m = PATTERN_INSTAGRAM.matcher(link);
			if (m.find()) return getInstagramImage(matcherGroup(m, INSTAGRAM_GROUP_ID), force_ssl);
		} else if (link.matches(STRING_PATTERN_LOCKERZ_AND_PLIXI))
			return getLockerzAndPlixiImage(link, force_ssl);
		else if (link.matches(STRING_PATTERN_SINA_WEIBO_IMAGES))
			return getSinaWeiboImage(link);
		else if (link.matches(STRING_PATTERN_TWITPIC)) {
			final Matcher m = PATTERN_TWITPIC.matcher(link);
			if (m.find()) return getTwitpicImage(matcherGroup(m, TWITPIC_GROUP_ID), force_ssl);
		} else if (link.matches(STRING_PATTERN_TWITTER_IMAGES))
			return getTwitterImage(link, force_ssl);
		else if (link.matches(STRING_PATTERN_YFROG)) {
			final Matcher m = PATTERN_YFROG.matcher(link);
			if (m.find()) return getYfrogImage(matcherGroup(m, YFROG_GROUP_ID), force_ssl);
		} else if (link.matches(STRING_PATTERN_TWITGOO)) {
			final Matcher m = PATTERN_TWITGOO.matcher(link);
			if (m.find()) return getTwitgooImage(matcherGroup(m, TWITGOO_GROUP_ID), force_ssl);
		} else if (link.matches(STRING_PATTERN_MOBYPICTURE)) {
			final Matcher m = PATTERN_MOBYPICTURE.matcher(link);
			if (m.find()) return getMobyPictureImage(matcherGroup(m, MOBYPICTURE_GROUP_ID), force_ssl);
		}
		return null;
	}

	public static String getBiggerTwitterProfileImage(String url, boolean force_ssl) {
		if (url == null) return null;
		if (force_ssl) {
			url = url.replaceFirst("http:\\/\\/", "https:\\/\\/");
		}
		if (url.matches(STRING_PATTERN_TWITTER_PROFILE_IMAGES))
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVALIABLE_SIZES, "_bigger");
		return url;
	}

	public static Bitmap getColorPreviewBitmap(Context context, int color) {
		if (context == null) return null;
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
		if (context == null) return -1;
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		return preferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
	}

	public static Twitter getDefaultTwitterInstance(Context context, boolean include_entities) {
		if (context == null) return null;
		return getDefaultTwitterInstance(context, include_entities, true);
	}

	public static Twitter getDefaultTwitterInstance(Context context, boolean include_entities, boolean include_rts) {
		if (context == null) return null;
		return getTwitterInstance(context, getDefaultAccountId(context), include_entities, include_rts);
	}

	public static String getImagePathFromUri(Context context, Uri uri) {
		if (context == null || uri == null) return null;

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

	public static List<ImageSpec> getImagesInStatus(String status_string, boolean force_ssl) {
		if (status_string == null) return Collections.emptyList();
		final List<ImageSpec> images = new ArrayList<ImageSpec>();
		// get instagram images
		{
			final Matcher matcher = PATTERN_INSTAGRAM.matcher(status_string);
			while (matcher.find()) {
				images.add(getInstagramImage(matcherGroup(matcher, INSTAGRAM_GROUP_ID), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_TWITPIC.matcher(status_string);
			while (matcher.find()) {
				images.add(getTwitpicImage(matcherGroup(matcher, TWITPIC_GROUP_ID), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_TWITTER_IMAGES.matcher(status_string);
			while (matcher.find()) {
				images.add(getTwitterImage(matcher.group(), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_LOCKERZ_AND_PLIXI.matcher(status_string);
			while (matcher.find()) {
				images.add(getLockerzAndPlixiImage(matcher.group(), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_SINA_WEIBO_IMAGES.matcher(status_string);
			while (matcher.find()) {
				images.add(getSinaWeiboImage(matcher.group()));
			}
		}
		{
			final Matcher matcher = PATTERN_IMGLY.matcher(status_string);
			while (matcher.find()) {
				images.add(getImglyImage(matcherGroup(matcher, IMGLY_GROUP_ID), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_YFROG.matcher(status_string);
			while (matcher.find()) {
				images.add(getYfrogImage(matcherGroup(matcher, YFROG_GROUP_ID), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_TWITGOO.matcher(status_string);
			while (matcher.find()) {
				images.add(getTwitgooImage(matcherGroup(matcher, TWITGOO_GROUP_ID), force_ssl));
			}
		}
		{
			final Matcher matcher = PATTERN_MOBYPICTURE.matcher(status_string);
			while (matcher.find()) {
				images.add(getMobyPictureImage(matcherGroup(matcher, MOBYPICTURE_GROUP_ID), force_ssl));
			}
		}
		return images;
	}

	public static ImageSpec getImglyImage(String id, boolean force_ssl) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http") + "://img.ly/show/thumb/" + id;
		final String full_size = (force_ssl ? "https" : "http") + "://img.ly/show/full/" + id;
		return new ImageSpec(thumbnail_size, full_size);

	}

	public static ImageSpec getInstagramImage(String id, boolean force_ssl) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http") + "://instagr.am/p/" + id + "/media/?size=t";
		final String full_size = (force_ssl ? "https" : "http") + "://instagr.am/p/" + id + "/media/?size=l";
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static long[] getLastStatusIdsFromDatabase(Context context, Uri uri) {
		if (context == null || uri == null) return null;
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

	public static ImageSpec getLockerzAndPlixiImage(String url, boolean force_ssl) {
		if (isNullOrEmpty(url)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http")
				+ "://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url + "&size=small";
		final String full_size = (force_ssl ? "https" : "http") + "://api.plixi.com/api/tpapi.svc/imagefromurl?url="
				+ url + "&size=big";
		return new ImageSpec(thumbnail_size, full_size);

	}

	public static ImageSpec getMobyPictureImage(String id, boolean force_ssl) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http") + "://moby.to/" + id + ":thumb";
		final String full_size = (force_ssl ? "https" : "http") + "://moby.to/" + id + ":full";
		return new ImageSpec(thumbnail_size, full_size);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public static String getNameForStatusId(Context context, long status_id) {
		if (context == null) return null;
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

	public static String getNormalTwitterProfileImage(String url, boolean force_ssl) {
		if (url == null) return null;
		if (force_ssl) {
			url = url.replaceFirst("http:\\/\\/", "https:\\/\\/");
		}
		if (url.matches(STRING_PATTERN_TWITTER_PROFILE_IMAGES))
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVALIABLE_SIZES, "_normal");
		return url;
	}

	public static String getOriginalTwitterProfileImage(String url, boolean force_ssl) {
		if (url == null) return null;
		if (force_ssl) {
			url = url.replaceFirst("http:\\/\\/", "https:\\/\\/");
		}
		if (url.matches(STRING_PATTERN_TWITTER_PROFILE_IMAGES))
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVALIABLE_SIZES, "");
		return url;
	}

	public static PreviewImage getPreviewImage(String html, boolean include_preview, boolean force_ssl) {
		if (html == null) return new PreviewImage(false, null);
		final Matcher m = PATTERN_ALL_AVALIABLE_IMAGES.matcher(html);
		if (m.find()) {
			if (!include_preview) return new PreviewImage(true, null);
			final String image_url = m.group();
			String temp_thumbnail_url = null;
			if (image_url.matches(STRING_PATTERN_IMGLY)) {
				final Matcher url_m = PATTERN_IMGLY.matcher(image_url);
				final ImageSpec spec = url_m.find() ? getImglyImage(matcherGroup(url_m, IMGLY_GROUP_ID), force_ssl)
						: null;
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_INSTAGRAM)) {
				final Matcher url_m = PATTERN_INSTAGRAM.matcher(image_url);
				final ImageSpec spec = url_m.find() ? getInstagramImage(matcherGroup(url_m, INSTAGRAM_GROUP_ID),
						force_ssl) : null;
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_LOCKERZ_AND_PLIXI)) {
				final ImageSpec spec = getLockerzAndPlixiImage(image_url, force_ssl);
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_SINA_WEIBO_IMAGES)) {
				final ImageSpec spec = getSinaWeiboImage(image_url);
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_TWITPIC)) {
				final Matcher url_m = PATTERN_TWITPIC.matcher(image_url);
				final ImageSpec spec = url_m.find() ? getTwitpicImage(matcherGroup(url_m, TWITPIC_GROUP_ID), force_ssl)
						: null;
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_TWITTER_IMAGES)) {
				final ImageSpec spec = getTwitterImage(image_url, force_ssl);
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_YFROG)) {
				final Matcher url_m = PATTERN_YFROG.matcher(image_url);
				final ImageSpec spec = url_m.find() ? getYfrogImage(matcherGroup(url_m, YFROG_GROUP_ID), force_ssl)
						: null;
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_TWITGOO)) {
				final Matcher url_m = PATTERN_TWITGOO.matcher(image_url);
				final ImageSpec spec = url_m.find() ? getTwitgooImage(matcherGroup(url_m, TWITGOO_GROUP_ID), force_ssl)
						: null;
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			} else if (image_url.matches(STRING_PATTERN_MOBYPICTURE)) {
				final Matcher url_m = PATTERN_MOBYPICTURE.matcher(image_url);
				final ImageSpec spec = url_m.find() ? getMobyPictureImage(matcherGroup(url_m, MOBYPICTURE_GROUP_ID),
						force_ssl) : null;
				if (spec != null) {
					temp_thumbnail_url = spec.thumbnail_link;
				}
			}
			return new PreviewImage(true, temp_thumbnail_url);
		}
		return new PreviewImage(false, null);
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
		if (context == null) return null;
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
		if (context == null) return -1;
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
		if (context == null) return -1;
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
		if (context == null) return null;
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

	public static ImageSpec getSinaWeiboImage(String url) {
		if (isNullOrEmpty(url)) return null;
		final String thumbnail_size = url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVALIABLE_SIZES + "\\/", "/thumbnail/");
		final String full_size = url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVALIABLE_SIZES + "\\/", "/large/");
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static ArrayList<Long> getStatusIdsInDatabase(Context context, Uri uri, long account_id) {
		final ArrayList<Long> list = new ArrayList<Long>();
		if (context == null) return list;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Statuses.ACCOUNT_ID + " = " + account_id;
		final String[] projection = new String[] { Statuses.STATUS_ID };
		final Cursor cur = resolver.query(uri, projection, where, null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Statuses.STATUS_ID);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				list.add(cur.getLong(idx));
				cur.moveToNext();
			}
			cur.close();
		}
		return list;
	}

	public static int getStatusTypeIconRes(boolean is_fav, boolean has_location, boolean has_media) {
		if (is_fav)
			return R.drawable.ic_indicator_starred;
		else if (has_media)
			return R.drawable.ic_indicator_has_media;
		else if (has_location) return R.drawable.ic_indicator_has_location;
		return 0;
	}

	public static int getTableId(Uri uri) {
		if (uri == null) return -1;
		return CONTENT_PROVIDER_URI_MATCHER.match(uri);
	}

	public static String getTableNameForContentUri(Uri uri) {
		if (uri == null) return null;
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
			case URI_DIRECT_MESSAGES:
				return TABLE_DIRECT_MESSAGES;
			case URI_DIRECT_MESSAGES_INBOX:
				return TABLE_DIRECT_MESSAGES_INBOX;
			case URI_DIRECT_MESSAGES_OUTBOX:
				return TABLE_DIRECT_MESSAGES_OUTBOX;
			case URI_DIRECT_MESSAGES_CONVERSATION:
				return TABLE_DIRECT_MESSAGES_CONVERSATION;
			case URI_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
				return TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME;
			case URI_DIRECT_MESSAGES_CONVERSATIONS_ENTRY:
				return TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY;
			case URI_TRENDS_DAILY:
				return TABLE_TRENDS_DAILY;
			case URI_TRENDS_WEEKLY:
				return TABLE_TRENDS_WEEKLY;
			case URI_TRENDS_LOCAL:
				return TABLE_TRENDS_LOCAL;
			default:
				return null;
		}
	}

	public static long getTimestampFromDate(Date date) {
		if (date == null) return -1;
		return date.getTime();
	}

	public static ImageSpec getTwitgooImage(String id, boolean force_ssl) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http") + "://twitgoo.com/show/thumb/" + id;
		final String full_size = (force_ssl ? "https" : "http") + "://twitgoo.com/show/img/" + id;
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static ImageSpec getTwitpicImage(String id, boolean force_ssl) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http") + "://twitpic.com/show/thumb/" + id;
		final String full_size = (force_ssl ? "https" : "http") + "://twitpic.com/show/large/" + id;
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static ImageSpec getTwitterImage(String url, boolean force_ssl) {
		if (isNullOrEmpty(url)) return null;
		if (force_ssl) {
			url = url.replaceFirst("http:\\/\\/", "https:\\/\\/");
		}
		return new ImageSpec(url + ":thumb", url);
	}

	public static Twitter getTwitterInstance(Context context, long account_id, boolean include_entities) {
		return getTwitterInstance(context, account_id, include_entities, true);
	}

	public static Twitter getTwitterInstance(Context context, long account_id, boolean include_entities,
			boolean include_rts) {
		if (context == null) return null;
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final boolean enable_gzip_compressing = preferences != null ? preferences.getBoolean(
				PREFERENCE_KEY_GZIP_COMPRESSING, true) : true;
		final boolean ignore_ssl_error = preferences != null ? preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR,
				false) : false;
		final boolean force_ssl_connection = preferences != null ? preferences.getBoolean(
				PREFERENCE_KEY_FORCE_SSL_CONNECTION, false) : false;
		final boolean enable_proxy = preferences != null ? preferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false)
				: false;
		final String consumer_key = preferences != null ? preferences.getString(PREFERENCE_KEY_CONSUMER_KEY,
				CONSUMER_KEY) : CONSUMER_KEY;
		final String consumer_secret = preferences != null ? preferences.getString(PREFERENCE_KEY_CONSUMER_SECRET,
				CONSUMER_SECRET) : CONSUMER_SECRET;

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
				if (force_ssl_connection) {
					cb.setUseSSL(true);
				}
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
		if (context == null) return null;
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
		if (account_id > 0) return getTwitterInstance(context, account_id, include_entities, include_rts);
		return null;
	}

	public static int getUserTypeIconRes(boolean is_verified, boolean is_protected) {
		if (is_verified)
			return R.drawable.ic_indicator_verified;
		else if (is_protected) return R.drawable.ic_indicator_is_protected;
		return 0;
	}

	public static ImageSpec getYfrogImage(String id, boolean force_ssl) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = (force_ssl ? "https" : "http") + "://yfrog.com/" + id + ":small";
		final String full_size = (force_ssl ? "https" : "http") + "://yfrog.com/" + id + ":medium";
		return new ImageSpec(thumbnail_size, full_size);

	}

	public static boolean isMyAccount(Context context, long account_id) {
		if (context == null) return false;
		for (final long id : getAccountIds(context)) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isMyActivatedAccount(Context context, long account_id) {
		if (context == null || account_id <= 0) return false;
		for (final long id : getActivatedAccountIds(context)) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isMyActivatedUserName(Context context, String screen_name) {
		if (context == null || screen_name == null) return false;
		for (final String account_user_name : getActivatedAccountScreenNames(context)) {
			if (account_user_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isMyRetweet(Context context, long account_id, long status_id) {
		if (context == null) return false;
		return account_id == getRetweetedByUserId(context, status_id);
	}

	public static boolean isMyUserName(Context context, String screen_name) {
		if (context == null) return false;
		for (final String account_screen_name : getAccountScreenNames(context)) {
			if (account_screen_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isNullOrEmpty(CharSequence text) {
		return text == null || "".equals(text);
	}

	public static boolean isUserLoggedIn(Context context, long account_id) {
		if (context == null) return false;
		final long[] ids = getAccountIds(context);
		if (ids == null) return false;
		for (final long id : ids) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static ContentValues makeAccountContentValues(int color, AccessToken access_token, User user,
			String rest_base_url, String oauth_base_url, String signing_rest_base_url, String signing_oauth_base_url,
			String search_base_url, String upload_base_url, String basic_password, int auth_type) {
		if (user == null || user.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		switch (auth_type) {
			case Accounts.AUTH_TYPE_TWIP_O_MODE: {
				break;
			}
			case Accounts.AUTH_TYPE_BASIC: {
				if (basic_password == null) return null;
				values.put(Accounts.BASIC_AUTH_PASSWORD, basic_password);
				break;
			}
			case Accounts.AUTH_TYPE_OAUTH:
			case Accounts.AUTH_TYPE_XAUTH: {
				if (access_token == null) return null;
				if (user.getId() != access_token.getUserId()) return null;
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
		values.put(Accounts.REST_BASE_URL, rest_base_url);
		values.put(Accounts.SIGNING_REST_BASE_URL, signing_rest_base_url);
		values.put(Accounts.SEARCH_BASE_URL, search_base_url);
		values.put(Accounts.UPLOAD_BASE_URL, upload_base_url);
		values.put(Accounts.OAUTH_BASE_URL, oauth_base_url);
		values.put(Accounts.SIGNING_OAUTH_BASE_URL, signing_oauth_base_url);
		return values;
	}

	public static ContentValues makeCachedUserContentValues(User user) {
		if (user == null || user.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		values.put(CachedUsers.NAME, user.getName());
		values.put(CachedUsers.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
		values.put(CachedUsers.USER_ID, user.getId());
		return values;
	}

	public static ContentValues makeDirectMessageContentValues(DirectMessage message, long account_id) {
		if (message == null || message.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		final User sender = message.getSender(), recipient = message.getRecipient();
		if (sender == null || recipient == null) return null;
		values.put(DirectMessages.ACCOUNT_ID, account_id);
		values.put(DirectMessages.MESSAGE_ID, message.getId());
		values.put(DirectMessages.MESSAGE_TIMESTAMP, message.getCreatedAt().getTime());
		values.put(DirectMessages.SENDER_ID, sender.getId());
		values.put(DirectMessages.RECIPIENT_ID, recipient.getId());
		values.put(DirectMessages.TEXT, message.getText());
		values.put(DirectMessages.SENDER_NAME, sender.getName());
		values.put(DirectMessages.SENDER_SCREEN_NAME, sender.getScreenName());
		values.put(DirectMessages.RECIPIENT_NAME, recipient.getName());
		values.put(DirectMessages.RECIPIENT_SCREEN_NAME, recipient.getScreenName());
		final URL sender_profile_image_url = sender.getProfileImageURL();
		final URL recipient_profile_image_url = recipient.getProfileImageURL();
		if (sender_profile_image_url != null) {
			values.put(DirectMessages.SENDER_PROFILE_IMAGE_URL, sender_profile_image_url.toString());
		}
		if (recipient_profile_image_url != null) {
			values.put(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, recipient_profile_image_url.toString());
		}
		return values;
	}

	public static ContentValues makeStatusContentValues(Status status, long account_id) {
		if (status == null || status.getId() <= 0) return null;
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
			values.put(Statuses.IS_VERIFIED, user.isVerified() ? 1 : 0);
			values.put(Statuses.PROFILE_IMAGE_URL, profile_image_url);
		}
		values.put(Statuses.ACCOUNT_ID, account_id);
		if (status.getCreatedAt() != null) {
			values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
		}
		values.put(Statuses.TEXT, formatStatusText(status));
		values.put(Statuses.TEXT_PLAIN, status.getText());
		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
		values.put(Statuses.IN_REPLY_TO_SCREEN_NAME, status.getInReplyToScreenName());
		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
		values.put(Statuses.SOURCE, status.getSource());
		values.put(Statuses.LOCATION, new ParcelableLocation(status.getGeoLocation()).toString());
		values.put(Statuses.IS_RETWEET, is_retweet);
		values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);
		return values;
	}

	public static ContentValues[] makeTrendsContentValues(List<Trends> trends_list) {
		if (trends_list == null) return new ContentValues[0];
		final List<ContentValues> result_list = new ArrayList<ContentValues>();
		for (final Trends trends : trends_list) {
			if (trends == null) {
				continue;
			}
			final long timestamp = trends.getTrendAt().getTime();
			for (final Trend trend : trends.getTrends()) {
				final ContentValues values = new ContentValues();
				values.put(CachedTrends.NAME, trend.getName());
				values.put(CachedTrends.TIMESTAMP, timestamp);
				result_list.add(values);
			}
		}
		return result_list.toArray(new ContentValues[result_list.size()]);
	}

	public static final int matcherEnd(Matcher matcher, int group) {
		try {
			return matcher.end(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return -1;
	}

	public static final String matcherGroup(Matcher matcher, int group) {
		try {
			return matcher.group(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return null;
	}

	public static final int matcherStart(Matcher matcher, int group) {
		try {
			return matcher.start(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return -1;
	}

	public static void notifyForUpdatedUri(Context context, Uri uri) {
		if (context == null) return;
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
			case URI_DIRECT_MESSAGES_INBOX: {
				context.sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED).putExtra(
						INTENT_KEY_SUCCEED, true));
				break;
			}
			case URI_DIRECT_MESSAGES_OUTBOX: {
				context.sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED).putExtra(
						INTENT_KEY_SUCCEED, true));
				break;
			}
			default: {
				return;
			}
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	public static void openConversation(Activity activity, long account_id, long status_id) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new ConversationFragment();
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
		if (activity == null) return;
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
		if (activity == null) return;
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
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserFavoritesFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id > 0) {
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
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static void openUserFollowers(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserFollowersFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id > 0) {
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
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static void openUserFriends(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserFriendsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id > 0) {
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
			builder.authority(AUTHORITY_USER_FRIENDS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static void openUserListCreated(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListCreatedFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_CREATED);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListDetails(Activity activity, long account_id, int list_id, long user_id,
			String screen_name, String list_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListDetailsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_DETAILS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (list_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(list_id));
			}
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			if (list_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, list_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListMembers(Activity activity, long account_id, int list_id, long user_id,
			String screen_name, String list_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListMembersFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_MEMBERS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (list_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(list_id));
			}
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			if (list_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, list_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListMemberships(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListMembershipsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_MEMBERSHIPS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListSubscribers(Activity activity, long account_id, int list_id, long user_id,
			String screen_name, String list_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListSubscribersFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_SUBSCRIBERS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (list_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(list_id));
			}
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			if (list_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, list_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListSubscriptions(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListSubscriptionsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_SUBSCRIPTIONS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListTimeline(Activity activity, long account_id, int list_id, long user_id,
			String screen_name, String list_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListTimelineFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_TIMELINE);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (list_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(list_id));
			}
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			if (list_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, list_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserListTypes(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserListTypesFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LIST_TYPES);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserProfile(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserProfileFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id > 0) {
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
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserRetweetedStatus(Activity activity, long account_id, long status_id) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserRetweetedStatusFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (status_id > 0) {
				args.putLong(INTENT_KEY_STATUS_ID, status_id);
			}
			fragment.setArguments(args);
			home_activity.showAtPane(HomeActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USERS_RETWEETED_STATUS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (status_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserTimeline(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			final Fragment fragment = new UserTimelineFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (user_id > 0) {
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
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}

	}

	public static double parseDouble(String source) {
		if (source == null) return -1;
		try {
			return Double.parseDouble(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static int parseInt(String source) {
		if (source == null) return -1;
		try {
			return Integer.valueOf(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
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

	public static String parseString(Object object) {
		if (object == null) return null;
		return String.valueOf(object);
	}

	public static URL parseURL(String url_string) {
		if (url_string == null) return null;
		try {
			return new URL(url_string);
		} catch (final MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}

	public static String replaceLast(String text, String regex, String replacement) {
		if (text == null || regex == null || replacement == null) return text;
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	public static void restartActivity(Activity activity, boolean animation) {
		if (activity == null) return;
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
		final MenuItem itemDelete = menu.findItem(R.id.delete_submenu);
		if (itemDelete != null) {
			itemDelete.setVisible(isMyActivatedAccount(context, status.user_id));
		}
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
		final MenuItem itemConversation = menu.findItem(MENU_CONVERSATION);
		if (itemConversation != null) {
			itemConversation.setVisible(status.in_reply_to_status_id > 0 && status.in_reply_to_screen_name != null);
		}
	}

	public static void showErrorToast(Context context, Exception e, boolean long_message) {
		if (context == null) return;
		final String message = e != null ? context.getString(R.string.error_message,
				trimLineBreak(unescapeHTML(e.getMessage()))) : context.getString(R.string.error_unknown_error);
		final int length = long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(context, message, length);
		toast.show();
	}

	public static String trimLineBreak(String orig) {
		if (orig == null) return null;
		return orig.replaceAll("\\n+", "\n");
	}

}
