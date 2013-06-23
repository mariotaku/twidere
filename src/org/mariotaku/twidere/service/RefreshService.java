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

package org.mariotaku.twidere.service;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNewestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.hasActiveConnection;
import static org.mariotaku.twidere.util.Utils.isBatteryOkay;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.preference.AutoRefreshContentPreference;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ParseUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;

public class RefreshService extends Service implements Constants {

	private SharedPreferences mPreferences;
	private AlarmManager mAlarmManager;
	private ContentResolver mResolver;
	private AsyncTwitterWrapper mTwitterWrapper;

	private PendingIntent mPendingRefreshHomeTimelineIntent, mPendingRefreshMentionsIntent,
			mPendingRefreshDirectMessagesIntent;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_NOTIFICATION_CLEARED.equals(action)) {
				final Bundle extras = intent.getExtras();
				if (extras != null && extras.containsKey(INTENT_KEY_NOTIFICATION_ID)) {
					clearNotification(extras.getInt(INTENT_KEY_NOTIFICATION_ID));
				}
			} else if (BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING.equals(action)) {
				rescheduleHomeTimelineRefreshing();
			} else if (BROADCAST_RESCHEDULE_MENTIONS_REFRESHING.equals(action)) {
				rescheduleMentionsRefreshing();
			} else if (BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING.equals(action)) {
				rescheduleDirectMessagesRefreshing();
			} else if (hasActiveConnection(context)
					&& (isBatteryOkay(context) || !mPreferences.getBoolean(
							PREFERENCE_KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW, true))) {
				if (BROADCAST_REFRESH_HOME_TIMELINE.equals(action)) {
					final long[] activated_ids = getActivatedAccountIds(context);
					final long[] since_ids = getNewestStatusIdsFromDatabase(context, Statuses.CONTENT_URI);
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE,
							AutoRefreshContentPreference.DEFAULT_ENABLE_HOME_TTMELINE)) {
						if (!isHomeTimelineRefreshing()) {
							getHomeTimeline(activated_ids, null, since_ids);
						}
					}
				} else if (BROADCAST_REFRESH_MENTIONS.equals(action)) {
					final long[] activated_ids = getActivatedAccountIds(context);
					final long[] since_ids = getNewestStatusIdsFromDatabase(context, Mentions.CONTENT_URI);
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS,
							AutoRefreshContentPreference.DEFAULT_ENABLE_MENTIONS)) {
						if (!isMentionsRefreshing()) {
							getMentions(activated_ids, null, since_ids);
						}
					}
				} else if (BROADCAST_REFRESH_DIRECT_MESSAGES.equals(action)) {
					final long[] activated_ids = getActivatedAccountIds(context);
					final long[] since_ids = getNewestMessageIdsFromDatabase(context, DirectMessages.Inbox.CONTENT_URI);
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES,
							AutoRefreshContentPreference.DEFAULT_ENABLE_DIRECT_MESSAGES)) {
						if (!isReceivedDirectMessagesRefreshing()) {
							getReceivedDirectMessages(activated_ids, null, since_ids);
						}
					}
				}
			}
		}

	};

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		final TwidereApplication app = TwidereApplication.getInstance(this);
		mTwitterWrapper = app.getTwitterWrapper();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mResolver = getContentResolver();
		mPendingRefreshHomeTimelineIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				BROADCAST_REFRESH_HOME_TIMELINE), 0);
		mPendingRefreshMentionsIntent = PendingIntent.getBroadcast(this, 0, new Intent(BROADCAST_REFRESH_MENTIONS), 0);
		mPendingRefreshDirectMessagesIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				BROADCAST_REFRESH_DIRECT_MESSAGES), 0);
		final IntentFilter filter = new IntentFilter(BROADCAST_NOTIFICATION_CLEARED);
		filter.addAction(BROADCAST_REFRESH_HOME_TIMELINE);
		filter.addAction(BROADCAST_REFRESH_MENTIONS);
		filter.addAction(BROADCAST_REFRESH_DIRECT_MESSAGES);
		filter.addAction(BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING);
		filter.addAction(BROADCAST_RESCHEDULE_MENTIONS_REFRESHING);
		filter.addAction(BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING);
		registerReceiver(mStateReceiver, filter);
		startAutoRefresh();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mStateReceiver);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			// Auto refresh enabled, so I will try to start service after it was
			// stopped.
			startService(new Intent(this, getClass()));
		}
		super.onDestroy();
	}

	private void clearNotification(final int id) {
		final Uri uri = TweetStore.CONTENT_URI_NOTOFICATIONS.buildUpon().appendPath(String.valueOf(id)).build();
		mResolver.delete(uri, null, null);
	}

	private int getHomeTimeline(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getHomeTimeline(account_ids, max_ids, since_ids);
	}

	private int getMentions(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getMentions(account_ids, max_ids, since_ids);
	}

	private int getReceivedDirectMessages(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getReceivedDirectMessages(account_ids, max_ids, since_ids);
	}

	private boolean isHomeTimelineRefreshing() {
		return mTwitterWrapper.isHomeTimelineRefreshing();
	}

	private boolean isMentionsRefreshing() {
		return mTwitterWrapper.isMentionsRefreshing();
	}

	private boolean isReceivedDirectMessagesRefreshing() {
		return mTwitterWrapper.isReceivedDirectMessagesRefreshing();
	}

	private void rescheduleDirectMessagesRefreshing() {
		mAlarmManager.cancel(mPendingRefreshDirectMessagesIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval = ParseUtils.parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					"30")) * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshDirectMessagesIntent);
			}
		}
	}

	private void rescheduleHomeTimelineRefreshing() {
		mAlarmManager.cancel(mPendingRefreshHomeTimelineIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval = ParseUtils.parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					"30")) * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshHomeTimelineIntent);
			}
		}
	}

	private void rescheduleMentionsRefreshing() {
		mAlarmManager.cancel(mPendingRefreshMentionsIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval = ParseUtils.parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					"30")) * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshMentionsIntent);
			}
		}
	}

	private boolean startAutoRefresh() {
		stopAutoRefresh();
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval = ParseUtils.parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					"30")) * 60 * 1000;
			if (update_interval <= 0) return false;
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
					update_interval, mPendingRefreshHomeTimelineIntent);
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
					update_interval, mPendingRefreshMentionsIntent);
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
					update_interval, mPendingRefreshDirectMessagesIntent);
			return true;
		}
		return false;
	}

	private void stopAutoRefresh() {
		mAlarmManager.cancel(mPendingRefreshHomeTimelineIntent);
		mAlarmManager.cancel(mPendingRefreshMentionsIntent);
		mAlarmManager.cancel(mPendingRefreshDirectMessagesIntent);
	}
}
