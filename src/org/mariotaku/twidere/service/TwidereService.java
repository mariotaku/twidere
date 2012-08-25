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

import static org.mariotaku.twidere.util.Utils.buildQueryUri;
import static org.mariotaku.twidere.util.Utils.getAccountUsername;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getRetweetId;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.makeDirectMessageContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusContentValues;
import static org.mariotaku.twidere.util.Utils.makeTrendsContentValues;
import static org.mariotaku.twidere.util.Utils.notifyForUpdatedUri;
import static org.mariotaku.twidere.util.Utils.parseInt;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.ITwidereService;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.ImageUploaderInterface;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.ManagedAsyncTask;
import org.mariotaku.twidere.util.TweetShortenerInterface;
import org.mariotaku.twidere.util.Utils;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.StatusUpdate;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.twitter.Validator;

public class TwidereService extends Service implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);

	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private NotificationManager mNotificationManager;
	private AlarmManager mAlarmManager;
	private ContentResolver mResolver;

	private int mGetHomeTimelineTaskId, mGetMentionsTaskId;
	private int mStoreStatusesTaskId, mStoreMentionsTaskId;
	private int mGetReceivedDirectMessagesTaskId, mGetSentDirectMessagesTaskId;
	private int mStoreReceivedDirectMessagesTaskId, mStoreSentDirectMessagesTaskId;
	private int mGetLocalTrendsTaskId, mGetWeeklyTrendsTaskId, mGetDailyTrendsTaskId;
	private int mStoreLocalTrendsTaskId, mStoreWeeklyTrendsTaskId, mStoreDailyTrendsTaskId;

	private boolean mShouldShutdown = false;

	private int mNewMessagesCount, mNewMentionsCount, mNewStatusesCount;

	private PendingIntent mPendingRefreshIntent;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (!mAsyncTaskManager.hasActivatedTask() && mShouldShutdown) {
					stopSelf();
				}
			} else if (BROADCAST_NOTIFICATION_CLEARED.equals(action)) {
				final Bundle extras = intent.getExtras();
				if (extras != null && extras.containsKey(INTENT_KEY_NOTIFICATION_ID)) {
					clearNotification(extras.getInt(INTENT_KEY_NOTIFICATION_ID));
				}
			} else if (BROADCAST_AUTO_REFRESH.equals(action)) {
				final long[] activated_ids = getActivatedAccountIds(context);
				if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_HOME_TIMELINE, false)) {
					if (!isHomeTimelineRefreshing()) {
						getHomeTimeline(activated_ids, null, true);
					}
				}
				if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_MENTIONS, false)) {
					if (!isMentionsRefreshing()) {
						getMentions(activated_ids, null, true);
					}
				}
				if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ENABLE_DIRECT_MESSAGES, false)) {
					if (!isReceivedDirectMessagesRefreshing()) {
						getReceivedDirectMessages(activated_ids, null, true);
					}
				}
			}
		}

	};

	public int addUserListMember(long account_id, int list_id, long user_id, String screen_name) {
		final AddUserListMemberTask task = new AddUserListMemberTask(account_id, list_id, user_id, screen_name);
		return mAsyncTaskManager.add(task, true);
	}

	public int cancelRetweet(long account_id, long status_id) {
		final CancelRetweetTask task = new CancelRetweetTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public void clearNotification(int id) {
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

	public int createBlock(long account_id, long user_id) {
		final CreateBlockTask task = new CreateBlockTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createFavorite(long account_id, long status_id) {
		final CreateFavoriteTask task = new CreateFavoriteTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createFriendship(long account_id, long user_id) {
		final CreateFriendshipTask task = new CreateFriendshipTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createUserList(long account_id, String list_name, boolean is_public, String description) {
		final CreateUserListTask task = new CreateUserListTask(account_id, list_name, is_public, description);
		return mAsyncTaskManager.add(task, true);
	}

	public int createUserListSubscription(long account_id, int list_id) {
		final CreateUserListSubscriptionTask task = new CreateUserListSubscriptionTask(account_id, list_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int deleteUserListMember(long account_id, int list_id, long user_id) {
		final DeleteUserListMemberTask task = new DeleteUserListMemberTask(account_id, list_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyBlock(long account_id, long user_id) {
		final DestroyBlockTask task = new DestroyBlockTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyDirectMessage(long account_id, long message_id) {
		final DestroyDirectMessageTask task = new DestroyDirectMessageTask(account_id, message_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyFavorite(long account_id, long status_id) {
		final DestroyFavoriteTask task = new DestroyFavoriteTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyFriendship(long account_id, long user_id) {
		final DestroyFriendshipTask task = new DestroyFriendshipTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyStatus(long account_id, long status_id) {
		final DestroyStatusTask task = new DestroyStatusTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyUserList(long account_id, int list_id) {
		final DestroyUserListTask task = new DestroyUserListTask(account_id, list_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyUserListSubscription(long account_id, int list_id) {
		final DestroyUserListSubscriptionTask task = new DestroyUserListSubscriptionTask(account_id, list_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int getDailyTrends(long account_id) {
		mAsyncTaskManager.cancel(mGetDailyTrendsTaskId);
		final GetDailyTrendsTask task = new GetDailyTrendsTask(account_id);
		return mGetDailyTrendsTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getHomeTimeline(long[] account_ids, long[] max_ids) {
		final boolean notification = mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_HOME_TIMELINE, false);
		return getHomeTimeline(account_ids, max_ids, notification);
	}

	public int getLocalTrends(long account_id, int woeid) {
		mAsyncTaskManager.cancel(mGetLocalTrendsTaskId);
		final GetLocalTrendsTask task = new GetLocalTrendsTask(account_id, woeid);
		return mGetLocalTrendsTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getMentions(long[] account_ids, long[] max_ids) {
		final boolean notification = mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS, false);
		return getMentions(account_ids, max_ids, notification);
	}

	public int getReceivedDirectMessages(long[] account_ids, long[] max_ids) {
		final boolean notification = mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES, false);
		return getReceivedDirectMessages(account_ids, max_ids, notification);
	}

	public int getSentDirectMessages(long[] account_ids, long[] max_ids) {
		mAsyncTaskManager.cancel(mGetSentDirectMessagesTaskId);
		final GetSentDirectMessagesTask task = new GetSentDirectMessagesTask(account_ids, max_ids);
		return mGetSentDirectMessagesTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getWeeklyTrends(long account_id) {
		mAsyncTaskManager.cancel(mGetWeeklyTrendsTaskId);
		final GetWeeklyTrendsTask task = new GetWeeklyTrendsTask(account_id);
		return mGetWeeklyTrendsTaskId = mAsyncTaskManager.add(task, true);
	}

	public boolean hasActivatedTask() {
		return mAsyncTaskManager.hasActivatedTask();
	}

	public boolean isDailyTrendsRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetDailyTrendsTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreDailyTrendsTaskId);
	}

	public boolean isHomeTimelineRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetHomeTimelineTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreStatusesTaskId);
	}

	public boolean isLocalTrendsRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetLocalTrendsTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreLocalTrendsTaskId);
	}

	public boolean isMentionsRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetMentionsTaskId) || mAsyncTaskManager.isExcuting(mStoreMentionsTaskId);
	}

	public boolean isReceivedDirectMessagesRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetReceivedDirectMessagesTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreReceivedDirectMessagesTaskId);
	}

	public boolean isSentDirectMessagesRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetSentDirectMessagesTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreSentDirectMessagesTaskId);
	}

	public boolean isWeeklyTrendsRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetWeeklyTrendsTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreWeeklyTrendsTaskId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		mAsyncTaskManager = ((TwidereApplication) getApplication()).getAsyncTaskManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mResolver = getContentResolver();
		final Intent refresh_intent = new Intent(BROADCAST_AUTO_REFRESH);
		mPendingRefreshIntent = PendingIntent.getBroadcast(this, 0, refresh_intent, 0);
		final IntentFilter filter = new IntentFilter(BROADCAST_REFRESHSTATE_CHANGED);
		filter.addAction(BROADCAST_NOTIFICATION_CLEARED);
		filter.addAction(BROADCAST_AUTO_REFRESH);
		registerReceiver(mStateReceiver, filter);

		startAutoRefresh();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mStateReceiver);
		mNotificationManager.cancelAll();
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			// Auto refresh enabled, so I will try to start service after it was
			// stopped.
			startService(new Intent(INTENT_ACTION_SERVICE));
		}
		super.onDestroy();
	}

	public int reportSpam(long account_id, long user_id) {
		final ReportSpamTask task = new ReportSpamTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int retweetStatus(long account_id, long status_id) {
		final RetweetStatusTask task = new RetweetStatusTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int sendDirectMessage(long account_id, String screen_name, long user_id, String message) {
		final SendDirectMessageTask task = new SendDirectMessageTask(account_id, screen_name, user_id, message);
		return mAsyncTaskManager.add(task, true);
	}

	public void shutdownService() {
		// Auto refresh is enabled, so this service cannot be shut down.
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) return;
		if (!mAsyncTaskManager.hasActivatedTask()) {
			stopSelf();
		} else {
			mShouldShutdown = true;
		}
	}

	public boolean startAutoRefresh() {
		mAlarmManager.cancel(mPendingRefreshIntent);
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval = parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")) * 60 * 1000;
			if (update_interval <= 0) return false;
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + update_interval,
					update_interval, mPendingRefreshIntent);
			return true;
		}
		return false;
	}

	public void stopAutoRefresh() {
		mAlarmManager.cancel(mPendingRefreshIntent);
	}

	public boolean test() {
		try {
			return startService(new Intent(INTENT_ACTION_SERVICE)) != null;
		} catch (final Exception e) {
			return false;
		}
	}

	public int updateProfile(long account_id, String name, String url, String location, String description) {
		final UpdateProfileTask task = new UpdateProfileTask(account_id, name, url, location, description);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateProfileImage(long account_id, Uri image_uri, boolean delete_image) {
		final UpdateProfileImageTask task = new UpdateProfileImageTask(account_id, image_uri, delete_image);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateStatus(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to,
			boolean delete_image) {
		final UpdateStatusTask task = new UpdateStatusTask(account_ids, content, location, image_uri, in_reply_to,
				delete_image);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateUserListDetails(long account_id, int list_id, boolean is_public, String name, String description) {
		final UpdateUserListProfileTask task = new UpdateUserListProfileTask(account_id, list_id, is_public, name,
				description);
		return mAsyncTaskManager.add(task, true);
	}

	private Notification buildNotification(String title, String message, int icon, Intent content_intent,
			Intent delete_intent) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setTicker(message);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(icon);
		builder.setDeleteIntent(PendingIntent.getBroadcast(this, 0, delete_intent, PendingIntent.FLAG_UPDATE_CURRENT));
		builder.setContentIntent(PendingIntent.getActivity(this, 0, content_intent, PendingIntent.FLAG_UPDATE_CURRENT));
		int defaults = 0;
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, false)) {
			builder.setSound(Uri.parse(mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE,
					Settings.System.DEFAULT_RINGTONE_URI.getPath())), Notification.STREAM_DEFAULT);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION, false)) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS, false)) {
			final int color_def = getResources().getColor(R.color.holo_blue_light);
			final int color = mPreferences.getInt(PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR, color_def);
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
		return builder.getNotification();
	}

	private int getHomeTimeline(long[] account_ids, long[] max_ids, boolean is_auto_refresh) {
		mAsyncTaskManager.cancel(mGetHomeTimelineTaskId);
		final GetHomeTimelineTask task = new GetHomeTimelineTask(account_ids, max_ids, is_auto_refresh);
		return mGetHomeTimelineTaskId = mAsyncTaskManager.add(task, true);
	}

	private int getMentions(long[] account_ids, long[] max_ids, boolean is_auto_refresh) {
		mAsyncTaskManager.cancel(mGetMentionsTaskId);
		final GetMentionsTask task = new GetMentionsTask(account_ids, max_ids, is_auto_refresh);
		return mGetMentionsTaskId = mAsyncTaskManager.add(task, true);
	}

	private int getReceivedDirectMessages(long[] account_ids, long[] max_ids, boolean is_auto_refresh) {
		mAsyncTaskManager.cancel(mGetReceivedDirectMessagesTaskId);
		final GetReceivedDirectMessagesTask task = new GetReceivedDirectMessagesTask(account_ids, max_ids,
				is_auto_refresh);
		return mGetReceivedDirectMessagesTaskId = mAsyncTaskManager.add(task, true);
	}

	private void showErrorToast(Exception e, boolean long_message) {
		Utils.showErrorToast(this, e, long_message);
	}

	class AddUserListMemberTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id, user_id;
		private final int list_id;
		private final String screen_name;

		public AddUserListMemberTask(long account_id, int list_id, long user_id, String screen_name) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
			this.user_id = user_id;
			this.screen_name = screen_name;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					if (user_id > 0) {
						final UserList list = twitter.addUserListMember(list_id, user_id);
						return new SingleResponse<UserList>(account_id, list, null);
					} else if (screen_name != null) {
						final User user = twitter.showUser(screen_name);
						if (user != null && user.getId() > 0) {
							final UserList list = twitter.addUserListMember(list_id, user.getId());
							return new SingleResponse<UserList>(account_id, list, null);
						}
					}
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.add_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_MEMBER_DELETED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_LIST_ID, list_id);
			intent.putExtra(INTENT_KEY_SUCCEED, succeed);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CacheUsersTask extends ManagedAsyncTask<Void, Void, Void> {

		private final List<ListResponse<twitter4j.Status>> responses;

		public CacheUsersTask(List<ListResponse<twitter4j.Status>> result) {
			super(TwidereService.this, mAsyncTaskManager);
			responses = result;
		}

		@Override
		protected Void doInBackground(Void... args) {

			for (final ListResponse<twitter4j.Status> response : responses) {
				final List<twitter4j.Status> statuses = response.list;
				if (statuses == null || statuses.size() <= 0) {
					continue;
				}
				final List<ContentValues> cached_users_list = new ArrayList<ContentValues>();
				final List<Long> user_ids = new ArrayList<Long>();

				for (final twitter4j.Status status : statuses) {
					if (status == null) {
						continue;
					}
					final User user = status.getUser();
					final long user_id = user.getId();

					if (!user_ids.contains(user_id)) {
						user_ids.add(user_id);
						cached_users_list.add(makeCachedUserContentValues(user));
					}

				}

				mResolver.delete(CachedUsers.CONTENT_URI,
						CachedUsers.USER_ID + " IN (" + ListUtils.buildString(user_ids, ',', true) + " )", null);
				mResolver.bulkInsert(CachedUsers.CONTENT_URI,
						cached_users_list.toArray(new ContentValues[cached_users_list.size()]));

			}
			return null;
		}

	}

	class CancelRetweetTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private long account_id;
		private long status_id, retweeted_id;

		public CancelRetweetTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyStatus(retweeted_id);
					return new SingleResponse<twitter4j.Status>(account_id, status, null);
				} catch (final TwitterException e) {
					return new SingleResponse<twitter4j.Status>(account_id, null, e);
				}
			}
			return new SingleResponse<twitter4j.Status>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<twitter4j.Status> result) {
			if (result != null && result.data != null) {

				final User user = result.data.getUser();
				final twitter4j.Status retweeted_status = result.data.getRetweetedStatus();
				if (user != null && retweeted_status != null) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.RETWEET_COUNT, result.data.getRetweetCount());
					values.put(Statuses.RETWEET_ID, -1);
					values.put(Statuses.RETWEETED_BY_ID, -1);
					values.put(Statuses.RETWEETED_BY_NAME, "");
					values.put(Statuses.RETWEETED_BY_SCREEN_NAME, "");
					values.put(Statuses.IS_RETWEET, 0);
					final String status_where = Statuses.STATUS_ID + " = " + result.data.getId();
					final String retweet_where = Statuses.STATUS_ID + " = " + retweeted_status.getId();
					for (final Uri uri : TweetStore.STATUSES_URIS) {
						mResolver.delete(uri, status_where, null);
						mResolver.update(uri, values, retweet_where, null);
					}
				}
				Toast.makeText(TwidereService.this, R.string.cancel_retweet_success, Toast.LENGTH_SHORT).show();
				final Intent intent = new Intent(BROADCAST_RETWEET_CHANGED);
				intent.putExtra(INTENT_KEY_STATUS_ID, status_id);
				intent.putExtra(INTENT_KEY_RETWEETED, false);
				sendBroadcast(intent);
			} else {
				showErrorToast(result.exception, true);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			retweeted_id = getRetweetId(TwidereService.this, status_id);
		}

	}

	class CreateBlockTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private long account_id;
		private long user_id;

		public CreateBlockTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.createBlock(user_id);
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null && result.data.getId() > 0) {
				for (final Uri uri : Utils.STATUSES_URIS) {
					mResolver.delete(uri, Statuses.ACCOUNT_ID + " = " + account_id, null);
				}
				Toast.makeText(TwidereService.this, R.string.user_blocked, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateFavoriteTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private long account_id;

		private long status_id;

		public CreateFavoriteTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(Void... params) {

			if (account_id < 0) return new SingleResponse<twitter4j.Status>(account_id, null, null);

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.createFavorite(status_id);
					return new SingleResponse<twitter4j.Status>(account_id, status, null);
				} catch (final TwitterException e) {
					return new SingleResponse<twitter4j.Status>(account_id, null, e);
				}
			}
			return new SingleResponse<twitter4j.Status>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<twitter4j.Status> result) {

			if (result.data != null) {
				final long status_id = result.data.getId();
				final ContentValues values = new ContentValues();
				values.put(Statuses.IS_FAVORITE, 1);
				final StringBuilder where = new StringBuilder();
				where.append(Statuses.ACCOUNT_ID + " = " + result.account_id);
				where.append(" AND ");
				where.append("(");
				where.append(Statuses.STATUS_ID + "=" + status_id);
				where.append(" OR ");
				where.append(Statuses.RETWEET_ID + "=" + status_id);
				where.append(")");
				for (final Uri uri : TweetStore.STATUSES_URIS) {
					mResolver.update(uri, values, where.toString(), null);
				}
				final Intent intent = new Intent(BROADCAST_FAVORITE_CHANGED);
				intent.putExtra(INTENT_KEY_STATUS_ID, status_id);
				intent.putExtra(INTENT_KEY_FAVORITED, true);
				sendBroadcast(intent);
				Toast.makeText(TwidereService.this, R.string.favorite_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	class CreateFriendshipTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private long account_id;
		private long user_id;

		public CreateFriendshipTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.createFriendship(user_id);
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null) {
				Toast.makeText(TwidereService.this, R.string.follow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_FRIENDSHIP_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateUserListSubscriptionTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id;
		private final int list_id;

		public CreateUserListSubscriptionTask(long account_id, int list_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final UserList list = twitter.createUserListSubscription(list_id);
					return new SingleResponse<UserList>(account_id, list, null);
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.follow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_SUBSCRIPTION_CHANGED);
			intent.putExtra(INTENT_KEY_LIST_ID, list_id);
			intent.putExtra(INTENT_KEY_SUCCEED, succeed);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateUserListTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id;
		private final String list_name, description;
		private final boolean is_public;

		public CreateUserListTask(long account_id, String list_name, boolean is_public, String description) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_name = list_name;
			this.description = description;
			this.is_public = is_public;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					if (list_name != null) {
						final UserList list = twitter.createUserList(list_name, is_public, description);
						return new SingleResponse<UserList>(account_id, list, null);
					}
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.create_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_CREATED);
			intent.putExtra(INTENT_KEY_SUCCEED, succeed);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DeleteUserListMemberTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id, user_id;
		private final int list_id;

		public DeleteUserListMemberTask(long account_id, int list_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final UserList list = twitter.deleteUserListMember(list_id, user_id);
					return new SingleResponse<UserList>(account_id, list, null);
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_MEMBER_DELETED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_LIST_ID, list_id);
			intent.putExtra(INTENT_KEY_SUCCEED, succeed);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyBlockTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private long account_id;
		private long user_id;

		public DestroyBlockTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.destroyBlock(user_id);
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null) {
				Toast.makeText(TwidereService.this, R.string.user_unblocked, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyDirectMessageTask extends ManagedAsyncTask<Void, Void, SingleResponse<DirectMessage>> {

		private final Twitter twitter;
		private final long message_id;
		private final long account_id;

		public DestroyDirectMessageTask(long account_id, long message_id) {
			super(TwidereService.this, mAsyncTaskManager);
			twitter = getTwitterInstance(TwidereService.this, account_id, false);
			this.account_id = account_id;
			this.message_id = message_id;
		}

		@Override
		protected SingleResponse<DirectMessage> doInBackground(Void... args) {
			if (twitter == null) return new SingleResponse<DirectMessage>(account_id, null, null);
			try {
				return new SingleResponse<DirectMessage>(account_id, twitter.destroyDirectMessage(message_id), null);
			} catch (final TwitterException e) {
				return new SingleResponse<DirectMessage>(account_id, null, e);
			}
		}

		@Override
		protected void onPostExecute(SingleResponse<DirectMessage> result) {
			super.onPostExecute(result);
			if (result == null) return;
			if (result.data != null && result.data.getId() > 0) {
				Toast.makeText(TwidereService.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
				final String where = DirectMessages.MESSAGE_ID + " = " + result.data.getId();

				mResolver.delete(DirectMessages.Inbox.CONTENT_URI, where, null);
				mResolver.delete(DirectMessages.Outbox.CONTENT_URI, where, null);
			} else {
				showErrorToast(result.exception, true);
			}
		}

	}

	class DestroyFavoriteTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private long account_id;

		private long status_id;

		public DestroyFavoriteTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(Void... params) {

			if (account_id < 0) {
				new SingleResponse<twitter4j.Status>(account_id, null, null);
			}

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyFavorite(status_id);
					return new SingleResponse<twitter4j.Status>(account_id, status, null);
				} catch (final TwitterException e) {
					return new SingleResponse<twitter4j.Status>(account_id, null, e);
				}
			}
			return new SingleResponse<twitter4j.Status>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<twitter4j.Status> result) {

			if (result.data != null) {
				final long status_id = result.data.getId();
				final ContentValues values = new ContentValues();
				values.put(Statuses.IS_FAVORITE, 0);
				final StringBuilder where = new StringBuilder();
				where.append(Statuses.ACCOUNT_ID + " = " + result.account_id);
				where.append(" AND ");
				where.append("(");
				where.append(Statuses.STATUS_ID + "=" + status_id);
				where.append(" OR ");
				where.append(Statuses.RETWEET_ID + "=" + status_id);
				where.append(")");
				for (final Uri uri : TweetStore.STATUSES_URIS) {
					mResolver.update(uri, values, where.toString(), null);
				}
				final Intent intent = new Intent(BROADCAST_FAVORITE_CHANGED);
				intent.putExtra(INTENT_KEY_USER_ID, account_id);
				intent.putExtra(INTENT_KEY_STATUS_ID, status_id);
				intent.putExtra(INTENT_KEY_FAVORITED, false);
				sendBroadcast(intent);
				Toast.makeText(TwidereService.this, R.string.unfavorite_success, Toast.LENGTH_SHORT).show();

			} else {
				showErrorToast(result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	class DestroyFriendshipTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private long account_id;
		private long user_id;

		public DestroyFriendshipTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.destroyFriendship(user_id);
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null) {
				Toast.makeText(TwidereService.this, R.string.unfollow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_FRIENDSHIP_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyStatusTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private long account_id;

		private long status_id;

		public DestroyStatusTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyStatus(status_id);
					return new SingleResponse<twitter4j.Status>(account_id, status, null);
				} catch (final TwitterException e) {
					return new SingleResponse<twitter4j.Status>(account_id, null, e);
				}
			}
			return new SingleResponse<twitter4j.Status>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<twitter4j.Status> result) {
			final Intent intent = new Intent(BROADCAST_STATUS_DESTROYED);
			if (result != null && result.data != null && result.data.getId() > 0) {
				final long status_id = result.data.getId();

				final StringBuilder where = new StringBuilder();
				where.append(Statuses.STATUS_ID + " = " + status_id);
				where.append(" OR " + Statuses.RETWEET_ID + " = " + status_id);
				for (final Uri uri : TweetStore.STATUSES_URIS) {
					mResolver.delete(uri, where.toString(), null);
				}
				intent.putExtra(INTENT_KEY_STATUS_ID, status_id);
				intent.putExtra(INTENT_KEY_SUCCEED, true);
				Toast.makeText(TwidereService.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyUserListSubscriptionTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id;
		private final int list_id;

		public DestroyUserListSubscriptionTask(long account_id, int list_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final UserList list = twitter.destroyUserListSubscription(list_id);
					return new SingleResponse<UserList>(account_id, list, null);
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.unfollow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_SUBSCRIPTION_CHANGED);
			intent.putExtra(INTENT_KEY_LIST_ID, list_id);
			intent.putExtra(INTENT_KEY_SUCCEED, succeed);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyUserListTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id;
		private final int list_id;

		public DestroyUserListTask(long account_id, int list_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					if (list_id > 0) {
						final UserList list = twitter.destroyUserList(list_id);
						return new SingleResponse<UserList>(account_id, list, null);
					}
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_DELETED);
			intent.putExtra(INTENT_KEY_SUCCEED, succeed);
			intent.putExtra(INTENT_KEY_LIST_ID, list_id);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class GetDailyTrendsTask extends GetTrendsTask {

		public GetDailyTrendsTask(long account_id) {
			super(account_id);
		}

		@Override
		public ResponseList<Trends> getTrends(Twitter twitter) throws TwitterException {
			if (twitter == null) return null;
			return twitter.getDailyTrends();
		}

		@Override
		protected void onPostExecute(ListResponse<Trends> result) {
			mStoreDailyTrendsTaskId = mAsyncTaskManager.add(new StoreDailyTrendsTask(result), true);
			super.onPostExecute(result);

		}

	}

	abstract class GetDirectMessagesTask extends ManagedAsyncTask<Void, Void, List<ListResponse<DirectMessage>>> {

		private final long[] account_ids, max_ids;

		public GetDirectMessagesTask(long[] account_ids, long[] max_ids) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.max_ids = max_ids;
		}

		public abstract ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging)
				throws TwitterException;

		@Override
		protected List<ListResponse<DirectMessage>> doInBackground(Void... params) {

			final List<ListResponse<DirectMessage>> result = new ArrayList<ListResponse<DirectMessage>>();

			if (account_ids == null) return result;

			final boolean max_ids_valid = max_ids != null && max_ids.length == account_ids.length;

			int idx = 0;
			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, true);
				if (twitter != null) {
					try {
						final Paging paging = new Paging();
						paging.setCount(load_item_limit);
						long max_id = -1;
						if (max_ids_valid && max_ids[idx] > 0) {
							max_id = max_ids[idx];
							paging.setMaxId(max_id);
						}
						final ResponseList<DirectMessage> statuses = getDirectMessages(twitter, paging);

						if (statuses != null) {
							result.add(new ListResponse<DirectMessage>(account_id, max_id, statuses));
						}
					} catch (final TwitterException e) {
						e.printStackTrace();
					}
				}
				idx++;
			}
			return result;

		}

	}

	class GetHomeTimelineTask extends GetStatusesTask {

		private final boolean is_auto_refresh;

		public GetHomeTimelineTask(long[] account_ids, long[] max_ids, boolean is_auto_refresh) {
			super(Statuses.CONTENT_URI, account_ids, max_ids);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getHomeTimeline(paging);
		}

		@Override
		public Twitter getTwitter(long account_id) {
			return getTwitterInstance(TwidereService.this, account_id, true, true);
		}

		@Override
		protected void onPostExecute(List<ListResponse<twitter4j.Status>> responses) {
			super.onPostExecute(responses);
			mStoreStatusesTaskId = mAsyncTaskManager.add(new StoreHomeTimelineTask(responses, is_auto_refresh,
					shouldSetMinId()), true);
			mGetHomeTimelineTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	class GetLocalTrendsTask extends GetTrendsTask {

		private final int woeid;

		public GetLocalTrendsTask(long account_id, int woeid) {
			super(account_id);
			this.woeid = woeid;
		}

		@Override
		public List<Trends> getTrends(Twitter twitter) throws TwitterException {
			final ArrayList<Trends> trends_list = new ArrayList<Trends>();
			if (twitter != null) {
				trends_list.add(twitter.getLocationTrends(woeid));
			}
			return trends_list;
		}

		@Override
		protected void onPostExecute(ListResponse<Trends> result) {
			mStoreLocalTrendsTaskId = mAsyncTaskManager.add(new StoreLocalTrendsTask(result), true);
			super.onPostExecute(result);

		}

	}

	class GetMentionsTask extends GetStatusesTask {

		private final boolean is_auto_refresh;

		public GetMentionsTask(long[] account_ids, long[] max_ids, boolean is_auto_refresh) {
			super(Mentions.CONTENT_URI, account_ids, max_ids);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getMentions(paging);
		}

		@Override
		public Twitter getTwitter(long account_id) {
			return getTwitterInstance(TwidereService.this, account_id, true, false);
		}

		@Override
		protected void onPostExecute(List<ListResponse<twitter4j.Status>> responses) {
			super.onPostExecute(responses);
			mStoreMentionsTaskId = mAsyncTaskManager.add(new StoreMentionsTask(responses, is_auto_refresh,
					shouldSetMinId()), true);
			mGetMentionsTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	class GetReceivedDirectMessagesTask extends GetDirectMessagesTask {

		private final boolean is_auto_refresh;

		public GetReceivedDirectMessagesTask(long[] account_ids, long[] max_ids, boolean is_auto_refresh) {
			super(account_ids, max_ids);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(List<ListResponse<DirectMessage>> responses) {
			super.onPostExecute(responses);
			mStoreReceivedDirectMessagesTaskId = mAsyncTaskManager.add(new StoreReceivedDirectMessagesTask(responses,
					is_auto_refresh), true);
			mGetReceivedDirectMessagesTaskId = -1;
		}

	}

	class GetSentDirectMessagesTask extends GetDirectMessagesTask {

		public GetSentDirectMessagesTask(long[] account_ids, long[] max_ids) {
			super(account_ids, max_ids);
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getSentDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(List<ListResponse<DirectMessage>> responses) {
			super.onPostExecute(responses);
			mStoreSentDirectMessagesTaskId = mAsyncTaskManager.add(new StoreSentDirectMessagesTask(responses), true);
			mGetSentDirectMessagesTaskId = -1;
		}

	}

	abstract class GetStatusesTask extends ManagedAsyncTask<Void, Void, List<ListResponse<twitter4j.Status>>> {

		private final long[] account_ids, max_ids;

		private boolean should_set_min_id;

		public GetStatusesTask(Uri uri, long[] account_ids, long[] max_ids) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.max_ids = max_ids;
		}

		public abstract ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging)
				throws TwitterException;

		public abstract Twitter getTwitter(long account_id);

		public boolean shouldSetMinId() {
			return should_set_min_id;
		}

		@Override
		protected List<ListResponse<twitter4j.Status>> doInBackground(Void... params) {

			final List<ListResponse<twitter4j.Status>> result = new ArrayList<ListResponse<twitter4j.Status>>();

			if (account_ids == null) return result;

			final boolean max_ids_valid = max_ids != null && max_ids.length == account_ids.length;
			should_set_min_id = !max_ids_valid;

			int idx = 0;
			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitter(account_id);
				if (twitter != null) {
					try {
						final Paging paging = new Paging();
						paging.setCount(load_item_limit);
						long max_id = -1;
						if (max_ids_valid && max_ids[idx] > 0) {
							max_id = max_ids[idx];
							paging.setMaxId(max_id);
						}
						final ResponseList<twitter4j.Status> statuses = getStatuses(twitter, paging);

						if (statuses != null) {
							result.add(new ListResponse<twitter4j.Status>(account_id, max_id, statuses));
						}
					} catch (final TwitterException e) {
						e.printStackTrace();
					}
				}
				idx++;
			}
			return result;
		}

	}

	abstract class GetTrendsTask extends ManagedAsyncTask<Void, Void, ListResponse<Trends>> {

		private final long account_id;

		public GetTrendsTask(long account_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
		}

		public abstract List<Trends> getTrends(Twitter twitter) throws TwitterException;

		@Override
		protected ListResponse<Trends> doInBackground(Void... params) {
			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					return new ListResponse<Trends>(account_id, -1, getTrends(twitter));
				} catch (final TwitterException e) {
					e.printStackTrace();
				}
			}
			return new ListResponse<Trends>(account_id, -1, null);
		}

	}

	class GetWeeklyTrendsTask extends GetTrendsTask {

		public GetWeeklyTrendsTask(long account_id) {
			super(account_id);
		}

		@Override
		public ResponseList<Trends> getTrends(Twitter twitter) throws TwitterException {
			if (twitter == null) return null;
			return twitter.getWeeklyTrends();
		}

		@Override
		protected void onPostExecute(ListResponse<Trends> result) {
			mStoreWeeklyTrendsTaskId = mAsyncTaskManager.add(new StoreWeeklyTrendsTask(result), true);
			super.onPostExecute(result);

		}

	}

	static final class ListResponse<Data> {

		public final long account_id, max_id;
		public final List<Data> list;

		public ListResponse(long account_id, long max_id, List<Data> responselist) {
			this.account_id = account_id;
			this.max_id = max_id;
			this.list = responselist;
		}
	}

	class ReportSpamTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private long account_id;
		private long user_id;

		public ReportSpamTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.reportSpam(user_id);
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null && result.data.getId() > 0) {
				for (final Uri uri : Utils.STATUSES_URIS) {
					mResolver.delete(uri, Statuses.ACCOUNT_ID + " = " + account_id, null);
				}
				Toast.makeText(TwidereService.this, R.string.reported_user_for_spam, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class RetweetStatusTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private long account_id;

		private long status_id;

		public RetweetStatusTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(Void... params) {

			if (account_id < 0) return new SingleResponse<twitter4j.Status>(account_id, null, null);

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.retweetStatus(status_id);
					return new SingleResponse<twitter4j.Status>(account_id, status, null);
				} catch (final TwitterException e) {
					return new SingleResponse<twitter4j.Status>(account_id, null, e);
				}
			}
			return new SingleResponse<twitter4j.Status>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<twitter4j.Status> result) {

			if (result.data != null && result.data.getId() > 0) {
				final User user = result.data.getUser();
				if (user != null) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.RETWEET_ID, result.data.getId());
					values.put(Statuses.RETWEETED_BY_ID, user.getId());
					values.put(Statuses.RETWEETED_BY_NAME, user.getName());
					values.put(Statuses.RETWEETED_BY_SCREEN_NAME, user.getScreenName());
					values.put(Statuses.RETWEET_COUNT, result.data.getRetweetCount());
					values.put(Statuses.IS_RETWEET, 1);
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.STATUS_ID + " = " + status_id);
					where.append(" OR " + Statuses.RETWEET_ID + " = " + status_id);
					for (final Uri uri : TweetStore.STATUSES_URIS) {
						mResolver.update(uri, values, where.toString(), null);
					}
				}
				Toast.makeText(TwidereService.this, R.string.retweet_success, Toast.LENGTH_SHORT).show();
				final Intent intent = new Intent(BROADCAST_RETWEET_CHANGED);
				intent.putExtra(INTENT_KEY_STATUS_ID, status_id);
				intent.putExtra(INTENT_KEY_RETWEETED, true);
				sendBroadcast(intent);
			} else {
				showErrorToast(result.exception, true);
			}

			super.onPostExecute(result);
		}

	}

	class SendDirectMessageTask extends ManagedAsyncTask<Void, Void, SingleResponse<DirectMessage>> {

		private final Twitter twitter;
		private final long user_id;
		private final String screen_name;
		private final String message;
		private final long account_id;

		public SendDirectMessageTask(long account_id, String screen_name, long user_id, String message) {
			super(TwidereService.this, mAsyncTaskManager);
			twitter = getTwitterInstance(TwidereService.this, account_id, false);
			this.account_id = account_id;
			this.user_id = user_id;
			this.screen_name = screen_name;
			this.message = message;
		}

		@Override
		protected SingleResponse<DirectMessage> doInBackground(Void... args) {
			if (twitter == null) return new SingleResponse<DirectMessage>(account_id, null, null);
			try {
				if (user_id > 0)
					return new SingleResponse<DirectMessage>(account_id, twitter.sendDirectMessage(user_id, message),
							null);
				else if (screen_name != null)
					return new SingleResponse<DirectMessage>(account_id,
							twitter.sendDirectMessage(screen_name, message), null);
			} catch (final TwitterException e) {
				return new SingleResponse<DirectMessage>(account_id, null, e);
			}
			return new SingleResponse<DirectMessage>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<DirectMessage> result) {
			super.onPostExecute(result);
			if (result == null) return;
			if (result.data != null && result.data.getId() > 0) {
				final Uri.Builder builder = DirectMessages.Outbox.CONTENT_URI.buildUpon();
				builder.appendQueryParameter(QUERY_PARAM_NOTIFY, String.valueOf(true));
				final ContentValues values = makeDirectMessageContentValues(result.data, account_id);
				getContentResolver().insert(builder.build(), values);
				Toast.makeText(TwidereService.this, R.string.send_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
		}

	}

	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	static final class ServiceStub extends ITwidereService.Stub {

		final WeakReference<TwidereService> mService;

		public ServiceStub(TwidereService service) {

			mService = new WeakReference<TwidereService>(service);
		}

		@Override
		public int addUserListMember(long account_id, int list_id, long user_id, String screen_name) {
			return mService.get().addUserListMember(account_id, list_id, user_id, screen_name);
		}

		@Override
		public int cancelRetweet(long account_id, long status_id) {
			return mService.get().cancelRetweet(account_id, status_id);
		}

		@Override
		public void clearNotification(int id) {
			mService.get().clearNotification(id);
		}

		@Override
		public int createBlock(long account_id, long user_id) {
			return mService.get().createBlock(account_id, user_id);
		}

		@Override
		public int createFavorite(long account_id, long status_id) {
			return mService.get().createFavorite(account_id, status_id);
		}

		@Override
		public int createFriendship(long account_id, long user_id) {
			return mService.get().createFriendship(account_id, user_id);
		}

		@Override
		public int createUserList(long account_id, String list_name, boolean is_public, String description) {
			return mService.get().createUserList(account_id, list_name, is_public, description);
		}

		@Override
		public int createUserListSubscription(long account_id, int list_id) {
			return mService.get().createUserListSubscription(account_id, list_id);
		}

		@Override
		public int deleteUserListMember(long account_id, int list_id, long user_id) {
			return mService.get().deleteUserListMember(account_id, list_id, user_id);
		}

		@Override
		public int destroyBlock(long account_id, long user_id) {
			return mService.get().destroyBlock(account_id, user_id);
		}

		@Override
		public int destroyDirectMessage(long account_id, long message_id) {
			return mService.get().destroyDirectMessage(account_id, message_id);
		}

		@Override
		public int destroyFavorite(long account_id, long status_id) {
			return mService.get().destroyFavorite(account_id, status_id);
		}

		@Override
		public int destroyFriendship(long account_id, long user_id) {
			return mService.get().destroyFriendship(account_id, user_id);
		}

		@Override
		public int destroyStatus(long account_id, long status_id) {
			return mService.get().destroyStatus(account_id, status_id);
		}

		@Override
		public int destroyUserList(long account_id, int list_id) {
			return mService.get().destroyUserList(account_id, list_id);
		}

		@Override
		public int destroyUserListSubscription(long account_id, int list_id) {
			return mService.get().destroyUserListSubscription(account_id, list_id);
		}

		@Override
		public int getDailyTrends(long account_id) {
			return mService.get().getDailyTrends(account_id);
		}

		@Override
		public int getHomeTimeline(long[] account_ids, long[] max_ids) {
			return mService.get().getHomeTimeline(account_ids, max_ids);
		}

		@Override
		public int getLocalTrends(long account_id, int woeid) {
			return mService.get().getLocalTrends(account_id, woeid);
		}

		@Override
		public int getMentions(long[] account_ids, long[] max_ids) {
			return mService.get().getMentions(account_ids, max_ids);
		}

		@Override
		public int getReceivedDirectMessages(long[] account_ids, long[] max_ids) {
			return mService.get().getReceivedDirectMessages(account_ids, max_ids);
		}

		@Override
		public int getSentDirectMessages(long[] account_ids, long[] max_ids) {
			return mService.get().getSentDirectMessages(account_ids, max_ids);
		}

		@Override
		public int getWeeklyTrends(long account_id) {
			return mService.get().getWeeklyTrends(account_id);
		}

		@Override
		public boolean hasActivatedTask() {
			return mService.get().hasActivatedTask();
		}

		@Override
		public boolean isDailyTrendsRefreshing() {
			return mService.get().isDailyTrendsRefreshing();
		}

		@Override
		public boolean isHomeTimelineRefreshing() {
			return mService.get().isHomeTimelineRefreshing();
		}

		@Override
		public boolean isLocalTrendsRefreshing() {
			return mService.get().isLocalTrendsRefreshing();
		}

		@Override
		public boolean isMentionsRefreshing() {
			return mService.get().isMentionsRefreshing();
		}

		@Override
		public boolean isReceivedDirectMessagesRefreshing() {
			return mService.get().isReceivedDirectMessagesRefreshing();
		}

		@Override
		public boolean isSentDirectMessagesRefreshing() {
			return mService.get().isSentDirectMessagesRefreshing();
		}

		@Override
		public boolean isWeeklyTrendsRefreshing() {
			return mService.get().isWeeklyTrendsRefreshing();
		}

		@Override
		public int reportSpam(long account_id, long user_id) {
			return mService.get().reportSpam(account_id, user_id);
		}

		@Override
		public int retweetStatus(long account_id, long status_id) {
			return mService.get().retweetStatus(account_id, status_id);
		}

		@Override
		public int sendDirectMessage(long account_id, String screen_name, long user_id, String message) {
			return mService.get().sendDirectMessage(account_id, screen_name, user_id, message);
		}

		@Override
		public void shutdownService() {
			mService.get().shutdownService();
		}

		@Override
		public boolean startAutoRefresh() {
			return mService.get().startAutoRefresh();
		}

		@Override
		public void stopAutoRefresh() {
			mService.get().stopAutoRefresh();
		}

		@Override
		public boolean test() {
			return mService.get().test();
		}

		@Override
		public int updateProfile(long account_id, String name, String url, String location, String description) {
			return mService.get().updateProfile(account_id, name, url, location, description);
		}

		@Override
		public int updateProfileImage(long account_id, Uri image_uri, boolean delete_image) {
			return mService.get().updateProfileImage(account_id, image_uri, delete_image);
		}

		@Override
		public int updateStatus(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to,
				boolean delete_image) {
			return mService.get().updateStatus(account_ids, content, location, image_uri, in_reply_to, delete_image);

		}

		@Override
		public int updateUserListDetails(long account_id, int list_id, boolean is_public, String name,
				String description) {
			return mService.get().updateUserListDetails(account_id, list_id, is_public, name, description);
		}
	}

	static final class SingleResponse<Data> {
		public final Exception exception;
		public final Data data;
		public final long account_id;

		public SingleResponse(long account_id, Data data, Exception exception) {
			this.exception = exception;
			this.data = data;
			this.account_id = account_id;
		}
	}

	class StoreDailyTrendsTask extends StoreTrendsTask {

		public StoreDailyTrendsTask(ListResponse<Trends> result) {
			super(result, CachedTrends.Daily.CONTENT_URI);
		}

	}

	class StoreDirectMessagesTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final List<ListResponse<DirectMessage>> responses;
		private final Uri uri;

		public StoreDirectMessagesTask(List<ListResponse<DirectMessage>> result, Uri uri) {
			super(TwidereService.this, mAsyncTaskManager);
			responses = result;
			this.uri = uri;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(Void... args) {

			boolean succeed = false;
			final Uri query_uri = buildQueryUri(uri, false);
			int total_items_inserted = 0;
			for (final ListResponse<DirectMessage> response : responses) {
				final long account_id = response.account_id;
				final List<DirectMessage> messages = response.list;
				final Cursor cur = mResolver.query(uri, new String[0], DirectMessages.ACCOUNT_ID + " = " + account_id,
						null, null);
				boolean no_items_before = false;
				if (cur != null) {
					no_items_before = cur.getCount() <= 0;
					cur.close();
				}
				if (messages != null) {
					final List<ContentValues> values_list = new ArrayList<ContentValues>();
					final List<Long> message_ids = new ArrayList<Long>();

					final long min_id = -1;

					for (final DirectMessage message : messages) {
						if (message == null || message.getId() <= 0) {
							continue;
						}
						message_ids.add(message.getId());

						values_list.add(makeDirectMessageContentValues(message, account_id));

					}

					int rows_deleted = -1;

					// Delete all rows conflicting before new data inserted.
					{
						final StringBuilder where = new StringBuilder();
						where.append(DirectMessages.ACCOUNT_ID + " = " + account_id);
						where.append(" AND ");
						where.append(DirectMessages.MESSAGE_ID + " IN ( "
								+ ListUtils.buildString(message_ids, ',', true) + " ) ");
						rows_deleted = mResolver.delete(query_uri, where.toString(), null);
					}

					// Insert previously fetched items.
					mResolver.bulkInsert(query_uri, values_list.toArray(new ContentValues[values_list.size()]));

					// No row deleted, so I will insert a gap.
					final boolean insert_gap = rows_deleted == 1 && message_ids.contains(response.max_id)
							|| rows_deleted == 0 && response.max_id == -1 && !no_items_before;
					if (insert_gap) {
						final ContentValues values = new ContentValues();
						values.put(DirectMessages.IS_GAP, 1);
						final StringBuilder where = new StringBuilder();
						where.append(DirectMessages.ACCOUNT_ID + "=" + account_id);
						where.append(" AND " + DirectMessages.MESSAGE_ID + "=" + min_id);
						mResolver.update(query_uri, values, where.toString(), null);
					}
					final int actual_items_inserted = values_list.size() - rows_deleted;
					if (actual_items_inserted > 0) {
						total_items_inserted += actual_items_inserted;
					}
				}
				succeed = true;
			}
			final Bundle bundle = new Bundle();
			bundle.putBoolean(INTENT_KEY_SUCCEED, succeed);
			bundle.putInt(INTENT_KEY_ITEMS_INSERTED, total_items_inserted);
			return new SingleResponse<Bundle>(-1, bundle, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			if (response != null && response.data != null && response.data.getBoolean(INTENT_KEY_SUCCEED)) {
				notifyForUpdatedUri(TwidereService.this, uri);
			}
			super.onPostExecute(response);
		}

	}

	class StoreHomeTimelineTask extends StoreStatusesTask {

		private final boolean is_auto_refresh;

		public StoreHomeTimelineTask(List<ListResponse<twitter4j.Status>> result, boolean is_auto_refresh,
				boolean should_set_min_id) {
			super(result, Statuses.CONTENT_URI, should_set_min_id);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			mStoreStatusesTaskId = -1;
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			final Bundle extras = new Bundle();
			extras.putBoolean(INTENT_KEY_SUCCEED, succeed);
			if (shouldSetMinId() && getTotalItemsInserted() > 0) {
				extras.putLong(INTENT_KEY_MIN_ID,
						response != null && response.data != null ? response.data.getLong(INTENT_KEY_MIN_ID, -1) : -1);
			}
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtras(extras));
			if (succeed && is_auto_refresh
					&& mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_HOME_TIMELINE, false)) {
				mNewStatusesCount += response.data.getInt(INTENT_KEY_ITEMS_INSERTED);
				if (mNewStatusesCount > 0) {
					final String message = getResources().getQuantityString(R.plurals.Ntweets, mNewStatusesCount,
							mNewStatusesCount);
					final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
					final Bundle delete_extras = new Bundle();
					delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_HOME_TIMELINE);
					delete_intent.putExtras(delete_extras);
					final Intent content_intent = new Intent(TwidereService.this, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_HOME);
					content_intent.putExtras(content_extras);
					mNotificationManager.notify(
							NOTIFICATION_ID_HOME_TIMELINE,
							buildNotification(getString(R.string.new_notifications), message, R.drawable.ic_stat_tweet,
									content_intent, delete_intent));
				}
			}
			super.onPostExecute(response);
		}

	}

	class StoreLocalTrendsTask extends StoreTrendsTask {

		public StoreLocalTrendsTask(ListResponse<Trends> result) {
			super(result, CachedTrends.Local.CONTENT_URI);
		}

	}

	class StoreMentionsTask extends StoreStatusesTask {

		private final boolean is_auto_refresh;

		public StoreMentionsTask(List<ListResponse<twitter4j.Status>> result, boolean is_auto_refresh,
				boolean should_set_min_id) {
			super(result, Mentions.CONTENT_URI, should_set_min_id);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			mStoreMentionsTaskId = -1;
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			final Bundle extras = new Bundle();
			extras.putBoolean(INTENT_KEY_SUCCEED, succeed);
			if (shouldSetMinId() && getTotalItemsInserted() > 0) {
				extras.putLong(INTENT_KEY_MIN_ID,
						response != null && response.data != null ? response.data.getLong(INTENT_KEY_MIN_ID, -1) : -1);
			}
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED).putExtras(extras));
			if (succeed && is_auto_refresh
					&& mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS, false)) {
				mNewMentionsCount += response.data.getInt(INTENT_KEY_ITEMS_INSERTED);
				if (mNewMentionsCount > 0) {
					final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
					final Bundle delete_extras = new Bundle();
					delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_MENTIONS);
					delete_intent.putExtras(delete_extras);
					final Intent content_intent = new Intent(TwidereService.this, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MENTIONS);
					content_intent.putExtras(content_extras);
					final String title = getString(R.string.mentions);
					final String message = getResources().getQuantityString(R.plurals.Nmentions, mNewMentionsCount,
							mNewMentionsCount);
					final Notification notification = buildNotification(title, message, R.drawable.ic_stat_mention,
							content_intent, delete_intent);
					mNotificationManager.notify(NOTIFICATION_ID_MENTIONS, notification);
				}
			}
			super.onPostExecute(response);
		}

	}

	class StoreReceivedDirectMessagesTask extends StoreDirectMessagesTask {

		private final boolean is_auto_refresh;

		public StoreReceivedDirectMessagesTask(List<ListResponse<DirectMessage>> result, boolean is_auto_refresh) {
			super(result, DirectMessages.Inbox.CONTENT_URI);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			mStoreReceivedDirectMessagesTaskId = -1;
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED)
					.putExtra(INTENT_KEY_SUCCEED, succeed));
			if (succeed && is_auto_refresh
					&& mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_DIRECT_MESSAGES, false)) {
				mNewMessagesCount += response.data.getInt(INTENT_KEY_ITEMS_INSERTED);
				if (mNewMessagesCount > 0) {
					final String message = getResources().getQuantityString(R.plurals.Ndirect_messages,
							mNewMessagesCount, mNewMessagesCount);
					final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
					final Bundle delete_extras = new Bundle();
					delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_DIRECT_MESSAGES);
					delete_intent.putExtras(delete_extras);
					final Intent content_intent = new Intent(TwidereService.this, HomeActivity.class);
					content_intent.setAction(Intent.ACTION_MAIN);
					content_intent.addCategory(Intent.CATEGORY_LAUNCHER);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MESSAGES);
					content_intent.putExtras(content_extras);
					mNotificationManager.notify(
							NOTIFICATION_ID_DIRECT_MESSAGES,
							buildNotification(getString(R.string.new_notifications), message,
									R.drawable.ic_stat_direct_message, content_intent, delete_intent));
				}
			}
			super.onPostExecute(response);
		}

	}

	class StoreSentDirectMessagesTask extends StoreDirectMessagesTask {

		public StoreSentDirectMessagesTask(List<ListResponse<DirectMessage>> result) {
			super(result, DirectMessages.Outbox.CONTENT_URI);
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			mStoreSentDirectMessagesTaskId = -1;
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed));
			super.onPostExecute(response);
		}

	}

	abstract class StoreStatusesTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final List<ListResponse<twitter4j.Status>> responses;
		private final Uri uri;
		private final boolean should_set_min_id;

		int total_items_inserted = 0;

		public StoreStatusesTask(List<ListResponse<twitter4j.Status>> result, Uri uri, boolean should_set_min_id) {
			super(TwidereService.this, mAsyncTaskManager);
			responses = result;
			this.should_set_min_id = should_set_min_id;
			this.uri = uri;
		}

		public int getTotalItemsInserted() {
			return total_items_inserted;
		}

		public boolean shouldSetMinId() {
			return should_set_min_id;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(Void... args) {

			boolean succeed = false;
			final Uri query_uri = buildQueryUri(uri, false);

			final ArrayList<Long> newly_inserted_ids = new ArrayList<Long>();
			for (final ListResponse<twitter4j.Status> response : responses) {
				final long account_id = response.account_id;
				final List<twitter4j.Status> statuses = response.list;
				if (statuses == null || statuses.size() <= 0) {
					continue;
				}
				final ArrayList<Long> ids_in_db = Utils.getStatusIdsInDatabase(TwidereService.this, query_uri,
						account_id);
				final boolean no_items_before = ids_in_db.size() <= 0;
				final List<ContentValues> values_list = new ArrayList<ContentValues>();
				final List<Long> status_ids = new ArrayList<Long>(), retweet_ids = new ArrayList<Long>();

				long min_id = -1;

				for (final twitter4j.Status status : statuses) {
					if (status == null) {
						continue;
					}
					final long status_id = status.getId();
					final long retweet_id = status.getRetweetedStatus() != null ? status.getRetweetedStatus().getId()
							: -1;

					status_ids.add(status_id);

					if ((retweet_id <= 0 || !retweet_ids.contains(retweet_id)) && !retweet_ids.contains(status_id)) {
						if (status_id < min_id || min_id == -1) {
							min_id = status_id;
						}
						if (retweet_id > 0) {
							retweet_ids.add(retweet_id);
						}
						values_list.add(makeStatusContentValues(status, account_id));
					}

				}

				int rows_deleted = -1;

				// Delete all rows conflicting before new data inserted.
				{

					final ArrayList<Long> account_newly_inserted = new ArrayList<Long>();
					account_newly_inserted.addAll(status_ids);
					account_newly_inserted.removeAll(ids_in_db);
					newly_inserted_ids.addAll(account_newly_inserted);
					final StringBuilder where = new StringBuilder();
					final String ids_string = ListUtils.buildString(status_ids, ',', true);
					where.append(Statuses.ACCOUNT_ID + " = " + account_id);
					where.append(" AND ");
					where.append("(");
					where.append(Statuses.STATUS_ID + " IN ( " + ids_string + " ) ");
					where.append(" OR ");
					where.append(Statuses.RETWEET_ID + " IN ( " + ids_string + " ) ");
					where.append(")");
					rows_deleted = mResolver.delete(query_uri, where.toString(), null);
				}

				// Insert previously fetched items.
				mResolver.bulkInsert(query_uri, values_list.toArray(new ContentValues[values_list.size()]));

				final int actual_items_inserted = values_list.size() - rows_deleted;

				if (actual_items_inserted > 0) {
					total_items_inserted += actual_items_inserted;
				}

				// No row deleted, so I will insert a gap.
				final boolean insert_gap = rows_deleted == 1 && status_ids.contains(response.max_id)
						|| rows_deleted == 0 && response.max_id == -1 && !no_items_before;
				if (insert_gap) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.IS_GAP, 1);
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + "=" + account_id);
					where.append(" AND " + Statuses.STATUS_ID + "=" + min_id);
					mResolver.update(query_uri, values, where.toString(), null);
					// Ignore gaps
					newly_inserted_ids.remove(min_id);
				}
				succeed = true;
			}
			final Bundle bundle = new Bundle();
			bundle.putBoolean(INTENT_KEY_SUCCEED, succeed);
			bundle.putInt(INTENT_KEY_ITEMS_INSERTED, total_items_inserted);
			if (should_set_min_id && total_items_inserted > 0) {
				bundle.putLong(INTENT_KEY_MIN_ID, ListUtils.min(newly_inserted_ids));
			}
			return new SingleResponse<Bundle>(-1, bundle, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			if (response.data.getBoolean(INTENT_KEY_SUCCEED)) {
				notifyForUpdatedUri(TwidereService.this, uri);
			}
			super.onPostExecute(response);
			mAsyncTaskManager.add(new CacheUsersTask(responses), true);
		}

	}

	class StoreTrendsTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final ListResponse<Trends> response;
		private final Uri uri;

		public StoreTrendsTask(ListResponse<Trends> result, Uri uri) {
			super(TwidereService.this, mAsyncTaskManager);
			response = result;
			this.uri = uri;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(Void... args) {
			final Bundle bundle = new Bundle();
			if (response != null) {

				final Uri query_uri = buildQueryUri(uri, false);
				final List<Trends> messages = response.list;
				if (messages != null && messages.size() > 0) {
					final ContentValues[] values_array = makeTrendsContentValues(messages);
					mResolver.delete(query_uri, null, null);
					mResolver.bulkInsert(query_uri, values_array);

					bundle.putBoolean(INTENT_KEY_SUCCEED, true);
				}
			}
			return new SingleResponse<Bundle>(-1, bundle, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			final Intent intent = new Intent(BROADCAST_TRENDS_UPDATED);
			if (response != null && response.data != null && response.data.getBoolean(INTENT_KEY_SUCCEED)) {
				notifyForUpdatedUri(TwidereService.this, uri);
				intent.putExtra(INTENT_KEY_SUCCEED, true);
			}
			super.onPostExecute(response);
			sendBroadcast(intent);
		}

	}

	class StoreWeeklyTrendsTask extends StoreTrendsTask {

		public StoreWeeklyTrendsTask(ListResponse<Trends> result) {
			super(result, CachedTrends.Weekly.CONTENT_URI);
		}

	}

	class UpdateProfileImageTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id;
		private final Uri image_uri;
		private final boolean delete_image;

		public UpdateProfileImageTask(long account_id, Uri image_uri, boolean delete_image) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.image_uri = image_uri;
			this.delete_image = delete_image;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
				try {
					final User user = twitter.updateProfileImage(new File(image_uri.getPath()));
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null) {
				Toast.makeText(TwidereService.this, R.string.profile_image_update_success, Toast.LENGTH_SHORT).show();
				if (delete_image) {
					new File(image_uri.getPath()).delete();
				}
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_UPDATED);
			intent.putExtra(INTENT_KEY_USER_ID, account_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class UpdateProfileTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id;
		private final String name, url, location, description;

		public UpdateProfileTask(long account_id, String name, String url, String location, String description) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.name = name;
			this.url = url;
			this.location = location;
			this.description = description;
		}

		@Override
		protected SingleResponse<User> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.updateProfile(name, url, location, description);
					return new SingleResponse<User>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<User>(account_id, null, e);
				}
			}
			return new SingleResponse<User>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<User> result) {
			if (result != null && result.data != null) {
				Toast.makeText(TwidereService.this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_UPDATED);
			intent.putExtra(INTENT_KEY_USER_ID, account_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.data != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class UpdateStatusTask extends ManagedAsyncTask<Void, Void, List<SingleResponse<twitter4j.Status>>> {

		private final ImageUploaderInterface uploader;
		private final TweetShortenerInterface shortener;
		private final Validator validator = new Validator();

		private long[] account_ids;
		private String content;
		private Location location;
		private Uri image_uri;
		private long in_reply_to;
		private boolean use_uploader, use_shortener, delete_image;

		public UpdateStatusTask(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to,
				boolean delete_image) {
			super(TwidereService.this, mAsyncTaskManager);
			final String uploader_component = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
			final String shortener_component = mPreferences.getString(PREFERENCE_KEY_TWEET_SHORTENER, null);
			use_uploader = !isNullOrEmpty(uploader_component);
			uploader = use_uploader ? ImageUploaderInterface.getInstance(getApplication(), uploader_component) : null;
			use_shortener = !isNullOrEmpty(shortener_component);
			shortener = use_shortener ? TweetShortenerInterface.getInstance(getApplication(), shortener_component)
					: null;
			this.account_ids = account_ids != null ? account_ids : new long[0];
			this.content = content;
			this.location = location;
			this.image_uri = image_uri;
			this.in_reply_to = in_reply_to;
			this.delete_image = delete_image;
		}

		@Override
		protected List<SingleResponse<twitter4j.Status>> doInBackground(Void... params) {

			final List<SingleResponse<twitter4j.Status>> result = new ArrayList<SingleResponse<twitter4j.Status>>();

			if (account_ids.length == 0) return result;

			try {
				if (use_uploader && uploader == null) throw new ImageUploaderNotFoundException();
				if (use_shortener && shortener == null) throw new TweetShortenerNotFoundException();

				final String image_path = getImagePathFromUri(TwidereService.this, image_uri);
				final File image_file = image_path != null ? new File(image_path) : null;
				if (uploader != null) {
					uploader.waitForService();
				}
				final Uri upload_result_uri = image_file != null && image_file.exists() && uploader != null ? uploader
						.upload(Uri.fromFile(image_file), content) : null;
				if (image_file != null && image_file.exists() && upload_result_uri == null)
					throw new ImageUploadException();

				final String unshortened_content = use_uploader && upload_result_uri != null ? getImageUploadStatus(
						TwidereService.this, upload_result_uri.toString(), content) : content;

				final boolean should_shorten = unshortened_content != null && unshortened_content.length() > 0
						&& !validator.isValidTweet(unshortened_content);
				final String screen_name = getAccountUsername(TwidereService.this, account_ids[0]);
				if (shortener != null) {
					shortener.waitForService();
				}
				final String shortened_content = should_shorten && use_shortener ? shortener.shorten(
						unshortened_content, screen_name, in_reply_to) : null;

				if (should_shorten) {
					if (!use_shortener)
						throw new StatusTooLongException();
					else if (unshortened_content == null) throw new TweetShortenException();
				}

				final StatusUpdate status = new StatusUpdate(should_shorten && use_shortener ? shortened_content
						: unshortened_content);
				status.setInReplyToStatusId(in_reply_to);
				if (location != null) {
					status.setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
				}
				if (!use_uploader && image_file != null && image_file.exists()) {
					status.setMedia(image_file);
				}

				for (final long account_id : account_ids) {
					final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
					if (twitter != null) {
						try {
							result.add(new SingleResponse<twitter4j.Status>(account_id, twitter.updateStatus(status),
									null));
						} catch (final TwitterException e) {
							e.printStackTrace();
							result.add(new SingleResponse<twitter4j.Status>(account_id, null, e));
						}
					}
				}
			} catch (final UpdateStatusException e) {
				for (final long account_id : account_ids) {
					result.add(new SingleResponse<twitter4j.Status>(account_id, null, e));
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<SingleResponse<twitter4j.Status>> result) {

			boolean succeed = true;
			Exception exception = null;
			final List<Long> failed_account_ids = new ArrayList<Long>();

			for (final SingleResponse<twitter4j.Status> response : result) {
				if (response.data == null) {
					succeed = false;
					failed_account_ids.add(response.account_id);
					if (exception == null) {
						exception = response.exception;
					}
				}
			}
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.send_success, Toast.LENGTH_SHORT).show();
				if (image_uri != null && delete_image) {
					final String path = getImagePathFromUri(TwidereService.this, image_uri);
					if (path != null) {
						new File(path).delete();
					}
				}
			} else {
				showErrorToast(exception, true);
				final StringBuilder ids_builder = new StringBuilder();
				final int size = failed_account_ids.size();
				for (int i = 0; i < size; i++) {
					final String id_string = String.valueOf(failed_account_ids.get(i));
					if (id_string != null) {
						if (i > 0) {
							ids_builder.append(';');
						}
						ids_builder.append(id_string);
					}
				}
				final ContentValues values = new ContentValues();
				values.put(Drafts.ACCOUNT_IDS, ids_builder.toString());
				values.put(Drafts.IN_REPLY_TO_STATUS_ID, in_reply_to);
				values.put(Drafts.TEXT, content);
				if (image_uri != null) {
					values.put(Drafts.MEDIA_URI, image_uri.toString());
				}

				mResolver.insert(Drafts.CONTENT_URI, values);
			}
			super.onPostExecute(result);
			if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_AFTER_TWEET, false)) {
				final long[] activated_ids = getActivatedAccountIds(TwidereService.this);
				getHomeTimeline(activated_ids, null);
				getMentions(activated_ids, null);
			}
		}

		class ImageUploaderNotFoundException extends UpdateStatusException {
			private static final long serialVersionUID = 1041685850011544106L;

			public ImageUploaderNotFoundException() {
				super(R.string.error_message_image_uploader_not_found);
			}
		}

		class ImageUploadException extends UpdateStatusException {
			private static final long serialVersionUID = 8596614696393917525L;

			public ImageUploadException() {
				super(R.string.error_message_image_upload_failed);
			}
		}

		class StatusTooLongException extends UpdateStatusException {
			private static final long serialVersionUID = -6469920130856384219L;

			public StatusTooLongException() {
				super(R.string.error_message_status_too_long);
			}
		}

		class TweetShortenerNotFoundException extends UpdateStatusException {
			private static final long serialVersionUID = -7262474256595304566L;

			public TweetShortenerNotFoundException() {
				super(R.string.error_message_tweet_shortener_not_found);
			}
		}

		class TweetShortenException extends UpdateStatusException {
			private static final long serialVersionUID = 3075877185536740034L;

			public TweetShortenException() {
				super(R.string.error_message_tweet_shorten_failed);
			}
		}

		class UpdateStatusException extends Exception {
			private static final long serialVersionUID = -1267218921727097910L;

			public UpdateStatusException(int message) {
				super(getString(message));
			}
		}
	}

	class UpdateUserListProfileTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id;
		private final int list_id;
		private final boolean is_public;
		private final String name, description;

		public UpdateUserListProfileTask(long account_id, int list_id, boolean is_public, String name,
				String description) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.name = name;
			this.list_id = list_id;
			this.is_public = is_public;
			this.description = description;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final UserList user = twitter.updateUserList(list_id, name, is_public, description);
					return new SingleResponse<UserList>(account_id, user, null);
				} catch (final TwitterException e) {
					return new SingleResponse<UserList>(account_id, null, e);
				}
			}
			return new SingleResponse<UserList>(account_id, null, null);
		}

		@Override
		protected void onPostExecute(SingleResponse<UserList> result) {
			final Intent intent = new Intent(BROADCAST_USER_LIST_DETAILS_UPDATED);
			intent.putExtra(INTENT_KEY_LIST_ID, list_id);
			if (result != null && result.data != null && result.data.getId() > 0) {
				Toast.makeText(TwidereService.this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
				intent.putExtra(INTENT_KEY_SUCCEED, true);
			} else {
				showErrorToast(result.exception, true);
			}
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

}
