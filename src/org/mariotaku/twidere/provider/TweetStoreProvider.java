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

import static org.mariotaku.twidere.util.Utils.clearAccountColor;
import static org.mariotaku.twidere.util.Utils.clearAccountName;
import static org.mariotaku.twidere.util.Utils.getAllStatusesCount;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameById;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.notifyForUpdatedUri;
import static org.mariotaku.twidere.util.Utils.parseInt;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.DatabaseHelper;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;
import org.mariotaku.twidere.util.PermissionManager;

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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

public final class TweetStoreProvider extends ContentProvider implements Constants {

	private SQLiteDatabase mDatabase;
	private PermissionManager mPermissionManager;
	private NotificationManager mNotificationManager;
	private SharedPreferences mPreferences;
	private LazyImageLoader mProfileImageLoader;

	private int mNewMessagesCount, mNewMentionsCount, mNewStatusesCount;

	private boolean mNotificationIsAudible;

	private Context mContext;

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
			final int notification_count;
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
				for (final ContentValues contentValues : values) {
					mDatabase.insert(table, null, contentValues);
					result++;
				}
				mDatabase.setTransactionSuccessful();
				mDatabase.endTransaction();
				if (!"false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
					switch (table_id) {
						case TABLE_ID_STATUSES: {
							mNewStatusesCount += notification_count = getAllStatusesCount(mContext,
									Statuses.CONTENT_URI) - old_count;
							break;
						}
						case TABLE_ID_MENTIONS: {
							mNewMentionsCount += notification_count = getAllStatusesCount(mContext,
									Mentions.CONTENT_URI) - old_count;
							break;
						}
						case TABLE_ID_DIRECT_MESSAGES_INBOX: {
							mNewMessagesCount += notification_count = result;
							break;
						}
						default:
							notification_count = 0;
					}
				} else {
					notification_count = 0;
				}
			} else {
				notification_count = 0;
			}
			if (result > 0) {
				onDatabaseUpdated(uri);
			}
			onNewItemsInserted(uri, notification_count, values);
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
				clearNotification(parseInt(segments.get(1)));
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
			final long row_id = mDatabase.insert(table, null, values);
			if (!"false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
				switch (getTableId(uri)) {
					case TABLE_ID_STATUSES: {
						mNewStatusesCount++;
						break;
					}
					case TABLE_ID_MENTIONS: {
						mNewMentionsCount++;
						break;
					}
					case TABLE_ID_DIRECT_MESSAGES_INBOX: {
						mNewMessagesCount++;
						break;
					}
					default:
				}
			}
			onDatabaseUpdated(uri);
			onNewItemsInserted(uri, 1, values);
			return Uri.withAppendedPath(uri, String.valueOf(row_id));
		} catch (final SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public boolean onCreate() {
		mContext = getContext();
		final TwidereApplication app = TwidereApplication.getInstance(mContext);
		mDatabase = new DatabaseHelper(mContext, DATABASES_NAME, DATABASES_VERSION).getWritableDatabase();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mProfileImageLoader = app.getProfileImageLoader();
		mPermissionManager = new PermissionManager(mContext);
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTART);
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTOP);
		mContext.registerReceiver(mHomeActivityStateReceiver, filter);
		return mDatabase != null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		try {
			final int table_id = getTableId(uri);
			final String table = getTableNameById(table_id);
			checkReadPermission(table_id, table, projection);
			switch (table_id) {
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

	private Notification buildNotification(final NotificationCompat.Builder builder, final String ticker,
			final String title, final String message, final int icon, final Bitmap large_icon,
			final Intent content_intent, final Intent delete_intent) {
		final Context context = getContext();
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
			builder.setDeleteIntent(PendingIntent.getBroadcast(context, 0, delete_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		if (content_intent != null) {
			builder.setContentIntent(PendingIntent.getActivity(context, 0, content_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		int defaults = 0;
		final Calendar now = Calendar.getInstance();
		if (mNotificationIsAudible
				&& !mPreferences.getBoolean("slient_notifications_at_" + now.get(Calendar.HOUR_OF_DAY), false)) {
			if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, false)) {
				builder.setSound(Uri.parse(mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE,
						Settings.System.DEFAULT_RINGTONE_URI.getPath())), Notification.STREAM_DEFAULT);
			}
			if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION, false)) {
				defaults |= Notification.DEFAULT_VIBRATE;
			}
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS, false)) {
			final int color_def = context.getResources().getColor(R.color.holo_blue_dark);
			final int color = mPreferences.getInt(PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR, color_def);
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
		return builder.build();
	}

	private boolean checkPermission(final int level) {
		return mPermissionManager.checkCallingPermission(level);
	}

	private void checkReadPermission(final int id, final String table, final String[] projection) {
		switch (id) {
			case TABLE_ID_ACCOUNTS: {
				// Reading some infomation like user_id, screen_name etc is
				// okay, but reading columns like password requires higher
				// permission level.
				if (ArrayUtils.contains(projection, Accounts.BASIC_AUTH_PASSWORD, Accounts.OAUTH_TOKEN,
						Accounts.TOKEN_SECRET) && !checkPermission(PERMISSION_LEVEL_ACCOUNTS))
					throw new SecurityException("Access column " + ArrayUtils.toString(projection, ',', true)
							+ " in database accounts requires level PERMISSION_LEVEL_ACCOUNTS");
				if (!checkPermission(PERMISSION_LEVEL_READ))
					throw new SecurityException("Access database " + table + " requires level PERMISSION_LEVEL_READ");
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES:
			case TABLE_ID_DIRECT_MESSAGES_INBOX:
			case TABLE_ID_DIRECT_MESSAGES_OUTBOX:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME:
			case TABLE_ID_DIRECT_MESSAGES_CONVERSATIONS_ENTRY: {
				if (!checkPermission(PERMISSION_LEVEL_DIRECT_MESSAGES))
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
				if (!checkPermission(PERMISSION_LEVEL_READ))
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
				if (!mPermissionManager.isSameSignature(Binder.getCallingUid()))
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
				if (!checkPermission(PERMISSION_LEVEL_DIRECT_MESSAGES))
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
				if (!checkPermission(PERMISSION_LEVEL_WRITE))
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
				mNewMentionsCount = 0;
				break;
			}
			case NOTIFICATION_ID_DIRECT_MESSAGES: {
				mNewMessagesCount = 0;
				break;
			}
		}
		mNotificationManager.cancel(id);
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

	private void onNewItemsInserted(final Uri uri, final int count, final ContentValues... values) {
		if (uri == null || values == null || values.length == 0 || count == 0) return;
		if ("false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) return;
		final Context context = getContext();
		final Resources res = context.getResources();
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		final boolean display_screen_name = NAME_DISPLAY_OPTION_SCREEN_NAME.equals(mPreferences.getString(
				PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH));
		final boolean display_hires_profile_image = res.getBoolean(R.bool.hires_profile_image);
		switch (getTableId(uri)) {
			case TABLE_ID_STATUSES: {
				if (!mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_HOME_TIMELINE, false)) return;
				final String message = res.getQuantityString(R.plurals.Ntweets, mNewStatusesCount, mNewStatusesCount);
				final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
				final Bundle delete_extras = new Bundle();
				delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_HOME_TIMELINE);
				delete_intent.putExtras(delete_extras);
				final Intent content_intent = new Intent(context, HomeActivity.class);
				content_intent.setAction(Intent.ACTION_MAIN);
				content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
				final Bundle content_extras = new Bundle();
				content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_HOME);
				content_intent.putExtras(content_extras);
				builder.setOnlyAlertOnce(true);
				final Notification notification = buildNotification(builder, res.getString(R.string.new_notifications),
						message, message, R.drawable.ic_stat_tweet, null, content_intent, delete_intent);
				mNotificationManager.notify(NOTIFICATION_ID_HOME_TIMELINE, notification);
				break;
			}
			case TABLE_ID_MENTIONS: {
				if (!mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS, false)) return;
				if (mNewMentionsCount > 1) {
					builder.setNumber(mNewMentionsCount);
				}
				// builder.setStyle(new NotificationCompat.InboxStyle());
				final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
				final Bundle delete_extras = new Bundle();
				delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_MENTIONS);
				delete_intent.putExtras(delete_extras);
				final Intent content_intent;
				final List<String> screen_names = new NoDuplicatesArrayList<String>();
				ContentValues notification_value = null;
				int notified_count = 0;
				for (final ContentValues value : values) {
					final String screen_name = value.getAsString(Statuses.SCREEN_NAME);
					if (!isFiltered(mDatabase, screen_name, value.getAsString(Statuses.SOURCE),
							value.getAsString(Statuses.TEXT_PLAIN))) {
						if (notification_value == null) {
							notification_value = value;
						}
						screen_names.add(screen_name);
						notified_count++;
					}
				}
				if (notified_count == 1) {
					final Uri.Builder uri_builder = new Uri.Builder();
					uri_builder.scheme(SCHEME_TWIDERE);
					uri_builder.authority(AUTHORITY_STATUS);
					uri_builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID,
							notification_value.getAsString(Statuses.ACCOUNT_ID));
					uri_builder.appendQueryParameter(QUERY_PARAM_STATUS_ID,
							notification_value.getAsString(Statuses.STATUS_ID));
					content_intent = new Intent(Intent.ACTION_VIEW, uri_builder.build());

				} else {
					content_intent = new Intent(context, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MENTIONS);
					content_intent.putExtras(content_extras);
				}
				if (notification_value == null) return;
				final String title;
				if (screen_names.size() > 1) {
					title = res.getString(R.string.notification_mention_multiple,
							display_screen_name ? notification_value.getAsString(Statuses.SCREEN_NAME)
									: notification_value.getAsString(Statuses.NAME), screen_names.size() - 1);
				} else {
					title = res.getString(R.string.notification_mention,
							display_screen_name ? notification_value.getAsString(Statuses.SCREEN_NAME)
									: notification_value.getAsString(Statuses.NAME));
				}
				final String message = notification_value.getAsString(Statuses.TEXT_PLAIN);
				final String profile_image_url_string = notification_value.getAsString(Statuses.PROFILE_IMAGE_URL);
				final File profile_image_file = mProfileImageLoader
						.getCachedImageFile(display_hires_profile_image ? getBiggerTwitterProfileImage(profile_image_url_string)
								: profile_image_url_string);
				final int w = res.getDimensionPixelSize(R.dimen.notification_large_icon_width);
				final int h = res.getDimensionPixelSize(R.dimen.notification_large_icon_height);
				final Bitmap profile_image = profile_image_file != null && profile_image_file.isFile() ? BitmapFactory
						.decodeFile(profile_image_file.getPath()) : null;
				final Bitmap profile_image_fallback = BitmapFactory.decodeResource(res,
						R.drawable.ic_profile_image_default);
				builder.setLargeIcon(Bitmap.createScaledBitmap(profile_image != null ? profile_image
						: profile_image_fallback, w, h, true));
				final Notification notification = buildNotification(builder, title, title, message,
						R.drawable.ic_stat_mention, null, content_intent, delete_intent);
				mNotificationManager.notify(NOTIFICATION_ID_MENTIONS, notification);
				break;
			}
			case TABLE_ID_DIRECT_MESSAGES_INBOX: {
				if (!mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES, false)) return;
				if (mNewMessagesCount > 1) {
					builder.setNumber(mNewMessagesCount);
				}
				final List<String> screen_names = new NoDuplicatesArrayList<String>();
				final ContentValues notification_value = values[0];
				for (final ContentValues value : values) {
					screen_names.add(value.getAsString(DirectMessages.SENDER_SCREEN_NAME));
				}
				if (notification_value == null) return;
				final String title;
				if (screen_names.size() > 1) {
					title = res.getString(R.string.notification_direct_message_multiple,
							display_screen_name ? notification_value.getAsString(DirectMessages.SENDER_SCREEN_NAME)
									: notification_value.getAsString(DirectMessages.SENDER_NAME),
							screen_names.size() - 1);
				} else {
					title = res.getString(R.string.notification_direct_message,
							display_screen_name ? notification_value.getAsString(DirectMessages.SENDER_SCREEN_NAME)
									: notification_value.getAsString(DirectMessages.SENDER_NAME));
				}
				final String message = notification_value.getAsString(DirectMessages.TEXT_PLAIN);
				final String profile_image_url_string = notification_value
						.getAsString(DirectMessages.SENDER_PROFILE_IMAGE_URL);
				final File profile_image_file = mProfileImageLoader
						.getCachedImageFile(display_hires_profile_image ? getBiggerTwitterProfileImage(profile_image_url_string)
								: profile_image_url_string);
				final int w = res.getDimensionPixelSize(R.dimen.notification_large_icon_width);
				final int h = res.getDimensionPixelSize(R.dimen.notification_large_icon_height);
				final Bitmap profile_image = profile_image_file != null && profile_image_file.isFile() ? BitmapFactory
						.decodeFile(profile_image_file.getPath()) : null;
				final Bitmap profile_image_fallback = BitmapFactory.decodeResource(res,
						R.drawable.ic_profile_image_default);
				builder.setLargeIcon(Bitmap.createScaledBitmap(profile_image != null ? profile_image
						: profile_image_fallback, w, h, true));
				final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
				final Bundle delete_extras = new Bundle();
				delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_DIRECT_MESSAGES);
				delete_intent.putExtras(delete_extras);
				final Intent content_intent;
				if (values.length == 1) {
					final Uri.Builder uri_builder = new Uri.Builder();
					final long account_id = notification_value.getAsLong(DirectMessages.ACCOUNT_ID);
					final long conversation_id = notification_value.getAsLong(DirectMessages.SENDER_ID);
					uri_builder.scheme(SCHEME_TWIDERE);
					uri_builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
					uri_builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
					uri_builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, String.valueOf(conversation_id));
					content_intent = new Intent(Intent.ACTION_VIEW, uri_builder.build());
				} else {
					content_intent = new Intent(context, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MESSAGES);
					content_intent.putExtras(content_extras);
				}
				final Notification notification = buildNotification(builder, title, title, message,
						R.drawable.ic_stat_direct_message, null, content_intent, delete_intent);
				mNotificationManager.notify(NOTIFICATION_ID_DIRECT_MESSAGES, notification);
				break;
			}
		}
	}

	void handleCommandCall() {
		try {
			// something blah blah blah
		} catch (final RuntimeException e) {
			throw new IllegalArgumentException("This method cannot be called from non-UI thread");
		}
	}
}
