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

import static org.mariotaku.twidere.util.Utils.*;

import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;
import org.mariotaku.twidere.util.DatabaseHelper;

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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

public final class TweetStoreProvider extends ContentProvider implements Constants {

	private SQLiteDatabase mDatabase;
	
	private int mNewMessagesCount, mNewMentionsCount, mNewStatusesCount;

	private boolean mNotificationIsAudible;

	private final BroadcastReceiver mHomeActivityStateReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_HOME_ACTIVITY_ONSTART.equals(action)) {
				mNotificationIsAudible = true;
			} else if (BROADCAST_HOME_ACTIVITY_ONSTOP.equals(action)) {
				mNotificationIsAudible = false;
			}
		}
		
		
	};

	private NotificationManager mNotificationManager;

	private SharedPreferences mPreferences;
	
	@Override
	public int bulkInsert(final Uri uri, final ContentValues[] values) {
		final String table = getTableNameForContentUri(uri);
		final int table_id = getTableId(uri);
		int result = 0;
		if (table != null && values != null) {
			final Context context = getContext();
			final int old_count;
			switch (table_id) {
				case URI_STATUSES: {
					old_count = getAllStatusesCount(context, Statuses.CONTENT_URI);
					break;
				}
				case URI_MENTIONS: {
					old_count = getAllStatusesCount(context, Mentions.CONTENT_URI);
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
					case URI_STATUSES: {
						mNewStatusesCount += getAllStatusesCount(context, Statuses.CONTENT_URI) - old_count;
						break;
					}
					case URI_MENTIONS: {
						mNewMentionsCount += getAllStatusesCount(context, Mentions.CONTENT_URI) - old_count;
						break;
					}
					case URI_DIRECT_MESSAGES_INBOX: {
						mNewMessagesCount += result;
						break;
					}
					default:
				}
			}
		}
		if (result > 0) {
			onDatabaseUpdated(uri);
		}
		onNewItemsInserted(uri, result, values);
		return result;
	};

	@Override
	public int delete(final Uri uri, final String selection, final String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		if (getTableId(uri) == URI_NOTIFICATIONS) {			
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 2) return 0;
			clearNotification(parseInt(segments.get(1)));
		}
		if (table == null) return 0;
		final int result = mDatabase.delete(table, selection, selectionArgs);		
		if (result > 0) {
			onDatabaseUpdated(uri);
		}
		return result;
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		final String table = getTableNameForContentUri(uri);
		if (table == null) return null;
		if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table))
			return null;
		else if (TABLE_DIRECT_MESSAGES.equals(table))
			return null;
		else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table)) 
			return null;
		final long row_id = mDatabase.insert(table, null, values);
		if (!"false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) {
			switch (getTableId(uri)) {
				case URI_STATUSES: {
					mNewStatusesCount ++;
					break;
				}
				case URI_MENTIONS: {
					mNewMentionsCount ++;
					break;
				}
				case URI_DIRECT_MESSAGES_INBOX: {
					mNewMessagesCount ++;
					break;
				}
				default:
			}
		}
		onDatabaseUpdated(uri);
		onNewItemsInserted(uri, 1, values);
		return Uri.withAppendedPath(uri, String.valueOf(row_id));
	}

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		mDatabase = new DatabaseHelper(context, DATABASES_NAME, DATABASES_VERSION).getWritableDatabase();
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTART);
		filter.addAction(BROADCAST_HOME_ACTIVITY_ONSTOP);
		context.registerReceiver(mHomeActivityStateReceiver, filter);
		return mDatabase != null;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs,
			final String sortOrder) {
		final String table = getTableNameForContentUri(uri);
		if (table == null) return null;
		if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table)) {
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 3) return null;
			final String query = Conversation.QueryBuilder.buildByConversationId(projection,
					Long.parseLong(segments.get(1)), Long.parseLong(segments.get(2)), selection, sortOrder);
			return mDatabase.rawQuery(query, selectionArgs);
		} else if (TABLE_DIRECT_MESSAGES_CONVERSATION_SCREEN_NAME.equals(table)) {
			final List<String> segments = uri.getPathSegments();
			if (segments.size() != 3) return null;
			final String query = Conversation.QueryBuilder.buildByScreenName(projection,
					Long.parseLong(segments.get(1)), segments.get(2), selection, sortOrder);
			return mDatabase.rawQuery(query, selectionArgs);
		} else if (TABLE_DIRECT_MESSAGES.equals(table)) {
			final String query = DirectMessages.QueryBuilder.build(projection, selection, sortOrder);
			return mDatabase.rawQuery(query, selectionArgs);
		} else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table))
			return mDatabase.rawQuery(ConversationsEntry.QueryBuilder.build(selection), null);
		return mDatabase.query(table, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs) {
		final String table = getTableNameForContentUri(uri);
		int result = 0;
		if (table != null) {
			if (TABLE_DIRECT_MESSAGES_CONVERSATION.equals(table))
				return 0;
			else if (TABLE_DIRECT_MESSAGES.equals(table))
				return 0;
			else if (TABLE_DIRECT_MESSAGES_CONVERSATIONS_ENTRY.equals(table))
				return 0;
			result = mDatabase.update(table, values, selection, selectionArgs);
		}
		if (result > 0) {
			onDatabaseUpdated(uri);
		}
		return result;
	}

	private void onNewItemsInserted(final Uri uri, final int count, final ContentValues... values) {
		if (uri == null || values == null || values.length == 0 || count == 0) return;
		if ("false".equals(uri.getQueryParameter(QUERY_PARAM_NOTIFY))) return;
		final Context context = getContext();
		final Resources res = context.getResources();
		final int length = values.length;
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		final boolean display_screen_name = NAME_DISPLAY_OPTION_SCREEN_NAME.equals(mPreferences.getString(PREFERENCE_KEY_NAME_DISPLAY_OPTION,
				NAME_DISPLAY_OPTION_BOTH));
		switch (getTableId(uri)) {
			case URI_STATUSES: {
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
				final Notification notification = buildNotification(builder, res.getString(R.string.new_notifications),
						message, message, R.drawable.ic_stat_tweet, null, content_intent, delete_intent);
				mNotificationManager.notify(NOTIFICATION_ID_HOME_TIMELINE, notification);
				break;
			}
			case URI_MENTIONS: {
				if (!mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS, false)) return;
				if (mNewMentionsCount > 1) {
					builder.setNumber(mNewMentionsCount);
				}
				final Notification notification;
				final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
				final Bundle delete_extras = new Bundle();
				delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_MENTIONS);
				delete_intent.putExtras(delete_extras);
				if (length > 1) {
					final Intent content_intent = new Intent(context, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MENTIONS);
					content_intent.putExtras(content_extras);
					final String title = res.getString(R.string.mentions);
					final String message = res.getQuantityString(R.plurals.Nmentions, mNewMentionsCount,
							mNewMentionsCount);
					notification = buildNotification(builder, message, title, message, R.drawable.ic_stat_mention, null, 
							content_intent, delete_intent);
				} else {
					final Intent content_intent = new Intent(context, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MENTIONS);
					content_intent.putExtras(content_extras);
					final String title = res.getString(R.string.notification_mention, display_screen_name ? 
							values[0].getAsString(Statuses.SCREEN_NAME) : values[0].getAsString(Statuses.NAME));
					final String message = values[0].getAsString(Statuses.TEXT_PLAIN);
					notification = buildNotification(builder, title, title, message, R.drawable.ic_stat_mention, null, 
							content_intent, delete_intent);
				}
				mNotificationManager.notify(NOTIFICATION_ID_MENTIONS, notification);
				break;
			}
			case URI_DIRECT_MESSAGES_INBOX: {
				if (!mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES, false)) return;
				if (mNewMessagesCount > 1) {
					builder.setNumber(mNewMessagesCount);
				}
				final String title = res.getString(R.string.notification_direct_message, display_screen_name ? 
						   values[0].getAsString(DirectMessages.SENDER_SCREEN_NAME) : values[0].getAsString(DirectMessages.SENDER_NAME));
				final String message = values[0].getAsString(DirectMessages.TEXT_PLAIN);
				final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
				final Bundle delete_extras = new Bundle();
				delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_DIRECT_MESSAGES);
				delete_intent.putExtras(delete_extras);
				final Intent content_intent = new Intent(context, HomeActivity.class);
				content_intent.setAction(Intent.ACTION_MAIN);
				content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
				final Bundle content_extras = new Bundle();
				content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MESSAGES);
				content_intent.putExtras(content_extras);
				final Notification notification = buildNotification(builder, title, title, message, R.drawable.ic_stat_direct_message,
						null, content_intent, delete_intent);
				mNotificationManager.notify(NOTIFICATION_ID_DIRECT_MESSAGES, notification);
				break;
			}
		}
	}
	
	private void onDatabaseUpdated(final Uri uri) {
		if (uri == null) return;
		final Context context = getContext();
		switch (getTableId(uri)) {
			case URI_ACCOUNTS: {
				clearAccountColor();
				clearAccountName();
				context.sendBroadcast(new Intent(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED));
				break;
			}
			case URI_DRAFTS: {
				context.sendBroadcast(new Intent(BROADCAST_DRAFTS_DATABASE_UPDATED));
				break;
			}
			case URI_STATUSES: {
				context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED));
				break;
			}
			case URI_MENTIONS: {
				context.sendBroadcast(new Intent(BROADCAST_MENTIONS_DATABASE_UPDATED));
				break;
			}
			case URI_DIRECT_MESSAGES_INBOX: {
				context.sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_DATABASE_UPDATED));
				break;
			}
			case URI_DIRECT_MESSAGES_OUTBOX: {
				context.sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_DATABASE_UPDATED));
				break;
			}
			case URI_TRENDS_LOCAL: {
				context.sendBroadcast(new Intent(BROADCAST_TRENDS_UPDATED));
				break;
			}
			case URI_TABS: {
				context.sendBroadcast(new Intent(BROADCAST_TABS_UPDATED));		
				break;
			}
			case URI_FILTERED_USERS:
			case URI_FILTERED_KEYWORDS:
			case URI_FILTERED_SOURCES: {
				context.sendBroadcast(new Intent(BROADCAST_FILTERS_UPDATED));
				break;
			}
			default:
				return;
		}
		context.sendBroadcast(new Intent(BROADCAST_DATABASE_UPDATED));
	}


	private Notification buildNotification(final NotificationCompat.Builder builder, final String ticker, final String title, final String message, final int icon,
				final Bitmap large_icon, final Intent content_intent, final Intent delete_intent) {
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
		if (mNotificationIsAudible && mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, false)) {
			builder.setSound(Uri.parse(mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE,
					  Settings.System.DEFAULT_RINGTONE_URI.getPath())), Notification.STREAM_DEFAULT);
		}
		if (mNotificationIsAudible && mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION, false)) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS, false)) {
			final int color_def = context.getResources().getColor(R.color.holo_blue_dark);
			final int color = mPreferences.getInt(PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR, color_def);
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
		return builder.getNotification();
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
}
