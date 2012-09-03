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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;
import static org.mariotaku.twidere.util.TwidereLinkify.IMGLY_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.IMGUR_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.INSTAGRAM_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.MOBYPICTURE_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_IMGLY;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_IMGUR;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_INSTAGRAM;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_LOCKERZ_AND_PLIXI;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_MOBYPICTURE;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_PREVIEW_AVAILABLE_IMAGES_IN_HTML;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_SINA_WEIBO_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITGOO;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITPIC;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_YFROG;
import static org.mariotaku.twidere.util.TwidereLinkify.PREVIEW_AVAILABLE_IMAGES_IN_HTML_GROUP_LINK;
import static org.mariotaku.twidere.util.TwidereLinkify.SINA_WEIBO_IMAGES_AVAILABLE_SIZES;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITGOO_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITPIC_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES;
import static org.mariotaku.twidere.util.TwidereLinkify.YFROG_GROUP_ID;

import java.io.File;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.DualPaneActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.fragment.ConversationFragment;
import org.mariotaku.twidere.fragment.DMConversationFragment;
import org.mariotaku.twidere.fragment.RetweetedToMeFragment;
import org.mariotaku.twidere.fragment.SavedSearchesListFragment;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.fragment.StatusFragment;
import org.mariotaku.twidere.fragment.TrendsFragment;
import org.mariotaku.twidere.fragment.UserBlocksListFragment;
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
import org.mariotaku.twidere.fragment.UserMentionsFragment;
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
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import twitter4j.DirectMessage;
import twitter4j.EntitySupport;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public final class Utils implements Constants {

	private static final UriMatcher CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final UriMatcher LINK_HANDLER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	public static final HashMap<String, Class<? extends Fragment>> CUSTOM_TABS_FRAGMENT_MAP = new HashMap<String, Class<? extends Fragment>>();
	public static final HashMap<String, Integer> CUSTOM_TABS_TYPE_NAME_MAP = new HashMap<String, Integer>();
	public static final HashMap<String, Integer> CUSTOM_TABS_ICON_NAME_MAP = new HashMap<String, Integer>();

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
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY,
				URI_DIRECT_MESSAGES_CONVERSATIONS_ENTRY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_DAILY, URI_TRENDS_DAILY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_WEEKLY, URI_TRENDS_WEEKLY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_LOCAL, URI_TRENDS_LOCAL);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TABS, URI_TABS);

		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS, null, LINK_ID_STATUS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER, null, LINK_ID_USER);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_TIMELINE, null, LINK_ID_USER_TIMELINE);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FOLLOWERS, null, LINK_ID_USER_FOLLOWERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FRIENDS, null, LINK_ID_USER_FRIENDS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FAVORITES, null, LINK_ID_USER_FAVORITES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_BLOCKS, null, LINK_ID_USER_BLOCKS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_CONVERSATION, null, LINK_ID_CONVERSATION);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_DIRECT_MESSAGES_CONVERSATION, null,
				LINK_ID_DIRECT_MESSAGES_CONVERSATION);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_DETAILS, null, LINK_ID_LIST_DETAILS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_TYPES, null, LINK_ID_LIST_TYPES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_TIMELINE, null, LINK_ID_LIST_TIMELINE);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_MEMBERS, null, LINK_ID_LIST_MEMBERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_SUBSCRIBERS, null, LINK_ID_LIST_SUBSCRIBERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_CREATED, null, LINK_ID_LIST_CREATED);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_SUBSCRIPTIONS, null, LINK_ID_LIST_SUBSCRIPTIONS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_MEMBERSHIPS, null, LINK_ID_LIST_MEMBERSHIPS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USERS_RETWEETED_STATUS, null, LINK_ID_USERS_RETWEETED_STATUS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SAVED_SEARCHES, null, LINK_ID_SAVED_SEARCHES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_RETWEETED_TO_ME, null, LINK_ID_RETWEETED_TO_ME);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_MENTIONS, null, LINK_ID_USER_MENTIONS);

		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_CREATED, UserListCreatedFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_MEMBERS, UserListMembersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_MEMBERSHIPS, UserListMembershipsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_SUBSCRIBERS, UserListSubscribersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_SUBSCRIPTIONS, UserListSubscriptionsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_TIMELINE, UserListTimelineFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_RETWEETED_TO_ME, RetweetedToMeFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_SAVED_SEARCHES, SavedSearchesListFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_SEARCH_TWEETS, SearchTweetsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_SEARCH_USERS, SearchUsersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_FAVORITES, UserFavoritesFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_FOLLOWERS, UserFollowersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_FRIENDS, UserFriendsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_MENTIONS, UserMentionsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_TIMELINE, UserTimelineFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_TRENDS, TrendsFragment.class);

		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_CREATED, R.string.list_created_by_user);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_MEMBERS, R.string.list_members);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_MEMBERSHIPS, R.string.list_following_user);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_SUBSCRIBERS, R.string.list_subscribers);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_SUBSCRIPTIONS, R.string.list_user_followed);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_TIMELINE, R.string.list_timeline);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_RETWEETED_TO_ME, R.string.retweeted_to_me);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_SAVED_SEARCHES, R.string.saved_searches);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_SEARCH_TWEETS, R.string.search_tweets);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_SEARCH_USERS, R.string.search_users);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_FAVORITES, R.string.favorites);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_FOLLOWERS, R.string.followers);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_FRIENDS, R.string.following);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_MENTIONS, R.string.user_mentions);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_TIMELINE, R.string.user_timeline);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_TRENDS, R.string.trends);

		CUSTOM_TABS_ICON_NAME_MAP.put("accounts", R.drawable.ic_tab_accounts);
		CUSTOM_TABS_ICON_NAME_MAP.put("fire", R.drawable.ic_tab_fire);
		CUSTOM_TABS_ICON_NAME_MAP.put("hasmter", R.drawable.ic_tab_hasmter);
		CUSTOM_TABS_ICON_NAME_MAP.put("heart", R.drawable.ic_tab_heart);
		CUSTOM_TABS_ICON_NAME_MAP.put("home", R.drawable.ic_tab_home);
		CUSTOM_TABS_ICON_NAME_MAP.put("list", R.drawable.ic_tab_list);
		CUSTOM_TABS_ICON_NAME_MAP.put("mention", R.drawable.ic_tab_mention);
		CUSTOM_TABS_ICON_NAME_MAP.put("message", R.drawable.ic_tab_message);
		CUSTOM_TABS_ICON_NAME_MAP.put("neko", R.drawable.ic_tab_neko);
		CUSTOM_TABS_ICON_NAME_MAP.put("person", R.drawable.ic_tab_person);
		CUSTOM_TABS_ICON_NAME_MAP.put("pin", R.drawable.ic_tab_pin);
		CUSTOM_TABS_ICON_NAME_MAP.put("ribbon", R.drawable.ic_tab_ribbon);
		CUSTOM_TABS_ICON_NAME_MAP.put("search", R.drawable.ic_tab_search);
		CUSTOM_TABS_ICON_NAME_MAP.put("star", R.drawable.ic_tab_star);
		CUSTOM_TABS_ICON_NAME_MAP.put("trends", R.drawable.ic_tab_trends);
		CUSTOM_TABS_ICON_NAME_MAP.put("twitter", R.drawable.ic_tab_twitter);
		CUSTOM_TABS_ICON_NAME_MAP.put(ICON_SPECIAL_TYPE_CUSTOMIZE, -1);

	}

	private static Map<Long, Integer> sAccountColors = new LinkedHashMap<Long, Integer>();

	private static Map<Long, Integer> sUserColors = new LinkedHashMap<Long, Integer>(512, 0.75f, true);

	private static Map<Long, String> sAccountNames = new LinkedHashMap<Long, String>();
	public static final Uri[] STATUSES_URIS = new Uri[] { Statuses.CONTENT_URI, Mentions.CONTENT_URI };
	public static final Uri[] DIRECT_MESSAGES_URIS = new Uri[] { DirectMessages.Inbox.CONTENT_URI,
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
		builder.append(ArrayUtils.toString(account_ids, ',', true));
		builder.append(" )");

		return builder.toString();
	}

	public static String buildArguments(Bundle args) {
		final Set<String> keys = args.keySet();
		final JSONObject json = new JSONObject();
		for (final String key : keys) {
			final Object value = args.get(key);
			if (value == null) {
				continue;
			}
			try {
				if (value instanceof Boolean) {
					json.put(key, args.getBoolean(key));
				} else if (value instanceof Integer) {
					json.put(key, args.getInt(key));
				} else if (value instanceof Long) {
					json.put(key, args.getLong(key));
				} else if (value instanceof String) {
					json.put(key, args.getString(key));
				} else {
					Log.w(LOGTAG, "Unknown type " + (value != null ? value.getClass().getSimpleName() : null)
							+ " in arguments key " + key);
				}
			} catch (final JSONException e) {
				e.printStackTrace();
			}
		}
		return json.toString();
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

	public static boolean bundleEquals(Bundle args1, Bundle args2) {
		if (args1 == null || args2 == null) return args1 == args2;
		final Iterator<String> keys = args1.keySet().iterator();
		while (keys.hasNext()) {
			final String key = keys.next();
			if (!objectEquals(args1.get(key), args2.get(key))) return false;
		}
		return true;
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

	public static void clearAccountName() {
		sAccountNames.clear();
	}

	public static void clearUserColor(Context context, long user_id) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
		sUserColors.remove(user_id);
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
				status = new ParcelableStatus(cur, new StatusCursorIndices(cur));
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
		final String text = status.getRawText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false);
		parseEntities(builder, status);
		return builder.build(true);
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

	public static String formatTweetText(Tweet tweet) {
		if (tweet == null) return null;
		final String text = tweet.getRawText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false);
		parseEntities(builder, tweet);
		return builder.build(true);
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
		String username = sAccountNames.get(account_id);
		if (username == null) {
			final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.USERNAME }, Accounts.USER_ID + " = " + account_id, null, null);
			if (cur == null) return username;

			if (cur.getCount() > 0) {
				cur.moveToFirst();
				username = cur.getString(cur.getColumnIndex(Accounts.USERNAME));
				sAccountNames.put(account_id, username);
			}
			cur.close();
		}
		return username;
	}

	public static long[] getActivatedAccountIds(Context context) {
		long[] accounts = new long[0];
		if (context == null) return accounts;
		final String[] cols = new String[] { Accounts.USER_ID };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
				null, Accounts.USER_ID);
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

	public static ImageSpec getAllAvailableImage(String link) {
		if (link == null) return null;
		Matcher m;
		m = PATTERN_TWITTER_IMAGES.matcher(link);
		if (m.matches()) return getTwitterImage(link);
		m = PATTERN_TWITPIC.matcher(link);
		if (m.matches()) return getTwitpicImage(matcherGroup(m, TWITPIC_GROUP_ID));
		m = PATTERN_INSTAGRAM.matcher(link);
		if (m.matches()) return getInstagramImage(matcherGroup(m, INSTAGRAM_GROUP_ID));
		m = PATTERN_IMGUR.matcher(link);
		if (m.matches()) return getImgurImage(matcherGroup(m, IMGUR_GROUP_ID));
		m = PATTERN_IMGLY.matcher(link);
		if (m.matches()) return getImglyImage(matcherGroup(m, IMGLY_GROUP_ID));
		m = PATTERN_YFROG.matcher(link);
		if (m.matches()) return getYfrogImage(matcherGroup(m, YFROG_GROUP_ID));
		m = PATTERN_LOCKERZ_AND_PLIXI.matcher(link);
		if (m.matches()) return getLockerzAndPlixiImage(link);
		m = PATTERN_SINA_WEIBO_IMAGES.matcher(link);
		if (m.matches()) return getSinaWeiboImage(link);
		m = PATTERN_TWITGOO.matcher(link);
		if (m.matches()) return getTwitgooImage(matcherGroup(m, TWITGOO_GROUP_ID));
		m = PATTERN_MOBYPICTURE.matcher(link);
		if (m.matches()) return getMobyPictureImage(matcherGroup(m, MOBYPICTURE_GROUP_ID));
		return null;
	}

	public static long[] getAllStatusesIds(Context context, Uri uri, boolean filter_enabled) {
		if (context == null) return new long[0];
		final ContentResolver resolver = context.getContentResolver();
		final ArrayList<Long> ids_list = new ArrayList<Long>();
		final Cursor cur = resolver.query(uri, new String[] { Statuses.STATUS_ID },
				filter_enabled ? buildFilterWhereClause(getTableNameForContentUri(uri), null) : null, null, null);
		if (cur == null) return new long[0];
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			ids_list.add(cur.getLong(0));
			cur.moveToNext();
		}
		cur.close();
		return ArrayUtils.fromList(ids_list);
	}

	public static String getBiggerTwitterProfileImage(String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "_bigger");
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
		} else {
			final String path = uri.getPath();
			if (path != null) {
				if (new File(path).exists()) return path;
			}
		}
		return null;
	}

	public static List<ImageSpec> getImagesInStatus(String status_string) {
		if (status_string == null) return Collections.emptyList();
		final List<ImageSpec> images = new ArrayList<ImageSpec>();
		final Matcher matcher = PATTERN_PREVIEW_AVAILABLE_IMAGES_IN_HTML.matcher(status_string);
		while (matcher.find()) {
			images.add(getAllAvailableImage(matcherGroup(matcher, PREVIEW_AVAILABLE_IMAGES_IN_HTML_GROUP_LINK)));
		}
		return images;
	}

	public static String getImageUploadStatus(Context context, String link, String text) {
		if (context == null) return null;
		String image_upload_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getString(PREFERENCE_KEY_IMAGE_UPLOAD_FORMAT, PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT);
		if (isNullOrEmpty(image_upload_format)) {
			image_upload_format = PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT;
		}
		if (link == null) return text;
		return image_upload_format.replace(FORMAT_PATTERN_LINK, link).replace(FORMAT_PATTERN_TEXT, text);
	}

	public static ImageSpec getImglyImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://img.ly/show/thumb/" + id;
		final String full_size = "http://img.ly/show/full/" + id;
		return new ImageSpec(thumbnail_size, full_size);

	}

	public static ImageSpec getImgurImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://i.imgur.com/" + id + "s.jpg";
		final String full_size = "http://i.imgur.com/" + id + ".jpg";
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static ImageSpec getInstagramImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://instagr.am/p/" + id + "/media/?size=t";
		final String full_size = "http://instagr.am/p/" + id + "/media/?size=l";
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static long[] getLastMessageIdsFromDatabase(Context context, Uri uri) {
		if (context == null || uri == null) return null;
		final long[] account_ids = getActivatedAccountIds(context);
		final String[] cols = new String[] { DirectMessages.MESSAGE_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = resolver.query(uri, cols, where, null, DirectMessages.MESSAGE_ID);
			if (cur == null) {
				continue;
			}

			if (cur.getCount() > 0) {
				cur.moveToFirst();
				status_ids[idx] = cur.getLong(cur.getColumnIndexOrThrow(DirectMessages.MESSAGE_ID));
			}
			cur.close();
			idx++;
		}
		return status_ids;
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

	public static ImageSpec getLockerzAndPlixiImage(String url) {
		if (isNullOrEmpty(url)) return null;
		final String thumbnail_size = "http://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url + "&size=small";
		final String full_size = "http://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url + "&size=big";
		return new ImageSpec(thumbnail_size, full_size);

	}

	public static ImageSpec getMobyPictureImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://moby.to/" + id + ":thumb";
		final String full_size = "http://moby.to/" + id + ":full";
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static String getNormalTwitterProfileImage(String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "_normal");
		return url;
	}

	public static String getOriginalTwitterProfileImage(String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "");
		return url;
	}

	public static PreviewImage getPreviewImage(String html, boolean include_preview) {
		if (html == null) return new PreviewImage(false, null, null);
		if (!include_preview)
			return new PreviewImage(html.contains("://p.twimg.com/") || html.contains("://instagr.am/")
					|| html.contains("://instagram.com/") || html.contains("://imgur.com/")
					|| html.contains("://i.imgur.com/") || html.contains("://twitpic.com/")
					|| html.contains("://img.ly/") || html.contains("://yfrog.com/")
					|| html.contains("://twitgoo.com/") || html.contains("://moby.to/")
					|| html.contains("://plixi.com/p/") || html.contains("://lockerz.com/s/")
					|| html.contains(".sinaimg.cn/"), null, null);
		final Matcher m = PATTERN_PREVIEW_AVAILABLE_IMAGES_IN_HTML.matcher(html);
		while (m.find()) {
			final String image_url = m.group(PREVIEW_AVAILABLE_IMAGES_IN_HTML_GROUP_LINK);
			Matcher url_m;
			url_m = PATTERN_TWITTER_IMAGES.matcher(image_url);
			if (url_m.matches()) return new PreviewImage(getTwitterImage(image_url), image_url);
			url_m = PATTERN_TWITPIC.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getTwitpicImage(matcherGroup(url_m, TWITPIC_GROUP_ID)), image_url);
			url_m = PATTERN_INSTAGRAM.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getInstagramImage(matcherGroup(url_m, INSTAGRAM_GROUP_ID)), image_url);
			url_m = PATTERN_IMGUR.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getImgurImage(matcherGroup(url_m, IMGUR_GROUP_ID)), image_url);
			url_m = PATTERN_IMGLY.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getImglyImage(matcherGroup(url_m, IMGLY_GROUP_ID)), image_url);
			url_m = PATTERN_YFROG.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getYfrogImage(matcherGroup(url_m, YFROG_GROUP_ID)), image_url);
			url_m = PATTERN_LOCKERZ_AND_PLIXI.matcher(image_url);
			if (url_m.matches()) return new PreviewImage(getLockerzAndPlixiImage(image_url), image_url);
			url_m = PATTERN_SINA_WEIBO_IMAGES.matcher(image_url);
			if (url_m.matches()) return new PreviewImage(getSinaWeiboImage(image_url), image_url);
			url_m = PATTERN_TWITGOO.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getTwitgooImage(matcherGroup(url_m, TWITGOO_GROUP_ID)), image_url);
			url_m = PATTERN_MOBYPICTURE.matcher(image_url);
			if (url_m.matches())
				return new PreviewImage(getMobyPictureImage(matcherGroup(url_m, MOBYPICTURE_GROUP_ID)), image_url);
		}
		return new PreviewImage(false, null, null);
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
		return quote_format.replace(FORMAT_PATTERN_NAME, screen_name).replace(FORMAT_PATTERN_TEXT, text);
	}

	public static String getShareStatus(Context context, String title, String text) {
		if (context == null) return null;
		String share_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
				PREFERENCE_KEY_SHARE_FORMAT, PREFERENCE_DEFAULT_SHARE_FORMAT);
		if (isNullOrEmpty(share_format)) {
			share_format = PREFERENCE_DEFAULT_SHARE_FORMAT;
		}
		if (title == null) return text;
		return share_format.replace(FORMAT_PATTERN_TITLE, title).replace(FORMAT_PATTERN_TEXT, text);
	}

	public static ImageSpec getSinaWeiboImage(String url) {
		if (isNullOrEmpty(url)) return null;
		final String thumbnail_size = url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "\\/", "/thumbnail/");
		final String full_size = url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "\\/", "/large/");
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static int getStatusBackground(boolean is_mention, boolean is_favorite, boolean is_retweet) {
		if (is_mention)
			return 0x1A33B5E5;
		else if (is_favorite)
			return 0x1AFFBB33;
		else if (is_retweet) return 0x1A66CC00;
		return Color.TRANSPARENT;
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

	public static Drawable getTabIconDrawable(Context context, Object icon_obj) {
		if (context == null) return null;
		final Resources res = context.getResources();
		if (icon_obj instanceof Integer) {
			try {
				return res.getDrawable((Integer) icon_obj);
			} catch (final Resources.NotFoundException e) {
				// Ignore.
			}
		} else if (icon_obj instanceof Bitmap)
			return new BitmapDrawable(res, (Bitmap) icon_obj);
		else if (icon_obj instanceof Drawable)
			return (Drawable) icon_obj;
		else if (icon_obj instanceof File) {
			final Bitmap b = getTabIconFromFile((File) icon_obj, res);
			if (b != null) return new BitmapDrawable(res, b);
		}
		return res.getDrawable(R.drawable.ic_tab_list);
	}

	public static Object getTabIconObject(String type) {
		if (type == null) return R.drawable.ic_tab_list;
		final Integer value = CUSTOM_TABS_ICON_NAME_MAP.get(type);
		if (value != null)
			return value;
		else if (type.contains("/")) {
			try {
				final File file = new File(type);
				if (file.exists()) return file;
			} catch (final Exception e) {
				return R.drawable.ic_tab_list;
			}
		}
		return R.drawable.ic_tab_list;
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
			case URI_TABS:
				return TABLE_TABS;
			default:
				return null;
		}
	}

	public static List<TabSpec> getTabs(Context context) {
		if (context == null) return Collections.emptyList();
		final ArrayList<TabSpec> tabs = new ArrayList<TabSpec>();
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
		if (cur != null) {
			cur.moveToFirst();
			final int idx_name = cur.getColumnIndex(Tabs.NAME), idx_icon = cur.getColumnIndex(Tabs.ICON), idx_type = cur
					.getColumnIndex(Tabs.TYPE), idx_arguments = cur.getColumnIndex(Tabs.ARGUMENTS), idx_position = cur
					.getColumnIndex(Tabs.POSITION);
			while (!cur.isAfterLast()) {
				final int position = cur.getInt(idx_position) + HomeActivity.TAB_POSITION_MESSAGES + 1;
				final String icon_type = cur.getString(idx_icon);
				final String type = cur.getString(idx_type);
				final String name = cur.getString(idx_name);
				final Bundle args = parseArguments(cur.getString(idx_arguments));
				final Class<? extends Fragment> fragment = CUSTOM_TABS_FRAGMENT_MAP.get(type);
				if (name != null && fragment != null) {
					tabs.add(new TabSpec(name, getTabIconObject(icon_type), fragment, args, position));
				}
				cur.moveToNext();
			}
			cur.close();
		}
		return tabs;
	}

	public static String getTabTypeName(Context context, String type) {
		if (context == null) return null;
		final Integer res_id = CUSTOM_TABS_TYPE_NAME_MAP.get(type);
		return res_id != null ? context.getString(res_id) : null;
	}

	public static long getTimestampFromDate(Date date) {
		if (date == null) return -1;
		return date.getTime();
	}

	public static ImageSpec getTwitgooImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://twitgoo.com/show/thumb/" + id;
		final String full_size = "http://twitgoo.com/show/img/" + id;
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static ImageSpec getTwitpicImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://twitpic.com/show/thumb/" + id;
		final String full_size = "http://twitpic.com/show/large/" + id;
		return new ImageSpec(thumbnail_size, full_size);
	}

	public static ImageSpec getTwitterImage(String url) {
		if (isNullOrEmpty(url)) return null;
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
				setUserAgent(context, cb);
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

	public static int getUserColor(Context context, long user_id) {
		if (context == null) return Color.TRANSPARENT;
		Integer color = sUserColors.get(user_id);
		if (color == null) {
			final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			color = prefs.getInt(Long.toString(user_id), Color.TRANSPARENT);
			sUserColors.put(user_id, color);
		}
		return color != null ? color : Color.TRANSPARENT;
	}

	public static int getUserTypeIconRes(boolean is_verified, boolean is_protected) {
		if (is_verified)
			return R.drawable.ic_indicator_verified;
		else if (is_protected) return R.drawable.ic_indicator_is_protected;
		return 0;
	}

	public static ImageSpec getYfrogImage(String id) {
		if (isNullOrEmpty(id)) return null;
		final String thumbnail_size = "http://yfrog.com/" + id + ":small";
		final String full_size = "http://yfrog.com/" + id + ":medium";
		return new ImageSpec(thumbnail_size, full_size);

	}

	public static boolean isFiltered(Context context, String screen_name, String source, String text) {
		if (context == null) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String[] cols = new String[] { Filters.TEXT };
		Cursor cur;
		if (screen_name != null) {
			cur = resolver.query(Filters.Users.CONTENT_URI, cols, null, null, null);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (screen_name.equals(cur.getString(0))) return true;
				cur.moveToNext();
			}
			cur.close();
		}
		if (source != null) {
			cur = resolver.query(Filters.Sources.CONTENT_URI, cols, null, null, null);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (HtmlEscapeHelper.unescape(source).equals(cur.getString(0))) return true;
				cur.moveToNext();
			}
			cur.close();
		}
		if (text != null) {
			cur = resolver.query(Filters.Users.CONTENT_URI, cols, null, null, null);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (text.contains(cur.getString(0))) return true;
				cur.moveToNext();
			}
			cur.close();
		}
		return false;
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

	public static boolean isMyRetweet(ParcelableStatus status) {
		if (status == null) return false;
		return status.retweeted_by_id == status.account_id;
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
		values.put(Statuses.ACCOUNT_ID, account_id);
		values.put(Statuses.STATUS_ID, status.getId());
		final boolean is_retweet = status.isRetweet();
		final Status retweeted_status = is_retweet ? status.getRetweetedStatus() : null;
		if (retweeted_status != null) {
			final User retweet_user = status.getUser();
			values.put(Statuses.RETWEET_ID, retweeted_status.getId());
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
		values.put(Statuses.IS_RETWEET, is_retweet ? 1 : 0);
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

	public static int matchLinkId(Uri uri) {
		return LINK_HANDLER_URI_MATCHER.match(uri);
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

	public static boolean objectEquals(Object object1, Object object2) {
		if (object1 == null || object2 == null) return object1 == object2;
		return object1.equals(object2);
	}

	public static void openConversation(Activity activity, long account_id, long status_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new ConversationFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_STATUS_ID, status_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_CONVERSATION);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openDirectMessagesConversation(Activity activity, long account_id, long conversation_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof DMConversationFragment && details_fragment.isAdded()) {
				((DMConversationFragment) details_fragment).showConversation(account_id, conversation_id);
			} else {
				final Fragment fragment = new DMConversationFragment();
				final Bundle args = new Bundle();
				if (account_id > 0 && conversation_id > 0) {
					args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					if (conversation_id > 0) {
						args.putLong(INTENT_KEY_CONVERSATION_ID, conversation_id);
					}
				}
				fragment.setArguments(args);
				dual_pane_activity.showAtPane(PANE_RIGHT, fragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
			if (account_id > 0 && conversation_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
				if (conversation_id > 0) {
					builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, String.valueOf(conversation_id));
				}
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openSavedSearches(Activity activity, long account_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new SavedSearchesListFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_SAVED_SEARCHES);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openStatus(Activity activity, ParcelableStatus status) {
		if (activity == null || status == null) return;
		final long account_id = status.account_id, status_id = status.status_id;
		final Bundle bundle = new Bundle();
		bundle.putParcelable(INTENT_KEY_STATUS, status);
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof StatusFragment && details_fragment.isAdded()) {
				((StatusFragment) details_fragment).displayStatus(status);
				dual_pane_activity.bringRightPaneToFront();
			} else {
				final Fragment fragment = new StatusFragment();
				final Bundle args = new Bundle(bundle);
				args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				args.putLong(INTENT_KEY_STATUS_ID, status_id);
				fragment.setArguments(args);
				dual_pane_activity.showAtPane(DualPaneActivity.PANE_RIGHT, fragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());

			intent.putExtras(bundle);
			activity.startActivity(intent);
		}
	}

	public static void openTweetSearch(Activity activity, long account_id, String query) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new SearchTweetsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (query != null) {
				args.putString(INTENT_KEY_QUERY, query);
			}
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserBlocksListFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
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
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
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
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
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
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListCreatedFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListDetailsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListMembersFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListMembershipsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListSubscribersFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListSubscriptionsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListTimelineFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putInt(INTENT_KEY_LIST_ID, list_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			args.putString(INTENT_KEY_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListTypesFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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

	public static void openUserMentions(Activity activity, long account_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserMentionsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (screen_name != null) {
				args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_MENTIONS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openUserProfile(Activity activity, long account_id, long user_id, String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
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
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_RIGHT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserRetweetedStatusFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			if (status_id > 0) {
				args.putLong(INTENT_KEY_STATUS_ID, status_id);
			}
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
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
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
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

	public static Bundle parseArguments(String string) {
		final Bundle bundle = new Bundle();
		if (string != null) {
			try {
				final JSONObject json = new JSONObject(string);
				final Iterator<?> it = json.keys();
				while (it.hasNext()) {
					final Object key_obj = it.next();
					if (key_obj == null) {
						continue;
					}
					final String key = key_obj.toString();
					final Object value = json.get(key);
					if (value instanceof Boolean) {
						bundle.putBoolean(key, json.getBoolean(key));
					} else if (value instanceof Integer) {
						// Simple workaround for account_id
						if (INTENT_KEY_ACCOUNT_ID.equals(key)) {
							bundle.putLong(key, json.getLong(key));
						} else {
							bundle.putInt(key, json.getInt(key));
						}
					} else if (value instanceof Long) {
						bundle.putLong(key, json.getLong(key));
					} else if (value instanceof String) {
						bundle.putString(key, json.getString(key));
					} else {
						Log.w(LOGTAG, "Unknown type " + value.getClass().getSimpleName() + " in arguments key " + key);
					}
				}
			} catch (final JSONException e) {
				e.printStackTrace();
			} catch (final ClassCastException e) {
				e.printStackTrace();
			}
		}
		return bundle;
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
			itemDelete.setVisible(status.account_id == status.user_id && status.account_id != status.retweeted_by_id);
		}
		final MenuItem itemRetweet = menu.findItem(MENU_RETWEET);
		if (itemRetweet != null) {
			itemRetweet.setVisible(!status.is_protected && status.account_id != status.user_id);
			if (isMyRetweet(status)) {
				itemRetweet.setTitle(R.string.cancel_retweet);
			} else {
				itemRetweet.setTitle(R.string.retweet);
			}
		}
		final MenuItem itemFav = menu.findItem(MENU_FAV);
		if (itemFav != null) {
			final Drawable iconFav = itemFav.getIcon();
			if (status.is_favorite) {
				iconFav.mutate().setColorFilter(activated_color, Mode.MULTIPLY);
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

	public static void setUserAgent(Context context, ConfigurationBuilder cb) {
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean gzip_compressing = prefs.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, true);
		final PackageManager pm = context.getPackageManager();
		try {
			final PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			final String version_name = pi.versionName;
			cb.setClientVersion(pi.versionName);
			cb.setClientName(APP_NAME);
			cb.setClientURL(APP_PROJECT_URL);
			cb.setUserAgent(APP_NAME + " " + APP_PROJECT_URL + " / " + version_name
					+ (gzip_compressing ? " (gzip)" : ""));
		} catch (final PackageManager.NameNotFoundException e) {

		}
	}

	public static void setUserColor(Context context, long user_id, int color) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Long.toString(user_id), color);
		editor.commit();
		sUserColors.put(user_id, color);
	}

	public static void showErrorToast(Context context, Object e, boolean long_message) {
		if (context == null) return;
		final String message;
		if (e != null) {
			message = context.getString(R.string.error_message,
					e instanceof Throwable ? trimLineBreak(unescape(((Throwable) e).getMessage())) : parseString(e));
		} else {
			message = context.getString(R.string.error_unknown_error);
		}
		final int length = long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(context, message, length);
		toast.show();
	}

	public static String trimLineBreak(String orig) {
		if (orig == null) return null;
		return orig.replaceAll("\\n+", "\n");
	}

	private static Bitmap getTabIconFromFile(File file, Resources res) {
		if (file == null || !file.exists()) return null;
		final String path = file.getPath();
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, o);
		if (o.outHeight <= 0 || o.outWidth <= 0) return null;
		final BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = (int) (Math.max(o.outWidth, o.outHeight) / (48 * res.getDisplayMetrics().density));
		return BitmapFactory.decodeFile(path, o2);
	}

	private static void parseEntities(HtmlBuilder builder, EntitySupport entities) {
		final URLEntity[] urls = entities.getURLEntities();
		// Format media.
		final MediaEntity[] medias = entities.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity media : medias) {
				final URL media_url = media.getMediaURL();
				if (media_url != null) {
					builder.addLink(parseString(media_url), media.getDisplayURL(), media.getStart(), media.getEnd());
				}
			}
		}
		if (urls != null) {
			for (final URLEntity url : urls) {
				final URL expanded_url = url.getExpandedURL();
				if (expanded_url != null) {
					builder.addLink(parseString(expanded_url), url.getDisplayURL(), url.getStart(), url.getEnd());
				}
			}
		}
	}

}
