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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonConfiguration;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import javax.net.ssl.SSLException;
import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.mariotaku.gallery3d.app.ImageViewerGLActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.CameraCropActivity;
import org.mariotaku.twidere.activity.DualPaneActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.activity.ImageViewerActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.ActivitiesAboutMeFragment;
import org.mariotaku.twidere.fragment.ActivitiesByFriendsFragment;
import org.mariotaku.twidere.fragment.DirectMessagesConversationFragment;
import org.mariotaku.twidere.fragment.IncomingFriendshipsFragment;
import org.mariotaku.twidere.fragment.SavedSearchesListFragment;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.fragment.SensitiveContentWarningDialogFragment;
import org.mariotaku.twidere.fragment.StatusFragment;
import org.mariotaku.twidere.fragment.StatusRetweetersListFragment;
import org.mariotaku.twidere.fragment.StatusesListFragment;
import org.mariotaku.twidere.fragment.TrendsFragment;
import org.mariotaku.twidere.fragment.UserBlocksListFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserFollowersFragment;
import org.mariotaku.twidere.fragment.UserFriendsFragment;
import org.mariotaku.twidere.fragment.UserListDetailsFragment;
import org.mariotaku.twidere.fragment.UserListMembersFragment;
import org.mariotaku.twidere.fragment.UserListSubscribersFragment;
import org.mariotaku.twidere.fragment.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.UserListsListFragment;
import org.mariotaku.twidere.fragment.UserMentionsFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.fragment.UsersListFragment;
import org.mariotaku.twidere.model.DirectMessageCursorIndices;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;
import org.mariotaku.twidere.util.HtmlLinkExtractor.HtmlLink;
import org.mariotaku.twidere.util.httpclient.HttpClientImpl;
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
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.TwipOModeAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;

import static android.content.res.Configuration.SCREENLAYOUT_LAYOUTDIR_RTL;
import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.provider.TweetStore.CACHE_URIS;
import static org.mariotaku.twidere.provider.TweetStore.DIRECT_MESSAGES_URIS;
import static org.mariotaku.twidere.provider.TweetStore.STATUSES_URIS;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.TwidereLinkify.IMGLY_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.IMGUR_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.INSTAGRAM_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.MOBYPICTURE_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_IMGLY;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_IMGUR;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_INSTAGRAM;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_LOCKERZ_AND_PLIXI;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_MOBYPICTURE;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_PHOTOZOU;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_SINA_WEIBO_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITGOO;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITPIC;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_YFROG;
import static org.mariotaku.twidere.util.TwidereLinkify.PHOTOZOU_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.SINA_WEIBO_IMAGES_AVAILABLE_SIZES;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITGOO_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITPIC_GROUP_ID;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES;
import static org.mariotaku.twidere.util.TwidereLinkify.YFROG_GROUP_ID;

public final class Utils implements Constants {

	private static final UriMatcher CONTENT_PROVIDER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final UriMatcher LINK_HANDLER_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	public static final HashMap<String, Class<? extends Fragment>> CUSTOM_TABS_FRAGMENT_MAP = new HashMap<String, Class<? extends Fragment>>();
	public static final HashMap<String, Integer> CUSTOM_TABS_TYPE_NAME_MAP = new HashMap<String, Integer>();
	public static final HashMap<String, Integer> CUSTOM_TABS_ICON_NAME_MAP = new HashMap<String, Integer>();
	
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
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_PREFERENCES, VIRTUAL_TABLE_ID_PREFERENCES);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_PERMISSIONS, VIRTUAL_TABLE_ID_PERMISSIONS);
		CONTENT_PROVIDER_URI_MATCHER.addURI(TweetStore.AUTHORITY, TABLE_DNS + "/*", VIRTUAL_TABLE_ID_DNS);

		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS, null, LINK_ID_STATUS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER, null, LINK_ID_USER);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_TIMELINE, null, LINK_ID_USER_TIMELINE);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FOLLOWERS, null, LINK_ID_USER_FOLLOWERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FRIENDS, null, LINK_ID_USER_FRIENDS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_FAVORITES, null, LINK_ID_USER_FAVORITES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_BLOCKS, null, LINK_ID_USER_BLOCKS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_DIRECT_MESSAGES_CONVERSATION, null,
				LINK_ID_DIRECT_MESSAGES_CONVERSATION);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_DETAILS, null, LINK_ID_LIST_DETAILS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_TYPES, null, LINK_ID_LISTS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_TIMELINE, null, LINK_ID_LIST_TIMELINE);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LIST_MEMBERS, null, LINK_ID_LIST_MEMBERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_LISTS, null, LINK_ID_LISTS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_SAVED_SEARCHES, null, LINK_ID_SAVED_SEARCHES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USER_MENTIONS, null, LINK_ID_USER_MENTIONS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_INCOMING_FRIENDSHIPS, null, LINK_ID_INCOMING_FRIENDSHIPS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_USERS, null, LINK_ID_USERS);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUSES, null, LINK_ID_STATUSES);
		LINK_HANDLER_URI_MATCHER.addURI(AUTHORITY_STATUS_RETWEETERS, null, LINK_ID_RETWEETERS);

		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LISTS, UserListsListFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_MEMBERS, UserListMembersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LISTS, UserListSubscribersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_LIST_TIMELINE, UserListTimelineFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_SAVED_SEARCHES, SavedSearchesListFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_SEARCH_TWEETS, SearchTweetsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_SEARCH_USERS, SearchUsersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_FAVORITES, UserFavoritesFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_FOLLOWERS, UserFollowersFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_FRIENDS, UserFriendsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_MENTIONS, UserMentionsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_USER_TIMELINE, UserTimelineFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_TRENDS, TrendsFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_ACTIVITIES_ABOUT_ME, ActivitiesAboutMeFragment.class);
		CUSTOM_TABS_FRAGMENT_MAP.put(AUTHORITY_ACTIVITIES_BY_FRIENDS, ActivitiesByFriendsFragment.class);

		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_MEMBERS, R.string.list_members);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LISTS, R.string.user_list);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_LIST_TIMELINE, R.string.list_timeline);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_SAVED_SEARCHES, R.string.saved_searches);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_SEARCH_TWEETS, R.string.search_tweets);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_SEARCH_USERS, R.string.search_users);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_FAVORITES, R.string.favorites);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_FOLLOWERS, R.string.followers);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_FRIENDS, R.string.following);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_MENTIONS, R.string.user_mentions);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_USER_TIMELINE, R.string.user_timeline);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_TRENDS, R.string.trends);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_ACTIVITIES_ABOUT_ME, R.string.activities_about_me);
		CUSTOM_TABS_TYPE_NAME_MAP.put(AUTHORITY_ACTIVITIES_BY_FRIENDS, R.string.activities_by_friends);

		CUSTOM_TABS_ICON_NAME_MAP.put("accounts", R.drawable.ic_tab_accounts);
		CUSTOM_TABS_ICON_NAME_MAP.put("fire", R.drawable.ic_tab_fire);
		CUSTOM_TABS_ICON_NAME_MAP.put("hamster", R.drawable.ic_tab_hamster);
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

	private static Map<Long, String> sAccountScreenNames = new LinkedHashMap<Long, String>();
	private static Map<Long, String> sAccountNames = new LinkedHashMap<Long, String>();

	private Utils() {
		throw new AssertionError("You are trying to create an instance for this utility class!");
	}

	public static void addIntentToMenu(final Context context, final Menu menu, final Intent query_intent) {
		if (context == null || menu == null || query_intent == null) return;
		final PackageManager pm = context.getPackageManager();
		final Resources res = context.getResources();
		final float density = res.getDisplayMetrics().density;
		final List<ResolveInfo> activities = pm.queryIntentActivities(query_intent, 0);
		for (final ResolveInfo info : activities) {
			final Intent intent = new Intent(query_intent);
			final Drawable icon = info.loadIcon(pm);
			intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
			final MenuItem item = menu.add(info.loadLabel(pm));
			item.setIntent(intent);
			if (icon instanceof BitmapDrawable) {
				final int paddings = Math.round(density * 4);
				final Bitmap orig = ((BitmapDrawable) icon).getBitmap();
				final Bitmap bitmap = Bitmap.createBitmap(orig.getWidth() + paddings * 2, orig.getHeight() + paddings * 2, Bitmap.Config.ARGB_8888);
				final Canvas canvas = new Canvas(bitmap);
				canvas.drawBitmap(orig, paddings, paddings, null);
				item.setIcon(new BitmapDrawable(res, bitmap));
			} else {
				item.setIcon(icon);
			}
		}
	}
	
	public static void announceForAccessibilityCompat(final Context context, final View view, final CharSequence text, final Class<?> cls) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) return;
		final AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
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
		AccessibilityEventAccessor.setSource(event, view);

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

	public static String buildArguments(final Bundle args) {
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

	public static Uri buildDirectMessageConversationUri(final long account_id, final long conversation_id,
			final String screen_name) {
		if (conversation_id <= 0 && screen_name == null) return TweetStore.CONTENT_URI_NULL;
		final Uri.Builder builder = conversation_id > 0 ? DirectMessages.Conversation.CONTENT_URI.buildUpon()
				: DirectMessages.Conversation.CONTENT_URI_SCREEN_NAME.buildUpon();
		builder.appendPath(String.valueOf(account_id));
		builder.appendPath(conversation_id > 0 ? String.valueOf(conversation_id) : screen_name);
		return builder.build();
	}

	public static String buildStatusFilterWhereClause(final String table, final String selection) {
		if (table == null) return null;
		final StringBuilder builder = new StringBuilder();
		if (selection != null) {
			builder.append(selection);
			builder.append(" AND ");
		}
		builder.append(Statuses._ID + " NOT IN ( ");
		builder.append("SELECT DISTINCT " + table + "." + Statuses._ID + " FROM " + table);
		builder.append(" WHERE " + table + "." + Statuses.SCREEN_NAME + " IN ( SELECT " + TABLE_FILTERED_USERS + "."
				+ Filters.Users.VALUE + " FROM " + TABLE_FILTERED_USERS + " )");
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

	public static int cancelRetweet(final AsyncTwitterWrapper wrapper, final ParcelableStatus status) {
		if (wrapper == null || status == null) return -1;
		if (status.my_retweet_id > 0)
			return wrapper.destroyStatus(status.account_id, status.my_retweet_id);
		else if (status.retweeted_by_id == status.account_id)
			return wrapper.destroyStatus(status.account_id, status.retweet_id);
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
				final String table = getTableNameByUri(uri);
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
		// Clean cached values.
		for (final Uri uri : CACHE_URIS) {
			final String table = getTableNameByUri(uri);
			final StringBuilder where = new StringBuilder();
			where.append(Statuses._ID + " NOT IN (");
			where.append(" SELECT " + BaseColumns._ID + " FROM " + table);
			where.append(" LIMIT " + (int) (Math.sqrt(item_limit) * 100) + ")");
			resolver.delete(uri, where.toString(), null);
		}
	}

	public static void clearAccountColor() {
		sAccountColors.clear();
	}

	public static void clearAccountName() {
		sAccountScreenNames.clear();
	}

	public static void clearUserColor(final Context context, final long user_id) {
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.remove(Long.toString(user_id));
		editor.commit();
		sUserColors.remove(user_id);
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

	public static void copyStream(final InputStream is, final OutputStream os) throws IOException {
		final int buffer_size = 8192;
		final byte[] bytes = new byte[buffer_size];
		int count = is.read(bytes, 0, buffer_size);
		while (count != -1) {
			os.write(bytes, 0, count);
			count = is.read(bytes, 0, buffer_size);
		}
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
				final boolean large_preview_image = Utils.getImagePreviewDisplayOptionInt(context) == IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE;
		resolver.delete(CachedStatuses.CONTENT_URI, where, null);
		resolver.insert(CachedStatuses.CONTENT_URI, makeStatusContentValues(status, account_id, large_profile_image, large_preview_image));
		return new ParcelableStatus(status, account_id, false, large_profile_image, true);
	}

	public static ParcelableStatus findStatusInDatabases(final Context context, final long account_id,
			final long status_id) {
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

	public static String formatDirectMessageText(final DirectMessage message) {
		if (message == null) return null;
		final String text = message.getRawText();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		parseEntities(builder, message);
		return builder.build().replace("\n", "<br/>");
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
	
	public static String formatExpandedUserDescription(final User user) {
		if (user == null) return null;
		final String text = user.getDescription();
		if (text == null) return null;
		final HtmlBuilder builder = new HtmlBuilder(text, false, true, true);
		final URLEntity[] urls = user.getDescriptionEntities();
		if (urls != null) {
			for (final URLEntity url : urls) {
				final String expanded_url = parseString(url.getExpandedURL());
				if (expanded_url != null) {
					builder.addLink(expanded_url, expanded_url, url.getStart(), url.getEnd());
				}
			}
		}
		return toPlainText(builder.build().replace("\n", "<br/>"));
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
					builder.addLink(parseString(expanded_url), url.getDisplayURL(), url.getStart(), url.getEnd());
				}
			}
		}
		return builder.build().replace("\n", "<br/>");
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

	public static int getAccountColor(final Context context, final long account_id) {
		if (context == null) return Color.TRANSPARENT;
		Integer color = sAccountColors.get(account_id);
		if (color == null) {
			final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.USER_COLOR }, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
			if (cur == null) return Color.TRANSPARENT;
			if (cur.getCount() <= 0) {
				cur.close();
				return Color.TRANSPARENT;
			}
			cur.moveToFirst();
			sAccountColors.put(account_id, color = cur.getInt(cur.getColumnIndexOrThrow(Accounts.USER_COLOR)));
			cur.close();
		}
		return color;
	}

	public static int[] getAccountColors(final Context context, final long[] account_ids) {
		if (context == null || account_ids == null) return null;
		final int length = account_ids.length;
		final int[] colors = new int[length];
		for (int i = 0; i < length; i++) {
			colors[i] = getAccountColor(context, account_ids[i]);
		}
		return colors;
	}

	public static long getAccountId(final Context context, final String screen_name) {
		if (context == null) return -1;
		long user_id = -1;

		final Cursor cur = context.getContentResolver()
				.query(Accounts.CONTENT_URI, new String[] { Accounts.ACCOUNT_ID }, Accounts.SCREEN_NAME + " = ?",
						new String[] { screen_name }, null);
		if (cur == null) return user_id;

		if (cur.getCount() > 0) {
			cur.moveToFirst();
			user_id = cur.getLong(cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID));
		}
		cur.close();
		return user_id;
	}

	public static long[] getAccountIds(final Context context) {
		long[] accounts = new long[0];
		if (context == null) return accounts;
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
				new String[] { Accounts.ACCOUNT_ID }, null, null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID);
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

	public static String getAccountName(final Context context, final long account_id) {
		if (context == null) return null;
		String name = sAccountNames.get(account_id);
		if (name == null) {
			final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts.NAME },
					Accounts.ACCOUNT_ID + " = " + account_id, null, null);
			if (cur == null) return name;

			if (cur.getCount() > 0) {
				cur.moveToFirst();
				name = cur.getString(cur.getColumnIndex(Accounts.NAME));
				sAccountNames.put(account_id, name);
			}
			cur.close();
		}
		return name;
	}

	public static String getAccountScreenName(final Context context, final long account_id) {
		if (context == null) return null;
		String screen_name = sAccountScreenNames.get(account_id);
		if (screen_name == null) {
			final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
					new String[] { Accounts.SCREEN_NAME }, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
			if (cur == null) return screen_name;

			if (cur.getCount() > 0) {
				cur.moveToFirst();
				screen_name = cur.getString(cur.getColumnIndex(Accounts.SCREEN_NAME));
				sAccountScreenNames.put(account_id, screen_name);
			}
			cur.close();
		}
		return screen_name;
	}

	public static String[] getAccountScreenNames(final Context context) {
		String[] accounts = new String[0];
		if (context == null) return accounts;
		final String[] cols = new String[] { Accounts.SCREEN_NAME };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, null, null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.SCREEN_NAME);
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

	public static long[] getActivatedAccountIds(final Context context) {
		long[] accounts = new long[0];
		if (context == null) return accounts;
		final String[] cols = new String[] { Accounts.ACCOUNT_ID };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
				null, Accounts.ACCOUNT_ID);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.ACCOUNT_ID);
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

	public static String[] getActivatedAccountScreenNames(final Context context) {
		String[] accounts = new String[0];
		if (context == null) return accounts;
		final String[] cols = new String[] { Accounts.SCREEN_NAME };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, Accounts.IS_ACTIVATED + "=1",
				null, null);
		if (cur != null) {
			final int idx = cur.getColumnIndexOrThrow(Accounts.SCREEN_NAME);
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

	public static ImageSpec getAllAvailableImage(final String link, final boolean large_image_preview) {
		if (link == null) return null;
		Matcher m;
		m = PATTERN_TWITTER_IMAGES.matcher(link);
		if (m.matches()) return getTwitterImage(link, large_image_preview);
		m = PATTERN_TWITPIC.matcher(link);
		if (m.matches()) return getTwitpicImage(matcherGroup(m, TWITPIC_GROUP_ID), link, large_image_preview);
		m = PATTERN_INSTAGRAM.matcher(link);
		if (m.matches()) return getInstagramImage(matcherGroup(m, INSTAGRAM_GROUP_ID), link, large_image_preview);
		m = PATTERN_IMGUR.matcher(link);
		if (m.matches()) return getImgurImage(matcherGroup(m, IMGUR_GROUP_ID), link, large_image_preview);
		m = PATTERN_IMGLY.matcher(link);
		if (m.matches()) return getImglyImage(matcherGroup(m, IMGLY_GROUP_ID), link, large_image_preview);
		m = PATTERN_YFROG.matcher(link);
		if (m.matches()) return getYfrogImage(matcherGroup(m, YFROG_GROUP_ID), link, large_image_preview);
		m = PATTERN_LOCKERZ_AND_PLIXI.matcher(link);
		if (m.matches()) return getLockerzAndPlixiImage(link, large_image_preview);
		m = PATTERN_SINA_WEIBO_IMAGES.matcher(link);
		if (m.matches()) return getSinaWeiboImage(link, large_image_preview);
		m = PATTERN_TWITGOO.matcher(link);
		if (m.matches()) return getTwitgooImage(matcherGroup(m, TWITGOO_GROUP_ID), link, large_image_preview);
		m = PATTERN_MOBYPICTURE.matcher(link);
		if (m.matches()) return getMobyPictureImage(matcherGroup(m, MOBYPICTURE_GROUP_ID), link, large_image_preview);
		m = PATTERN_PHOTOZOU.matcher(link);
		if (m.matches()) return getPhotozouImage(matcherGroup(m, PHOTOZOU_GROUP_ID), link, large_image_preview);
		return null;
	}

	public static int getAllStatusesCount(final Context context, final Uri uri) {
		if (context == null) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Cursor cur = resolver.query(uri, new String[] { Statuses.STATUS_ID },
				buildStatusFilterWhereClause(getTableNameByUri(uri), null), null, null);
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
		final Cursor cur = resolver.query(uri, new String[] { Statuses.STATUS_ID },
				buildStatusFilterWhereClause(getTableNameByUri(uri), null), null, null);
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

	public static boolean getAsBoolean(final ContentValues values, final String key, final boolean def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Boolean.valueOf(value.toString());
	}

	public static long getAsInteger(final ContentValues values, final String key, final int def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Integer.valueOf(value.toString());
	}

	public static long getAsLong(final ContentValues values, final String key, final long def) {
		if (values == null || key == null) return def;
		final Object value = values.get(key);
		if (value == null) return def;
		return Long.valueOf(value.toString());
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
		final File ext_cache_dir = EnvironmentAccessor.getExternalCacheDir(context);
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

	public static String getBrowserUserAgent(final Context context) {
		if (context == null) return null;
		return TwidereApplication.getInstance(context).getBrowserUserAgent();
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

	public static long getDefaultAccountId(final Context context) {
		if (context == null) return -1;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return prefs.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
	}

	public static String getDefaultAccountScreenName(final Context context) {
		if (context == null) return null;
		return getAccountScreenName(context, getDefaultAccountId(context));
	}

	public static Twitter getDefaultTwitterInstance(final Context context, final boolean include_entities) {
		if (context == null) return null;
		return getDefaultTwitterInstance(context, include_entities, true);
	}

	public static Twitter getDefaultTwitterInstance(final Context context, final boolean include_entities,
			final boolean use_httpclient) {
		if (context == null) return null;
		return getTwitterInstance(context, getDefaultAccountId(context), include_entities, use_httpclient);
	}
	
	public static String getErrorMessage(final Context context, final Throwable t) {
		if (t == null) return null;
		if (context != null && t instanceof TwitterException) return getTwitterErrorMessage(context, (TwitterException) t);
		return t.getMessage();
	}
	
	public static String getTwitterErrorMessage(final Context context, final TwitterException te) {
		if (te == null) return null;
		final String msg = TwitterErrorCodes.getErrorMessage(context, te.getErrorCode());
		if (isEmpty(msg)) return te.getMessage();
		return msg;
	}

	public static HttpClientWrapper getHttpClient(final int timeout_millis, final boolean ignore_ssl_error,
			final Proxy proxy, final HostAddressResolver resolver, final String user_agent) {
		final ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setHttpConnectionTimeout(timeout_millis);
		cb.setIgnoreSSLError(ignore_ssl_error);
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
		final String user_agent = getBrowserUserAgent(context);
		final HostAddressResolver resolver = TwidereApplication.getInstance(context).getHostAddressResolver();
		return getHttpClient(timeout_millis, true, proxy, resolver, user_agent);
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

		final String media_uri_start = parseString(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

		if (parseString(uri).startsWith(media_uri_start)) {

			final String[] proj = { MediaStore.Images.Media.DATA };
			final Cursor cur = context.getContentResolver().query(uri, proj, null, null, null);

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

	public static List<ImageSpec> getImagesInStatus(final String status_string) {
		if (status_string == null) return Collections.emptyList();
		final List<ImageSpec> images = new ArrayList<ImageSpec>();
		final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
		extractor.grabLinks(status_string);
		for (final HtmlLink link : extractor.grabLinks(status_string)) {
			final ImageSpec spec = getAllAvailableImage(link.getLink(), true);
			if (spec != null) {
				images.add(spec);
			}
		}
		return images;
	}

	public static String getImageUploadStatus(final Context context, final String link, final String text) {
		if (context == null) return null;
		String image_upload_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getString(PREFERENCE_KEY_IMAGE_UPLOAD_FORMAT, PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT);
		if (isEmpty(image_upload_format)) {
			image_upload_format = PREFERENCE_DEFAULT_IMAGE_UPLOAD_FORMAT;
		}
		if (link == null) return text;
		return image_upload_format.replace(FORMAT_PATTERN_LINK, link).replace(FORMAT_PATTERN_TEXT, text);
	}

	public static ImageSpec getImglyImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "https://img.ly/show/full/" + id;
		final String preview = "https://img.ly/show/" + (large_image_preview ? "medium" : "thumb") + "/" + id;
		return new ImageSpec(preview, full, orig);

	}

	public static ImageSpec getImgurImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "http://i.imgur.com/" + id + ".jpg";
		final String preview = "http://i.imgur.com/" + id + (large_image_preview ? "l.jpg" : "s.jpg");
		return new ImageSpec(preview, full, orig);
	}

	public static int getImagePreviewDisplayOptionInt(final Context context) {
		if (context == null) return IMAGE_PREVIEW_DISPLAY_OPTION_CODE_NONE;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String option = prefs.getString(PREFERENCE_KEY_IMAGE_PREVIEW_DISPLAY_OPTION,
				IMAGE_PREVIEW_DISPLAY_OPTION_NONE);
		return getImagePreviewDisplayOptionInt(option);
	}

	public static int getImagePreviewDisplayOptionInt(final String option) {
		if (IMAGE_PREVIEW_DISPLAY_OPTION_LARGE.equals(option))
			return IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE;
		else if (IMAGE_PREVIEW_DISPLAY_OPTION_SMALL.equals(option))
			return IMAGE_PREVIEW_DISPLAY_OPTION_CODE_SMALL;
		return IMAGE_PREVIEW_DISPLAY_OPTION_CODE_NONE;
	}

	public static ImageSpec getInstagramImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "https://instagr.am/p/" + id + "/media/?size=l";
		final String preview = large_image_preview ? full : "https://instagr.am/p/" + id + "/media/?size=t";
		return new ImageSpec(preview, full, orig);
	}

	public static ImageSpec getLockerzAndPlixiImage(final String url, final boolean large_image_preview) {
		if (isEmpty(url)) return null;
		final String full = "https://api.plixi.com/api/tpapi.svc/imagefromurl?url=" + url + "&size=big";
		final String preview = large_image_preview ? full : "https://api.plixi.com/api/tpapi.svc/imagefromurl?url="
				+ url + "&size=small";
		return new ImageSpec(preview, full, url);

	}

	public static ImageSpec getMobyPictureImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "https://moby.to/" + id + ":full";
		final String preview = large_image_preview ? full : "https://moby.to/" + id + ":thumb";
		return new ImageSpec(preview, full, orig);
	}

	public static String getNameDisplayOption(final Context context) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH);
	}

	public static int getNameDisplayOptionInt(final Context context) {
		return getNameDisplayOptionInt(getNameDisplayOption(context));
	}

	public static int getNameDisplayOptionInt(final String option) {
		if (NAME_DISPLAY_OPTION_NAME.equals(option))
			return NAME_DISPLAY_OPTION_CODE_NAME;
		else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(option)) return NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		return NAME_DISPLAY_OPTION_CODE_BOTH;
	}

	public static long[] getNewestMessageIdsFromDatabase(final Context context, final Uri uri) {
		if (context == null || uri == null) return null;
		final long[] account_ids = getActivatedAccountIds(context);
		final String[] cols = new String[] { DirectMessages.MESSAGE_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = resolver.query(uri, cols, where, null, DirectMessages.DEFAULT_SORT_ORDER);
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
		if (context == null || uri == null) return null;
		final long[] account_ids = getActivatedAccountIds(context);
		final String[] cols = new String[] { Statuses.STATUS_ID };
		final ContentResolver resolver = context.getContentResolver();
		final long[] status_ids = new long[account_ids.length];
		int idx = 0;
		for (final long account_id : account_ids) {
			final String where = Statuses.ACCOUNT_ID + " = " + account_id;
			final Cursor cur = resolver.query(uri, cols, where, null, Statuses.DEFAULT_SORT_ORDER);
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

	public static String getNormalTwitterProfileImage(final String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "_normal");
		return url;
	}

	public static long[] getOldestMessageIdsFromDatabase(final Context context, final Uri uri) {
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

	public static long[] getOldestStatusIdsFromDatabase(final Context context, final Uri uri) {
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

	public static String getOriginalTwitterProfileImage(final String url) {
		if (url == null) return null;
		if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(url).matches())
			return replaceLast(url, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES, "");
		return url;
	}

	public static ImageSpec getPhotozouImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "http://photozou.jp/p/img/" + id;
		final String preview = large_image_preview ? full : "http://photozou.jp/p/thumb/" + id;
		return new ImageSpec(preview, full, orig);
	}

	public static ImageSpec getPreviewImage(final String html, final int display_option) {
		if (html == null) return null;
		if (display_option == IMAGE_PREVIEW_DISPLAY_OPTION_CODE_NONE
				&& (html.contains(".twimg.com/") || html.contains("://instagr.am/")
						|| html.contains("://instagram.com/") || html.contains("://imgur.com/")
						|| html.contains("://i.imgur.com/") || html.contains("://twitpic.com/")
						|| html.contains("://img.ly/") || html.contains("://yfrog.com/")
						|| html.contains("://twitgoo.com/") || html.contains("://moby.to/")
						|| html.contains("://plixi.com/p/") || html.contains("://lockerz.com/s/")
						|| html.contains(".sinaimg.cn/") || html.contains("://photozou.jp/")))
			return ImageSpec.getEmpty();
		final boolean large_image_preview = display_option == IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE;
		final HtmlLinkExtractor extractor = new HtmlLinkExtractor();
		for (final HtmlLink link : extractor.grabLinks(html)) {
			final ImageSpec image = getAllAvailableImage(link.getLink(), large_image_preview);
			if (image != null) return image;
		}
		return null;
	}

	public static Proxy getProxy(final Context context) {
		if (context == null) return null;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean enable_proxy = prefs.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		if (!enable_proxy) return Proxy.NO_PROXY;
		final String proxy_host = prefs.getString(PREFERENCE_KEY_PROXY_HOST, null);
		final int proxy_port = parseInt(prefs.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
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

	public static String getShareStatus(final Context context, final CharSequence title, final CharSequence text) {
		if (context == null) return null;
		String share_format = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString(
				PREFERENCE_KEY_SHARE_FORMAT, PREFERENCE_DEFAULT_SHARE_FORMAT);
		if (isEmpty(share_format)) {
			share_format = PREFERENCE_DEFAULT_SHARE_FORMAT;
		}
		if (isEmpty(title)) return parseString(text);
		return share_format.replace(FORMAT_PATTERN_TITLE, title).replace(FORMAT_PATTERN_TEXT, text != null ? text : "");
	}

	public static ImageSpec getSinaWeiboImage(final String url, final boolean large_image_preview) {
		if (isEmpty(url)) return null;
		final String full = url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES + "\\/", "/large/");
		final String preview = large_image_preview ? full : url.replaceAll("\\/" + SINA_WEIBO_IMAGES_AVAILABLE_SIZES
				+ "\\/", "/thumbnail/");
		return new ImageSpec(preview, full, full);
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

	public static int getStatusTypeIconRes(final boolean is_fav, final boolean has_location, final boolean has_media, 
			final boolean is_possibly_sensitive) {
		if (is_fav)
			return R.drawable.ic_indicator_starred;
		else if (is_possibly_sensitive && has_media)
			return R.drawable.ic_indicator_reported_media;
		else if (has_media)
			return R.drawable.ic_indicator_has_media;
		else if (has_location) return R.drawable.ic_indicator_has_location;
		return 0;
	}

	public static Drawable getTabIconDrawable(final Context context, final Object icon_obj) {
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

	public static Object getTabIconObject(final String type) {
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

	public static List<TabSpec> getTabs(final Context context) {
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
				args.putInt(INTENT_KEY_TAB_POSITION, position);
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

	public static String getTabTypeName(final Context context, final String type) {
		if (context == null) return null;
		final Integer res_id = CUSTOM_TABS_TYPE_NAME_MAP.get(type);
		return res_id != null ? context.getString(res_id) : null;
	}

	public static int getTextCount(final String string) {
		if (string == null) return 0;
		return ArrayUtils.toStringArray(string).length;
	}
	
	public static int getThemeColor(final Context context) {
		if (context == null) return Color.TRANSPARENT;
		final int def = context.getResources().getColor(R.color.holo_blue_light);
		try {		
			final TypedArray a = context.obtainStyledAttributes(new int[] { android.R.attr.colorActivatedHighlight });
			final int color = a.getColor(0, def);
			a.recycle();
			return color;	
		} catch (final Exception e) {
			return def;
		}
	}

	public static int getTextCount(final TextView view) {
		if (view == null) return 0;
		final String string = parseString(view.getText());
		return getTextCount(string);
	}

	public static long getTimestampFromDate(final Date date) {
		if (date == null) return -1;
		return date.getTime();
	}

	public static ImageSpec getTwitgooImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "https://twitgoo.com/show/img/" + id;
		final String preview = large_image_preview ? full : "https://twitgoo.com/show/thumb/" + id;
		return new ImageSpec(preview, full, orig);
	}

	public static ImageSpec getTwitpicImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String full = "https://twitpic.com/show/large/" + id;
		final String preview = large_image_preview ? full : "https://twitpic.com/show/thumb/" + id;
		return new ImageSpec(preview, full, orig);
	}

	public static ImageSpec getTwitterImage(final String url, final boolean large_image_preview) {
		if (isEmpty(url)) return null;
		final String full = (url + ":large").replaceFirst("https?://", "https://");
		final String preview = large_image_preview ? full : (url + ":thumb").replaceFirst("https?://", "https://");
		return new ImageSpec(preview, full, full);
	}

	public static Twitter getTwitterInstance(final Context context, final long account_id,
			final boolean include_entities) {
		return getTwitterInstance(context, account_id, include_entities, true);
	}

	public static Twitter getTwitterInstance(final Context context, final long account_id,
			final boolean include_entities, final boolean use_apache_httpclient) {
		if (context == null) return null;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final int connection_timeout = preferences.getInt(PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
		final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, true);
		final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
		final boolean enable_proxy = preferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		final String consumer_key = preferences.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY).trim();
		final String consumer_secret = preferences.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET)
				.trim();
		final StringBuilder where = new StringBuilder();
		where.append(Accounts.ACCOUNT_ID + " = " + account_id);
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS, where.toString(),
				null, null);
		if (cur == null) return null;
		try {
			if (cur.getCount() > 0) {
				cur.moveToFirst();
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
					final String proxy_host = preferences.getString(PREFERENCE_KEY_PROXY_HOST, null);
					final int proxy_port = parseInt(preferences.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
					if (!isEmpty(proxy_host) && proxy_port > 0) {
						cb.setHttpProxyHost(proxy_host);
						cb.setHttpProxyPort(proxy_port);
					}
				}
				final String rest_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.REST_BASE_URL));
				final String signing_rest_base_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.SIGNING_REST_BASE_URL));
				final String oauth_base_url = cur.getString(cur.getColumnIndexOrThrow(Accounts.OAUTH_BASE_URL));
				final String signing_oauth_base_url = cur.getString(cur
						.getColumnIndexOrThrow(Accounts.SIGNING_OAUTH_BASE_URL));
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
				switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
					case Accounts.AUTH_TYPE_OAUTH:
					case Accounts.AUTH_TYPE_XAUTH: {
						if (isEmpty(consumer_key) || isEmpty(consumer_secret)) {
							cb.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
							cb.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
						} else {
							cb.setOAuthConsumerKey(consumer_key);
							cb.setOAuthConsumerSecret(consumer_secret);
						}
						final String oauth_token = cur.getString(cur.getColumnIndexOrThrow(Accounts.OAUTH_TOKEN));
						final String token_secret = cur.getString(cur.getColumnIndexOrThrow(Accounts.TOKEN_SECRET));
						if (isEmpty(oauth_token) || isEmpty(token_secret)) return null;
						return new TwitterFactory(cb.build()).getInstance(new AccessToken(oauth_token,
								token_secret));
					}
					case Accounts.AUTH_TYPE_BASIC: {
						final String screen_name = cur.getString(cur.getColumnIndexOrThrow(Accounts.SCREEN_NAME));
						final String password = cur.getString(cur.getColumnIndexOrThrow(Accounts.BASIC_AUTH_PASSWORD));
						if (isEmpty(screen_name) || isEmpty(password)) return null;
						return new TwitterFactory(cb.build()).getInstance(new BasicAuthorization(screen_name,
								password));
					}
					case Accounts.AUTH_TYPE_TWIP_O_MODE: {
						return new TwitterFactory(cb.build()).getInstance(new TwipOModeAuthorization());
					}
					default:
				}
			}
			return null;
		} finally {
			cur.close();
		}
	}

	public static String getUnescapedStatusString(final String string) {
		if (string == null) return null;
		return string.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">");
	}

	public static int getUserColor(final Context context, final long user_id) {
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

	public static String getUserName(final Context context, final ParcelableUser user) {
		if (context == null || user == null) return null;
		final boolean display_screen_name = getNameDisplayOptionInt(context) == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		return display_screen_name ? user.screen_name : user.name;
	}

	public static String getUserName(final Context context, final ParcelableStatus user) {
		if (context == null || user == null) return null;
		final boolean display_screen_name = getNameDisplayOptionInt(context) == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		return display_screen_name ? user.user_screen_name : user.user_name;
	}
	
	public static String getSenderUserName(final Context context, final ParcelableDirectMessage user) {
		if (context == null || user == null) return null;
		final boolean display_screen_name = getNameDisplayOptionInt(context) == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		return display_screen_name ? user.sender_screen_name : user.sender_name;
	}
	
	public static String getUserName(final Context context, final User user) {
		if (context == null || user == null) return null;
		final boolean display_screen_name = getNameDisplayOptionInt(context) == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		return display_screen_name ? user.getScreenName() : user.getName();
	}

	public static int getUserTypeIconRes(final boolean is_verified, final boolean is_protected) {
		if (is_verified)
			return R.drawable.ic_indicator_verified;
		else if (is_protected) return R.drawable.ic_indicator_is_protected;
		return 0;
	}

	public static ImageSpec getYfrogImage(final String id, final String orig, final boolean large_image_preview) {
		if (isEmpty(id)) return null;
		final String preview = "https://yfrog.com/" + id + ":iphone";
		final String full = "https://yfrog.com/" + id + (large_image_preview ? ":medium" : ":small");
		return new ImageSpec(preview, full, orig);

	}

	public static boolean hasActiveConnection(final Context context) {
		if (context == null) return false;
		final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) return true;
		return false;
	}

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

	public static boolean isFiltered(final SQLiteDatabase database, final ParcelableStatus status) {
		if (status == null) return false;
		return isFiltered(database, status.text_plain, status.text_html, status.user_screen_name, status.source);
	}

	public static boolean isFiltered(final SQLiteDatabase database, final String text_plain, final String text_html,
			final String screen_name, final String source) {
		if (database == null) return false;
		if (text_plain == null && text_html == null && screen_name == null && source == null) return false;
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
		if (screen_name != null) {
			if (!selection_args.isEmpty()) {			
				builder.append(" OR ");	
			}
			selection_args.add(screen_name);
			builder.append("(SELECT ? IN (SELECT " + Filters.VALUE + " FROM " + TABLE_FILTERED_USERS + "))");
		}
		if (source != null) {
			if (!selection_args.isEmpty()) {			
				builder.append(" OR ");	
			}
			selection_args.add(source);
			builder.append("(SELECT 1 IN (SELECT ? LIKE '%>'||" + TABLE_FILTERED_SOURCES + "." + Filters.VALUE
					+ "||'</a>%' FROM " + TABLE_FILTERED_SOURCES + "))");
		}
		final Cursor cur = database.rawQuery(builder.toString(), selection_args.toArray(new String[selection_args.size()]));
		if (cur == null) return false;
		try {
			return cur.getCount() > 0;
		} finally {
			cur.close();
		}
	}

	public static boolean isMyAccount(final Context context, final long account_id) {
		if (context == null) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Accounts.ACCOUNT_ID + " = " + account_id;
		final Cursor cur = resolver.query(Accounts.CONTENT_URI, new String[0], where, null, null);
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
		final Cursor cur = resolver.query(Accounts.CONTENT_URI, new String[0], where, new String[] { screen_name },
				null);
		try {
			return cur != null && cur.getCount() > 0;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static boolean isMyActivatedAccount(final Context context, final long account_id) {
		if (context == null || account_id <= 0) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Accounts.IS_ACTIVATED + " = 1 AND " + Accounts.ACCOUNT_ID + " = " + account_id;
		final Cursor cur = resolver.query(Accounts.CONTENT_URI, new String[0], where, null, null);
		try {
			return cur != null && cur.getCount() > 0;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public static boolean isMyActivatedAccount(final Context context, final String screen_name) {
		if (context == null) return false;
		final ContentResolver resolver = context.getContentResolver();
		final String where = Accounts.IS_ACTIVATED + " = 1 AND " + Accounts.SCREEN_NAME + " = ?";
		final Cursor cur = resolver.query(Accounts.CONTENT_URI, new String[0], where, new String[] { screen_name },
				null);
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
		final SharedPreferences prefs = context.getSharedPreferences(SILENT_NOTIFICATIONS_PREFERENCE_NAME, Context.MODE_PRIVATE);
		final Calendar now = Calendar.getInstance();
		return prefs.getBoolean("silent_notifications_at_" + now.get(Calendar.HOUR_OF_DAY), false);
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
		//return ConfigurationAccessor.getLayoutDirection(res.getConfiguration()) == SCREENLAYOUT_LAYOUTDIR_RTL;
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
			new URL(parseString(url));
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
				break;
			}
		}
		values.put(Accounts.AUTH_TYPE, auth_type);
		values.put(Accounts.ACCOUNT_ID, user.getId());
		values.put(Accounts.SCREEN_NAME, user.getScreenName());
		values.put(Accounts.NAME, user.getName());
		values.put(Accounts.PROFILE_IMAGE_URL, parseString(user.getProfileImageUrlHttps()));
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
		final String profile_image_url = parseString(user.getProfileImageUrlHttps());
		final String url = parseString(user.getURL());
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
		values.put(CachedUsers.URL_EXPANDED, url != null && urls != null && urls.length > 0 ? parseString(urls[0].getExpandedURL()) : null);
		values.put(CachedUsers.PROFILE_BANNER_URL, user.getProfileBannerImageUrl());
		return values;
	}

	public static ContentValues makeDirectMessageContentValues(final DirectMessage message, final long account_id,
			final boolean is_outgoing, final boolean large_profile_image) {
		if (message == null || message.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		final User sender = message.getSender(), recipient = message.getRecipient();
		if (sender == null || recipient == null) return null;
		final String sender_profile_image_url = parseString(sender.getProfileImageUrlHttps());
		final String recipient_profile_image_url = parseString(recipient.getProfileImageUrlHttps());
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

	public static ContentValues makeStatusContentValues(Status status, final long account_id,
			final boolean large_profile_image, final boolean large_preview_image) {
		if (status == null || status.getId() <= 0) return null;
		final ContentValues values = new ContentValues();
		values.put(Statuses.ACCOUNT_ID, account_id);
		values.put(Statuses.STATUS_ID, status.getId());
		values.put(Statuses.MY_RETWEET_ID, status.getCurrentUserRetweet());
		final boolean is_retweet = status.isRetweet();
		User user = status.getUser();
		values.put(CachedUsers.IS_FOLLOWING, user != null ? user.isFollowing() : false);
		final Status retweeted_status = is_retweet ? status.getRetweetedStatus() : null;
		if (retweeted_status != null) {
			final User retweet_user = status.getUser();
			values.put(Statuses.RETWEET_ID, retweeted_status.getId());
			values.put(Statuses.RETWEETED_BY_ID, retweet_user.getId());
			values.put(Statuses.RETWEETED_BY_NAME, retweet_user.getName());
			values.put(Statuses.RETWEETED_BY_SCREEN_NAME, retweet_user.getScreenName());
			user = retweeted_status.getUser();
			status = retweeted_status;
		}
		if (user != null) {
			final long user_id = user.getId();
			final String profile_image_url = parseString(user.getProfileImageUrlHttps());
			final String name = user.getName(), screen_name = user.getScreenName();
			values.put(Statuses.USER_ID, user_id);
			values.put(Statuses.NAME, name);
			values.put(Statuses.SCREEN_NAME, screen_name);
			values.put(Statuses.IS_PROTECTED, user.isProtected());
			values.put(Statuses.IS_VERIFIED, user.isVerified());
			values.put(Statuses.PROFILE_IMAGE_URL,
					large_profile_image ? getBiggerTwitterProfileImage(profile_image_url) : profile_image_url);
		}
		if (status.getCreatedAt() != null) {
			values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
		}
		final String text_html = formatStatusText(status);
		values.put(Statuses.TEXT_HTML, text_html);
		values.put(Statuses.TEXT_PLAIN, status.getText());
		values.put(Statuses.TEXT_UNESCAPED, toPlainText(text_html));
		values.put(Statuses.RETWEET_COUNT, status.getRetweetCount());
		values.put(Statuses.IN_REPLY_TO_SCREEN_NAME, status.getInReplyToScreenName());
		values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
		values.put(Statuses.SOURCE, status.getSource());
		values.put(Statuses.IS_POSSIBLY_SENSITIVE, status.isPossiblySensitive());
		final GeoLocation location = status.getGeoLocation();
		if (location != null) {
			values.put(Statuses.LOCATION, location.getLatitude() + "," + location.getLongitude());
		}
		values.put(Statuses.IS_RETWEET, is_retweet);
		values.put(Statuses.IS_FAVORITE, status.isFavorited());
		final ImageSpec preview = getPreviewImage(text_html,
				!large_preview_image ? IMAGE_PREVIEW_DISPLAY_OPTION_CODE_SMALL : IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE);
		values.put(Statuses.IMAGE_PREVIEW_URL, preview != null ? preview.image_preview_url : null);
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

	public static void openDirectMessagesConversation(final Activity activity, final long account_id,
			final long conversation_id, final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof DirectMessagesConversationFragment && details_fragment.isAdded()) {
				((DirectMessagesConversationFragment) details_fragment).showConversation(account_id, conversation_id, screen_name);
				dual_pane_activity.showRightPane();
			} else {
				final Fragment fragment = new DirectMessagesConversationFragment();
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

	public static void openImage(final Context context, final String uri, final String orig,
			final boolean is_possibly_sensitive) {
		if (context == null || uri == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (context instanceof FragmentActivity && is_possibly_sensitive
				&& !prefs.getBoolean(PREFERENCE_KEY_DISPLAY_SENSITIVE_CONTENTS, false)) {
			final FragmentActivity activity = (FragmentActivity) context;
			final FragmentManager fm = activity.getSupportFragmentManager();
			final DialogFragment fragment = new SensitiveContentWarningDialogFragment();
			final Bundle args = new Bundle();
			args.putParcelable(INTENT_KEY_URI, Uri.parse(uri));
			if (orig != null) {
				args.putParcelable(INTENT_KEY_URI_ORIG, Uri.parse(orig));
			}
			fragment.setArguments(args);
			fragment.show(fm, "sensitive_content_warning");
		} else {
			openImageDirectly(context, uri, orig);
		}
	}

	public static void openImageDirectly(final Context context, final String uri, final String orig) {
		if (context == null || uri == null) return;
		final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE);
		intent.setData(Uri.parse(uri));
		if (orig != null) {
			intent.putExtra(INTENT_KEY_URI_ORIG, Uri.parse(orig));
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
			intent.setClass(context, ImageViewerGLActivity.class);
		} else {
			intent.setClass(context, ImageViewerActivity.class);
		}
		context.startActivity(intent);
	}

	public static void openIncomingFriendships(final Activity activity, final long account_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new IncomingFriendshipsFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_INCOMING_FRIENDSHIPS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}

	public static void openSavedSearches(final Activity activity, final long account_id) {
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

	public static void openStatus(final Activity activity, final long account_id, final long status_id) {
		if (activity == null || account_id <= 0 || status_id <= 0) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new StatusFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_STATUS_ID, status_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_RIGHT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			activity.startActivity(intent);
		}
	}

	public static void openStatus(final Activity activity, final ParcelableStatus status) {
		if (activity == null || status == null) return;
		final long account_id = status.account_id, status_id = status.id;
		final Bundle bundle = new Bundle();
		bundle.putParcelable(INTENT_KEY_STATUS, status);
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof StatusFragment && details_fragment.isAdded()) {
				((StatusFragment) details_fragment).displayStatus(status);
				dual_pane_activity.showRightPane();
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

	public static void openStatusRetweeters(final Activity activity, final long account_id, final long status_id) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new StatusRetweetersListFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_STATUS_ID, status_id);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS_RETWEETERS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			activity.startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
		}
	}
	
	public static void openTweetSearch(final Activity activity, final long account_id, final String query) {
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

	public static void openUserBlocks(final Activity activity, final long account_id) {
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

	public static void openUserFavorites(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
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

	public static void openUserFollowers(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
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

	public static void openUserFriends(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
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

	public static void openUserListDetails(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
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
			dual_pane_activity.showFragment(fragment, true);
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

	public static void openUserListMembers(final Activity activity, final ParcelableUserList list) {
		if (activity == null || list == null) return;
		openUserListMembers(activity, list.account_id, list.id, list.user_id, list.user_screen_name, list.name);
	}

	public static void openUserListMembers(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
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

	public static void openUserLists(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null) return;
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UserListsListFragment();
			final Bundle args = new Bundle();
			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			args.putLong(INTENT_KEY_USER_ID, user_id);
			args.putString(INTENT_KEY_SCREEN_NAME, screen_name);
			fragment.setArguments(args);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_LISTS);
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

	public static void openUserListSubscribers(final Activity activity, final ParcelableUserList list) {
		if (activity == null || list == null) return;
		openUserListSubscribers(activity, list.account_id, list.id, list.user_id, list.user_screen_name, list.name);
	}
	
	public static void openUserListSubscribers(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
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
			builder.authority(AUTHORITY_LISTS);
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
	
	public static void openUserListTimeline(final Activity activity, final ParcelableUserList list) {
		if (activity == null || list == null) return;
		openUserListTimeline(activity, list.account_id, list.id, list.user_id, list.user_screen_name, list.name);
	}

	public static void openUserListTimeline(final Activity activity, final long account_id, final int list_id,
			final long user_id, final String screen_name, final String list_name) {
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

	public static void openUserMentions(final Activity activity, final long account_id, final String screen_name) {
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

	public static void openUserProfile(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
		if (activity == null || account_id <= 0 || user_id <= 0 && isEmpty(screen_name)) return;
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
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			activity.startActivity(intent);
		}
	}

	public static void openUserProfile(final Activity activity, final ParcelableUser user) {
		if (activity == null || user == null) return;
		final Bundle bundle = new Bundle();
		bundle.putParcelable(INTENT_KEY_USER, user);
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment details_fragment = dual_pane_activity.getDetailsFragment();
			if (details_fragment instanceof UserProfileFragment && details_fragment.isAdded()) {
				((UserProfileFragment) details_fragment).displayUser(user);
				dual_pane_activity.showRightPane();
			} else {
				final Fragment fragment = new UserProfileFragment();
				final Bundle args = new Bundle(bundle);
				args.putLong(INTENT_KEY_ACCOUNT_ID, user.account_id);
				if (user.id > 0) {
					args.putLong(INTENT_KEY_USER_ID, user.id);
				}
				if (user.screen_name != null) {
					args.putString(INTENT_KEY_SCREEN_NAME, user.screen_name);
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
			intent.putExtras(bundle);
			activity.startActivity(intent);
		}
	}
	
	public static void openStatuses(final Activity activity, final List<ParcelableStatus> statuses) {
		if (activity == null || statuses == null) return;
		final Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(INTENT_KEY_STATUSES, new ArrayList<ParcelableStatus>(statuses));
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new StatusesListFragment();
			fragment.setArguments(bundle);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUSES);
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			intent.putExtras(bundle);
			activity.startActivity(intent);
		}
	}
	
	public static void openUsers(final Activity activity, final List<ParcelableUser> users) {
		if (activity == null || users == null) return;
		final Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(INTENT_KEY_USERS, new ArrayList<ParcelableUser>(users));
		if (activity instanceof DualPaneActivity && ((DualPaneActivity) activity).isDualPaneMode()) {
			final DualPaneActivity dual_pane_activity = (DualPaneActivity) activity;
			final Fragment fragment = new UsersListFragment();
			fragment.setArguments(bundle);
			dual_pane_activity.showAtPane(DualPaneActivity.PANE_LEFT, fragment, true);
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_USERS);
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
			intent.putExtras(bundle);
			activity.startActivity(intent);
		}
	}

	public static void openUserTimeline(final Activity activity, final long account_id, final long user_id,
			final String screen_name) {
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

	public static Bundle parseArguments(final String string) {
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
						bundle.putBoolean(key, json.optBoolean(key));
					} else if (value instanceof Integer) {
						// Simple workaround for account_id
						if (INTENT_KEY_ACCOUNT_ID.equals(key)) {
							bundle.putLong(key, json.optLong(key));
						} else {
							bundle.putInt(key, json.optInt(key));
						}
					} else if (value instanceof Long) {
						bundle.putLong(key, json.optLong(key));
					} else if (value instanceof String) {
						bundle.putString(key, json.optString(key));
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

	public static double parseDouble(final String source) {
		if (source == null) return -1;
		try {
			return Double.parseDouble(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static int parseInt(final String source) {
		return parseInt(source, -1);
	}

	public static int parseInt(final String source, final int def) {
		if (source == null) return def;
		try {
			return Integer.valueOf(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return def;
	}

	public static long parseLong(final String source) {
		if (source == null) return -1;
		try {
			return Long.parseLong(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	public static String parseString(final Object object) {
		return parseString(object, null);
	}

	public static String parseString(final Object object, final String def) {
		if (object == null) return def;
		return String.valueOf(object);
	}

	public static URL parseURL(final String url_string) {
		if (url_string == null) return null;
		try {
			return new URL(url_string);
		} catch (final MalformedURLException e) {
			// This should not happen.
		}
		return null;
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			ActivityAccessor.overridePendingTransition(activity, enter_anim, exit_anim);
		} else {
			activity.getWindow().setWindowAnimations(0);
		}
		activity.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			ActivityAccessor.overridePendingTransition(activity, enter_anim, exit_anim);
		} else {
			activity.getWindow().setWindowAnimations(0);
		}
		activity.startActivity(activity.getIntent());
	}

	public static void scrollListToPosition(final ListView list, final int position) {
		if (list == null) return;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			list.setSelection(position);
			list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_CANCEL, 0, 0, 0));
		} else {
			list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_DOWN, 0, 0, 0));
			list.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
					MotionEvent.ACTION_UP, 0, 0, 0));
			list.setSelection(position);
		}
	}

	public static void scrollListToTop(final ListView list) {
		if (list == null) return;
		scrollListToPosition(list, 0);
	}

	public static void setMenuForStatus(final Context context, final Menu menu, final ParcelableStatus status) {
		if (context == null || menu == null || status == null) return;
		final int activated_color = getThemeColor(context);
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
		final Intent extension_intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_STATUS);
		final Bundle extension_extras = new Bundle();
		extension_extras.putParcelable(INTENT_KEY_STATUS, status);
		extension_intent.putExtras(extension_extras);
		final MenuItem more_submenu = menu.findItem(R.id.more_submenu);
		addIntentToMenu(context, more_submenu != null ? more_submenu.getSubMenu() : menu, extension_intent);
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
		if (context == null) return;
		final SharedPreferences prefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(parseString(user_id), color);
		editor.commit();
		sUserColors.put(user_id, color);
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

	public static void showErrorMessage(final Context context, final CharSequence message, final boolean long_message) {
		if (context == null) return;
		final String text;
		if (message != null) {
			text = context.getString(R.string.error_message, message);
		} else {
			text = context.getString(R.string.error_unknown_error);
		}
		if (context instanceof Activity) {
			final Crouton crouton = Crouton.makeText((Activity) context, message, CroutonStyle.ALERT);
			final CroutonConfiguration.Builder cb = new CroutonConfiguration.Builder();
			cb.setDuration(long_message ? CroutonConfiguration.DURATION_LONG : CroutonConfiguration.DURATION_SHORT);
			crouton.setConfiguration(cb.build());
			crouton.show();
		} else {
			final Toast toast = Toast.makeText(context, text, long_message ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public static void showErrorMessage(final Context context, final CharSequence action, final CharSequence msg,
			final boolean long_message) {
		if (context == null) return;
		final String message;
		if (msg != null) {
			message = context.getString(R.string.error_message, msg);
		} else {
			message = context.getString(R.string.error_unknown_error);
		}
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

	public static void showErrorMessage(final Context context, final CharSequence action, final Throwable t,
			final boolean long_message) {
		if (context == null) return;
		final String message;
		if (t instanceof TwitterException) {
			showTwitterErrorMessage(context, action, (TwitterException) t, long_message);
			return;
		} else if (t != null) {
			t.printStackTrace();
			final String t_message = trimLineBreak(t.getMessage());
			if (action != null) {
				message = context.getString(R.string.error_message_with_action, action, t_message);
			} else {
				message = context.getString(R.string.error_message, t_message);
			}
		} else {
			message = context.getString(R.string.error_unknown_error);
		}
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

	public static void showOkMessage(final Context context, final int resId, final boolean long_message) {
		if (context == null) return;
		showOkMessage(context, context.getText(resId), long_message);
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

	public static void showInfoMessage(final Context context, final int resId, final boolean long_message) {
		if (context == null) return;
		showInfoMessage(context, context.getText(resId), long_message);
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
	
	public static void showTwitterErrorMessage(final Context context, final CharSequence action, final TwitterException te,
			final boolean long_message) {
		if (context == null) return;
		final String message;
		if (te != null) {
			te.printStackTrace();
			if (action != null) {
				if (te.exceededRateLimitation()) {
					final RateLimitStatus status = te.getRateLimitStatus();
					final long sec_until_reset = status.getSecondsUntilReset() * 1000;
					final String next_reset_time = parseString(getRelativeTimeSpanString(System.currentTimeMillis() + sec_until_reset));
					message = context.getString(R.string.error_message_rate_limit, action, next_reset_time.trim());
				} else if (te.getErrorCode() > 0) {
					final String msg = TwitterErrorCodes.getErrorMessage(context, te.getErrorCode());
					message = context.getString(R.string.error_message_with_action, action, msg != null ? msg : trimLineBreak(te.getMessage()));
				} else if (te.getCause() instanceof SSLException) {
					final String msg = te.getCause().getMessage();
					if (msg != null && msg.contains("!=")) {						
						message = context.getString(R.string.error_message_with_action, action, context.getString(R.string.ssl_error));
					} else {						
						message = context.getString(R.string.error_message_with_action, action, context.getString(R.string.network_error));
					}
				} else if (te.getCause() instanceof IOException) {					
					message = context.getString(R.string.error_message_with_action, action, context.getString(R.string.network_error));
				} else {					
					message = context.getString(R.string.error_message_with_action, action, trimLineBreak(te.getMessage()));
				}
			} else {
				message = context.getString(R.string.error_message, trimLineBreak(te.getMessage()));
			}
		} else {
			message = context.getString(R.string.error_unknown_error);
		}
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
	
	public static void showWarnMessage(final Context context, final int resId, final boolean long_message) {
		if (context == null) return;
		showWarnMessage(context, context.getText(resId), long_message);
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
	
	public static String trimLineBreak(final String orig) {
		if (orig == null) return null;
		return orig.replaceAll("\\n+", "\n");
	}

	private static Bitmap getTabIconFromFile(final File file, final Resources res) {
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

	private static void parseEntities(final HtmlBuilder builder, final EntitySupport entities) {
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
