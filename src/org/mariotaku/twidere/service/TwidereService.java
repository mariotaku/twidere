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
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getRetweetId;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.makeDirectMessageContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusContentValues;
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
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.ListUtils;
import org.mariotaku.twidere.util.ManagedAsyncTask;
import org.mariotaku.twidere.util.Utils;

import twitter4j.DirectMessage;
import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.annotation.SuppressLint;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class TwidereService extends Service implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);

	private AsyncTaskManager mAsyncTaskManager;

	private int mGetHomeTimelineTaskId, mGetMentionsTaskId;

	private int mStoreStatusesTaskId, mStoreMentionsTaskId;

	private SharedPreferences mPreferences;

	private int mGetReceivedDirectMessagesTaskId, mGetSentDirectMessagesTaskId;

	private int mStoreReceivedDirectMessagesTaskId, mStoreSentDirectMessagesTaskId;

	private NotificationManager mNotificationManager;

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
					clearNewNotificationCount(extras.getInt(INTENT_KEY_NOTIFICATION_ID));
				}
			}
		}

	};

	private boolean mShouldShutdown = false;
	private static final int ACTION_AUTO_REFRESH = 1;

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ACTION_AUTO_REFRESH: {
					final long[] activated_ids = getActivatedAccountIds(TwidereService.this);
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
							getReceivedDirectMessages(mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1), -1,
									true);
						}
					}
					mHandler.removeMessages(ACTION_AUTO_REFRESH);
					final long update_interval = parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")) * 60 * 1000;
					if (update_interval <= 0 || !mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
						break;
					}
					mHandler.sendEmptyMessageDelayed(ACTION_AUTO_REFRESH, update_interval);
					break;
				}
			}
		}
	};

	private int mNewMessagesCount, mNewMentionsCount, mNewStatusesCount;

	public int cancelRetweet(long account_id, long status_id) {
		final CancelRetweetTask task = new CancelRetweetTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public void clearNewNotificationCount(int id) {
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

	public int getHomeTimeline(long[] account_ids, long[] max_ids) {
		return getHomeTimeline(account_ids, max_ids, false);
	}

	public int getMentions(long[] account_ids, long[] max_ids) {
		return getMentions(account_ids, max_ids, false);
	}

	public int getReceivedDirectMessages(long account_id, long max_id) {
		return getReceivedDirectMessages(account_id, max_id, false);
	}

	public int getSentDirectMessages(long account_id, long max_id) {
		mAsyncTaskManager.cancel(mGetSentDirectMessagesTaskId);
		final GetSentDirectMessagesTask task = new GetSentDirectMessagesTask(account_id, max_id);
		return mGetSentDirectMessagesTaskId = mAsyncTaskManager.add(task, true);
	}

	public boolean hasActivatedTask() {
		return mAsyncTaskManager.hasActivatedTask();
	}

	public boolean isHomeTimelineRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetHomeTimelineTaskId)
				|| mAsyncTaskManager.isExcuting(mStoreStatusesTaskId);
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

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mAsyncTaskManager = ((TwidereApplication) getApplication()).getAsyncTaskManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		final IntentFilter filter = new IntentFilter(BROADCAST_REFRESHSTATE_CHANGED);
		filter.addAction(BROADCAST_NOTIFICATION_CLEARED);
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
		if (mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			final long update_interval = parseInt(mPreferences.getString(PREFERENCE_KEY_REFRESH_INTERVAL, "30")) * 60 * 1000;
			if (update_interval <= 0) return false;
			mHandler.sendEmptyMessageDelayed(ACTION_AUTO_REFRESH, update_interval);
			return true;
		}
		return false;
	}

	public void stopAutoRefresh() {
		mHandler.removeMessages(ACTION_AUTO_REFRESH);
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

	private int getReceivedDirectMessages(long account_id, long max_id, boolean is_auto_refresh) {
		mAsyncTaskManager.cancel(mGetReceivedDirectMessagesTaskId);
		final GetReceivedDirectMessagesTask task = new GetReceivedDirectMessagesTask(account_id, max_id,
				is_auto_refresh);
		return mGetReceivedDirectMessagesTaskId = mAsyncTaskManager.add(task, true);
	}

	private void showErrorToast(Exception e, boolean long_message) {
		Utils.showErrorToast(this, e, long_message);
	}

	private class CancelRetweetTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

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
				final ContentResolver resolver = getContentResolver();
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
						resolver.delete(uri, status_where, null);
						resolver.update(uri, values, retweet_where, null);
					}
				}
				Toast.makeText(TwidereService.this, R.string.cancel_retweet_success, Toast.LENGTH_SHORT).show();
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

	private class CreateBlockTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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
			if (result != null && result.data != null) {
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

	private class CreateFavoriteTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

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
			final ContentResolver resolver = getContentResolver();

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
					resolver.update(uri, values, where.toString(), null);
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

	private class CreateFriendshipTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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

	private class DestroyBlockTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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

	private class DestroyDirectMessageTask extends ManagedAsyncTask<Void, Void, SingleResponse<DirectMessage>> {

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
				final ContentResolver resolver = getContentResolver();
				resolver.delete(DirectMessages.Inbox.CONTENT_URI, where, null);
				resolver.delete(DirectMessages.Outbox.CONTENT_URI, where, null);
			} else {
				showErrorToast(result.exception, true);
			}
		}

	}

	private class DestroyFavoriteTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

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
			final ContentResolver resolver = getContentResolver();

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
					resolver.update(uri, values, where.toString(), null);
				}
				final Intent intent = new Intent(BROADCAST_FAVORITE_CHANGED);
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

	private class DestroyFriendshipTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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

	private class DestroyStatusTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

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
			if (result != null && result.data != null) {
				final long status_id = result.data.getId();
				final ContentResolver resolver = getContentResolver();
				final StringBuilder where = new StringBuilder();
				where.append(Statuses.STATUS_ID + " = " + status_id);
				where.append(" OR " + Statuses.RETWEET_ID + " = " + status_id);
				for (final Uri uri : TweetStore.STATUSES_URIS) {
					resolver.delete(uri, where.toString(), null);
				}
				Toast.makeText(TwidereService.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	private abstract class GetDirectMessagesTask extends ManagedAsyncTask<Void, Void, ListResponse<DirectMessage>> {

		private long account_id, max_id;

		public GetDirectMessagesTask(Uri uri, long account_id, long max_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.max_id = max_id;
		}

		public abstract ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging)
				throws TwitterException;

		@Override
		protected ListResponse<DirectMessage> doInBackground(Void... params) {

			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, true);
			if (twitter != null) {
				try {
					final Paging paging = new Paging();
					paging.setCount(load_item_limit);
					if (max_id > 0) {
						paging.setMaxId(max_id);
					}
					final ResponseList<DirectMessage> statuses = getDirectMessages(twitter, paging);

					if (statuses != null) return new ListResponse<DirectMessage>(account_id, max_id, statuses);
				} catch (final TwitterException e) {
					e.printStackTrace();
				}
			}
			return new ListResponse<DirectMessage>(account_id, max_id, null);
		}

	}

	private class GetHomeTimelineTask extends GetStatusesTask {

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
		public Twitter getTwitter(Context context, long account_id, boolean include_entities) {
			return getTwitterInstance(context, account_id, include_entities, true);
		}

		@Override
		protected void onPostExecute(List<ListResponse<twitter4j.Status>> responses) {
			super.onPostExecute(responses);
			mStoreStatusesTaskId = mAsyncTaskManager.add(new StoreHomeTimelineTask(responses, is_auto_refresh), true);
			mGetHomeTimelineTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	private class GetMentionsTask extends GetStatusesTask {

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
		public Twitter getTwitter(Context context, long account_id, boolean include_entities) {
			return getTwitterInstance(context, account_id, include_entities, false);
		}

		@Override
		protected void onPostExecute(List<ListResponse<twitter4j.Status>> responses) {
			super.onPostExecute(responses);
			mStoreMentionsTaskId = mAsyncTaskManager.add(new StoreMentionsTask(responses, is_auto_refresh), true);
			mGetMentionsTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

	}

	private class GetReceivedDirectMessagesTask extends GetDirectMessagesTask {

		private final boolean is_auto_refresh;

		public GetReceivedDirectMessagesTask(long account_ids, long max_ids, boolean is_auto_refresh) {
			super(DirectMessages.Inbox.CONTENT_URI, account_ids, max_ids);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(ListResponse<DirectMessage> responses) {
			super.onPostExecute(responses);
			mStoreReceivedDirectMessagesTaskId = mAsyncTaskManager.add(new StoreReceivedDirectMessagesTask(responses,
					is_auto_refresh), true);
			mGetReceivedDirectMessagesTaskId = -1;
		}

	}

	private class GetSentDirectMessagesTask extends GetDirectMessagesTask {

		public GetSentDirectMessagesTask(long account_ids, long max_ids) {
			super(DirectMessages.Outbox.CONTENT_URI, account_ids, max_ids);
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getSentDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(ListResponse<DirectMessage> responses) {
			super.onPostExecute(responses);
			mStoreSentDirectMessagesTaskId = mAsyncTaskManager.add(new StoreSentDirectMessagesTask(responses), true);
			mGetSentDirectMessagesTaskId = -1;
		}

	}

	private abstract class GetStatusesTask extends ManagedAsyncTask<Void, Void, List<ListResponse<twitter4j.Status>>> {

		private long[] account_ids, max_ids;

		public GetStatusesTask(Uri uri, long[] account_ids, long[] max_ids) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.max_ids = max_ids;
		}

		public abstract ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging)
				throws TwitterException;

		public abstract Twitter getTwitter(Context context, long account_id, boolean include_entities);

		@Override
		protected List<ListResponse<twitter4j.Status>> doInBackground(Void... params) {

			final List<ListResponse<twitter4j.Status>> result = new ArrayList<ListResponse<twitter4j.Status>>();

			if (account_ids == null) return result;

			final boolean max_ids_valid = max_ids != null && max_ids.length == account_ids.length;

			int idx = 0;
			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitter(TwidereService.this, account_id, true);
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

	private static final class ListResponse<Data> {

		public final long account_id, max_id;
		public final ResponseList<Data> list;

		public ListResponse(long account_id, long max_id, ResponseList<Data> responselist) {
			this.account_id = account_id;
			this.max_id = max_id;
			this.list = responselist;
		}
	}

	private class ReportSpamTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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
			if (result != null && result.data != null) {
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

	private class RetweetStatusTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

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
			final ContentResolver resolver = getContentResolver();

			if (result.data != null) {
				final User user = result.data.getUser();
				final twitter4j.Status retweeted_status = result.data.getRetweetedStatus();
				if (user != null && retweeted_status != null) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.RETWEET_ID, result.data.getId());
					values.put(Statuses.RETWEETED_BY_ID, user.getId());
					values.put(Statuses.RETWEETED_BY_NAME, user.getName());
					values.put(Statuses.RETWEETED_BY_SCREEN_NAME, user.getScreenName());
					values.put(Statuses.RETWEET_COUNT, retweeted_status.getRetweetCount());
					values.put(Statuses.IS_RETWEET, 1);
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.STATUS_ID + " = " + retweeted_status.getId());
					where.append(" OR " + Statuses.RETWEET_ID + " = " + retweeted_status.getId());
					for (final Uri uri : TweetStore.STATUSES_URIS) {
						resolver.update(uri, values, where.toString(), null);
					}
				}
				Toast.makeText(TwidereService.this, R.string.retweet_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}

			super.onPostExecute(result);
		}

	}

	private class SendDirectMessageTask extends ManagedAsyncTask<Void, Void, SingleResponse<DirectMessage>> {

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
	private static final class ServiceStub extends ITwidereService.Stub {

		final WeakReference<TwidereService> mService;

		public ServiceStub(TwidereService service) {

			mService = new WeakReference<TwidereService>(service);
		}

		@Override
		public int cancelRetweet(long account_id, long status_id) {
			return mService.get().cancelRetweet(account_id, status_id);
		}

		@Override
		public void clearNewNotificationCount(int id) {
			mService.get().clearNewNotificationCount(id);
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
		public int getHomeTimeline(long[] account_ids, long[] max_ids) {
			return mService.get().getHomeTimeline(account_ids, max_ids);
		}

		@Override
		public int getMentions(long[] account_ids, long[] max_ids) {
			return mService.get().getMentions(account_ids, max_ids);
		}

		@Override
		public int getReceivedDirectMessages(long account_id, long max_id) {
			return mService.get().getReceivedDirectMessages(account_id, max_id);
		}

		@Override
		public int getSentDirectMessages(long account_id, long max_id) {
			return mService.get().getSentDirectMessages(account_id, max_id);
		}

		@Override
		public boolean hasActivatedTask() {
			return mService.get().hasActivatedTask();
		}

		@Override
		public boolean isHomeTimelineRefreshing() {
			return mService.get().isHomeTimelineRefreshing();
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
			return true;
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

	}

	private static final class SingleResponse<Data> {
		public final Exception exception;
		public final Data data;
		public final long account_id;

		public SingleResponse(long account_id, Data data, Exception exception) {
			this.exception = exception;
			this.data = data;
			this.account_id = account_id;
		}
	}

	private class StoreDirectMessagesTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final ListResponse<DirectMessage> response;
		private final Uri uri;

		public StoreDirectMessagesTask(ListResponse<DirectMessage> result, Uri uri) {
			super(TwidereService.this, mAsyncTaskManager);
			response = result;
			this.uri = uri;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(Void... args) {
			final ContentResolver resolver = getContentResolver();
			final Uri query_uri = buildQueryUri(uri, false);
			final Bundle bundle = new Bundle();
			final long account_id = response.account_id;
			final ResponseList<DirectMessage> messages = response.list;
			final Cursor cur = resolver.query(uri, new String[0], DirectMessages.ACCOUNT_ID + " = " + account_id, null,
					null);
			boolean no_items_before = false;
			if (cur != null) {
				no_items_before = cur.getCount() <= 0;
				cur.close();
			}
			bundle.putBoolean(INTENT_KEY_SUCCEED, messages != null);
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
					where.append(Statuses.ACCOUNT_ID + " = " + account_id);
					where.append(" AND ");
					where.append(DirectMessages.MESSAGE_ID + " IN ( " + ListUtils.buildString(message_ids, ',', true)
							+ " ) ");
					rows_deleted = resolver.delete(query_uri, where.toString(), null);
				}

				// Insert previously fetched items.
				resolver.bulkInsert(query_uri, values_list.toArray(new ContentValues[values_list.size()]));

				// No row deleted, so I will insert a gap.
				final boolean insert_gap = rows_deleted == 1 && message_ids.contains(response.max_id)
						|| rows_deleted == 0 && response.max_id == -1 && !no_items_before;
				if (insert_gap) {
					final ContentValues values = new ContentValues();
					values.put(DirectMessages.IS_GAP, 1);
					final StringBuilder where = new StringBuilder();
					where.append(DirectMessages.ACCOUNT_ID + "=" + account_id);
					where.append(" AND " + DirectMessages.MESSAGE_ID + "=" + min_id);
					resolver.update(query_uri, values, where.toString(), null);
				}
				final int actual_items_inserted = values_list.size() - rows_deleted;
				if (actual_items_inserted > 0) {
					bundle.putInt(INTENT_KEY_ITEMS_INSERTED, actual_items_inserted);
				}
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			}
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

	private class StoreHomeTimelineTask extends StoreStatusesTask {

		private final boolean is_auto_refresh;

		public StoreHomeTimelineTask(List<ListResponse<twitter4j.Status>> result, boolean is_auto_refresh) {
			super(result, Statuses.CONTENT_URI);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			mStoreStatusesTaskId = -1;
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed));
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
					final Intent content_intent = new Intent(INTENT_ACTION_HOME);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_HOME);
					content_intent.putExtras(content_extras);
					mNotificationManager.notify(NOTIFICATION_ID_HOME_TIMELINE, buildNotification(message, R.drawable.ic_stat_tweet, content_intent, delete_intent));
				}
			}
			super.onPostExecute(response);
		}

	}

	private class StoreMentionsTask extends StoreStatusesTask {

		private final boolean is_auto_refresh;

		public StoreMentionsTask(List<ListResponse<twitter4j.Status>> result, boolean is_auto_refresh) {
			super(result, Mentions.CONTENT_URI);
			this.is_auto_refresh = is_auto_refresh;
		}

		@Override
		protected void onPostExecute(SingleResponse<Bundle> response) {
			mStoreMentionsTaskId = -1;
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed));
			if (succeed && is_auto_refresh
					&& mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_ENABLE_MENTIONS, false)) {
				mNewMentionsCount += response.data.getInt(INTENT_KEY_ITEMS_INSERTED);
				if (mNewMentionsCount > 0) {
					final String message = getResources().getQuantityString(R.plurals.Nmentions, mNewMentionsCount,
							mNewMentionsCount);
					final Intent delete_intent = new Intent(BROADCAST_NOTIFICATION_CLEARED);
					final Bundle delete_extras = new Bundle();
					delete_extras.putInt(INTENT_KEY_NOTIFICATION_ID, NOTIFICATION_ID_MENTIONS);
					delete_intent.putExtras(delete_extras);
					final Intent content_intent = new Intent(INTENT_ACTION_HOME);
					final Bundle content_extras = new Bundle();
					content_extras.putInt(INTENT_KEY_INITIAL_TAB, HomeActivity.TAB_POSITION_MENTIONS);
					content_intent.putExtras(content_extras);
					mNotificationManager.notify(NOTIFICATION_ID_MENTIONS, buildNotification(message, R.drawable.ic_stat_mention, content_intent, delete_intent));
				}
			}
			super.onPostExecute(response);
		}

	}
	
	private Notification buildNotification(String message, int icon, Intent content_intent, Intent delete_intent) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setTicker(message);
		builder.setContentTitle(getString(R.string.new_notifications));
		builder.setContentText(message);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(icon);
		builder.setDeleteIntent(PendingIntent.getBroadcast(this, 0, delete_intent, PendingIntent.FLAG_UPDATE_CURRENT));
		builder.setContentIntent(PendingIntent.getActivity(this, 0, content_intent, PendingIntent.FLAG_UPDATE_CURRENT));
		int defaults = 0;
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_HAVE_SOUND, false)) defaults |= Notification.DEFAULT_SOUND;
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_HAVE_VIBRATION, false)) defaults |= Notification.DEFAULT_VIBRATE;
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATIONS_HAVE_LIGHTS, false)) defaults |= Notification.DEFAULT_LIGHTS;
		builder.setDefaults(defaults);
		return builder.getNotification();
	}

	private class StoreReceivedDirectMessagesTask extends StoreDirectMessagesTask {

		private final boolean is_auto_refresh;

		public StoreReceivedDirectMessagesTask(ListResponse<DirectMessage> result, boolean is_auto_refresh) {
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
					final Intent content_intent = new Intent(INTENT_ACTION_DIRECT_MESSAGES);
					final Bundle content_extras = new Bundle(response.data);
					content_extras.putBoolean(INTENT_KEY_FROM_NOTIFICATION, true);
					content_intent.putExtras(content_extras);
					mNotificationManager.notify(NOTIFICATION_ID_DIRECT_MESSAGES, buildNotification(message, R.drawable.ic_stat_direct_message, content_intent, delete_intent));
				}
			}
			super.onPostExecute(response);
		}

	}

	private class StoreSentDirectMessagesTask extends StoreDirectMessagesTask {

		public StoreSentDirectMessagesTask(ListResponse<DirectMessage> result) {
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

	private class StoreStatusesTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final List<ListResponse<twitter4j.Status>> responses;
		private final Uri uri;

		public StoreStatusesTask(List<ListResponse<twitter4j.Status>> result, Uri uri) {
			super(TwidereService.this, mAsyncTaskManager);
			responses = result;
			this.uri = uri;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(Void... args) {
			final ContentResolver resolver = getContentResolver();
			boolean succeed = false;
			final Uri query_uri = buildQueryUri(uri, false);
			int total_items_inserted = 0;
			for (final ListResponse<twitter4j.Status> response : responses) {
				final long account_id = response.account_id;
				final ResponseList<twitter4j.Status> statuses = response.list;
				final Cursor cur = resolver.query(uri, new String[0], Statuses.ACCOUNT_ID + " = " + account_id, null,
						null);
				boolean no_items_before = false;
				if (cur != null) {
					no_items_before = cur.getCount() <= 0;
					cur.close();
				}
				if (statuses == null || statuses.size() <= 0) {
					continue;
				}
				final List<ContentValues> values_list = new ArrayList<ContentValues>(), cached_users_list = new ArrayList<ContentValues>();
				final List<Long> status_ids = new ArrayList<Long>(), retweet_ids = new ArrayList<Long>(), user_ids = new ArrayList<Long>();

				long min_id = -1;

				for (final twitter4j.Status status : statuses) {
					if (status == null) {
						continue;
					}
					final User user = status.getUser();
					final long user_id = user.getId();
					final long status_id = status.getId();
					final long retweet_id = status.getRetweetedStatus() != null ? status.getRetweetedStatus().getId()
							: -1;

					if (!user_ids.contains(user_id)) {
						user_ids.add(user_id);
						cached_users_list.add(makeCachedUserContentValues(user));
					}
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

				{
					resolver.delete(CachedUsers.CONTENT_URI,
							CachedUsers.USER_ID + " IN (" + ListUtils.buildString(user_ids, ',', true) + " )", null);
					resolver.bulkInsert(CachedUsers.CONTENT_URI,
							cached_users_list.toArray(new ContentValues[cached_users_list.size()]));
				}

				int rows_deleted = -1;

				// Delete all rows conflicting before new data inserted.
				{
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + " = " + account_id);
					where.append(" AND ");
					where.append("(");
					where.append(Statuses.STATUS_ID + " IN ( " + ListUtils.buildString(status_ids, ',', true) + " ) ");
					where.append(" OR ");
					where.append(Statuses.RETWEET_ID + " IN ( " + ListUtils.buildString(status_ids, ',', true) + " ) ");
					where.append(")");
					rows_deleted = resolver.delete(query_uri, where.toString(), null);
				}

				// Insert previously fetched items.
				resolver.bulkInsert(query_uri, values_list.toArray(new ContentValues[values_list.size()]));

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
					resolver.update(query_uri, values, where.toString(), null);
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
			if (response.data.getBoolean(INTENT_KEY_SUCCEED)) {
				notifyForUpdatedUri(TwidereService.this, uri);
			}
			super.onPostExecute(response);
		}

	}

	private class UpdateProfileImageTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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

	private class UpdateProfileTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

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

	private class UpdateStatusTask extends ManagedAsyncTask<Void, Void, List<SingleResponse<twitter4j.Status>>> {

		private long[] account_ids;
		private String content;
		private Location location;
		private Uri image_uri;
		private long in_reply_to;
		private boolean delete_image;

		public UpdateStatusTask(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to,
				boolean delete_image) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.content = content;
			this.location = location;
			this.image_uri = image_uri;
			this.in_reply_to = in_reply_to;
			this.delete_image = delete_image;
		}

		@Override
		protected List<SingleResponse<twitter4j.Status>> doInBackground(Void... params) {

			if (account_ids == null) return null;

			final List<SingleResponse<twitter4j.Status>> result = new ArrayList<SingleResponse<twitter4j.Status>>();

			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
				if (twitter != null) {
					try {
						final StatusUpdate status = new StatusUpdate(content);
						status.setInReplyToStatusId(in_reply_to);
						if (location != null) {
							status.setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
						}
						final String image_path = getImagePathFromUri(TwidereService.this, image_uri);
						if (image_path != null) {
							final File image_file = new File(image_path);
							if (image_file.exists()) {
								status.setMedia(image_file);
							}
						}
						result.add(new SingleResponse<twitter4j.Status>(account_id, twitter.updateStatus(status), null));
					} catch (final TwitterException e) {
						e.printStackTrace();
						result.add(new SingleResponse<twitter4j.Status>(account_id, null, e));
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<SingleResponse<twitter4j.Status>> result) {

			boolean succeed = false;
			Exception exception = null;
			final List<Long> failed_account_ids = new ArrayList<Long>();

			for (final SingleResponse<twitter4j.Status> response : result) {
				if (response.data != null) {
					succeed = true;
					break;
				} else {
					failed_account_ids.add(response.account_id);
					exception = response.exception;
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
				for (int i = 0; i < failed_account_ids.size(); i++) {
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
				final ContentResolver resolver = getContentResolver();
				resolver.insert(Drafts.CONTENT_URI, values);
			}
			super.onPostExecute(result);
		}

	}

}
