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

import static android.text.TextUtils.isEmpty;
import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.provider.TweetStore.CACHE_URIS;
import static org.mariotaku.twidere.provider.TweetStore.DIRECT_MESSAGES_URIS;
import static org.mariotaku.twidere.provider.TweetStore.STATUSES_URIS;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LongSparseArray;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.webkit.MimeTypeMap;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLAListView;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonConfiguration;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.mariotaku.gallery3d.ImageViewerGLActivity;
import org.mariotaku.jsonserializer.JSONSerializer;
import org.mariotaku.querybuilder.AllColumns;
import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.RawItemArray;
import org.mariotaku.querybuilder.SQLQueryBuilder;
import org.mariotaku.querybuilder.Selectable;
import org.mariotaku.querybuilder.Tables;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.CameraCropActivity;
import org.mariotaku.twidere.activity.MapViewerActivity;
import org.mariotaku.twidere.activity.support.DualPaneActivity;
import org.mariotaku.twidere.adapter.iface.IBaseAdapter;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.support.DirectMessagesConversationFragment;
import org.mariotaku.twidere.fragment.support.IncomingFriendshipsFragment;
import org.mariotaku.twidere.fragment.support.SavedSearchesListFragment;
import org.mariotaku.twidere.fragment.support.SearchFragment;
import org.mariotaku.twidere.fragment.support.SearchStatusesFragment;
import org.mariotaku.twidere.fragment.support.SensitiveContentWarningDialogFragment;
import org.mariotaku.twidere.fragment.support.StatusFragment;
import org.mariotaku.twidere.fragment.support.StatusRetweetersListFragment;
import org.mariotaku.twidere.fragment.support.StatusesListFragment;
import org.mariotaku.twidere.fragment.support.UserBlocksListFragment;
import org.mariotaku.twidere.fragment.support.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.support.UserFollowersFragment;
import org.mariotaku.twidere.fragment.support.UserFriendsFragment;
import org.mariotaku.twidere.fragment.support.UserListDetailsFragment;
import org.mariotaku.twidere.fragment.support.UserListMembersFragment;
import org.mariotaku.twidere.fragment.support.UserListMembershipsListFragment;
import org.mariotaku.twidere.fragment.support.UserListSubscribersFragment;
import org.mariotaku.twidere.fragment.support.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.support.UserListsListFragment;
import org.mariotaku.twidere.fragment.support.UserMentionsFragment;
import org.mariotaku.twidere.fragment.support.UserProfileFragment;
import org.mariotaku.twidere.fragment.support.UserTimelineFragment;
import org.mariotaku.twidere.fragment.support.UsersListFragment;
import org.mariotaku.twidere.model.DirectMessageCursorIndices;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.ParcelableUserMention;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.net.HttpClientImpl;

import twitter4j.DirectMessage;
import twitter4j.EntitySupport;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.TwipOModeAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

public final class Utils implements Constants {

	private static final String UA_TEMPLATE = "Mozilla/5.0 (Linux; Android %s; %s Build/%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.111 Safari/537.36";

	public static final Pattern PATTERN_XML_RESOURCE_IDENTIFIER = Pattern.compile("res\\/xml\\/([\\w_]+)\\.xml");
	public static final Pattern PATTERN_RESOURCE_IDENTIFIER = Pattern.compile("@([\\w_]+)\\/([\\w_]+)");

	private static final UriMatcher CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final UriMatcher LINK_HANDLER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_STATUSES, TABLE_ID_STATUSES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_ACCOUNTS, TABLE_ID_ACCOUNTS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_MENTIONS, TABLE_ID_MENTIONS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DRAFTS, TABLE_ID_DRAFTS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_USERS, TABLE_ID_CACHED_USERS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_USERS, TABLE_ID_FILTERED_USERS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_KEYWORDS, TABLE_ID_FILTERED_KEYWORDS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_SOURCES, TABLE_ID_FILTERED_SOURCES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_FILTERED_LINKS, TABLE_ID_FILTERED_LINKS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES, TABLE_ID_DIRECT_MESSAGES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_INBOX,
				TABLE_ID_DIRECT_MESSAGES_INBOX);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_OUTBOX,
				TABLE_ID_DIRECT_MESSAGES_OUTBOX);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATION + "/#/#",
				TABLE_ID_DIRECT_MESSAGES_CONVERSATION);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME
				+ "/#/*", TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY,
				TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TRENDS_LOCAL, TABLE_ID_TRENDS_LOCAL);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_TABS, TABLE_ID_TABS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_STATUSES, TABLE_ID_CACHED_STATUSES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_HASHTAGS, TABLE_ID_CACHED_HASHTAGS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_HASHTAGS, TABLE_ID_CACHED_HASHTAGS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_NOTIFICATIONS + "/#",
				VIRTUAL_TABLE_ID_NOTIFICATIONS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_NOTIFICATIONS, VIRTUAL_TABLE_ID_NOTIFICATIONS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_PERMISSIONS, VIRTUAL_TABLE_ID_PERMISSIONS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DNS + "/*", VIRTUAL_TABLE_ID_DNS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHED_IMAGES, VIRTUAL_TABLE_ID_CACHED_IMAGES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_CACHE_FILES + "/*",
				VIRTUAL_TABLE_ID_CACHE_FILES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_PREFERENCES, VIRTUAL_TABLE_ID_ALL_PREFERENCES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_PREFERENCES + "/*",
				VIRTUAL_TABLE_ID_PREFERENCES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_UNREAD_COUNTS, VIRTUAL_TABLE_ID_UNREAD_COUNTS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_UNREAD_COUNTS + "/#",
				VIRTUAL_TABLE_ID_UNREAD_COUNTS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_UNREAD_COUNTS + "/#/#/*",
				VIRTUAL_TABLE_ID_UNREAD_COUNTS);

		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS, null, LINK_ID_STATUS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER, null, LINK_ID_USER);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_TIMELINE, null, LINK_ID_USER_TIMELINE);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FOLLOWERS, null, LINK_ID_USER_FOLLOWERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FRIENDS, null, LINK_ID_USER_FRIENDS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FAVORITES, null, LINK_ID_USER_FAVORITES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_BLOCKS, null, LINK_ID_USER_BLOCKS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_DIRECT_MESSAGES_CONVERSATION, null,
				LINK_ID_DIRECT_MESSAGES_CONVERSATION);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST, null, LINK_ID_USER_LIST);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_TIMELINE, null, LINK_ID_USER_LIST_TIMELINE);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_MEMBERS, null, LINK_ID_USER_LIST_MEMBERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LIST_MEMBERSHIPS, null, LINK_ID_USER_LIST_MEMBERSHIPS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_LISTS, null, LINK_ID_USER_LISTS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SAVED_SEARCHES, null, LINK_ID_SAVED_SEARCHES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_MENTIONS, null, LINK_ID_USER_MENTIONS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_INCOMING_FRIENDSHIPS, null, LINK_ID_INCOMING_FRIENDSHIPS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USERS, null, LINK_ID_USERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUSES, null, LINK_ID_STATUSES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS_RETWEETERS, null, LINK_ID_STATUS_RETWEETERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SEARCH, null, LINK_ID_SEARCH);

	}
	private static LongSparseArray<Integer> sAccountColors = new LongSparseArray<Integer>();
	private static LongSparseArray<Integer> sUserColors = new LongSparseArray<Integer>();

	private static LongSparseArray<String> sUserNicknames = new LongSparseArray<String>();
	private static LongSparseArray<String> sAccountScreenNames = new LongSparseArray<String>();
	private static LongSparseArray<String> sAccountNames = new LongSparseArray<String>();

	static final String MAPS_STATIC_IMAGE_URI_TEMPLATE = "https://maps.googleapis.com/maps/api/staticmap?zoom=%d&size=%dx%d&sensor=false&language=%s&center=%f,%f&markers=%f,%f";

	private Utils() {
		throw new AssertionError("You are trying to create an instance for this utility class!");
	}

	public static void addIntentToMenu(final Context context, final Menu menu, final Intent query_intent) {
		addIntentToMenu(context, menu, query_intent, Menu.NONE);
	}

	public static void addIntentToMenu(final Context context, final Menu menu, final Intent queryIntent,
			final int groupId) {
		if (context == null || menu == null || queryIntent == null) return;
		final PackageManager pm = context.getPackageManager();
		final Resources res = context.getResources();
		final float density = res.getDisplayMetrics().density;
		final List<ResolveInfo> activities = pm.queryIntentActivities(queryIntent, 0);
		for (final ResolveInfo info : activities) {
			final Intent intent = new Intent(queryIntent);
			final Drawable icon = info.loadIcon(pm);
			intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
			final MenuItem item = menu.add(groupId, Menu.NONE, Menu.NONE, info.loadLabel(pm));
			item.setIntent(intent);
			if (icon instanceof BitmapDrawable) {
				final int paddings = Math.round(density * 4);
				final Bitmap orig = ((BitmapDrawable) icon).getBitmap();
				final Bitmap bitmap = Bitmap.createBitmap(orig.getWidth() + paddings * 2, orig.getHeight() + paddings
						* 2, Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);
				canvas.drawBitmap(orig, paddings, paddings, null);
				item.setIcon(new BitmapDrawable(res, bitmap));
			} else {
				item.setIcon(icon);
			}
		}
	}

	public static void announceForAccessibilityCompat(final Context context, final View view, final CharSequence text,
			final Class<?> cls) {
		final AccessibilityManager accessibilityManager = (AccessibilityManager) context
				.getSystemService(Context.ACCESSIBILITY_SERVICE);
		if (!accessibilityManager.isEnabled()) return;
		// Prior to SDK 16, announcements could only be made through FOCUSED
		// events. Jelly Bean (SDK 16) added support for speaking text verbatim
		// using the ANNOUNCEMENT event type.
		final int eventType;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;
		} else {
			eventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT;
		}

		// Construct an accessibility event with the minimum recommended
		// attributes. An event without a class name or package may be dropped.
		final AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
		event.getText().add(text);
		event.setClassName(cls.getName());
		event.setPackageName(context.getPackageName());
		event.setSource(view);

		// Sends the event directly through the accessibility manager. If your
		// application only targets SDK 14+, you should just call
		// getParent().requestSendAccessibilityEvent(this, event);
		accessibilityManager.sendAccessibilityEvent(event);
	}

	public static Uri appendQueryParameters(final Uri uri, final NameValuePair... params) {
		final Uri.Builder builder = uri.buildUpon();
		if (params != null) {
			for (final NameValuePair param : params) {
				builder.appendQueryParameter(param.getName(), param.getValue());
			}
		}
		return builder.build();
	}

	public static String buildActivatedStatsWhereClause(final Context context, final String selection) {
		if (context == null) return null;
		final long[] account_ids = getActivatedAccountIds(context);
		final Where account_where = Where.in(new Column(Statuses.ACCOUNT_ID), new RawItemArray(account_ids));
		if (selection != null) {
			account_where.and(new Where(selection));
		}
		return account_where.getSQL();
	}

	public static Uri buildDirectMessageConversationUri(final long account_id, final long conversation_id,
			final String screen_name) {
		if (conversation_id <= 0 && screen_name == null) return TweetStore.CONTENT_URI_NULL;
		final Uri.Builder builder = conversation_id > 0 ? DirectMessages.Conversation.CONTENT_URI.buildUpon()
				: DirectMessages.Conversation.CONTENT_URI_SCREEN_NAME.buildUpon();
		builder.appendPath(String.valueOf(account_id));
		builder.appendPath(conversation_id > 0 ? String.valueOf(conversation_id) : screen_name);
		return builder.build();
	}

	public static String buildStatusFilterWhereClause(final String table, final String selection,
			final boolean enable_in_rts) {
		if (table == null) return null;
		final StringBuilder builder = new StringBuilder();
		if (selection != null) {
			builder.append(selection);
			builder.append(" AND ");
		}
		builder.append(Statuses._ID + " NOT IN ( ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table);
		builder.append(" WHERE " + table + "." + Statuses.USER_ID + " IN ( SELECT " + TABLE_FILTERED_USERS + "."
				+ Filters.Users.USER_ID + " FROM " + TABLE_FILTERED_USERS + " )");
		if (enable_in_rts) {
			builder.append(" OR " + table + "." + Statuses.RETWEETED_BY_USER_ID + " IN ( SELECT "
					+ TABLE_FILTERED_USERS + "." + Filters.Users.USER_ID + " FROM " + TABLE_FILTERED_USERS + " )");
		}
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
				+ TABLE_FILTERED_SOURCES);
		builder.append(" WHERE " + table + "." + Statuses.SOURCE + " LIKE '%>'||" + TABLE_FILTERED_SOURCES + "."
				+ Filters.Sources.VALUE + "||'</a>%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", "
				+ TABLE_FILTERED_KEYWORDS);
		builder.append(" WHERE " + table + "." + Statuses.TEXT_PLAIN + " LIKE '%'||" + TABLE_FILTERED_KEYWORDS + "."
				+ Filters.Keywords.VALUE + "||'%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" UNION ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table + ", " + TABLE_FILTERED_LINKS);
		builder.append(" WHERE " + table + "." + Statuses.TEXT_HTML + " LIKE '%<a href=\"%'||" + TABLE_FILTERED_LINKS
				+ "." + Filters.Links.VALUE + "||'%\">%'");
		builder.append(" OR " + table + "." + Statuses.TEXT_HTML + " LIKE '%>%'||" + TABLE_FILTERED_LINKS + "."
				+ Filters.Links.VALUE + "||'%</a>%'");
		builder.append(" AND " + table + "." + Statuses.IS_GAP + " IS NULL");
		builder.append(" OR " + table + "." + Statuses.IS_GAP + " == 0");
		builder.append(" )");
		return builder.toString();
	}

	public static int calculateInSampleSize(final int width, final int height, final int preferredWidth,
			final int preferredHeight) {
		if (preferredHeight > height && preferredWidth > width) return 1;
		return Math.round(Math.max(width, height) / (float) Math.max(preferredWidth, preferredHeight));
	}

	public static int cancelRetweet(final AsyncTwitterWrapper wrapper, final ParcelableStatus status) {
		if (wrapper == null || status == null) return -1;
		if (status.my_retweet_id > 0)
			return wrapper.destroyStatusAsync(status.account_id, status.my_retweet_id);
		else if (status.retweeted_by_id == status.account_id)
			return wrapper.destroyStatusAsync(status.account_id, status.retweet_id);
		return -1;
	}

	public static boolean checkActivityValidity(final Context context, final Intent intent) {
		final PackageManager pm = context.getPackageManager();
		return !pm.queryIntentActivities(intent, 0).isEmpty();
	}

	public static synchronized void cleanDatabasesByItemLimit(final Context context) {
		if (context == null) return;
		final ContentResolver resolver = context.getContentResolver();
		final int item_limit = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_DATABASE_ITEM_LIMIT, PREFERENCE_DEFAULT_DATABASE_ITEM_LIMIT);

		for (final long account_id : getAccountIds(context)) {
			// Clean statuses.
			for (final Uri uri : STATUSES_URIS) {
				if (CachedStatuses.CONTENT_URI.equals(uri)) {
					continue;
				}
				final String table = getTableNameByUri(uri);
				final Where account_where = new Where(Statuses.ACCOUNT_ID + " = " + account_id);
				final SQLQueryBuilder qb = new SQLQueryBuilder();
				qb.select(new Column(Statuses._ID)).from(new Tables(table));
				qb.where(new Where(Statuses.ACCOUNT_ID + " = " + account_id));
				qb.orderBy(new OrderBy(Statuses.STATUS_ID + " DESC"));
				qb.limit(item_limit);
				final Where where = Where.notIn(new Column(Statuses._ID), qb.build()).and(account_where);
				resolver.delete(uri, where.getSQL(), null);
			}
			for (final Uri uri : DIRECT_MESSAGES_URIS) {
				final String table = getTableNameByUri(uri);
				final Where account_where = new Where(DirectMessages.ACCOUNT_ID + " = " + account_id);
				final SQLQueryBuilder qb = new SQLQueryBuilder();
				qb.select(new Column(DirectMessages._ID)).from(new Tables(table));
				qb.where(new Where(DirectMessages.ACCOUNT_ID + " = " + account_id));
				qb.orderBy(new OrderBy(DirectMessages.MESSAGE_ID + " DESC"));
				qb.limit(item_limit);
				final Where where = Where.notIn(new Column(DirectMessages._ID), qb.build()).and(account_where);
				resolver.delete(uri, where.getSQL(), null);
			}
		}
		// Clean cached values.
		for (final Uri uri : CACHE_URIS) {
			final String table = getTableNameByUri(uri);
			final SQLQueryBuilder qb = new SQLQueryBuilder();
			qb.select(new Column(BaseColumns._ID)).from(new Tables(table));
			final Where where = Where.notIn(new Column(Statuses._ID), qb.build());
			resolver.delete(uri, where.getSQL(), null);
		}
	}

	public static void clearAccountColor() {
		sAccountColors.clear();
	}

	public static void clearAccountName() {
		sAccountScreenNames.clear();
	}

	public static void clearListViewChoices(final ListView view) {
		if (view == null) return;
		final ListAdapter adapter = view.getAdapter();
		if (adapter == null) return;
		view.clearChoices();
		view.setChoiceMode(ListView.CHOICE_MODE_NONE);
		// Workaround for Android bug
		// http://stackoverflow.com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode
		final int position = view.getFirstVisiblePosition(), offset = Utils.getFirstChildOffset(view);
		view.setAdapter(adapter);
		Utils.scrollListToPosition(view, position, offset);
	}

	public static void clearListViewChoices(final MultiColumnListView view) {
		if (view == null) return;
		final ListAdapter adapter = view.getAdapter();
		if (adapter == null) return;
		view.clearChoices();
		view.setChoiceMode(MultiColumnListView.CHOICE_MODE_NONE);
		view.invalidateViews();
		// Workaround for Android bug
		// http://stackoverflow.com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode
		// final int position = view.getFirstVisiblePosition();
		// view.setAdapter(adapter);
		// Utils.scrollListToPosition(view, position);
	}

	public static void clearUserColor(final Context context, final long user_id) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
		sUserColors.remove(user_id);
	}

	public static void clearUserNickname(final Context context, final long user_id) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
		sUserNicknames.remove(user_id);
	}

	public static boolean closeSilently(final Closeable c) {
		if (c == null) return false;
		try {
			c.close();
		} catch (final IOException e) {
			return false;
		}
		return true;
	}

	public static void configBaseAdapter(final Context context, final IBaseAdapter adapter) {
		if (context == null) return;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		adapter.setDisplayProfileImage(pref.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true));
		adapter.setDisplayNameFirst(pref.getBoolean(PREFERENCE_KEY_NAME_FIRST, true));
		adapter.setLinkHighlightOption(pref.getString(PREFERENCE_KEY_LINK_HIGHLIGHT_OPTION, LINK_HIGHLIGHT_OPTION_NONE));
		adapter.setNicknameOnly(pref.getBoolean(PREFERENCE_KEY_NICKNAME_ONLY, false));
		adapter.setTextSize(pref.getInt(PREFERENCE_KEY_TEXT_SIZE, getDefaultTextSize(context)));
	}

	public static void configBaseCardAdapter(final Context context, final IBaseCardAdapter adapter) {
		if (context == null) return;
		configBaseAdapter(context, adapter);
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		adapter.setAnimationEnabled(pref.getBoolean(PREFERENCE_KEY_CARD_ANIMATION, true));
	}

	public static void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final int buffer_size = 8192;
		final byte[] bytes = new byte[buffer_size];
		int count = is.read(bytes, 0, buffer_size);
		while (count != -1) {
			os.write(bytes, 0, count);
			count = is.read(bytes, 0, buffer_size);
		}
	}

	public static Fragment createFragmentForIntent(final Context context, final Intent intent) {
		final Bundle extras = intent.getExtras();
		final Uri uri = intent.getData();
		final Fragment fragment;
		if (uri == null) return null;
		final Bundle args = new Bundle();
		if (extras != null) {
			args.putAll(extras);
		}
		switch (matchLinkId(uri)) {
			case LINK_ID_STATUS: {
				fragment = new StatusFragment();
				if (!args.containsKey(EXTRA_STATUS_ID)) {
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					args.putLong(EXTRA_STATUS_ID, ParseUtils.parseLong(param_status_id));
				}
				break;
			}
			case LINK_ID_USER: {
				fragment = new UserProfileFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				break;
			}
			case LINK_ID_USER_LIST_MEMBERSHIPS: {
				fragment = new UserListMembershipsListFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				break;
			}
			case LINK_ID_USER_TIMELINE: {
				fragment = new UserTimelineFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				if (isEmpty(param_screen_name) && isEmpty(param_user_id)) return null;
				break;
			}
			case LINK_ID_USER_FAVORITES: {
				fragment = new UserFavoritesFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				if (isEmpty(param_screen_name) && isEmpty(param_user_id)) return null;
				break;
			}
			case LINK_ID_USER_FOLLOWERS: {
				fragment = new UserFollowersFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				if (isEmpty(param_screen_name) && isEmpty(param_user_id)) return null;
				break;
			}
			case LINK_ID_USER_FRIENDS: {
				fragment = new UserFriendsFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				if (isEmpty(param_screen_name) && isEmpty(param_user_id)) return null;
				break;
			}
			case LINK_ID_USER_BLOCKS: {
				fragment = new UserBlocksListFragment();
				break;
			}
			case LINK_ID_DIRECT_MESSAGES_CONVERSATION: {
				fragment = new DirectMessagesConversationFragment();
				final String param_conversation_id = uri.getQueryParameter(QUERY_PARAM_CONVERSATION_ID);
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final long conversation_id = ParseUtils.parseLong(param_conversation_id);
				if (conversation_id > 0) {
					args.putLong(EXTRA_CONVERSATION_ID, conversation_id);
				} else if (param_screen_name != null) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				break;
			}
			case LINK_ID_USER_LIST: {
				fragment = new UserListDetailsFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
				final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
				if (isEmpty(param_list_id)
						&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id)))
					return null;
				args.putInt(EXTRA_LIST_ID, ParseUtils.parseInt(param_list_id));
				args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				args.putString(EXTRA_LIST_NAME, param_list_name);
				break;
			}
			case LINK_ID_USER_LISTS: {
				fragment = new UserListsListFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				if (!args.containsKey(EXTRA_SCREEN_NAME)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (!args.containsKey(EXTRA_USER_ID)) {
					args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				}
				if (isEmpty(param_screen_name) && isEmpty(param_user_id)) return null;
				break;
			}
			case LINK_ID_USER_LIST_TIMELINE: {
				fragment = new UserListTimelineFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
				final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
				if (isEmpty(param_list_id)
						&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id)))
					return null;
				args.putInt(EXTRA_LIST_ID, ParseUtils.parseInt(param_list_id));
				args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				args.putString(EXTRA_LIST_NAME, param_list_name);
				break;
			}
			case LINK_ID_USER_LIST_MEMBERS: {
				fragment = new UserListMembersFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
				final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
				if (isEmpty(param_list_id)
						&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id)))
					return null;
				args.putInt(EXTRA_LIST_ID, ParseUtils.parseInt(param_list_id));
				args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				args.putString(EXTRA_LIST_NAME, param_list_name);
				break;
			}
			case LINK_ID_LIST_SUBSCRIBERS: {
				fragment = new UserListSubscribersFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
				final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
				final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
				if (isEmpty(param_list_id)
						&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id)))
					return null;
				args.putInt(EXTRA_LIST_ID, ParseUtils.parseInt(param_list_id));
				args.putLong(EXTRA_USER_ID, ParseUtils.parseLong(param_user_id));
				args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				args.putString(EXTRA_LIST_NAME, param_list_name);
				break;
			}
			case LINK_ID_SAVED_SEARCHES: {
				fragment = new SavedSearchesListFragment();
				break;
			}
			case LINK_ID_USER_MENTIONS: {
				fragment = new UserMentionsFragment();
				final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
				if (!args.containsKey(EXTRA_SCREEN_NAME) && !isEmpty(param_screen_name)) {
					args.putString(EXTRA_SCREEN_NAME, param_screen_name);
				}
				if (isEmpty(args.getString(EXTRA_SCREEN_NAME))) return null;
				break;
			}
			case LINK_ID_INCOMING_FRIENDSHIPS: {
				fragment = new IncomingFriendshipsFragment();
				break;
			}
			case LINK_ID_USERS: {
				fragment = new UsersListFragment();
				break;
			}
			case LINK_ID_STATUSES: {
				fragment = new StatusesListFragment();
				break;
			}
			case LINK_ID_STATUS_RETWEETERS: {
				fragment = new StatusRetweetersListFragment();
				if (!args.containsKey(EXTRA_STATUS_ID)) {
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					args.putLong(EXTRA_STATUS_ID, ParseUtils.parseLong(param_status_id));
				}
				break;
			}
			case LINK_ID_SEARCH: {
				final String param_query = uri.getQueryParameter(QUERY_PARAM_QUERY);
				if (isEmpty(param_query)) return null;
				args.putString(EXTRA_QUERY, param_query);
				fragment = new SearchFragment();
				break;
			}
			default: {
				return null;
			}
		}
		final String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
		if (param_account_id != null) {
			args.putLong(EXTRA_ACCOUNT_ID, ParseUtils.parseLong(param_account_id));
		} else {
			final String param_account_name = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME);
			if (param_account_name != null) {
				args.putLong(EXTRA_ACCOUNT_ID, getAccountId(context, param_account_name));
			} else {
				final long account_id = getDefaultAccountId(context);
				if (isMyAccount(context, account_id)) {
					args.putLong(EXTRA_ACCOUNT_ID, account_id);
				}
			}
		}
		if (fragment != null) {
			fragment.setArguments(args);
		}
		return fragment;
	}

	public static Intent createPickImageIntent(final Uri uri) {
		final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		return intent;
	}

	public static Intent createPickImageIntent(final Uri uri, final Integer outputX, final Integer outputY,
			final Integer aspectX, final Integer aspectY, final boolean scaleUpIfNeeded) {
		final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		intent.setType("image/*");
		if (outputX != null && outputY != null) {
			intent.putExtra(CameraCropActivity.EXTRA_OUTPUT_X, outputX);
			intent.putExtra(CameraCropActivity.EXTRA_OUTPUT_Y, outputY);
		}
		if (aspectX != null && aspectY != null) {
			intent.putExtra(CameraCropActivity.EXTRA_ASPECT_X, aspectX);
			intent.putExtra(CameraCropActivity.EXTRA_ASPECT_Y, aspectY);
		}
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", scaleUpIfNeeded);
		intent.putExtra("crop", "true");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		return intent;
	}

	public static Intent createTakePhotoIntent(final Uri uri) {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		return intent;
	}

	public static Intent createTakePhotoIntent(final Uri uri, final Integer outputX, final Integer outputY,
			final Integer aspectX, final Integer aspectY, final boolean scaleUpIfNeeded) {
		final Intent intent = new Intent(CameraCropActivity.INTENT_ACTION);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		if (outputX != null && outputY != null) {
			intent.putExtra(CameraCropActivity.EXTRA_OUTPUT_X, outputX);
			intent.putExtra(CameraCropActivity.EXTRA_OUTPUT_Y, outputY);
		}
		if (aspectX != null && aspectY != null) {
			intent.putExtra(CameraCropActivity.EXTRA_ASPECT_X, aspectX);
			intent.putExtra(CameraCropActivity.EXTRA_ASPECT_Y, aspectY);
		}
		intent.putExtra(CameraCropActivity.EXTRA_SCALE_UP_IF_NEEDED, scaleUpIfNeeded);
		return intent;
	}

	public static boolean downscaleImageIfNeeded(final File imageFile, final int quality) {
		if (imageFile == null || !imageFile.isFile()) return false;
		final String path = imageFile.getAbsolutePath();
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, o);
		// Corrupted image, so return now.
		if (o.outWidth <= 0 || o.outHeight <= 0) return false;
		o.inJustDecodeBounds = false;
		if (o.outWidth > TWITTER_MAX_IMAGE_WIDTH || o.outHeight > TWITTER_MAX_IMAGE_HEIGHT) {
			// The image dimension is larger than Twitter's limit.
			o.inSampleSize = calculateInSampleSize(o.outWidth, o.outHeight, TWITTER_MAX_IMAGE_WIDTH,
					TWITTER_MAX_IMAGE_HEIGHT);
			try {
				final Bitmap b = BitmapDecodeHelper.decode(path, o);
				final Bitmap.CompressFormat format = getBitmapCompressFormatByMimetype(o.outMimeType,
						Bitmap.CompressFormat.PNG);
				final FileOutputStream fos = new FileOutputStream(imageFile);
				return b.compress(format, quality, fos);
			} catch (final OutOfMemoryError e) {
				return false;
			} catch (final FileNotFoundException e) {
				// This shouldn't happen.
			}
		} else if (imageFile.length() > TWITTER_MAX_IMAGE_SIZE) {
			// The file size is larger than Twitter's limit.
			try {
				final Bitmap b = BitmapDecodeHelper.decode(path, o);
				final FileOutputStream fos = new FileOutputStream(imageFile);
				return b.compress(Bitmap.CompressFormat.JPEG, 80, fos);
			} catch (final OutOfMemoryError e) {
				return false;
			} catch (final FileNotFoundException e) {
				// This shouldn't happen.
			}
		}
		return true;
	}

	public static String encodeQueryParams(final String value) throws IOException {
		final String encoded = URLEncoder.encode(value, "UTF-8");
		final StringBuilder buf = new StringBuilder();
		final int length = encoded.length();
		char focus;
		for (int i = 0; i < length; i++) {
			focus = encoded.charAt(i);
			if (focus == '*') {
				buf.append("%2A");
			} else if (focus == '+') {
				buf.append("%20");
			} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
					&& encoded.charAt(i + 2) == 'E') {
				buf.append('~');
				i += 2;
			} else {
				buf.append(focus);
			}
		}
		return buf.toString();
	}

	public static ParcelableDirectMessage findDirectMessageInDatabases(final Context context, final long account_id,
			final long message_id) {
		if (context == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		ParcelableDirectMessage message = null;
		final String where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND " + DirectMessages.MESSAGE_ID
				+ " = " + message_id;
		for (final Uri uri : DIRECT_MESSAGES_URIS) {
			final Cursor cur = ContentResolverUtils.query(resolver, uri, DirectMessages.COLUMNS, where, null, null);
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

	public static ParcelableStatus findStatus(final Context context, final long account_id, final long status_id)
			throws TwitterException {
		if (context == null || account_id <= 0 || status_id <= 0) return null;
		final ParcelableStatus p_status = findStatusInDatabases(context, account_id, status_id);
		if (p_status != null) return p_status;
		final Twitter twitter = getTwitterInstance(context, account_id, true);
		if (twitter == null) return null;
		final Status status = twitter.showStatus(status_id);
		if (status == null || status.getId() <= 0) return null;
		final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = "
				+ status.getId();
		final ContentResolver resolver = context.getContentResolver();
		final boolean large_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
		resolver.delete(CachedStatuses.CONTENT_URI, where, null);
		resolver.insert(CachedStatuses.CONTENT_URI, makeStatusContentValues(status, account_id, large_profile_image));
		return new ParcelableStatus(status, account_id, false, large_profile_image);
	}

	public static ParcelableStatus findStatusInDatabases(final Context context, final long account_id,
			final long status_id) {
		if (context == null) return null;
		final ContentResolver resolver = context.getContentResolver();
		ParcelableStatus status = null;
		final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.STATUS_ID + " = "
				+ status_id;
		for (final Uri uri : STATUSES_URIS) {
			final Cursor cur = ContentResolverUtils.query(resolver, uri, Statuses.COLUMNS, where, null, null);
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

	public static String formatDirectMessageText(final DirectMessage message) {
		if (message == null) return null;
		final String text = message.getRawText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		parseEntities(builder, message);
		return builder.build().replace("\n", "<br/>");
	}

	public static String formatExpandedUserDescription(final User user) {
		if (user == null) return null;
		final String text = user.getDescription();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		final URLEntity[] urls = user.getDescriptionEntities();
		if (urls != null) {
			for (final URLEntity url : urls) {
				final String expanded_url = ParseUtils.parseString(url.getExpandedURL());
				if (expanded_url != null) {
					builder.addLink(expanded_url, expanded_url, url.getStart(), url.getEnd());
				}
			}
		}
		return toPlainText(builder.build().replace("\n", "<br/>"));
	}

	@SuppressWarnings("deprecation")
	public static String formatSameDayTime(final Context context, final long timestamp) {
		if (context == null) return null;
		if (DateUtils.isToday(timestamp))
			return DateUtils.formatDateTime(context, timestamp,
					DateFormat.is24HourFormat(context) ? DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR
							: DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_12HOUR);
		return DateUtils.formatDateTime(context, timestamp, DateUtils.FORMAT_SHOW_DATE);
	}

	public static String formatStatusText(final Status status) {
		if (status == null) return null;
		final String text = status.getRawText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		parseEntities(builder, status);
		return builder.build().replace("\n", "<br/>");
	}

	@SuppressWarnings("deprecation")
	public static String formatTimeStampString(final Context context, final long timestamp) {
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
	public static String formatTimeStampString(final Context context, final String date_time) {
		if (context == null) return null;
		return formatTimeStampString(context, Date.parse(date_time));
	}

	@SuppressWarnings("deprecation")
	public static String formatToLongTimeString(final Context context, final long timestamp) {
		if (context == null) return null;
		final Time then = new Time();
		then.set(timestamp);
		final Time now = new Time();
		now.setToNow();

		int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

		format_flags |= DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME;

		return DateUtils.formatDateTime(context, timestamp, format_flags);
	}

	public static String formatUserDescription(final User user) {
		if (user == null) return null;
		final String text = user.getDescription();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		final URLEntity[] urls = user.getDescriptionEntities();
		if (urls != null) {
			for (final URLEntity url : urls) {
				final URL expanded_url = url.getExpandedURL();
				if (expanded_url != null) {
					builder.addLink(ParseUtils.parseString(expanded_url), url.getDisplayURL(), url.getStart(),
							url.getEnd());
				}
			}
		}
		return builder.build().replace("\n", "<br/>");
	}

	public static String generateBrowserUserAgent() {
		return String.format(UA_TEMPLATE, Build.VERSION.RELEASE, Build.MODEL, Build.ID);
	}

	public static int getAccountColor(final Context context, final long account_id) {
		if (context == null) return Color.TRANSPARENT;
		final Integer cached = sAccountColors.get(account_id);
		if (cached != null) return cached;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				new String[] { Accounts.USER_COLOR }, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
		if (cur == null) return Color.TRANSPARENT;
		try {
			if (cur.getCount() > 0 && cur.moveToFirst()) {
				final int color = cur.getInt(0);
				sAccountColors.put(account_id, color);
				return color;
			}
			return Color.TRANSPARENT;
		} finally {
			cur.close();
		}
	}

	public static int[] getAccountColors(final Context context, final long[] account_ids) {
		if (context == null || account_ids == null) return new int[0];
		final String[] cols = new String[] { Accounts.USER_COLOR };
		final String where = Where.in(new Column(Accounts.ACCOUNT_ID), new RawItemArray(account_ids)).getSQL();
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI, cols, where,
				null, null);
		if (cur == null) return new int[0];
		try {
			cur.moveToFirst();
			final int[] colors = new int[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				colors[i++] = cur.getInt(0);
				cur.moveToNext();
			}
			return colors;
		} finally {
			cur.close();
		}
	}

	public static long getAccountId(final Context context, final String screen_name) {
		if (context == null || isEmpty(screen_name)) return -1;
		final Cursor cur = ContentResolverUtils
				.query(context.getContentResolver(), Accounts.CONTENT_URI, new String[] { Accounts.ACCOUNT_ID },
						Accounts.SCREEN_NAME + " = ?", new String[] { screen_name }, null);
		if (cur == null) return -1;
		try {
			if (cur.getCount() > 0 && cur.moveToFirst()) return cur.getLong(0);
			return -1;
		} finally {
			cur.close();
		}
	}

	public static long[] getAccountIds(final Context context) {
		if (context == null) return new long[0];
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				new String[] { Accounts.ACCOUNT_ID }, null, null, null);
		if (cur == null) return new long[0];
		try {
			cur.moveToFirst();
			final long[] ids = new long[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				ids[i++] = cur.getLong(0);
				cur.moveToNext();
			}
			return ids;
		} finally {
			cur.close();
		}
	}

	public static String getAccountName(final Context context, final long account_id) {
		if (context == null) return null;
		final String cached = sAccountNames.get(account_id);
		if (!isEmpty(cached)) return cached;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				new String[] { Accounts.NAME }, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
		if (cur == null) return null;
		try {
			if (cur.getCount() > 0 && cur.moveToFirst()) {
				final String name = cur.getString(0);
				sAccountNames.put(account_id, name);
				return name;
			}
			return null;
		} finally {
			cur.close();
		}
	}

	public static String[] getAccountNames(final Context context) {
		return getAccountScreenNames(context, null);
	}

	public static String[] getAccountNames(final Context context, final long[] account_ids) {
		if (context == null) return new String[0];
		final String[] cols = new String[] { Accounts.NAME };
		final String where = account_ids != null ? Where.in(new Column(Accounts.ACCOUNT_ID),
				new RawItemArray(account_ids)).getSQL() : null;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI, cols, where,
				null, null);
		if (cur == null) return new String[0];
		try {
			cur.moveToFirst();
			final String[] names = new String[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				names[i++] = cur.getString(0);
				cur.moveToNext();
			}
			return names;
		} finally {
			cur.close();
		}
	}

	public static String getAccountScreenName(final Context context, final long account_id) {
		if (context == null) return null;
		final String cached = sAccountScreenNames.get(account_id);
		if (!isEmpty(cached)) return cached;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				new String[] { Accounts.SCREEN_NAME }, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
		if (cur == null) return null;
		try {
			if (cur.getCount() > 0 && cur.moveToFirst()) {
				final String name = cur.getString(0);
				sAccountScreenNames.put(account_id, name);
				return name;
			}
			return null;
		} finally {
			cur.close();
		}
	}

	public static String[] getAccountScreenNames(final Context context) {
		return getAccountScreenNames(context, false);
	}

	public static String[] getAccountScreenNames(final Context context, final boolean include_at_char) {
		return getAccountScreenNames(context, null, include_at_char);
	}

	public static String[] getAccountScreenNames(final Context context, final long[] account_ids) {
		return getAccountScreenNames(context, account_ids, false);
	}

	public static String[] getAccountScreenNames(final Context context, final long[] account_ids,
			final boolean include_at_char) {
		if (context == null) return new String[0];
		final String[] cols = new String[] { Accounts.SCREEN_NAME };
		final String where = account_ids != null ? Where.in(new Column(Accounts.ACCOUNT_ID),
				new RawItemArray(account_ids)).getSQL() : null;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI, cols, where,
				null, null);
		if (cur == null) return new String[0];
		try {
			cur.moveToFirst();
			final String[] screen_names = new String[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				screen_names[i++] = cur.getString(0);
				cur.moveToNext();
			}
			return screen_names;
		} finally {
			cur.close();
		}
	}

	public static long[] getActivatedAccountIds(final Context context) {
		if (context == null) return new long[0];
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				new String[] { Accounts.ACCOUNT_ID }, Accounts.IS_ACTIVATED + " = 1", null, null);
		if (cur == null) return new long[0];
		try {
			cur.moveToFirst();
			final long[] ids = new long[cur.getCount()];
			int i = 0;
			while (!cur.isAfterLast()) {
				ids[i++] = cur.getLong(0);
				cur.moveToNext();
			}
			return ids;
		} finally {
			cur.close();
		}
	}

	public static int getAllStatusesCount(final Context context, final Uri uri) {
		if (context == null) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = ContentResolverUtils.query(resolver, uri, new String[] { Statuses.STATUS_ID },
				buildStatusFilterWhereClause(getTableNameByUri(uri), null, shouldEnableFiltersForRTs(context)), null,
				null);
		if (cur == null) return 0;
		try {
			return cur.getCount();
		} finally {
			cur.close();
		}
	}

	public static long[] getAllStatusesIds(final Context context, final Uri uri) {
		if (context == null) return new long[0];
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = ContentResolverUtils.query(resolver, uri, new String[] { Statuses.STATUS_ID },
				buildStatusFilterWhereClause(getTableNameByUri(uri), null, shouldEnableFiltersForRTs(context)), null,
				null);
		if (cur == null) return new long[0];
		final long[] ids = new long[cur.getCount()];
		cur.moveToFirst();
		int i = 0;
		while (!cur.isAfterLast()) {
			ids[i] = cur.getLong(0);
			cur.moveToNext();
			i++;
		}
		cur.close();
		return ids;
	}

	public static String getBestBannerType(final int width) {
		if (width <= 320)
			return "mobile";
		else if (width <= 520)
			return "web";
		else if (width <= 626)
			return "ipad";
		else if (width <= 640)
			return "mobile_retina";
		else if (width <= 1040)
			return "web_retina";
		else
			return "ipad_retina";
	}

	public static File getBestCacheDir(final Context context, final String cache_dir_name) {
		final File ext_cache_dir = context.getExternalCacheDir();
		if (ext_cache_dir != null && ext_cache_dir.isDirectory()) {
			final File cache_dir = new File(ext_cache_dir, cache_dir_name);
			if (cache_dir.isDirectory() || cache_dir.mkdirs()) return cache_dir;
		}
		return new File(context.getCacheDir(), cache_dir_name);
	}

	public static String getBiggerTwitterProfileImage(final String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "_bigger");
		return url;
	}

	public static Bitmap getBitmap(final Drawable drawable) {
		if (drawable instanceof NinePatchDrawable) return null;
		if (drawable instanceof BitmapDrawable)
			return ((BitmapDrawable) drawable).getBitmap();
		else if (drawable instanceof TransitionDrawable) {
			final int layer_count = ((TransitionDrawable) drawable).getNumberOfLayers();
			for (int i = 0; i < layer_count; i++) {
				final Drawable layer = ((TransitionDrawable) drawable).getDrawable(i);
				if (layer instanceof BitmapDrawable) return ((BitmapDrawable) layer).getBitmap();
			}
		}
		return null;
	}

	public static Bitmap.CompressFormat getBitmapCompressFormatByMimetype(final String mimeType,
			final Bitmap.CompressFormat def) {
		final String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
		if ("jpeg".equalsIgnoreCase(extension) || "jpg".equalsIgnoreCase(extension))
			return Bitmap.CompressFormat.JPEG;
		else if ("png".equalsIgnoreCase(extension))
			return Bitmap.CompressFormat.PNG;
		else if ("webp".equalsIgnoreCase(extension)) return Bitmap.CompressFormat.WEBP;
		return def;
	}

	public static int getCharacterCount(final String string, final char c) {
		if (string == null) return 0;
		int count = 0;
		while (string.indexOf(c, count) != -1) {
			count++;
		}
		return count;
	}

	public static Bitmap getColorPreviewBitmap(final Context context, final int color) {
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

	public static Selectable getColumnsFromProjection(final String... projection) {
		if (projection == null) return new AllColumns();
		final int length = projection.length;
		final Column[] columns = new Column[length];
		for (int i = 0; i < length; i++) {
			columns[i] = new Column(projection[i]);
		}
		return new Columns(columns);
	}

	public static long getDefaultAccountId(final Context context) {
		if (context == null) return -1;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return prefs.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
	}

	public static String getDefaultAccountScreenName(final Context context) {
		if (context == null) return null;
		return getAccountScreenName(context, getDefaultAccountId(context));
	}

	public static int getDefaultTextSize(final Context context) {
		if (context == null) return 15;
		return context.getResources().getInteger(R.integer.default_text_size);
	}

	public static Twitter getDefaultTwitterInstance(final Context context, final boolean include_entities) {
		if (context == null) return null;
		return getDefaultTwitterInstance(context, include_entities, true, true);
	}

	public static Twitter getDefaultTwitterInstance(final Context context, final boolean include_entities,
			final boolean include_retweets) {
		if (context == null) return null;
		return getDefaultTwitterInstance(context, include_entities, include_retweets, true);
	}

	public static Twitter getDefaultTwitterInstance(final Context context, final boolean include_entities,
			final boolean include_retweets, final boolean use_httpclient) {
		if (context == null) return null;
		return getTwitterInstance(context, getDefaultAccountId(context), include_entities, include_retweets,
				use_httpclient);
	}

	public static String getDisplayName(final Context context, final long user_id, final String name,
			final String screen_name) {
		return getDisplayName(context, user_id, name, screen_name, false);
	}

	public static String getDisplayName(final Context context, final long user_id, final String name,
			final String screen_name, final boolean ignore_cache) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean name_first = prefs.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		final boolean nickname_only = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getBoolean(PREFERENCE_KEY_NICKNAME_ONLY, false);
		return getDisplayName(context, user_id, name, screen_name, name_first, nickname_only, ignore_cache);
	}

	public static String getDisplayName(final Context context, final long user_id, final String name,
			final String screen_name, final boolean name_first, final boolean nickname_only) {
		return getDisplayName(context, user_id, name, screen_name, name_first, nickname_only, false);
	}

	public static String getDisplayName(final Context context, final long user_id, final String name,
			final String screen_name, final boolean name_first, final boolean nickname_only, final boolean ignore_cache) {
		if (context == null) return null;
		final String nick = getUserNickname(context, user_id, ignore_cache);
		final boolean nick_available = !isEmpty(nick);
		if (nickname_only && nick_available) return nick;
		if (!nick_available) return name_first && !isEmpty(name) ? name : "@" + screen_name;
		return context.getString(R.string.name_with_nickname, name_first && !isEmpty(name) ? name : "@" + screen_name,
				nick);
	}

	public static String getErrorMessage(final Context context, final CharSequence message) {
		if (context == null) return ParseUtils.parseString(message);
		if (isEmpty(message)) return context.getString(R.string.error_unknown_error);
		return context.getString(R.string.error_message, message);
	}

	public static String getErrorMessage(final Context context, final CharSequence action, final CharSequence message) {
		if (context == null || isEmpty(action)) return ParseUtils.parseString(message);
		if (isEmpty(message)) return context.getString(R.string.error_unknown_error);
		return context.getString(R.string.error_message_with_action, action, message);
	}

	public static String getErrorMessage(final Context context, final CharSequence action, final Throwable t) {
		if (context == null) return null;
		if (t instanceof TwitterException)
			return getTwitterErrorMessage(context, action, (TwitterException) t);
		else if (t != null) return getErrorMessage(context, trimLineBreak(t.getMessage()));
		return context.getString(R.string.error_unknown_error);
	}

	public static String getErrorMessage(final Context context, final Throwable t) {
		if (t == null) return null;
		if (context != null && t instanceof TwitterException)
			return getTwitterErrorMessage(context, (TwitterException) t);
		return t.getMessage();
	}

	public static int getFirstChildOffset(final ListView list) {
		if (list == null || list.getChildCount() == 0) return 0;
		return list.getChildAt(0).getTop();
	}

	public static HttpClientWrapper getHttpClient(final int timeout_millis, final boolean ignore_ssl_error,
			final Proxy proxy, final HostAddressResolver resolver, final String user_agent,
			final boolean twitter_client_header) {
		final ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setHttpConnectionTimeout(timeout_millis);
		cb.setIgnoreSSLError(ignore_ssl_error);
		cb.setIncludeTwitterClientHeader(twitter_client_header);
		if (proxy != null && !Proxy.NO_PROXY.equals(proxy)) {
			final SocketAddress address = proxy.address();
			if (address instanceof InetSocketAddress) {
				cb.setHttpProxyHost(((InetSocketAddress) address).getHostName());
				cb.setHttpProxyPort(((InetSocketAddress) address).getPort());
			}
		}
		cb.setHostAddressResolver(resolver);
		if (user_agent != null) {
			cb.setUserAgent(user_agent);
		}
		// cb.setHttpClientImplementation(HttpClientImpl.class);
		return new HttpClientWrapper(cb.build());
	}

	public static HttpClientWrapper getImageLoaderHttpClient(final Context context) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int timeout_millis = prefs.getInt(PREFERENCE_KEY_CONNECTION_TIMEOUT, 10000) * 1000;
		final Proxy proxy = getProxy(context);
		final String user_agent = generateBrowserUserAgent();
		final HostAddressResolver resolver = TwidereApplication.getInstance(context).getHostAddressResolver();
		return getHttpClient(timeout_millis, true, proxy, resolver, user_agent, false);
	}

	public static String getImageMimeType(final File image) {
		if (image == null) return null;
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), o);
		return o.outMimeType;
	}

	public static String getImageMimeType(final InputStream is) {
		if (is == null) return null;
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, null, o);
		return o.outMimeType;
	}

	public static String getImagePathFromUri(final Context context, final Uri uri) {
		if (context == null || uri == null) return null;

		final String media_uri_start = ParseUtils.parseString(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		if (ParseUtils.parseString(uri).startsWith(media_uri_start)) {

			final String[] proj = { MediaStore.Images.Media.DATA };
			final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), uri, proj, null, null, null);

			if (cur == null) return null;

			final int column_index = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

			cur.moveToFirst();
			try {
				return cur.getString(column_index);
			} finally {
				cur.close();
			}
		} else {
			final String path = uri.getPath();
			if (path != null) {
				if (new File(path).exists()) return path;
			}
		}
		return null;
	}

	public static String getImageUploadStatus(final Context context, final CharSequence link, final CharSequence text) {
		if (context == null) return null;
		String image_upload_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getString(PREFERENCE_KEY_IMAGE_UPLOAD_FORMAT, PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT);
		if (isEmpty(image_upload_format)) {
			image_upload_format = PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT;
		}
		if (link == null) return ParseUtils.parseString(text);
		return image_upload_format.replace(FORMAT_PATTERN_LINK, link).replace(FORMAT_PATTERN_TEXT, text);
	}

	public static String getInReplyToName(final Status status) {
		if (status == null) return null;
		final Status orig = status.isRetweet() ? status.getRetweetedStatus() : status;
		final long in_reply_to_user_id = status.getInReplyToUserId();
		final UserMentionEntity[] entities = status.getUserMentionEntities();
		if (entities == null) return orig.getInReplyToScreenName();
		for (final UserMentionEntity entity : entities) {
			if (in_reply_to_user_id == entity.getId()) return entity.getName();
		}
		return orig.getInReplyToScreenName();
	}

	public static String getLinkHighlightOption(final Context context) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PREFERENCE_KEY_LINK_HIGHLIGHT_OPTION, LINK_HIGHLIGHT_OPTION_NONE);
	}

	public static int getLinkHighlightOptionInt(final Context context) {
		return getLinkHighlightOptionInt(getLinkHighlightOption(context));
	}

	public static int getLinkHighlightOptionInt(final String option) {
		if (LINK_HIGHLIGHT_OPTION_BOTH.equals(option))
			return LINK_HIGHLIGHT_OPTION_CODE_BOTH;
		else if (LINK_HIGHLIGHT_OPTION_HIGHLIGHT.equals(option))
			return LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT;
		else if (LINK_HIGHLIGHT_OPTION_UNDERLINE.equals(option)) return LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE;
		return LINK_HIGHLIGHT_OPTION_CODE_NONE;
	}

	public static String getLocalizedNumber(final Locale locale, final Number number) {
		final NumberFormat nf = NumberFormat.getInstance(locale);
		return nf.format(number);
	}

	public static String getMapStaticImageUri(final double lat, final double lng, final int zoom, final int w,
			final int h, final Locale locale) {
		return String.format(MAPS_STATIC_IMAGE_URI_TEMPLATE, zoom, w, h, locale.toString(), lat, lng, lat, lng);
	}

	public static String getMapStaticImageUri(final double lat, final double lng, final View v) {
		if (v == null) return null;
		final int wSpec = MeasureSpec.makeMeasureSpec(v.getWidth(), MeasureSpec.UNSPECIFIED);
		final int hSpec = MeasureSpec.makeMeasureSpec(v.getHeight(), MeasureSpec.UNSPECIFIED);
		v.measure(wSpec, hSpec);
		return getMapStaticImageUri(lat, lng, 12, v.getMeasuredWidth(), v.getMeasuredHeight(), v.getResources()
				.getConfiguration().locale);
	}

	public static long[] getNewestMessageIdsFromDatabase(final Context context, final Uri uri) {
		final long[] account_ids = getActivatedAccountIds(context);
		return getNewestMessageIdsFromDatabase(context, uri, account_ids);
	}

	public static long[] getNewestMessageIdsFromDatabase(final Context context, final Uri uri, final long[] account_ids) {
		if (context == null || uri == null || account_ids == null) return null;
		final String[] cols = new String[] { DirectMessages.MESSAGE_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = ContentResolverUtils.query(resolver, uri, cols, where, null,
					DirectMessages.DEFAULT_SORT_ORDER);
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

	public static long[] getNewestStatusIdsFromDatabase(final Context context, final Uri uri) {
		final long[] account_ids = getActivatedAccountIds(context);
		return getNewestStatusIdsFromDatabase(context, uri, account_ids);
	}

	public static long[] getNewestStatusIdsFromDatabase(final Context context, final Uri uri, final long[] account_ids) {
		if (context == null || uri == null || account_ids == null) return null;
		final String[] cols = new String[] { Statuses.STATUS_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = ContentResolverUtils
					.query(resolver, uri, cols, where, null, Statuses.DEFAULT_SORT_ORDER);
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

	public static String getNonEmptyString(final SharedPreferences pref, final String key, final String def) {
		if (pref == null) return def;
		final String val = pref.getString(key, def);
		return isEmpty(val) ? def : val;
	}

	public static String getNormalTwitterProfileImage(final String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "_normal");
		return url;
	}

	public static long[] getOldestMessageIdsFromDatabase(final Context context, final Uri uri) {
		final long[] account_ids = getActivatedAccountIds(context);
		return getOldestMessageIdsFromDatabase(context, uri, account_ids);
	}

	public static long[] getOldestMessageIdsFromDatabase(final Context context, final Uri uri, final long[] account_ids) {
		if (context == null || uri == null) return null;
		final String[] cols = new String[] { DirectMessages.MESSAGE_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = ContentResolverUtils.query(resolver, uri, cols, where, null, DirectMessages.MESSAGE_ID);
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

	public static long[] getOldestStatusIdsFromDatabase(final Context context, final Uri uri) {
		final long[] account_ids = getActivatedAccountIds(context);
		return getOldestStatusIdsFromDatabase(context, uri, account_ids);
	}

	public static long[] getOldestStatusIdsFromDatabase(final Context context, final Uri uri, final long[] account_ids) {
		if (context == null || uri == null || account_ids == null) return null;
		final String[] cols = new String[] { Statuses.STATUS_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = ContentResolverUtils.query(resolver, uri, cols, where, null, Statuses.STATUS_ID);
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

	public static String getOriginalTwitterProfileImage(final String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "");
		return url;
	}

	public static Proxy getProxy(final Context context) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean enable_proxy = prefs.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		if (!enable_proxy) return Proxy.NO_PROXY;
		final String proxy_host = prefs.getString(PREFERENCE_KEY_PROXY_HOST, null);
		final int proxy_port = ParseUtils.parseInt(prefs.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
		if (!isEmpty(proxy_host) && proxy_port > 0) {
			final SocketAddress addr = InetSocketAddress.createUnresolved(proxy_host, proxy_port);
			return new Proxy(Proxy.Type.HTTP, addr);
		}
		return Proxy.NO_PROXY;
	}

	public static String getQuoteStatus(final Context context, final String screen_name, final String text) {
		if (context == null) return null;
		String quote_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
				PREFERENCE_KEY_QUOTE_FORMAT, PREFERENCE_DEFAULT_QUOTE_FORMAT);
		if (isEmpty(quote_format)) {
			quote_format = PREFERENCE_DEFAULT_QUOTE_FORMAT;
		}
		return quote_format.replace(FORMAT_PATTERN_NAME, screen_name).replace(FORMAT_PATTERN_TEXT, text);
	}

	public static HttpResponse getRedirectedHttpResponse(final HttpClientWrapper client, final String url)
			throws TwitterException {
		if (url == null) return null;
		final ArrayList<String> urls = new ArrayList<String>();
		urls.add(url);
		HttpResponse resp;
		try {
			resp = client.get(url, url);
		} catch (final TwitterException te) {
			if (isRedirected(te.getStatusCode())) {
				resp = te.getHttpResponse();
			} else
				throw te;
		}
		while (resp != null && isRedirected(resp.getStatusCode())) {
			final String request_url = resp.getResponseHeader("Location");
			if (request_url == null) return null;
			if (urls.contains(request_url)) throw new TwitterException("Too many redirects");
			urls.add(request_url);
			try {
				resp = client.get(request_url, request_url);
			} catch (final TwitterException te) {
				if (isRedirected(te.getStatusCode())) {
					resp = te.getHttpResponse();
				} else
					throw te;
			}
		}
		return resp;
	}

	public static int getResId(final Context context, final String string) {
		if (context == null || string == null) return 0;
		Matcher m = PATTERN_RESOURCE_IDENTIFIER.matcher(string);
		final Resources res = context.getResources();
		if (m.matches()) return res.getIdentifier(m.group(2), m.group(1), context.getPackageName());
		m = PATTERN_XML_RESOURCE_IDENTIFIER.matcher(string);
		if (m.matches()) return res.getIdentifier(m.group(1), "xml", context.getPackageName());
		return 0;
	}

	public static String getSampleDisplayName(final Context context, final boolean name_first,
			final boolean nickname_only) {
		if (context == null) return null;
		if (nickname_only) return TWIDERE_PREVIEW_NICKNAME;
		return context.getString(R.string.name_with_nickname, name_first ? TWIDERE_PREVIEW_NAME : "@"
				+ TWIDERE_PREVIEW_SCREEN_NAME, TWIDERE_PREVIEW_NICKNAME);
	}

	public static String getSenderUserName(final Context context, final ParcelableDirectMessage user) {
		if (context == null || user == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean display_name = prefs.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		return display_name ? user.sender_name : "@" + user.sender_screen_name;
	}

	public static String getShareStatus(final Context context, final CharSequence title, final CharSequence text) {
		if (context == null) return null;
		String share_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
				PREFERENCE_KEY_SHARE_FORMAT, PREFERENCE_DEFAULT_SHARE_FORMAT);
		if (isEmpty(share_format)) {
			share_format = PREFERENCE_DEFAULT_SHARE_FORMAT;
		}
		if (isEmpty(title)) return ParseUtils.parseString(text);
		return share_format.replace(FORMAT_PATTERN_TITLE, title).replace(FORMAT_PATTERN_TEXT, text != null ? text : "");
	}

	public static int getStatusBackground(final boolean is_mention, final boolean is_favorite, final boolean is_retweet) {
		if (is_mention)
			return 0x1A33B5E5;
		else if (is_favorite)
			return 0x1AFFBB33;
		else if (is_retweet) return 0x1A66CC00;
		return Color.TRANSPARENT;
	}

	public static ArrayList<Long> getStatusIdsInDatabase(final Context context, final Uri uri, final long account_id) {
		final ArrayList<Long> list = new ArrayList<Long>();
		if (context == null) return list;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Statuses.ACCOUNT_ID + " = " + account_id;
		final String[] projection = new String[] { Statuses.STATUS_ID };
		final Cursor cur = ContentResolverUtils.query(resolver, uri, projection, where, null, null);
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

	public static int getStatusTypeIconRes(final boolean is_favorite, final boolean has_location,
			final boolean has_media, final boolean is_possibly_sensitive) {
		if (is_favorite)
			return R.drawable.ic_indicator_starred;
		else if (is_possibly_sensitive && has_media)
			return R.drawable.ic_indicator_reported_media;
		else if (has_media)
			return R.drawable.ic_indicator_media;
		else if (has_location) return R.drawable.ic_indicator_location;
		return 0;
	}

	public static Bitmap getTabIconFromFile(final File file, final Resources res) {
		if (file == null || !file.exists()) return null;
		final String path = file.getPath();
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, o);
		if (o.outHeight <= 0 || o.outWidth <= 0) return null;
		o.inSampleSize = (int) (Math.max(o.outWidth, o.outHeight) / (48 * res.getDisplayMetrics().density));
		o.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, o);
	}

	public static int getTableId(final Uri uri) {
		if (uri == null) return -1;
		return CONTENT_PROVIDER_URI_MATCHER.match(uri);
	}

	public static String getTableNameById(final int id) {
		switch (id) {
			case TABLE_ID_ACCOUNTS:
				return TABLE_ACCOUNTS;
			case TABLE_ID_STATUSES:
				return TABLE_STATUSES;
			case TABLE_ID_MENTIONS:
				return TABLE_MENTIONS;
			case TABLE_ID_DRAFTS:
				return TABLE_DRAFTS;
			case TABLE_ID_CACHED_USERS:
				return TABLE_CACHED_USERS;
			case TABLE_ID_FILTERED_USERS:
				return TABLE_FILTERED_USERS;
			case TABLE_ID_FILTERED_KEYWORDS:
				return TABLE_FILTERED_KEYWORDS;
			case TABLE_ID_FILTERED_SOURCES:
				return TABLE_FILTERED_SOURCES;
			case TABLE_ID_FILTERED_LINKS:
				return TABLE_FILTERED_LINKS;
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
				return TABLE_DIRECT_MESSAGES_INBOX;
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX:
				return TABLE_DIRECT_MESSAGES_OUTBOX;
			case TABLE_ID_TRENDS_LOCAL:
				return TABLE_TRENDS_LOCAL;
			case TABLE_ID_TABS:
				return TABLE_TABS;
			case TABLE_ID_CACHED_STATUSES:
				return TABLE_CACHED_STATUSES;
			case TABLE_ID_CACHED_HASHTAGS:
				return TABLE_CACHED_HASHTAGS;
			default:
				return null;
		}
	}

	public static String getTableNameByUri(final Uri uri) {
		if (uri == null) return null;
		return getTableNameById(getTableId(uri));
	}

	public static int getTextCount(final String string) {
		if (string == null) return 0;
		return ArrayUtils.toStringArray(string).length;
	}

	public static int getTextCount(final TextView view) {
		if (view == null) return 0;
		final String string = ParseUtils.parseString(view.getText());
		return getTextCount(string);
	}

	public static long getTimestampFromDate(final Date date) {
		if (date == null) return -1;
		return date.getTime();
	}

	public static String getTwitterErrorMessage(final Context context, final CharSequence action,
			final TwitterException te) {
		if (context == null) return null;
		if (te == null) return context.getString(R.string.error_unknown_error);
		if (te.exceededRateLimitation()) {
			final RateLimitStatus status = te.getRateLimitStatus();
			final long sec_until_reset = status.getSecondsUntilReset() * 1000;
			final String next_reset_time = ParseUtils.parseString(getRelativeTimeSpanString(System.currentTimeMillis()
					+ sec_until_reset));
			if (isEmpty(action)) return context.getString(R.string.error_message_rate_limit, next_reset_time.trim());
			return context.getString(R.string.error_message_rate_limit_with_action, action, next_reset_time.trim());
		} else if (te.getErrorCode() > 0) {
			final String msg = TwitterErrorCodes.getErrorMessage(context, te.getErrorCode());
			return getErrorMessage(context, action, msg != null ? msg : trimLineBreak(te.getMessage()));
		} else if (te.getCause() instanceof SSLException) {
			final String msg = te.getCause().getMessage();
			if (msg != null && msg.contains("!="))
				return getErrorMessage(context, action, context.getString(R.string.ssl_error));
			else
				return getErrorMessage(context, action, context.getString(R.string.network_error));
		} else if (te.getCause() instanceof IOException)
			return getErrorMessage(context, action, context.getString(R.string.network_error));
		else
			return getErrorMessage(context, action, trimLineBreak(te.getMessage()));
	}

	public static String getTwitterErrorMessage(final Context context, final TwitterException te) {
		if (te == null) return null;
		final String msg = TwitterErrorCodes.getErrorMessage(context, te.getErrorCode());
		if (isEmpty(msg)) return te.getMessage();
		return msg;
	}

	public static Twitter getTwitterInstance(final Context context, final long account_id,
			final boolean include_entities) {
		return getTwitterInstance(context, account_id, include_entities, true, true);
	}

	public static Twitter getTwitterInstance(final Context context, final long account_id,
			final boolean include_entities, final boolean include_retweets) {
		return getTwitterInstance(context, account_id, include_entities, include_retweets, true);
	}

	public static Twitter getTwitterInstance(final Context context, final long account_id,
			final boolean include_entities, final boolean include_retweets, final boolean use_apache_httpclient) {
		if (context == null) return null;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int connection_timeout = prefs.getInt(PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
		final boolean enable_gzip_compressing = prefs.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, true);
		final boolean ignore_ssl_error = prefs.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
		final boolean enable_proxy = prefs.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		// Here I use old consumer key/secret because it's default key for older
		// versions
		final String pref_consumer_key = prefs.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
		final String pref_consumer_secret = prefs.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
		final StringBuilder where = new StringBuilder();
		where.append(Accounts.ACCOUNT_ID + " = " + account_id);
		final Cursor c = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				Accounts.COLUMNS, where.toString(), null, null);
		if (c == null) return null;
		try {
			if (c.getCount() > 0) {
				c.moveToFirst();
				final ConfigurationBuilder cb = new ConfigurationBuilder();
				cb.setHostAddressResolver(app.getHostAddressResolver());
				if (use_apache_httpclient) {
					cb.setHttpClientImplementation(HttpClientImpl.class);
				}
				cb.setHttpConnectionTimeout(connection_timeout);
				setUserAgent(context, cb);
				cb.setGZIPEnabled(enable_gzip_compressing);
				cb.setIgnoreSSLError(ignore_ssl_error);
				if (enable_proxy) {
					final String proxy_host = prefs.getString(PREFERENCE_KEY_PROXY_HOST, null);
					final int proxy_port = ParseUtils.parseInt(prefs.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
					if (!isEmpty(proxy_host) && proxy_port > 0) {
						cb.setHttpProxyHost(proxy_host);
						cb.setHttpProxyPort(proxy_port);
					}
				}
				final String rest_base_url = c.getString(c.getColumnIndex(Accounts.REST_BASE_URL));
				final String signing_rest_base_url = c.getString(c.getColumnIndex(Accounts.SIGNING_REST_BASE_URL));
				final String oauth_base_url = c.getString(c.getColumnIndex(Accounts.OAUTH_BASE_URL));
				final String signing_oauth_base_url = c.getString(c.getColumnIndex(Accounts.SIGNING_OAUTH_BASE_URL));
				final String consumer_key = trim(c.getString(c.getColumnIndex(Accounts.CONSUMER_KEY)));
				final String consumer_secret = trim(c.getString(c.getColumnIndex(Accounts.CONSUMER_SECRET)));
				if (!isEmpty(rest_base_url)) {
					cb.setRestBaseURL(rest_base_url);
				}
				if (!isEmpty(signing_rest_base_url)) {
					cb.setSigningRestBaseURL(signing_rest_base_url);
				}
				if (!isEmpty(oauth_base_url)) {
					cb.setOAuthBaseURL(oauth_base_url);
				}
				if (!isEmpty(signing_oauth_base_url)) {
					cb.setSigningOAuthBaseURL(signing_oauth_base_url);
				}
				cb.setIncludeEntitiesEnabled(include_entities);
				cb.setIncludeRTsEnabled(include_retweets);
				switch (c.getInt(c.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
					case Accounts.AUTH_TYPE_OAUTH:
					case Accounts.AUTH_TYPE_XAUTH: {
						if (!isEmpty(consumer_key) && !isEmpty(consumer_secret)) {
							cb.setOAuthConsumerKey(consumer_key);
							cb.setOAuthConsumerSecret(consumer_secret);
						} else if (!isEmpty(pref_consumer_key) && !isEmpty(pref_consumer_secret)) {
							cb.setOAuthConsumerKey(pref_consumer_key);
							cb.setOAuthConsumerSecret(pref_consumer_secret);
						} else {
							cb.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
							cb.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
						}
						final String oauth_token = c.getString(c.getColumnIndexOrThrow(Accounts.OAUTH_TOKEN));
						final String token_secret = c.getString(c.getColumnIndexOrThrow(Accounts.TOKEN_SECRET));
						if (isEmpty(oauth_token) || isEmpty(token_secret)) return null;
						return new TwitterFactory(cb.build()).getInstance(new AccessToken(oauth_token, token_secret));
					}
					case Accounts.AUTH_TYPE_BASIC: {
						final String screen_name = c.getString(c.getColumnIndexOrThrow(Accounts.SCREEN_NAME));
						final String password = c.getString(c.getColumnIndexOrThrow(Accounts.BASIC_AUTH_PASSWORD));
						if (isEmpty(screen_name) || isEmpty(password)) return null;
						return new TwitterFactory(cb.build())
								.getInstance(new BasicAuthorization(screen_name, password));
					}
					case Accounts.AUTH_TYPE_TWIP_O_MODE: {
						return new TwitterFactory(cb.build()).getInstance(new TwipOModeAuthorization());
					}
					default:
				}
			}
			return null;
		} finally {
			c.close();
		}
	}

	public static String getUnescapedStatusString(final String string) {
		if (string == null) return null;
		return string.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
	}

	public static int getUserColor(final Context context, final long user_id) {
		return getUserColor(context, user_id, false);
	}

	public static int getUserColor(final Context context, final long user_id, final boolean ignore_cache) {
		if (context == null || user_id == -1) return Color.TRANSPARENT;
		if (!ignore_cache && sUserColors.indexOfKey(user_id) >= 0) return sUserColors.get(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final int color = prefs.getInt(Long.toString(user_id), Color.TRANSPARENT);
		sUserColors.put(user_id, color);
		return color;
	}

	public static String getUserName(final Context context, final ParcelableStatus status) {
		if (context == null || status == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean display_name = prefs.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		return display_name ? status.user_name : "@" + status.user_screen_name;
	}

	public static String getUserName(final Context context, final ParcelableUser user) {
		if (context == null || user == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean display_name = prefs.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		return display_name ? user.name : "@" + user.screen_name;
	}

	public static String getUserName(final Context context, final User user) {
		if (context == null || user == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean display_name = prefs.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		return display_name ? user.getName() : "@" + user.getScreenName();
	}

	public static String getUserNickname(final Context context, final long user_id) {
		return getUserNickname(context, user_id, false);
	}

	public static String getUserNickname(final Context context, final long user_id, final boolean ignore_cache) {
		if (context == null || user_id == -1) return null;
		if (!ignore_cache && LongSparseArrayUtils.hasKey(sUserNicknames, user_id)) return sUserNicknames.get(user_id);
		final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final String nickname = prefs.getString(Long.toString(user_id), null);
		sUserNicknames.put(user_id, nickname);
		return nickname;
	}

	public static String getUserNickname(final Context context, final long user_id, final String name) {
		final String nick = getUserNickname(context, user_id);
		if (isEmpty(nick)) return name;
		final boolean nickname_only = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getBoolean(PREFERENCE_KEY_NICKNAME_ONLY, false);
		return nickname_only ? nick : context.getString(R.string.name_with_nickname, name, nick);
	}

	public static int getUserTypeIconRes(final boolean is_verified, final boolean is_protected) {
		if (is_verified)
			return R.drawable.ic_indicator_verified;
		else if (is_protected) return R.drawable.ic_indicator_protected;
		return 0;
	}

	public static boolean hasActiveConnection(final Context context) {
		if (context == null) return false;
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) return true;
		return false;
	}

	public static void initAccountColor(final Context context) {
		if (context == null) return;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI, new String[] {
				Accounts.ACCOUNT_ID, Accounts.USER_COLOR }, null, null, null);
		if (cur == null) return;
		final int id_idx = cur.getColumnIndex(Accounts.ACCOUNT_ID), color_idx = cur.getColumnIndex(Accounts.USER_COLOR);
		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			sAccountColors.put(cur.getLong(id_idx), cur.getInt(color_idx));
			cur.moveToNext();
		}
		cur.close();
	}

	public static void initUserColor(final Context context) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		for (final Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
			sAccountColors.put(ParseUtils.parseLong(entry.getKey()),
					ParseUtils.parseInt(ParseUtils.parseString(entry.getValue())));
		}
	}

	@SuppressLint("InlinedApi")
	public static boolean isBatteryOkay(final Context context) {
		if (context == null) return false;
		final Context app = context.getApplicationContext();
		final IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		final Intent intent = app.registerReceiver(null, filter);
		if (intent == null) return false;
		final boolean plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) > 0;
		final float level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		final float scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
		return plugged || level / scale > 0.15f;
	}

	public static boolean isDebugBuild() {
		return BuildConfig.DEBUG;
	}

	public static boolean isDebuggable(final Context context) {
		if (context == null) return false;
		final ApplicationInfo info;
		try {
			info = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			return false;
		}
		return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
	}

	public static boolean isFiltered(final SQLiteDatabase database, final long user_id, final String text_plain,
			final String text_html, final String source, final long retweeted_by_id) {
		return isFiltered(database, user_id, text_plain, text_html, source, retweeted_by_id, true);
	}

	public static boolean isFiltered(final SQLiteDatabase database, final long user_id, final String text_plain,
			final String text_html, final String source, final long retweeted_by_id, final boolean filter_rts) {
		if (database == null) return false;
		if (text_plain == null && text_html == null && user_id <= 0 && source == null) return false;
		final StringBuilder builder = new StringBuilder();
		final List<String> selection_args = new ArrayList<String>();
		builder.append("SELECT NULL WHERE");
		if (text_plain != null) {
			selection_args.add(text_plain);
			builder.append("(SELECT 1 IN (SELECT ? LIKE '%'||" + TABLE_FILTERED_KEYWORDS + "." + Filters.VALUE
					+ "||'%' FROM " + TABLE_FILTERED_KEYWORDS + "))");
		}
		if (text_html != null) {
			if (!selection_args.isEmpty()) {
				builder.append(" OR ");
			}
			selection_args.add(text_html);
			builder.append("(SELECT 1 IN (SELECT ? LIKE '%<a href=\"%'||" + TABLE_FILTERED_LINKS + "." + Filters.VALUE
					+ "||'%\">%' FROM " + TABLE_FILTERED_LINKS + "))");
		}
		if (user_id > 0) {
			if (!selection_args.isEmpty()) {
				builder.append(" OR ");
			}
			builder.append("(SELECT " + user_id + " IN (SELECT " + Filters.Users.USER_ID + " FROM "
					+ TABLE_FILTERED_USERS + "))");
		}
		if (retweeted_by_id > 0) {
			if (!selection_args.isEmpty()) {
				builder.append(" OR ");
			}
			builder.append("(SELECT " + retweeted_by_id + " IN (SELECT " + Filters.Users.USER_ID + " FROM "
					+ TABLE_FILTERED_USERS + "))");
		}
		if (source != null) {
			if (!selection_args.isEmpty()) {
				builder.append(" OR ");
			}
			selection_args.add(source);
			builder.append("(SELECT 1 IN (SELECT ? LIKE '%>'||" + TABLE_FILTERED_SOURCES + "." + Filters.VALUE
					+ "||'</a>%' FROM " + TABLE_FILTERED_SOURCES + "))");
		}
		final Cursor cur = database.rawQuery(builder.toString(),
				selection_args.toArray(new String[selection_args.size()]));
		if (cur == null) return false;
		try {
			return cur.getCount() > 0;
		} finally {
			cur.close();
		}
	}

	public static boolean isFiltered(final SQLiteDatabase database, final ParcelableStatus status,
			final boolean filter_rts) {
		if (status == null) return false;
		return isFiltered(database, status.user_id, status.text_plain, status.text_html, status.source,
				status.retweeted_by_id, filter_rts);
	}

	public static boolean isMyAccount(final Context context, final long account_id) {
		if (context == null) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Accounts.ACCOUNT_ID + " = " + account_id;
		final Cursor cur = ContentResolverUtils.query(resolver, Accounts.CONTENT_URI, new String[0], where, null, null);
		try {
			return cur != null && cur.getCount() > 0;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static boolean isMyAccount(final Context context, final String screen_name) {
		if (context == null) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Accounts.SCREEN_NAME + " = ?";
		final Cursor cur = ContentResolverUtils.query(resolver, Accounts.CONTENT_URI, new String[0], where,
				new String[] { screen_name }, null);
		try {
			return cur != null && cur.getCount() > 0;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static boolean isMyRetweet(final ParcelableStatus status) {
		if (status == null) return false;
		return status.retweeted_by_id == status.account_id || status.my_retweet_id > 0;
	}

	public static boolean isMyUserName(final Context context, final String screen_name) {
		if (context == null) return false;
		for (final String account_screen_name : getAccountScreenNames(context)) {
			if (account_screen_name.equalsIgnoreCase(screen_name)) return true;
		}
		return false;
	}

	public static boolean isNotificationsSilent(final Context context) {
		if (context == null) return false;
		final SharedPreferences prefs = context.getSharedPreferences(SILENT_NOTIFICATIONS_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		final Calendar now = Calendar.getInstance();
		return prefs.getBoolean("silent_notifications_at_" + now.get(Calendar.HOUR_OF_DAY), false);
	}

	public static boolean isOfficialConsumerKeySecret(final Context context) {
		if (context == null) return false;
		final SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String[] key_secrets = context.getResources().getStringArray(R.array.values_consumer_key_secret);
		final String consumer_key = getNonEmptyString(pref, PREFERENCE_KEY_CONSUMER_KEY, null);
		final String consumer_secret = getNonEmptyString(pref, PREFERENCE_KEY_CONSUMER_SECRET, null);
		for (final String key_secret : key_secrets) {
			final String[] pair = key_secret.split(";");
			if (pair[0].equals(consumer_key) && pair[1].equals(consumer_secret)) return true;
		}
		return false;
	}

	public static boolean isOnWifi(final Context context) {
		if (context == null) return false;
		final ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo networkInfo = conn.getActiveNetworkInfo();

		return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI
				&& networkInfo.isConnected();
	}

	public static boolean isRedirected(final int code) {
		return code == 301 || code == 302 || code == 307;
	}

	public static boolean isRTL(final Context context) {
		if (context == null) return false;
		final Resources res = context.getResources();
		return "ar".equals(res.getConfiguration().locale.getLanguage());
		// return
		// ConfigurationAccessor.getLayoutDirection(res.getConfiguration()) ==
		// SCREENLAYOUT_LAYOUTDIR_RTL;
	}

	public static boolean isSameAccount(final Context context, final long account_id, final long user_id) {
		if (context == null || account_id <= 0 || user_id <= 0) return false;
		return account_id == user_id;
	}

	public static boolean isSameAccount(final Context context, final long account_id, final String screen_name) {
		if (context == null || account_id <= 0 || screen_name == null) return false;
		return screen_name.equalsIgnoreCase(getAccountScreenName(context, account_id));
	}

	public static boolean isUserLoggedIn(final Context context, final long account_id) {
		if (context == null) return false;
		final long[] ids = getAccountIds(context);
		if (ids == null) return false;
		for (final long id : ids) {
			if (id == account_id) return true;
		}
		return false;
	}

	public static boolean isValidImage(final File image) {
		if (image == null) return false;
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(image.getPath(), o);
		return o.outHeight > 0 && o.outWidth > 0;
	}

	public static boolean isValidImage(final InputStream is) {
		if (is == null) return false;
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(is, new Rect(), o);
		return o.outHeight > 0 && o.outWidth > 0;
	}

	public static boolean isValidUrl(final CharSequence url) {
		try {
			new URL(ParseUtils.parseString(url));
		} catch (final Exception e) {
			return false;
		}
		return true;
	}

	public static ContentValues makeAccountContentValues(final Configuration conf, final String basic_password,
			final AccessToken access_token, final User user, final int auth_type, final int color) {
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
				values.put(Accounts.CONSUMER_KEY, conf.getOAuthConsumerKey());
				values.put(Accounts.CONSUMER_SECRET, conf.getOAuthConsumerSecret());
				break;
			}
		}
		values.put(Accounts.AUTH_TYPE, auth_type);
		values.put(Accounts.ACCOUNT_ID, user.getId());
		values.put(Accounts.SCREEN_NAME, user.getScreenName());
		values.put(Accounts.NAME, user.getName());
		values.put(Accounts.PROFILE_IMAGE_URL, ParseUtils.parseString(user.getProfileImageUrlHttps()));
		values.put(Accounts.PROFILE_BANNER_URL, ParseUtils.parseString(user.getProfileBannerImageUrl()));
		values.put(Accounts.USER_COLOR, color);
		values.put(Accounts.IS_ACTIVATED, 1);
		values.put(Accounts.REST_BASE_URL, conf.getRestBaseURL());
		values.put(Accounts.SIGNING_REST_BASE_URL, conf.getSigningRestBaseURL());
		values.put(Accounts.OAUTH_BASE_URL, conf.getOAuthBaseURL());
		values.put(Accounts.SIGNING_OAUTH_BASE_URL, conf.getSigningOAuthBaseURL());
		return values;
	}

	public static ContentValues makeCachedUserContentValues(final User user, final boolean large_profile_image) {
		if (user == null || user.getId() <= 0) return null;
		final String profile_image_url = ParseUtils.parseString(user.getProfileImageUrlHttps());
		final String url = ParseUtils.parseString(user.getURL());
		final URLEntity[] urls = user.getURLEntities();
		final ContentValues values = new ContentValues();
		values.put(CachedUsers.USER_ID, user.getId());
		values.put(CachedUsers.NAME, user.getName());
		values.put(CachedUsers.SCREEN_NAME, user.getScreenName());
		values.put(CachedUsers.PROFILE_IMAGE_URL, large_profile_image ? getBiggerTwitterProfileImage(profile_image_url)
				: profile_image_url);
		values.put(CachedUsers.CREATED_AT, user.getCreatedAt().getTime());
		values.put(CachedUsers.IS_PROTECTED, user.isProtected());
		values.put(CachedUsers.IS_VERIFIED, user.isVerified());
		values.put(CachedUsers.IS_FOLLOWING, user.isFollowing());
		values.put(CachedUsers.FAVORITES_COUNT, user.getFavouritesCount());
		values.put(CachedUsers.FOLLOWERS_COUNT, user.getFollowersCount());
		values.put(CachedUsers.FRIENDS_COUNT, user.getFriendsCount());
		values.put(CachedUsers.STATUSES_COUNT, user.getStatusesCount());
		values.put(CachedUsers.LOCATION, user.getLocation());
		values.put(CachedUsers.DESCRIPTION_PLAIN, user.getDescription());
		values.put(CachedUsers.DESCRIPTION_HTML, formatUserDescription(user));
		values.put(CachedUsers.DESCRIPTION_EXPANDED, formatExpandedUserDescription(user));
		values.put(CachedUsers.URL, url);
		values.put(CachedUsers.URL_EXPANDED,
				url != null && urls != null && urls.length > 0 ? ParseUtils.parseString(urls[0].getExpandedURL())
						: null);
		values.put(CachedUsers.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());
		return values;
	}

	public static ContentValues makeDirectMessageContentValues(final DirectMessage message, final long account_id,
			final boolean is_outgoing, final boolean large_profile_image) {
		if (message == null || message.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		final User sender = message.getSender(), recipient = message.getRecipient();
		if (sender == null || recipient == null) return null;
		final String sender_profile_image_url = ParseUtils.parseString(sender.getProfileImageUrlHttps());
		final String recipient_profile_image_url = ParseUtils.parseString(recipient.getProfileImageUrlHttps());
		values.put(DirectMessages.ACCOUNT_ID, account_id);
		values.put(DirectMessages.MESSAGE_ID, message.getId());
		values.put(DirectMessages.MESSAGE_TIMESTAMP, message.getCreatedAt().getTime());
		values.put(DirectMessages.SENDER_ID, sender.getId());
		values.put(DirectMessages.RECIPIENT_ID, recipient.getId());
		values.put(DirectMessages.TEXT_HTML, formatDirectMessageText(message));
		values.put(DirectMessages.TEXT_PLAIN, message.getText());
		values.put(DirectMessages.IS_OUTGOING, is_outgoing);
		values.put(DirectMessages.SENDER_NAME, sender.getName());
		values.put(DirectMessages.SENDER_SCREEN_NAME, sender.getScreenName());
		values.put(DirectMessages.RECIPIENT_NAME, recipient.getName());
		values.put(DirectMessages.RECIPIENT_SCREEN_NAME, recipient.getScreenName());
		values.put(DirectMessages.SENDER_PROFILE_IMAGE_URL,
				large_profile_image ? getBiggerTwitterProfileImage(sender_profile_image_url) : sender_profile_image_url);
		values.put(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL,
				large_profile_image ? getBiggerTwitterProfileImage(recipient_profile_image_url)
						: recipient_profile_image_url);
		return values;
	}

	public static ContentValues makeFilterdUserContentValues(final ParcelableStatus status) {
		if (status == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Filters.Users.USER_ID, status.user_id);
		values.put(Filters.Users.NAME, status.user_name);
		values.put(Filters.Users.SCREEN_NAME, status.user_screen_name);
		return values;
	}

	public static ContentValues makeFilterdUserContentValues(final ParcelableUser user) {
		if (user == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Filters.Users.USER_ID, user.id);
		values.put(Filters.Users.NAME, user.name);
		values.put(Filters.Users.SCREEN_NAME, user.screen_name);
		return values;
	}

	public static ContentValues makeFilterdUserContentValues(final ParcelableUserMention user) {
		if (user == null) return null;
		final ContentValues values = new ContentValues();
		values.put(Filters.Users.USER_ID, user.id);
		values.put(Filters.Users.NAME, user.name);
		values.put(Filters.Users.SCREEN_NAME, user.screen_name);
		return values;
	}

	public static ContentValues makeStatusContentValues(final Status orig, final long account_id,
			final boolean large_profile_image) {
		if (orig == null || orig.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		values.put(Statuses.ACCOUNT_ID, account_id);
		values.put(Statuses.STATUS_ID, orig.getId());
		values.put(Statuses.MY_RETWEET_ID, orig.getCurrentUserRetweet());
		final boolean is_retweet = orig.isRetweet();
		final Status status;
		final Status retweeted_status = is_retweet ? orig.getRetweetedStatus() : null;
		if (retweeted_status != null) {
			final User retweet_user = orig.getUser();
			values.put(Statuses.RETWEET_ID, retweeted_status.getId());
			values.put(Statuses.RETWEETED_BY_USER_ID, retweet_user.getId());
			values.put(Statuses.RETWEETED_BY_USER_NAME, retweet_user.getName());
			values.put(Statuses.RETWEETED_BY_USER_SCREEN_NAME, retweet_user.getScreenName());
			status = retweeted_status;
		} else {
			status = orig;
		}
		final User user = status.getUser();
		if (user != null) {
			final long user_id = user.getId();
			final String profile_image_url = ParseUtils.parseString(user.getProfileImageUrlHttps());
			final String name = user.getName(), screen_name = user.getScreenName();
			values.put(Statuses.USER_ID, user_id);
			values.put(Statuses.USER_NAME, name);
			values.put(Statuses.USER_SCREEN_NAME, screen_name);
			values.put(Statuses.IS_PROTECTED, user.isProtected());
			values.put(Statuses.IS_VERIFIED, user.isVerified());
			values.put(Statuses.USER_PROFILE_IMAGE_URL,
					large_profile_image ? getBiggerTwitterProfileImage(profile_image_url) : profile_image_url);
			values.put(CachedUsers.IS_FOLLOWING, user != null ? user.isFollowing() : false);
		}
		if (status.getCreatedAt() != null) {
			values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
		}
		final String text_html = formatStatusText(status);
		values.put(Statuses.TEXT_HTML, text_html);
		values.put(Statuses.TEXT_PLAIN, status.getText());
		values.put(Statuses.TEXT_UNESCAPED, toPlainText(text_html));
		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
		values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
		values.put(Statuses.IN_REPLY_TO_USER_NAME, getInReplyToName(status));
		values.put(Statuses.IN_REPLY_TO_USER_SCREEN_NAME, status.getInReplyToScreenName());
		values.put(Statuses.SOURCE, status.getSource());
		values.put(Statuses.IS_POSSIBLY_SENSITIVE, status.isPossiblySensitive());
		final GeoLocation location = status.getGeoLocation();
		if (location != null) {
			values.put(Statuses.LOCATION, location.getLatitude() + "," + location.getLongitude());
		}
		values.put(Statuses.IS_RETWEET, is_retweet);
		values.put(Statuses.IS_FAVORITE, status.isFavorited());
		values.put(Statuses.MEDIA_LINK, MediaPreviewUtils.getSupportedFirstLink(status));
		final JSONArray json = JSONSerializer.toJSONArray(ParcelableUserMention.fromUserMentionEntities(status
				.getUserMentionEntities()));
		values.put(Statuses.MENTIONS, json.toString());
		return values;
	}

	public static ContentValues[] makeTrendsContentValues(final List<Trends> trends_list) {
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

	public static final int matcherEnd(final Matcher matcher, final int group) {
		try {
			return matcher.end(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return -1;
	}

	public static final String matcherGroup(final Matcher matcher, final int group) {
		try {
			return matcher.group(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return null;
	}

	public static final int matcherStart(final Matcher matcher, final int group) {
		try {
			return matcher.start(group);
		} catch (final IllegalStateException e) {
			// Ignore.
		}
		return -1;
	}

	public static int matchLinkId(final Uri uri) {
		return LINK_HANDLER_URI_MATCHER.match(uri);
	}

	public static void notifyForUpdatedUri(final Context context, final Uri uri) {
		if (context == null) return;
		switch (getTableId(uri)) {
			case TABLE_ID_STATUSES: {
				context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED));
				break;
			}
			case TABLE_ID_MENTIONS: {
				context.sendBroadcast(new Intent(BROADCAST_MENTIONS_DATABASE_UPDATED));
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES_INBOX: {
				context.sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED));
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX: {
				context.sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED));
				break;
			}
			default: {
				return;
			}
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	public static void openDirectMessagesConversation(final FragmentActivity activity, final long account_id,
			final long conversation_id, final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof DirectMessagesConversationFragment && details_fragment.isAdded()) {
				((DirectMessagesConversationFragment) details_fragment).showConversation(account_id, conversation_id,
						screen_name);
				dual_pane_activity.showRightPane();
			} else {
				final Fragment fragment = new DirectMessagesConversationFragment();
				final Bundle args = new Bundle();
				if (account_id > 0 && conversation_id > 0) {
					args.putLong(EXTRA_ACCOUNT_ID, account_id);
					if (conversation_id > 0) {
						args.putLong(EXTRA_CONVERSATION_ID, conversation_id);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openImage(final Context context, final String uri, final boolean is_possibly_sensitive) {
		if (context == null || uri == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (context instanceof FragmentActivity && is_possibly_sensitive
				&& !prefs.getBoolean(PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS, false)) {
			final FragmentActivity activity = (FragmentActivity) context;
			final FragmentManager fm = activity.getSupportFragmentManager();
			final DialogFragment fragment = new SensitiveContentWarningDialogFragment();
			final Bundle args = new Bundle();
			args.putParcelable(EXTRA_URI, Uri.parse(uri));
			fragment.setArguments(args);
			fragment.show(fm, "sensitive_content_warning");
		} else {
			openImageDirectly(context, uri);
		}
	}

	public static void openImageDirectly(final Context context, final String uri) {
		if (context == null || uri == null) return;
		final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE);
		intent.setData(Uri.parse(uri));
		intent.setClass(context, ImageViewerGLActivity.class);
		if (context instanceof Activity) {
			SwipebackActivityUtils.startSwipebackActivity((Activity) context, intent);
		} else {
			context.startActivity(intent);
		}
	}

	public static void openIncomingFriendships(final Activity activity, final long account_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new IncomingFriendshipsFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_INCOMING_FRIENDSHIPS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openMap(final Context context, final double latitude, final double longitude) {
		if (context == null || !new ParcelableLocation(latitude, longitude).isValid()) return;
		final Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_TWIDERE);
		builder.authority(AUTHORITY_MAP);
		builder.appendQueryParameter(QUERY_PARAM_LAT, String.valueOf(latitude));
		builder.appendQueryParameter(QUERY_PARAM_LNG, String.valueOf(longitude));
		final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
		intent.setClass(context, MapViewerActivity.class);
		if (context instanceof Activity) {
			SwipebackActivityUtils.startSwipebackActivity((Activity) context, intent);
		} else {
			context.startActivity(intent);
		}
	}

	public static void openSavedSearches(final Activity activity, final long account_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new SavedSearchesListFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_SAVED_SEARCHES);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openSearch(final Activity activity, final long account_id, final String query) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new SearchFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putString(EXTRA_QUERY, query);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_SEARCH);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_QUERY, query);
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openStatus(final Activity activity, final long account_id, final long status_id) {
		if (activity == null || account_id <= 0 || status_id <= 0) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new StatusFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putLong(EXTRA_STATUS_ID, status_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_RIGHT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openStatus(final Activity activity, final ParcelableStatus status) {
		if (activity == null || status == null) return;
		final long account_id = status.account_id, status_id = status.id;
		final Bundle extras = new Bundle();
		extras.putParcelable(EXTRA_STATUS, status);
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof StatusFragment && details_fragment.isAdded()) {
				((StatusFragment) details_fragment).displayStatus(status);
				dual_pane_activity.showRightPane();
			} else {
				final Fragment fragment = new StatusFragment();
				final Bundle args = new Bundle(extras);
				args.putLong(EXTRA_ACCOUNT_ID, account_id);
				args.putLong(EXTRA_STATUS_ID, status_id);
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
			intent.putExtras(extras);
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openStatuses(final Activity activity, final List<ParcelableStatus> statuses) {
		if (activity == null || statuses == null) return;
		final Bundle extras = new Bundle();
		extras.putParcelableArrayList(EXTRA_STATUSES, new ArrayList<ParcelableStatus>(statuses));
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new StatusesListFragment();
			fragment.setArguments(extras);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUSES);
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			intent.putExtras(extras);
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openStatusRetweeters(final Activity activity, final long account_id, final long status_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new StatusRetweetersListFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putLong(EXTRA_STATUS_ID, status_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS_RETWEETERS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openTweetSearch(final Activity activity, final long account_id, final String query) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new SearchStatusesFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (query != null) {
				args.putString(EXTRA_QUERY, query);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserBlocks(final Activity activity, final long account_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserBlocksListFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_BLOCKS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserFavorites(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserFavoritesFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (user_id > 0) {
				args.putLong(EXTRA_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}

	}

	public static void openUserFollowers(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserFollowersFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (user_id > 0) {
				args.putLong(EXTRA_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserFriends(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserFriendsFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (user_id > 0) {
				args.putLong(EXTRA_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}

	}

	public static void openUserListDetails(final Activity activity, final long accountId, final int listId,
			final long userId, final String screenName, final String listName) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListDetailsFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, accountId);
			args.putInt(EXTRA_LIST_ID, listId);
			args.putLong(EXTRA_USER_ID, userId);
			args.putString(EXTRA_SCREEN_NAME, screenName);
			args.putString(EXTRA_LIST_NAME, listName);
			fragment.setArguments(args);
			dual_pane_activity.showFragment(fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LIST);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(accountId));
			if (listId > 0) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(listId));
			}
			if (userId > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(userId));
			}
			if (screenName != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
			}
			if (listName != null) {
				builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, listName);
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserListDetails(final Activity activity, final ParcelableUserList userList) {
		if (activity == null || userList == null) return;
		final long accountId = userList.account_id, userId = userList.user_id;
		final int listId = userList.id;
		final Bundle extras = new Bundle();
		extras.putParcelable(EXTRA_USER_LIST, userList);
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof UserListDetailsFragment && details_fragment.isAdded()) {
				((UserListDetailsFragment) details_fragment).displayUserList(userList);
				dual_pane_activity.showRightPane();
			} else {
				final Fragment fragment = new UserListDetailsFragment();
				final Bundle args = new Bundle(extras);
				args.putLong(EXTRA_ACCOUNT_ID, accountId);
				args.putLong(EXTRA_USER_ID, userId);
				args.putInt(EXTRA_LIST_ID, listId);
				fragment.setArguments(args);
				dual_pane_activity.showAtPane(DualPaneActivity.PANE_RIGHT, fragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LIST);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(accountId));
			builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(userId));
			builder.appendQueryParameter(QUERY_PARAM_LIST_ID, String.valueOf(listId));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			intent.putExtras(extras);
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserListMembers(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListMembersFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putInt(EXTRA_LIST_ID, list_id);
			args.putLong(EXTRA_USER_ID, user_id);
			args.putString(EXTRA_SCREEN_NAME, screen_name);
			args.putString(EXTRA_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LIST_MEMBERS);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserListMembers(final Activity activity, final ParcelableUserList list) {
		if (activity == null || list == null) return;
		openUserListMembers(activity, list.account_id, list.id, list.user_id, list.user_screen_name, list.name);
	}

	public static void openUserListMemberships(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null || account_id <= 0 || user_id <= 0 && isEmpty(screen_name)) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListMembershipsListFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (user_id > 0) {
				args.putLong(EXTRA_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
			}
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LIST_MEMBERSHIPS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserLists(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListsListFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putLong(EXTRA_USER_ID, user_id);
			args.putString(EXTRA_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LISTS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			if (user_id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user_id));
			}
			if (screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screen_name);
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserListSubscribers(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListSubscribersFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putInt(EXTRA_LIST_ID, list_id);
			args.putLong(EXTRA_USER_ID, user_id);
			args.putString(EXTRA_SCREEN_NAME, screen_name);
			args.putString(EXTRA_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LISTS);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserListSubscribers(final Activity activity, final ParcelableUserList list) {
		if (activity == null || list == null) return;
		openUserListSubscribers(activity, list.account_id, list.id, list.user_id, list.user_screen_name, list.name);
	}

	public static void openUserListTimeline(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListTimelineFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			args.putInt(EXTRA_LIST_ID, list_id);
			args.putLong(EXTRA_USER_ID, user_id);
			args.putString(EXTRA_SCREEN_NAME, screen_name);
			args.putString(EXTRA_LIST_NAME, list_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER_LIST_TIMELINE);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserListTimeline(final Activity activity, final ParcelableUserList list) {
		if (activity == null || list == null) return;
		openUserListTimeline(activity, list.account_id, list.id, list.user_id, list.user_screen_name, list.name);
	}

	public static void openUserMentions(final Activity activity, final long account_id, final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserMentionsFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserProfile(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null || account_id <= 0 || user_id <= 0 && isEmpty(screen_name)) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserProfileFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (user_id > 0) {
				args.putLong(EXTRA_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserProfile(final Activity activity, final ParcelableUser user) {
		if (activity == null || user == null) return;
		final Bundle extras = new Bundle();
		extras.putParcelable(EXTRA_USER, user);
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof UserProfileFragment && details_fragment.isAdded()) {
				((UserProfileFragment) details_fragment).displayUser(user);
				dual_pane_activity.showRightPane();
			} else {
				final Fragment fragment = new UserProfileFragment();
				final Bundle args = new Bundle(extras);
				args.putLong(EXTRA_ACCOUNT_ID, user.account_id);
				if (user.id > 0) {
					args.putLong(EXTRA_USER_ID, user.id);
				}
				if (user.screen_name != null) {
					args.putString(EXTRA_SCREEN_NAME, user.screen_name);
				}
				fragment.setArguments(args);
				dual_pane_activity.showAtPane(DualPaneActivity.PANE_RIGHT, fragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USER);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(user.account_id));
			if (user.id > 0) {
				builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(user.id));
			}
			if (user.screen_name != null) {
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, user.screen_name);
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			intent.putExtras(extras);
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUsers(final Activity activity, final List<ParcelableUser> users) {
		if (activity == null || users == null) return;
		final Bundle extras = new Bundle();
		extras.putParcelableArrayList(EXTRA_USERS, new ArrayList<ParcelableUser>(users));
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UsersListFragment();
			fragment.setArguments(extras);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USERS);
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			intent.putExtras(extras);
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}
	}

	public static void openUserTimeline(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserTimelineFragment();
			final Bundle args = new Bundle();
			args.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (user_id > 0) {
				args.putLong(EXTRA_USER_ID, user_id);
			}
			if (screen_name != null) {
				args.putString(EXTRA_SCREEN_NAME, screen_name);
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			SwipebackActivityUtils.startSwipebackActivity(activity, intent);
		}

	}

	public static String replaceLast(final String text, final String regex, final String replacement) {
		if (text == null || regex == null || replacement == null) return text;
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	/**
	 * Resizes specific a Bitmap with keeping ratio.
	 */
	public static Bitmap resizeBitmap(Bitmap orig, final int desireWidth, final int desireHeight) {
		final int width = orig.getWidth();
		final int height = orig.getHeight();

		if (0 < width && 0 < height && desireWidth < width || desireHeight < height) {
			// Calculate scale
			float scale;
			if (width < height) {
				scale = (float) desireHeight / (float) height;
				if (desireWidth < width * scale) {
					scale = (float) desireWidth / (float) width;
				}
			} else {
				scale = (float) desireWidth / (float) width;
			}

			// Draw resized image
			final Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			final Bitmap bitmap = Bitmap.createBitmap(orig, 0, 0, width, height, matrix, true);
			final Canvas canvas = new Canvas(bitmap);
			canvas.drawBitmap(bitmap, 0, 0, null);

			orig = bitmap;
		}

		return orig;
	}

	public static void restartActivity(final Activity activity) {
		if (activity == null) return;
		final int enter_anim = android.R.anim.fade_in;
		final int exit_anim = android.R.anim.fade_out;
		activity.finish();
		activity.overridePendingTransition(enter_anim, exit_anim);
		activity.startActivity(activity.getIntent());
		activity.overridePendingTransition(enter_anim, exit_anim);
	}

	public static void scrollListToPosition(final ListView list, final int position) {
		scrollListToPosition(list, position, 0);
	}

	public static void scrollListToPosition(final ListView list, final int position, final int offset) {
		if (list == null) return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			list.setSelectionFromTop(position, offset);
			stopListView(list);
		} else {
			stopListView(list);
			list.setSelectionFromTop(position, offset);
		}
	}

	public static void scrollListToPosition(final PLAListView list, final int position) {
		if (list == null) return;
		// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
		list.setSelection(position);
		list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
				MotionEvent.ACTION_CANCEL, 0, 0, 0));
		// } else {
		// list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
		// SystemClock.uptimeMillis(),
		// MotionEvent.ACTION_DOWN, 0, 0, 0));
		// list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
		// SystemClock.uptimeMillis(),
		// MotionEvent.ACTION_UP, 0, 0, 0));
		// list.setSelection(position);
		// }
	}

	public static void scrollListToTop(final ListView list) {
		if (list == null) return;
		scrollListToPosition(list, 0);
	}

	public static void scrollListToTop(final PLAListView list) {
		if (list == null) return;
		scrollListToPosition(list, 0);
	}

	public static void setMenuForStatus(final Context context, final Menu menu, final ParcelableStatus status) {
		if (context == null || menu == null || status == null) return;
		final int activated_color = ThemeUtils.getThemeColor(context);
		final MenuItem delete = menu.findItem(R.id.delete_submenu);
		if (delete != null) {
			delete.setVisible(status.account_id == status.user_id && !isMyRetweet(status));
		}
		final MenuItem retweet = menu.findItem(MENU_RETWEET);
		if (retweet != null) {
			final Drawable icon = retweet.getIcon().mutate();
			retweet.setVisible(!status.user_is_protected || isMyRetweet(status));
			if (isMyRetweet(status)) {
				icon.setColorFilter(activated_color, Mode.MULTIPLY);
				retweet.setTitle(R.string.cancel_retweet);
			} else {
				icon.clearColorFilter();
				retweet.setTitle(R.string.retweet);
			}
		}
		final MenuItem favorite = menu.findItem(MENU_FAVORITE);
		if (favorite != null) {
			final Drawable icon = favorite.getIcon().mutate();
			if (status.is_favorite) {
				icon.setColorFilter(activated_color, Mode.MULTIPLY);
				favorite.setTitle(R.string.unfavorite);
			} else {
				icon.clearColorFilter();
				favorite.setTitle(R.string.favorite);
			}
		}
		final MenuItem more_item = menu.findItem(R.id.more_submenu);
		final Menu more_submenu = more_item != null && more_item.hasSubMenu() ? more_item.getSubMenu() : menu;
		more_submenu.removeGroup(MENU_GROUP_STATUS_EXTENSION);
		final Intent extension_intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_STATUS);
		final Bundle extension_extras = new Bundle();
		extension_extras.putParcelable(EXTRA_STATUS, status);
		extension_intent.putExtras(extension_extras);
		addIntentToMenu(context, more_submenu, extension_intent, MENU_GROUP_STATUS_EXTENSION);
		final MenuItem share_item = menu.findItem(R.id.share_submenu);
		final Menu share_submenu = share_item != null && share_item.hasSubMenu() ? share_item.getSubMenu() : null;
		if (share_submenu != null) {
			final Intent share_intent = new Intent(Intent.ACTION_SEND);
			share_intent.setType("text/plain");
			share_intent.putExtra(Intent.EXTRA_TEXT, "@" + status.user_screen_name + ": " + status.text_plain);
			share_submenu.removeGroup(MENU_GROUP_STATUS_SHARE);
			addIntentToMenu(context, share_submenu, share_intent, MENU_GROUP_STATUS_SHARE);
		}
	}

	public static void setMenuItemAvailability(final Menu menu, final int id, final boolean available) {
		if (menu == null) return;
		final MenuItem item = menu.findItem(id);
		if (item == null) return;
		item.setVisible(available);
		item.setEnabled(available);
	}

	public static void setMenuItemIcon(final Menu menu, final int id, final int icon) {
		if (menu == null) return;
		final MenuItem item = menu.findItem(id);
		if (item == null) return;
		item.setIcon(icon);
	}

	public static void setMenuItemTitle(final Menu menu, final int id, final int icon) {
		if (menu == null) return;
		final MenuItem item = menu.findItem(id);
		if (item == null) return;
		item.setTitle(icon);
	}

	public static void setUserAgent(final Context context, final ConfigurationBuilder cb) {
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

	public static void setUserColor(final Context context, final long user_id, final int color) {
		if (context == null || user_id == -1) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(String.valueOf(user_id), color);
		editor.commit();
		sUserColors.put(user_id, color);
	}

	public static void setUserNickname(final Context context, final long user_id, final String nickname) {
		if (context == null || user_id == -1) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(String.valueOf(user_id), nickname);
		editor.commit();
		sUserNicknames.put(user_id, nickname);
	}

	public static boolean shouldEnableFiltersForRTs(final Context context) {
		if (context == null) return false;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(PREFERENCE_KEY_FILTERS_FOR_RTS, true);
	}

	public static void showErrorMessage(final Context context, final CharSequence message, final boolean long_message) {
		if (context == null) return;
		if (context instanceof Activity) {
			final Crouton crouton = Crouton.makeText((Activity) context, message, CroutonStyle.ALERT);
			final CroutonConfiguration.Builder cb = new CroutonConfiguration.Builder();
			cb.setDuration(long_message ? CroutonConfiguration.DURATION_LONG : CroutonConfiguration.DURATION_SHORT);
			crouton.setConfiguration(cb.build());
			crouton.show();
		} else {
			final Toast toast = Toast.makeText(context, message, long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public static void showErrorMessage(final Context context, final CharSequence action, final CharSequence message,
			final boolean long_message) {
		if (context == null) return;
		showErrorMessage(context, getErrorMessage(context, message), long_message);
	}

	public static void showErrorMessage(final Context context, final CharSequence action, final Throwable t,
			final boolean long_message) {
		if (context == null) return;
		if (t instanceof TwitterException) {
			showTwitterErrorMessage(context, action, (TwitterException) t, long_message);
			return;
		}
		showErrorMessage(context, getErrorMessage(context, action, t), long_message);
	}

	public static void showErrorMessage(final Context context, final int action, final String desc,
			final boolean long_message) {
		if (context == null) return;
		showErrorMessage(context, context.getString(action), desc, long_message);
	}

	public static void showErrorMessage(final Context context, final int action, final Throwable t,
			final boolean long_message) {
		if (context == null) return;
		showErrorMessage(context, context.getString(action), t, long_message);
	}

	public static void showInfoMessage(final Context context, final CharSequence message, final boolean long_message) {
		if (context == null || isEmpty(message)) return;
		if (context instanceof Activity) {
			final Crouton crouton = Crouton.makeText((Activity) context, message, CroutonStyle.INFO);
			final CroutonConfiguration.Builder cb = new CroutonConfiguration.Builder();
			cb.setDuration(long_message ? CroutonConfiguration.DURATION_LONG : CroutonConfiguration.DURATION_SHORT);
			crouton.setConfiguration(cb.build());
			crouton.show();
		} else {
			final Toast toast = Toast.makeText(context, message, long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public static void showInfoMessage(final Context context, final int resId, final boolean long_message) {
		if (context == null) return;
		showInfoMessage(context, context.getText(resId), long_message);
	}

	public static void showOkMessage(final Context context, final CharSequence message, final boolean long_message) {
		if (context == null || isEmpty(message)) return;
		if (context instanceof Activity) {
			final Crouton crouton = Crouton.makeText((Activity) context, message, CroutonStyle.CONFIRM);
			final CroutonConfiguration.Builder cb = new CroutonConfiguration.Builder();
			cb.setDuration(long_message ? CroutonConfiguration.DURATION_LONG : CroutonConfiguration.DURATION_SHORT);
			crouton.setConfiguration(cb.build());
			crouton.show();
		} else {
			final Toast toast = Toast.makeText(context, message, long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public static void showOkMessage(final Context context, final int resId, final boolean long_message) {
		if (context == null) return;
		showOkMessage(context, context.getText(resId), long_message);
	}

	public static void showTwitterErrorMessage(final Context context, final CharSequence action,
			final TwitterException te, final boolean long_message) {
		if (context == null) return;
		final String message;
		if (te != null) {
			if (action != null) {
				if (te.exceededRateLimitation()) {
					final RateLimitStatus status = te.getRateLimitStatus();
					final long sec_until_reset = status.getSecondsUntilReset() * 1000;
					final String next_reset_time = ParseUtils.parseString(getRelativeTimeSpanString(System
							.currentTimeMillis() + sec_until_reset));
					message = context.getString(R.string.error_message_rate_limit_with_action, action,
							next_reset_time.trim());
				} else if (te.getErrorCode() > 0) {
					final String msg = TwitterErrorCodes.getErrorMessage(context, te.getErrorCode());
					message = context.getString(R.string.error_message_with_action, action, msg != null ? msg
							: trimLineBreak(te.getMessage()));
				} else if (te.getCause() instanceof SSLException) {
					final String msg = te.getCause().getMessage();
					if (msg != null && msg.contains("!=")) {
						message = context.getString(R.string.error_message_with_action, action,
								context.getString(R.string.ssl_error));
					} else {
						message = context.getString(R.string.error_message_with_action, action,
								context.getString(R.string.network_error));
					}
				} else if (te.getCause() instanceof IOException) {
					message = context.getString(R.string.error_message_with_action, action,
							context.getString(R.string.network_error));
				} else {
					message = context.getString(R.string.error_message_with_action, action,
							trimLineBreak(te.getMessage()));
				}
			} else {
				message = context.getString(R.string.error_message, trimLineBreak(te.getMessage()));
			}
		} else {
			message = context.getString(R.string.error_unknown_error);
		}
		showErrorMessage(context, message, long_message);
	}

	public static void showWarnMessage(final Context context, final CharSequence message, final boolean long_message) {
		if (context == null || isEmpty(message)) return;
		if (context instanceof Activity) {
			final Crouton crouton = Crouton.makeText((Activity) context, message, CroutonStyle.WARN);
			final CroutonConfiguration.Builder cb = new CroutonConfiguration.Builder();
			cb.setDuration(long_message ? CroutonConfiguration.DURATION_LONG : CroutonConfiguration.DURATION_SHORT);
			crouton.setConfiguration(cb.build());
			crouton.show();
		} else {
			final Toast toast = Toast.makeText(context, message, long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public static void showWarnMessage(final Context context, final int resId, final boolean long_message) {
		if (context == null) return;
		showWarnMessage(context, context.getText(resId), long_message);
	}

	public static void stopListView(final ListView list) {
		if (list == null) return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_CANCEL, 0, 0, 0));
		} else {
			list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_DOWN, 0, 0, 0));
			list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_UP, 0, 0, 0));
		}
	}

	public static String trim(final String str) {
		return str != null ? str.trim() : null;
	}

	public static String trimLineBreak(final String orig) {
		if (orig == null) return null;
		return orig.replaceAll("\\n+", "\n");
	}

	private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
		final URLEntity[] urls = entities.getURLEntities();
		// Format media.
		final MediaEntity[] medias = entities.getMediaEntities();
		if (medias != null) {
			for (final MediaEntity media : medias) {
				final URL media_url = media.getMediaURL();
				if (media_url != null) {
					builder.addLink(ParseUtils.parseString(media_url), media.getDisplayURL(), media.getStart(),
							media.getEnd());
				}
			}
		}
		if (urls != null) {
			for (final URLEntity url : urls) {
				final URL expanded_url = url.getExpandedURL();
				if (expanded_url != null) {
					builder.addLink(ParseUtils.parseString(expanded_url), url.getDisplayURL(), url.getStart(),
							url.getEnd());
				}
			}
		}
	}
}
