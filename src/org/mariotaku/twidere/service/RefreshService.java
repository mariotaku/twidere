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
import static org.mariotaku.twidere.util.Utils.getAccountIds;
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
import android.os.IBinder;
import android.util.Log;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.AccountPreferences;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Notifications;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

import java.util.Arrays;

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
			if (BROADCAST_NOTIFICATION_DELETED.equals(action)) {
				if (intent.hasExtra(EXTRA_NOTIFICATION_ID)) {
					clearNotification(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
				}
			} else if (BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING.equals(action)) {
				rescheduleHomeTimelineRefreshing();
			} else if (BROADCAST_RESCHEDULE_MENTIONS_REFRESHING.equals(action)) {
				rescheduleMentionsRefreshing();
			} else if (BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING.equals(action)) {
				rescheduleDirectMessagesRefreshing();
			} else if (BROADCAST_RESCHEDULE_TRENDS_REFRESHING.equals(action)) {
				rescheduleTrendsRefreshing();
			} else if (isAutoRefreshAllowed()) {
				final long[] accountIds = getAccountIds(context);
				final AccountPreferences[] accountPrefs = AccountPreferences.getAccountPreferences(context, accountIds);
				if (BROADCAST_REFRESH_HOME_TIMELINE.equals(action)) {
					final long[] refreshIds = getRefreshableIds(accountPrefs, new HomeRefreshableFilter());
					final long[] sinceIds = getNewestStatusIdsFromDatabase(context, Statuses.CONTENT_URI, refreshIds);
					if (BuildConfig.DEBUG) {
						Log.d(LOGTAG, String.format("Auto refreshing home for %s", Arrays.toString(refreshIds)));
					}
					if (!isHomeTimelineRefreshing()) {
						getHomeTimeline(refreshIds, null, sinceIds);
					}
				} else if (BROADCAST_REFRESH_MENTIONS.equals(action)) {
					final long[] refreshIds = getRefreshableIds(accountPrefs, new MentionsRefreshableFilter());
					final long[] sinceIds = getNewestStatusIdsFromDatabase(context, Mentions.CONTENT_URI, refreshIds);
					if (BuildConfig.DEBUG) {
						Log.d(LOGTAG, String.format("Auto refreshing mentions for %s", Arrays.toString(refreshIds)));
					}
					if (!isMentionsRefreshing()) {
						getMentions(refreshIds, null, sinceIds);
					}
				} else if (BROADCAST_REFRESH_DIRECT_MESSAGES.equals(action)) {
					final long[] refreshIds = getRefreshableIds(accountPrefs, new MessagesRefreshableFilter());
					final long[] sinceIds = getNewestMessageIdsFromDatabase(context, DirectMessages.Inbox.CONTENT_URI,
							refreshIds);
					if (BuildConfig.DEBUG) {
						Log.d(LOGTAG, String.format("Auto refreshing messages for %s", Arrays.toString(refreshIds)));
					}
					if (!isReceivedDirectMessagesRefreshing()) {
						getReceivedDirectMessages(refreshIds, null, sinceIds);
					}
				} else if (BROADCAST_REFRESH_TRENDS.equals(action)) {
					final long[] refreshIds = getRefreshableIds(accountPrefs, new TrendsRefreshableFilter());
					if (BuildConfig.DEBUG) {
						Log.d(LOGTAG, String.format("Auto refreshing messages for %s", Arrays.toString(refreshIds)));
					}
					if (!isLocalTrendsRefreshing()) {
						getLocalTrends(refreshIds);
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
		final IntentFilter filter = new IntentFilter(BROADCAST_NOTIFICATION_DELETED);
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

	protected boolean isAutoRefreshAllowed() {
		return hasActiveConnection(this)
				&& (isBatteryOkay(this) || !mPreferences.getBoolean(PREFERENCE_KEY_STOP_AUTO_REFRESH_WHEN_BATTERY_LOW,
						true));
	}

	private void clearNotification(final int id) {
		final Uri uri = Notifications.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
		mResolver.delete(uri, null, null);
	}

	private int getHomeTimeline(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
		return mTwitterWrapper.getHomeTimelineAsync(accountIds, maxIds, sinceIds);
	}

	private int getLocalTrends(final long[] accountIds) {
		final long account_id = getDefaultAccountId(this);
		final int woeid = mPreferences.getInt(PREFERENCE_KEY_LOCAL_TRENDS_WOEID, 1);
		return mTwitterWrapper.getLocalTrendsAsync(account_id, woeid);
	}

	private int getMentions(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
		return mTwitterWrapper.getMentionsAsync(accountIds, maxIds, sinceIds);
	}

	private int getReceivedDirectMessages(final long[] accountIds, final long[] maxIds, final long[] sinceIds) {
		return mTwitterWrapper.getReceivedDirectMessagesAsync(accountIds, maxIds, sinceIds);
	}

	private long[] getRefreshableIds(final AccountPreferences[] prefs, final RefreshableAccountFilter filter) {
		if (prefs == null) return null;
		final long[] temp = new long[prefs.length];
		int i = 0;
		for (final AccountPreferences pref : prefs) {
			if (pref.isAutoRefreshEnabled() && filter.isRefreshable(pref)) {
				temp[i++] = pref.getAccountId();
			}
		}
		final long[] result = new long[i];
		System.arraycopy(temp, 0, result, 0, i);
		return result;
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
			final long update_interval_mins = Math.max(parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					PREFERENCE_DEFAULT_REFRESH_INTERVAL)), 3);
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
			final long update_interval_mins = Math.max(parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					PREFERENCE_DEFAULT_REFRESH_INTERVAL)), 3);
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
			final long update_interval_mins = Math.max(parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					PREFERENCE_DEFAULT_REFRESH_INTERVAL)), 3);
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
			final long update_interval_mins = Math.max(parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					PREFERENCE_DEFAULT_REFRESH_INTERVAL)), 3);
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
			final long update_interval_mins = Math.max(parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL,
					PREFERENCE_DEFAULT_REFRESH_INTERVAL)), 3);
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

	private static class HomeRefreshableFilter implements RefreshableAccountFilter {
		@Override
		public boolean isRefreshable(final AccountPreferences pref) {
			return pref.isAutoRefreshHomeTimelineEnabled();
		}
	}

	private static class MentionsRefreshableFilter implements RefreshableAccountFilter {

		@Override
		public boolean isRefreshable(final AccountPreferences pref) {
			return pref.isAutoRefreshHomeTimelineEnabled();
		}

	}

	private static class MessagesRefreshableFilter implements RefreshableAccountFilter {
		@Override
		public boolean isRefreshable(final AccountPreferences pref) {
			return pref.isAutoRefreshDirectMessagesEnabled();
		}
	}

	private static interface RefreshableAccountFilter {
		boolean isRefreshable(AccountPreferences pref);
	}

	private static class TrendsRefreshableFilter implements RefreshableAccountFilter {
		@Override
		public boolean isRefreshable(final AccountPreferences pref) {
			return pref.isAutoRefreshTrendsEnabled();
		}
	}
}
