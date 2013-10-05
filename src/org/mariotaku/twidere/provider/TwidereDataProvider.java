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

package org.mariotaku.twidere.provider;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.clearAccountColor;
import static org.mariotaku.twidere.util.Utils.clearAccountName;
import static org.mariotaku.twidere.util.Utils.getAccountNames;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getAccountScreenNames;
import static org.mariotaku.twidere.util.Utils.getAllStatusesCount;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameById;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.isNotificationsSilent;
import static org.mariotaku.twidere.util.Utils.notifyForUpdatedUri;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.PreviewImage;
import org.mariotaku.twidere.preference.NotificationContentPreference;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Preferences;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ImagePreloader;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.PermissionsManager;
import org.mariotaku.twidere.util.Utils;

import twitter4j.http.HostAddressResolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TwidereDataProvider extends ContentProvider implements Constants {

	private Context mContext;
	private Resources mResources;

	private SQLiteDatabase mDatabase;
	private PermissionsManager mPermissionsManager;
	private NotificationManager mNotificationManager;
	private SharedPreferences mPreferences;
	private ImagePreloader mImagePreloader;
	private HostAddressResolver mHostAddressResolver;

	private int mNewStatusesCount;
	private final List<ParcelableStatus> mNewMentions = Collections.synchronizedList(new ArrayList<ParcelableStatus>());
	private final List<String> mNewMentionScreenNames = Collections
			.synchronizedList(new NoDuplicatesArrayList<String>());
	private final List<Long> mNewMentionAccounts = Collections.synchronizedList(new NoDuplicatesArrayList<Long>());
	private final List<ParcelableDirectMessage> mNewMessages = Collections
			.synchronizedList(new ArrayList<ParcelableDirectMessage>());
	private final List<String> mNewMessageScreenNames = Collections
			.synchronizedList(new NoDuplicatesArrayList<String>());
	private final List<Long> mNewMessageAccounts = Collections.synchronizedList(new NoDuplicatesArrayList<Long>());

	private boolean mNotificationIsAudible;

	private final BroadcastReceiver mHomeActivityStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_HOME_ACTIVITY_ONSTART.equals(action)) {
				mNotificationIsAudible = false;
			} else if (BROADCAST_HOME_ACTIVITY_ONSTOP.equals(action)) {
				mNotificationIsAudible = true;
			}
		}

	};

	@Override
	public int bulkInsert(final Uri uri, final ContentValues[] values) {
		try {
			final int table_id = getTableId(uri);
			final String table = getTableNameById(table_id);
			checkWritePermission(table_id, table);
			switch (table_id) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY:
					return 0;
			}
			int result = 0;
			if (table != null && values != null) {
				final int old_count;
				switch (table_id) {
					case TABLE_ID_STATUSES: {
						old_count = getAllStatusesCount(mContext, Statuses.CONTENT_URI);
						break;
					}
					case TABLE_ID_MENTIONS: {
						old_count = getAllStatusesCount(mContext, Mentions.CONTENT_URI);
						break;
					}
					default:
						old_count = 0;
				}
				mDatabase.beginTransaction();
				final boolean replace_on_conflict = shouldReplaceOnConflict(table_id);
				for (final ContentValues contentValues : values) {
					if (replace_on_conflict) {
						mDatabase.insertWithOnConflict(table, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE);
					} else {
						mDatabase.insert(table, null, contentValues);
					}
					result++;
				}
				mDatabase.setTransactionSuccessful();
				mDatabase.endTransaction();
				if (!"false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					switch (table_id) {
						case TABLE_ID_STATUSES: {
							mNewStatusesCount += getAllStatusesCount(mContext, Statuses.CONTENT_URI) - old_count;
							break;
						}
					}
				}
			}
			if (result > 0) {
				onDatabaseUpdated(uri);
			}
			onNewItemsInserted(uri, values);
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	};

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		try {
			final int table_id = getTableId(uri);
			final String table = getTableNameById(table_id);
			checkWritePermission(table_id, table);
			if (table_id == VIRTUAL_TABLE_ID_NOTIFICATIONS) {
				final List<String> segments = uri.getPathSegments();
				if (segments.size() != 2) return 0;
				clearNotification(ParseUtils.parseInt(segments.get(1)));
				return 1;
			}
			switch (table_id) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY:
					return 0;
			}
			if (table == null) return 0;
			final int result = mDatabase.delete(table, selection, selectionArgs);
			if (result > 0) {
				onDatabaseUpdated(uri);
			}
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		try {
			final int table_id = getTableId(uri);
			final String table = getTableNameById(table_id);
			checkWritePermission(table_id, table);
			switch (table_id) {
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
				case TABLE_ID_DIRECT_MESSAGES:
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY:
					return null;
			}
			if (table == null) return null;
			final boolean replace_on_conflict = shouldReplaceOnConflict(table_id);
			final long row_id;
			if (replace_on_conflict) {
				row_id = mDatabase.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			} else {
				row_id = mDatabase.insert(table, null, values);
			}
			if (!"false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
				switch (getTableId(uri)) {
					case TABLE_ID_STATUSES: {
						mNewStatusesCount++;
						break;
					}
					default:
				}
			}
			onDatabaseUpdated(uri);
			onNewItemsInserted(uri, values);
			return Uri.withAppendedPath(uri, String.valueOf(row_id));
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean onCreate() {
		mContext = getContext();
		mResources = mContext.getResources();
		final TwidereApplication app = TwidereApplication.getInstance(mContext);
		mDatabase = app.getSQLiteDatabase();
		mHostAddressResolver = app.getHostAddressResolver();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPermissionsManager = new PermissionsManager(mContext);
		mImagePreloader = new ImagePreloader(mContext, app.getImageLoader());
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTART);
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTOP);
		mContext.registerReceiver(mHomeActivityStateReceiver, filter);
		return mDatabase != null;
	}

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode) throws FileNotFoundException {
		if (uri == null || mode == null) throw new IllegalArgumentException();
		final int table_id = getTableId(uri);
		final String table = getTableNameById(table_id);
		final int mode_code;
		if ("r".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_ONLY;
		} else if ("rw".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_WRITE;
		} else if ("rwt".equals(mode)) {
			mode_code = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_TRUNCATE;
		} else
			throw new IllegalArgumentException();
		if (mode_code == ParcelFileDescriptor.MODE_READ_ONLY) {
			checkReadPermission(table_id, table, null);
		} else if ((mode_code & ParcelFileDescriptor.MODE_READ_WRITE) != 0) {
			checkReadPermission(table_id, table, null);
			checkWritePermission(table_id, table);
		}
		switch (table_id) {
			case VIRTUAL_TABLE_ID_CACHED_IMAGES: {
				return getCachedImageFd(uri.getQueryParameter(QUERY_PARAM_URL));
			}
			case VIRTUAL_TABLE_ID_CACHE_FILES: {
				return getCacheFileFd(uri.getLastPathSegment());
			}
		}
		return null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		try {
			final int table_id = getTableId(uri);
			if (table_id == VIRTUAL_TABLE_ID_PERMISSIONS) {
				final MatrixCursor c = new MatrixCursor(TweetStore.Permissions.MATRIX_COLUMNS);
				final Map<String, Integer> map = mPermissionsManager.getAll();
				for (final Map.Entry<String, Integer> item : map.entrySet()) {
					c.addRow(new Object[] { item.getKey(), item.getValue() });
				}
				return c;
			}
			final String table = getTableNameById(table_id);
			checkReadPermission(table_id, table, projection);
			switch (table_id) {
				case VIRTUAL_TABLE_ID_ALL_PREFERENCES: {
					return getPreferencesCursor(mPreferences, null);
				}
				case VIRTUAL_TABLE_ID_PREFERENCES: {
					return getPreferencesCursor(mPreferences, uri.getLastPathSegment());
				}
				case VIRTUAL_TABLE_ID_DNS: {
					return getDNSCursor(uri.getLastPathSegment());
				}
				case VIRTUAL_TABLE_ID_CACHED_IMAGES: {
					return getCachedImageCursor(uri.getQueryParameter(QUERY_PARAM_URL));
				}
				case VIRTUAL_TABLE_ID_NOTIFICATIONS: {
					return getNotificationsCursor();
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 3) return null;
					final String query = Conversation.QueryBuilder.buildByConversationId(projection,
							Long.parseLong(segments.get(1)), Long.parseLong(segments.get(2)), selection, sortOrder);
					return mDatabase.rawQuery(query, selectionArgs);
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME: {
					final List<String> segments = uri.getPathSegments();
					if (segments.size() != 3) return null;
					final String query = Conversation.QueryBuilder.buildByScreenName(projection,
							Long.parseLong(segments.get(1)), segments.get(2), selection, sortOrder);
					return mDatabase.rawQuery(query, selectionArgs);
				}
				case TABLE_ID_DIRECT_MESSAGES: {
					final String query = DirectMessages.QueryBuilder.build(projection, selection, sortOrder);
					return mDatabase.rawQuery(query, selectionArgs);
				}
				case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY: {
					return mDatabase.rawQuery(ConversationsEntry.QueryBuilder.build(selection), null);
				}
			}
			if (table == null) return null;
			return mDatabase.query(table, projection, selection, selectionArgs, null, null, sortOrder);
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		try {
			final int table_id = getTableId(uri);
			final String table = getTableNameById(table_id);
			int result = 0;
			if (table != null) {
				switch (table_id) {
					case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
					case TABLE_ID_DIRECT_MESSAGES:
					case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY:
						return 0;
				}
				result = mDatabase.update(table, values, selection, selectionArgs);
			}
			if (result > 0) {
				onDatabaseUpdated(uri);
			}
			return result;
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private void buildNotification(final NotificationCompat.Builder builder, final String ticker, final String title,
			final String message, final int icon, final Bitmap large_icon, final Intent content_intent,
			final Intent delete_intent) {
		builder.setTicker(ticker);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(icon);
		if (large_icon != null) {
			builder.setLargeIcon(large_icon);
		}
		if (delete_intent != null) {
			builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, delete_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		if (content_intent != null) {
			builder.setContentIntent(PendingIntent.getActivity(mContext, 0, content_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		int defaults = 0;
		if (mNotificationIsAudible && !isNotificationsSilent(mContext)) {
			if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, false)) {
				final Uri def_ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				final String path = mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE, "");
				builder.setSound(isEmpty(path) ? def_ringtone : Uri.parse(path), Notification.STREAM_DEFAULT);
			}
			if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION, false)) {
				defaults |= Notification.DEFAULT_VIBRATE;
			}
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS, false)) {
			final int color_def = mResources.getColor(android.R.color.holo_blue_dark);
			final int color = mPreferences.getInt(PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR, color_def);
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
	}

	private boolean checkPermission(final int level) {
		return mPermissionsManager.checkCallingPermission(level);
	}

	private void checkReadPermission(final int id, final String table, final String[] projection) {
		switch (id) {
			case VIRTUAL_TABLE_ID_PREFERENCES:
			case VIRTUAL_TABLE_ID_DNS: {
				if (!checkPermission(PERMISSION_PREFERENCES))
					throw new SecurityException("Access preferences requires level PERMISSION_LEVEL_PREFERENCES");
				break;
			}
			case TABLE_ID_ACCOUNTS: {
				// Reading some infomation like user_id, screen_name etc is
				// okay, but reading columns like password requires higher
				// permission level.
				if (projection == null
						|| ArrayUtils.contains(projection, Accounts.BASIC_AUTH_PASSWORD, Accounts.OAUTH_TOKEN,
								Accounts.TOKEN_SECRET) && !checkPermission(PERMISSION_ACCOUNTS))
					throw new SecurityException("Access column " + ArrayUtils.toString(projection, ',', true)
							+ " in database accounts requires level PERMISSION_LEVEL_ACCOUNTS");
				if (!checkPermission(PERMISSION_READ))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_READ");
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES:
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY: {
				if (!checkPermission(PERMISSION_DIRECT_MESSAGES))
					throw new SecurityException("Access database " + table
							+ " requires level PERMISSION_LEVEL_DIRECT_MESSAGES");
				break;
			}
			case TABLE_ID_STATUSES:
			case TABLE_ID_MENTIONS:
			case TABLE_ID_TABS:
			case TABLE_ID_DRAFTS:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
			case TABLE_ID_TRENDS_LOCAL:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_HASHTAGS: {
				if (!checkPermission(PERMISSION_READ))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_READ");
				break;
			}
		}
	}

	private void checkWritePermission(final int id, final String table) {
		switch (id) {
			case TABLE_ID_ACCOUNTS: {
				// Writing to accounts database is not allowed for third-party
				// applications.
				if (!mPermissionsManager.checkSignature(Binder.getCallingUid()))
					throw new SecurityException(
							"Writing to accounts database is not allowed for third-party applications");
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES:
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY: {
				if (!checkPermission(PERMISSION_DIRECT_MESSAGES))
					throw new SecurityException("Access database " + table
							+ " requires level PERMISSION_LEVEL_DIRECT_MESSAGES");
				break;
			}
			case TABLE_ID_STATUSES:
			case TABLE_ID_MENTIONS:
			case TABLE_ID_TABS:
			case TABLE_ID_DRAFTS:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
			case TABLE_ID_TRENDS_LOCAL:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_HASHTAGS: {
				if (!checkPermission(PERMISSION_WRITE))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_WRITE");
				break;
			}
		}
	}

	private void clearNotification(final int id) {
		switch (id) {
			case NOTIFICATION_ID_HOME_TIMELINE: {
				mNewStatusesCount = 0;
				break;
			}
			case NOTIFICATION_ID_MENTIONS: {
				mNewMentionAccounts.clear();
				mNewMentions.clear();
				mNewMentionScreenNames.clear();
				break;
			}
			case NOTIFICATION_ID_DIRECT_MESSAGES: {
				mNewMessageAccounts.clear();
				mNewMessages.clear();
				mNewMessageScreenNames.clear();
				break;
			}
		}
		mNotificationManager.cancel(id);
	}

	private void displayMentionsNotification(final Context context, final ContentValues[] values) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		final boolean display_screen_name = !mPreferences.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
		final Bundle delete_extras = new Bundle();
		delete_extras.putInt(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_MENTIONS);
		delete_intent.putExtras(delete_extras);
		final Intent content_intent;
		int notified_count = 0;
		// Add statuses that not filtered to list for future use.
		for (final ContentValues value : values) {
			final ParcelableStatus status = new ParcelableStatus(value);
			if (!isFiltered(mDatabase, status)) {
				mNewMentions.add(status);
				mNewMentionScreenNames.add(status.user_screen_name);
				mNewMentionAccounts.add(status.account_id);
				notified_count++;
			}
		}
		Collections.sort(mNewMentions);
		final int mentions_size = mNewMentions.size();
		if (notified_count == 0 || mentions_size == 0 || mNewMentionScreenNames.size() == 0) return;
		final String title;
		if (mNewMentions.size() > 1) {
			builder.setNumber(mentions_size);
		}
		final int screen_names_size = mNewMentionScreenNames.size();
		final ParcelableStatus status = mNewMentions.get(0);
		content_intent = new Intent(context, HomeActivity.class);
		content_intent.setAction(Intent.ACTION_MAIN);
		content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
		final Bundle content_extras = new Bundle();
		content_extras.putString(EXTRA_TAB_TYPE, TAB_TYPE_MENTIONS_TIMELINE);
		if (mNewMentions.size() == 1) {
			final Uri.Builder uri_builder = new Uri.Builder();
			uri_builder.scheme(SCHEME_TWIDERE);
			uri_builder.authority(AUTHORITY_STATUS);
			uri_builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_id));
			uri_builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.id));
			content_extras.putParcelable(EXTRA_EXTRA_INTENT, new Intent(Intent.ACTION_VIEW, uri_builder.build()));
		}
		content_intent.putExtras(content_extras);

		if (screen_names_size > 1) {
			title = mResources.getString(R.string.notification_mention_multiple, display_screen_name ? "@"
					+ status.user_screen_name : status.user_name, screen_names_size - 1);
		} else {
			title = mResources.getString(R.string.notification_mention, display_screen_name ? "@"
					+ status.user_screen_name : status.user_name);
		}
		builder.setLargeIcon(getProfileImageForNotification(status.user_profile_image_url));
		buildNotification(builder, title, title, status.text_plain, R.drawable.ic_stat_mention, null, content_intent,
				delete_intent);
		if (mNewMentions.isEmpty()) return;
		if (mNewMentions.size() > 1) {
			final NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
			final int max = Math.min(4, mNewMentions.size());
			for (int i = 0; i < max; i++) {
				final ParcelableStatus s = safeGet(mNewMentions, i);
				if (s == null) return;
				final String name = display_screen_name ? "@" + s.user_screen_name : s.user_name;
				style.addLine(Html.fromHtml("<b>" + name + "</b>: "
						+ stripMentionText(s.text_unescaped, getAccountScreenName(context, s.account_id))));
			}
			if (max == 4 && mentions_size - max > 0) {
				style.addLine(context.getString(R.string.and_more, mentions_size - max));
			}
			final int accounts_count = mNewMentionAccounts.size();
			if (accounts_count > 0) {
				final long[] ids = ArrayUtils.fromList(mNewMentionAccounts);
				final String[] names = display_screen_name ? getAccountScreenNames(context, ids, true)
						: getAccountNames(context, ids);
				style.setSummaryText(ArrayUtils.toString(names, ',', true));
			}
			mNotificationManager.notify(NOTIFICATION_ID_MENTIONS, style.build());
		} else {
			final Intent reply_intent = new Intent(INTENT_ACTION_REPLY);
			final Bundle bundle = new Bundle();
			bundle.putInt(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_MENTIONS);
			bundle.putParcelable(EXTRA_STATUS, status);
			reply_intent.putExtras(bundle);
			reply_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			builder.addAction(R.drawable.ic_menu_reply, context.getString(R.string.reply),
					PendingIntent.getActivity(context, 0, reply_intent, PendingIntent.FLAG_UPDATE_CURRENT));
			final NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
			style.bigText(stripMentionText(status.text_unescaped, getAccountScreenName(context, status.account_id)));
			mNotificationManager.notify(NOTIFICATION_ID_MENTIONS, style.build());
		}
	}

	private void displayMessagesNotification(final Context context, final ContentValues[] values) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		final boolean display_screen_name = !mPreferences.getBoolean(PREFERENCE_KEY_NAME_FIRST, true);
		final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
		final Bundle delete_extras = new Bundle();
		delete_extras.putInt(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_DIRECT_MESSAGES);
		delete_intent.putExtras(delete_extras);
		final Intent content_intent;
		int notified_count = 0;
		for (final ContentValues value : values) {
			final String screen_name = value.getAsString(DirectMessages.SENDER_SCREEN_NAME);
			final ParcelableDirectMessage message = new ParcelableDirectMessage(value);
			mNewMessages.add(message);
			mNewMessageScreenNames.add(screen_name);
			mNewMessageAccounts.add(message.account_id);
			notified_count++;
		}
		Collections.sort(mNewMessages);
		final int messages_size = mNewMessages.size();
		if (notified_count == 0 || messages_size == 0 || mNewMessageScreenNames.size() == 0) return;
		final String title;
		if (messages_size > 1) {
			builder.setNumber(messages_size);
		}
		final int screen_names_size = mNewMessageScreenNames.size();
		final ParcelableDirectMessage message = mNewMessages.get(0);

		content_intent = new Intent(context, HomeActivity.class);
		content_intent.setAction(Intent.ACTION_MAIN);
		content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
		final Bundle content_extras = new Bundle();
		content_extras.putString(EXTRA_TAB_TYPE, TAB_TYPE_DIRECT_MESSAGES);
		if (messages_size == 1) {
			final Uri.Builder uri_builder = new Uri.Builder();
			final long account_id = message.account_id;
			final long conversation_id = message.sender_id;
			uri_builder.scheme(SCHEME_TWIDERE);
			uri_builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
			uri_builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			uri_builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, String.valueOf(conversation_id));
			content_extras.putParcelable(EXTRA_EXTRA_INTENT, new Intent(Intent.ACTION_VIEW, uri_builder.build()));
		}
		content_intent.putExtras(content_extras);

		if (screen_names_size > 1) {
			title = mResources.getString(R.string.notification_direct_message_multiple_users, display_screen_name ? "@"
					+ message.sender_screen_name : message.sender_name, screen_names_size - 1, messages_size);
		} else if (messages_size > 1) {
			title = mResources.getString(R.string.notification_direct_message_multiple_messages,
					display_screen_name ? "@" + message.sender_screen_name : message.sender_name, messages_size);
		} else {
			title = mResources.getString(R.string.notification_direct_message, display_screen_name ? "@"
					+ message.sender_screen_name : message.sender_name);
		}
		final String text_plain = message.text_plain;
		builder.setLargeIcon(getProfileImageForNotification(message.sender_profile_image_url));
		buildNotification(builder, title, title, text_plain, R.drawable.ic_stat_direct_message, null, content_intent,
				delete_intent);
		final String summary;
		final int accounts_count = mNewMessageAccounts.size();
		if (accounts_count > 0) {
			final long[] ids = ArrayUtils.fromList(mNewMessageAccounts);
			final String[] names = display_screen_name ? getAccountScreenNames(context, ids, true) : getAccountNames(
					context, ids);
			summary = ArrayUtils.toString(names, ',', true);
		} else {
			summary = null;
		}
		if (messages_size > 1) {
			final NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
			final int max = Math.min(4, mNewMessages.size());
			for (int i = 0; i < max; i++) {
				final ParcelableDirectMessage s = safeGet(mNewMessages, i);
				if (s == null) return;
				final String name = display_screen_name ? "@" + s.sender_screen_name : s.sender_name;
				style.addLine(Html.fromHtml("<b>" + name + "</b>: " + s.text_plain));
			}
			if (max == 4 && messages_size - max > 0) {
				style.addLine(context.getString(R.string.and_more, messages_size - max));
			}
			if (summary != null) {
				style.setSummaryText(summary);
			}
			mNotificationManager.notify(NOTIFICATION_ID_DIRECT_MESSAGES, style.build());
		} else {
			final NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
			style.bigText(message.text_plain);
			if (summary != null) {
				style.setSummaryText(summary);
			}
			mNotificationManager.notify(NOTIFICATION_ID_DIRECT_MESSAGES, style.build());
		}
	}

	private Cursor getCachedImageCursor(final String url) {
		final MatrixCursor c = new MatrixCursor(TweetStore.CachedImages.MATRIX_COLUMNS);
		final File file = mImagePreloader.getCachedImageFile(url);
		if (url != null && file != null) {
			c.addRow(new String[] { url, file.getPath() });
		}
		return c;
	}

	private ParcelFileDescriptor getCachedImageFd(final String url) throws FileNotFoundException {
		final File file = mImagePreloader.getCachedImageFile(url);
		if (file == null) return null;
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	}

	private ParcelFileDescriptor getCacheFileFd(final String name) throws FileNotFoundException {
		if (name == null) return null;
		final File cacheDir = mContext.getCacheDir();
		final File file = new File(cacheDir, name);
		if (!file.exists()) return null;
		return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
	}

	private Cursor getDNSCursor(final String host) {
		final MatrixCursor c = new MatrixCursor(TweetStore.DNS.MATRIX_COLUMNS);
		try {
			final String address = mHostAddressResolver.resolve(host);
			if (host != null && address != null) {
				c.addRow(new String[] { host, address });
			}
		} catch (final IOException e) {

		}
		return c;
	}

	private Cursor getNotificationsCursor() {
		final MatrixCursor c = new MatrixCursor(TweetStore.Notifications.MATRIX_COLUMNS);
		c.addRow(new Integer[] { NOTIFICATION_ID_HOME_TIMELINE, mNewStatusesCount });
		c.addRow(new Integer[] { NOTIFICATION_ID_MENTIONS, mNewMentions.size() });
		c.addRow(new Integer[] { NOTIFICATION_ID_DIRECT_MESSAGES, mNewMessages.size() });
		return c;
	}

	private Bitmap getProfileImageForNotification(final String profile_image_url) {
		final boolean hires_profile_image = mResources.getBoolean(R.bool.hires_profile_image);
		final int w = mResources.getDimensionPixelSize(android.R.dimen.notification_large_icon_width);
		final int h = mResources.getDimensionPixelSize(android.R.dimen.notification_large_icon_height);
		final File profile_image_file = mImagePreloader
				.getCachedImageFile(hires_profile_image ? getBiggerTwitterProfileImage(profile_image_url)
						: profile_image_url);
		final Bitmap profile_image = profile_image_file != null && profile_image_file.isFile() ? BitmapFactory
				.decodeFile(profile_image_file.getPath()) : null;
		if (profile_image != null) return Bitmap.createScaledBitmap(profile_image, w, h, true);
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mResources, R.drawable.ic_profile_image_default),
				w, h, true);
	}

	private void onDatabaseUpdated(final Uri uri) {
		if (uri == null) return;
		if ("false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) return;
		final Context context = getContext();
		switch (getTableId(uri)) {
			case TABLE_ID_ACCOUNTS: {
				clearAccountColor();
				clearAccountName();
				context.sendBroadcast(new Intent(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED));
				break;
			}
			case TABLE_ID_DRAFTS: {
				context.sendBroadcast(new Intent(BROADCAST_DRAFTS_DATABASE_UPDATED));
				break;
			}
			case TABLE_ID_STATUSES:
			case TABLE_ID_MENTIONS:
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX: {
				notifyForUpdatedUri(context, uri);
				break;
			}
			case TABLE_ID_TRENDS_LOCAL: {
				context.sendBroadcast(new Intent(BROADCAST_TRENDS_UPDATED));
				break;
			}
			case TABLE_ID_TABS: {
				context.sendBroadcast(new Intent(BROADCAST_TABS_UPDATED));
				break;
			}
			case TABLE_ID_FILTERED_LINKS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES: {
				context.sendBroadcast(new Intent(BROADCAST_FILTERS_UPDATED));
				break;
			}
			default:
				return;
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}

	private void onNewItemsInserted(final Uri uri, final ContentValues... values) {
		if (uri == null || values == null || values.length == 0) return;
		preloadImages(values);
		if ("false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) return;
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		switch (getTableId(uri)) {
			case TABLE_ID_STATUSES: {
				if (!mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_HOME_TIMELINE,
						NotificationContentPreference.DEFAULT_ENABLE_HOME_TTMELINE)) return;
				final String message = mResources.getQuantityString(R.plurals.Ntweets, mNewStatusesCount,
						mNewStatusesCount);
				final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
				final Bundle delete_extras = new Bundle();
				delete_extras.putInt(EXTRA_NOTIFICATION_ID, NOTIFICATION_ID_HOME_TIMELINE);
				delete_intent.putExtras(delete_extras);
				final Intent content_intent = new Intent(mContext, HomeActivity.class);
				content_intent.setAction(Intent.ACTION_MAIN);
				content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
				final Bundle content_extras = new Bundle();
				content_extras.putString(EXTRA_TAB_TYPE, TAB_TYPE_HOME_TIMELINE);
				content_intent.putExtras(content_extras);
				builder.setOnlyAlertOnce(true);
				buildNotification(builder, mResources.getString(R.string.new_notifications), message, message,
						R.drawable.ic_stat_twitter, null, content_intent, delete_intent);
				mNotificationManager.notify(NOTIFICATION_ID_HOME_TIMELINE, builder.build());
				break;
			}
			case TABLE_ID_MENTIONS: {
				if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS,
						NotificationContentPreference.DEFAULT_ENABLE_MENTIONS)) {
					displayMentionsNotification(mContext, values);
				}
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES_INBOX: {
				if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES,
						NotificationContentPreference.DEFAULT_ENABLE_DIRECT_MESSAGES)) {
					displayMessagesNotification(mContext, values);
				}
				break;
			}
		}
	}

	private void preloadImages(final ContentValues... values) {
		if (values == null) return;
		for (final ContentValues v : values) {
			if (mPreferences.getBoolean(PREFERENCE_KEY_PRELOAD_PROFILE_IMAGES, false)) {
				mImagePreloader.preloadImage(v.getAsString(Statuses.PROFILE_IMAGE_URL));
				mImagePreloader.preloadImage(v.getAsString(DirectMessages.SENDER_PROFILE_IMAGE_URL));
				mImagePreloader.preloadImage(v.getAsString(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL));
			}
			if (mPreferences.getBoolean(PREFERENCE_KEY_PRELOAD_PREVIEW_IMAGES, false)) {
				final String text_html = v.getAsString(Statuses.TEXT_HTML);
				for (final PreviewImage spec : Utils.getImagesInStatus(text_html)) {
					mImagePreloader.preloadImage(spec.image_preview_url);
				}
			}
		}
	}

	private static Cursor getPreferencesCursor(final SharedPreferences mPreferences, final String key) {
		final MatrixCursor c = new MatrixCursor(TweetStore.Preferences.MATRIX_COLUMNS);
		final Map<String, Object> map = new HashMap<String, Object>();
		final Map<String, ?> all = mPreferences.getAll();
		if (key == null) {
			map.putAll(all);
		} else {
			map.put(key, all.get(key));
		}
		for (final Map.Entry<String, ?> item : map.entrySet()) {
			final Object value = item.getValue();
			final int type = getPreferenceType(value);
			c.addRow(new Object[] { item.getKey(), ParseUtils.parseString(value), type });
		}
		return c;
	}

	private static int getPreferenceType(final Object object) {
		if (object == null)
			return Preferences.TYPE_NULL;
		else if (object instanceof Boolean)
			return Preferences.TYPE_BOOLEAN;
		else if (object instanceof Integer)
			return Preferences.TYPE_INTEGER;
		else if (object instanceof Long)
			return Preferences.TYPE_LONG;
		else if (object instanceof Float)
			return Preferences.TYPE_FLOAT;
		else if (object instanceof String) return Preferences.TYPE_STRING;
		return Preferences.TYPE_INVALID;
	}

	private static <T> T safeGet(final List<T> list, final int index) {
		return index >= 0 && index < list.size() ? list.get(index) : null;
	}

	private static boolean shouldReplaceOnConflict(final int table_id) {
		switch (table_id) {
			case TABLE_ID_CACHED_HASHTAGS:
			case TABLE_ID_CACHED_STATUSES:
			case TABLE_ID_CACHED_USERS:
			case TABLE_ID_FILTERED_USERS:
			case TABLE_ID_FILTERED_KEYWORDS:
			case TABLE_ID_FILTERED_SOURCES:
			case TABLE_ID_FILTERED_LINKS:
				return true;
		}
		return false;
	}

	private static String stripMentionText(final String text, final String my_screen_name) {
		if (text == null || my_screen_name == null) return text;
		final String temp = "@" + my_screen_name + " ";
		if (text.startsWith(temp)) return text.substring(temp.length());
		return text;
	}
}
