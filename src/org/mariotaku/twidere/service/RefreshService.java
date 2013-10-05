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

import static org.mariotaku.twidere.util.ParseUtils.parseInt;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getNewestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.hasActiveConnection;
import static org.mariotaku.twidere.util.Utils.isBatteryOkay;

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

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.preference.AutoRefreshContentPreference;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Notifications;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

public class RefreshService extends Service implements Constants {

	private SharedPreferences mPreferences;
	private AlarmManager mAlarmManager;
	private ContentResolver mResolver;
	private AsyncTwitterWrapper mTwitterWrapper;

	private PendingIntent mPendingRefreshHomeTimelineIntent, mPendingRefreshMentionsIntent,
			mPendingRefreshDirectMessagesIntent, mPendingRefreshTrendsIntent;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_NOTIFICATION_CLEARED.equals(action)) {
				final Bundle extras = intent.getExtras();
				if (extras != null && extras.containsKey(EXTRA_NOTIFICATION_ID)) {
					clearNotification(extras.getInt(EXTRA_NOTIFICATION_ID));
				}
			} else if (BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING.equals(action)) {
				rescheduleHomeTimelineRefreshing();
			} else if (BROADCAST_RESCHEDULE_MENTIONS_REFRESHING.equals(action)) {
				rescheduleMentionsRefreshing();
			} else if (BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING.equals(action)) {
				rescheduleDirectMessagesRefreshing();
			} else if (BROADCAST_RESCHEDULE_TRENDS_REFRESHING.equals(action)) {
				rescheduleTrendsRefreshing();
			} else if (hasActiveConnection(context)
					&& (isBatteryOkay(context) || !mPreferences.getBoolean(
							PREFERENCE_KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW, true))) {
				if (BROADCAST_REFRESH_HOME_TIMELINE.equals(action)) {
					final long[] activated_ids = getActivatedAccountIds(context);
					final long[] since_ids = getNewestStatusIdsFromDatabase(context, Statuses.CONTENT_URI);
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE,
							AutoRefreshContentPreference.DEFAULT_ENABLE_HOME_TTMELINE) && !isHomeTimelineRefreshing()) {
						getHomeTimeline(activated_ids, null, since_ids);
					}
				} else if (BROADCAST_REFRESH_MENTIONS.equals(action)) {
					final long[] activated_ids = getActivatedAccountIds(context);
					final long[] since_ids = getNewestStatusIdsFromDatabase(context, Mentions.CONTENT_URI);
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS,
							AutoRefreshContentPreference.DEFAULT_ENABLE_MENTIONS) && !isMentionsRefreshing()) {
						getMentions(activated_ids, null, since_ids);
					}
				} else if (BROADCAST_REFRESH_DIRECT_MESSAGES.equals(action)) {
					final long[] activated_ids = getActivatedAccountIds(context);
					final long[] since_ids = getNewestMessageIdsFromDatabase(context, DirectMessages.Inbox.CONTENT_URI);
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES,
							AutoRefreshContentPreference.DEFAULT_ENABLE_DIRECT_MESSAGES)
							&& !isReceivedDirectMessagesRefreshing()) {
						getReceivedDirectMessages(activated_ids, null, since_ids);
					}
				} else if (BROADCAST_REFRESH_TRENDS.equals(action)) {
					if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_TRENDS,
							AutoRefreshContentPreference.DEFAULT_ENABLE_TRENDS) && !isLocalTrendsRefreshing()) {
						getLocalTrends();
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
		mPendingRefreshTrendsIntent = PendingIntent.getBroadcast(this, 0, new Intent(BROADCAST_REFRESH_TRENDS), 0);
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
		final Uri uri = Notifications.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
		mResolver.delete(uri, null, null);
	}

	private int getHomeTimeline(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getHomeTimelineAsync(account_ids, max_ids, since_ids);
	}

	private int getLocalTrends() {
		final long account_id = getDefaultAccountId(this);
		final int woeid = mPreferences.getInt(PREFERENCE_KEY_LOCAL_TRENDS_WOEID, 1);
		return mTwitterWrapper.getLocalTrendsAsync(account_id, woeid);
	}

	private int getMentions(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getMentionsAsync(account_ids, max_ids, since_ids);
	}

	private int getReceivedDirectMessages(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		return mTwitterWrapper.getReceivedDirectMessagesAsync(account_ids, max_ids, since_ids);
	}

	private boolean isHomeTimelineRefreshing() {
		return mTwitterWrapper.isHomeTimelineRefreshing();
	}

	private boolean isLocalTrendsRefreshing() {
		return mTwitterWrapper.isLocalTrendsRefreshing();
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
			final long update_interval_mins = Math.max(
					parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")), 3);
			final long update_interval = update_interval_mins * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshDirectMessagesIntent);
			}
		}
	}

	private void rescheduleHomeTimelineRefreshing() {
		mAlarmManager.cancel(mPendingRefreshHomeTimelineIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval_mins = Math.max(
					parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")), 3);
			final long update_interval = update_interval_mins * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshHomeTimelineIntent);
			}
		}
	}

	private void rescheduleMentionsRefreshing() {
		mAlarmManager.cancel(mPendingRefreshMentionsIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval_mins = Math.max(
					parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")), 3);
			final long update_interval = update_interval_mins * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshMentionsIntent);
			}
		}
	}

	private void rescheduleTrendsRefreshing() {
		mAlarmManager.cancel(mPendingRefreshTrendsIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval_mins = Math.max(
					parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")), 3);
			final long update_interval = update_interval_mins * 60 * 1000;
			if (update_interval > 0) {
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
						update_interval, mPendingRefreshTrendsIntent);
			}
		}
	}

	private boolean startAutoRefresh() {
		stopAutoRefresh();
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval_mins = Math.max(
					parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")), 3);
			final long update_interval = update_interval_mins * 60 * 1000;
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
