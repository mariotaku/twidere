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
import static org.mariotaku.twidere.provider.TweetStore.STATUSES_URIS;
import static org.mariotaku.twidere.util.ContentResolverUtils.bulkDelete;
import static org.mariotaku.twidere.util.Utils.appendQueryParameters;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getAllStatusesIds;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getImageUploadStatus;
import static org.mariotaku.twidere.util.Utils.getNewestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getStatusIdsInDatabase;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserName;
import static org.mariotaku.twidere.util.Utils.makeDirectMessageContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusContentValues;
import static org.mariotaku.twidere.util.Utils.makeTrendsContentValues;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.twitter.Extractor;
import com.twitter.Validator;

import edu.ucdavis.earlybird.ProfilingUtil;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatusUpdate;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.preference.HomeRefreshContentPreference;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Notifications;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.service.UpdateStatusService;
import org.mariotaku.twidere.util.ContentLengthInputStream.ReadListener;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.StatusUpdate;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncTwitterWrapper extends TwitterWrapper {

	private static AsyncTwitterWrapper sInstance;

	private final Context mContext;
	private final AsyncTaskManager mAsyncTaskManager;
	private final SharedPreferences mPreferences;
	private final NotificationManager mNotificationManager;
	private final MessagesManager mMessagesManager;
	private final ContentResolver mResolver;
	private final Resources mResources;

	private final boolean mLargeProfileImage;

	private int mGetHomeTimelineTaskId, mGetMentionsTaskId;
	private int mGetReceivedDirectMessagesTaskId, mGetSentDirectMessagesTaskId;
	private int mGetLocalTrendsTaskId;

	public AsyncTwitterWrapper(final Context context) {
		mContext = context;
		mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mAsyncTaskManager = app.getAsyncTaskManager();
		mMessagesManager = app.getMessagesManager();
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = context.getContentResolver();
		mResources = context.getResources();
		mLargeProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	public int addUserListMembers(final long account_id, final int list_id, final long... user_ids) {
		final AddUserListMembersTask task = new AddUserListMembersTask(account_id, list_id, user_ids, null);
		return mAsyncTaskManager.add(task, true);
	}

	public int addUserListMembers(final long account_id, final int list_id, final String... screen_names) {
		final AddUserListMembersTask task = new AddUserListMembersTask(account_id, list_id, null, screen_names);
		return mAsyncTaskManager.add(task, true);
	}

	public void clearNotification(final int id) {
		final Uri uri = Notifications.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
		mResolver.delete(uri, null, null);
	}

	public int createBlockAsync(final long account_id, final long user_id) {
		final CreateBlockTask task = new CreateBlockTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createFavoriteAsync(final long account_id, final long status_id) {
		final CreateFavoriteTask task = new CreateFavoriteTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createFriendship(final long account_id, final long user_id) {
		final CreateFriendshipTask task = new CreateFriendshipTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createMultiBlock(final long account_id, final long[] user_ids) {
		final CreateMultiBlockTask task = new CreateMultiBlockTask(account_id, user_ids);
		return mAsyncTaskManager.add(task, true);
	}

	public int createUserList(final long account_id, final String list_name, final boolean is_public,
			final String description) {
		final CreateUserListTask task = new CreateUserListTask(account_id, list_name, is_public, description);
		return mAsyncTaskManager.add(task, true);
	}

	public int createUserListSubscription(final long account_id, final int list_id) {
		final CreateUserListSubscriptionTask task = new CreateUserListSubscriptionTask(account_id, list_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int deleteUserListMembers(final long account_id, final int list_id, final long... user_ids) {
		final DeleteUserListMembersTask task = new DeleteUserListMembersTask(account_id, list_id, user_ids, null);
		return mAsyncTaskManager.add(task, true);
	}

	public int deleteUserListMembers(final long account_id, final int list_id, final String... screen_names) {
		final DeleteUserListMembersTask task = new DeleteUserListMembersTask(account_id, list_id, null, screen_names);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyBlock(final long account_id, final long user_id) {
		final DestroyBlockTask task = new DestroyBlockTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyDirectMessage(final long account_id, final long message_id) {
		final DestroyDirectMessageTask task = new DestroyDirectMessageTask(account_id, message_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyFavorite(final long account_id, final long status_id) {
		final DestroyFavoriteTask task = new DestroyFavoriteTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyFriendship(final long account_id, final long user_id) {
		final DestroyFriendshipTask task = new DestroyFriendshipTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyStatus(final long account_id, final long status_id) {
		final DestroyStatusTask task = new DestroyStatusTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyUserList(final long account_id, final int list_id) {
		final DestroyUserListTask task = new DestroyUserListTask(account_id, list_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyUserListSubscription(final long account_id, final int list_id) {
		final DestroyUserListSubscriptionTask task = new DestroyUserListSubscriptionTask(account_id, list_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int getHomeTimeline(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetHomeTimelineTaskId);
		final GetHomeTimelineTask task = new GetHomeTimelineTask(account_ids, max_ids, since_ids);
		return mGetHomeTimelineTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getLocalTrends(final long account_id, final int woeid) {
		mAsyncTaskManager.cancel(mGetLocalTrendsTaskId);
		final GetLocalTrendsTask task = new GetLocalTrendsTask(account_id, woeid);
		return mGetLocalTrendsTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getMentions(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetMentionsTaskId);
		final GetMentionsTask task = new GetMentionsTask(account_ids, max_ids, since_ids);
		return mGetMentionsTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getReceivedDirectMessages(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetReceivedDirectMessagesTaskId);
		final GetReceivedDirectMessagesTask task = new GetReceivedDirectMessagesTask(account_ids, max_ids, since_ids);
		return mGetReceivedDirectMessagesTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getSentDirectMessages(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetSentDirectMessagesTaskId);
		final GetSentDirectMessagesTask task = new GetSentDirectMessagesTask(account_ids, max_ids, since_ids);
		return mGetSentDirectMessagesTaskId = mAsyncTaskManager.add(task, true);
	}

	public boolean hasActivatedTask() {
		return mAsyncTaskManager.hasRunningTask();
	}

	public boolean isHomeTimelineRefreshing() {
		return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_HOME_TIMELINE)
				|| mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_STORE_HOME_TIMELINE);
	}

	public boolean isLocalTrendsRefreshing() {
		return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_TRENDS)
				|| mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_STORE_TRENDS);
	}

	public boolean isMentionsRefreshing() {
		return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_MENTIONS)
				|| mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_STORE_MENTIONS);
	}

	public boolean isReceivedDirectMessagesRefreshing() {
		return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_RECEIVED_DIRECT_MESSAGES)
				|| mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_STORE_RECEIVED_DIRECT_MESSAGES);
	}

	public boolean isSentDirectMessagesRefreshing() {
		return mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_GET_SENT_DIRECT_MESSAGES)
				|| mAsyncTaskManager.hasRunningTasksForTag(TASK_TAG_STORE_SENT_DIRECT_MESSAGES);
	}

	public int refreshAll() {
		final long[] account_ids = getActivatedAccountIds(mContext);
		return refreshAll(account_ids);
	}

	public int refreshAll(final long[] account_ids) {
		if (mPreferences.getBoolean(PREFERENCE_KEY_HOME_REFRESH_MENTIONS,
				HomeRefreshContentPreference.DEFAULT_ENABLE_MENTIONS)) {
			final long[] since_ids = getNewestStatusIdsFromDatabase(mContext, Mentions.CONTENT_URI, account_ids);
			getMentions(account_ids, null, since_ids);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_HOME_REFRESH_DIRECT_MESSAGES,
				HomeRefreshContentPreference.DEFAULT_ENABLE_DIRECT_MESSAGES)) {
			final long[] since_ids = getNewestMessageIdsFromDatabase(mContext, DirectMessages.Inbox.CONTENT_URI,
					account_ids);
			getReceivedDirectMessages(account_ids, null, since_ids);
			getSentDirectMessages(account_ids, null, null);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_HOME_REFRESH_TRENDS,
				HomeRefreshContentPreference.DEFAULT_ENABLE_TRENDS)) {
			final long account_id = getDefaultAccountId(mContext);
			final int woeid = mPreferences.getInt(PREFERENCE_KEY_LOCAL_TRENDS_WOEID, 1);
			getLocalTrends(account_id, woeid);
		}
		final long[] since_ids = getNewestStatusIdsFromDatabase(mContext, Statuses.CONTENT_URI, account_ids);
		return getHomeTimeline(account_ids, null, since_ids);
	}

	public int reportMultiSpam(final long account_id, final long[] user_ids) {
		final ReportMultiSpamTask task = new ReportMultiSpamTask(account_id, user_ids);
		return mAsyncTaskManager.add(task, true);
	}

	public int reportSpam(final long account_id, final long user_id) {
		final ReportSpamTask task = new ReportSpamTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int retweetStatus(final long account_id, final long status_id) {
		final RetweetStatusTask task = new RetweetStatusTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int sendDirectMessage(final long account_id, final String screen_name, final long user_id,
			final String message) {
		final SendDirectMessageTask task = new SendDirectMessageTask(account_id, screen_name, user_id, message);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateProfile(final long account_id, final String name, final String url, final String location,
			final String description) {
		final UpdateProfileTask task = new UpdateProfileTask(mContext, mAsyncTaskManager, account_id, name, url,
				location, description);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateProfileBannerImage(final long account_id, final Uri image_uri, final boolean delete_image) {
		final UpdateProfileBannerImageTask task = new UpdateProfileBannerImageTask(mContext, mAsyncTaskManager,
				account_id, image_uri, delete_image);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateProfileImage(final long account_id, final Uri image_uri, final boolean delete_image) {
		final UpdateProfileImageTask task = new UpdateProfileImageTask(mContext, mAsyncTaskManager, account_id,
				image_uri, delete_image);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateStatus(final long[] account_ids, final String content, final ParcelableLocation location,
			final Uri image_uri, final long in_reply_to, final boolean is_possibly_sensitive, final boolean delete_image) {
		// final UpdateStatusTask task = new UpdateStatusTask(account_ids,
		// content, location, image_uri, in_reply_to,
		// is_possibly_sensitive, delete_image);
		// return mAsyncTaskManager.add(task, true);
		final Intent intent = new Intent(mContext, UpdateStatusService.class);
		intent.putExtra(EXTRA_STATUS, new ParcelableStatusUpdate(account_ids, content, location, image_uri,
				in_reply_to, is_possibly_sensitive, delete_image));
		mContext.startService(intent);
		return 0;
	}

	public int updateUserListDetails(final long account_id, final int list_id, final boolean is_public,
			final String name, final String description) {
		final UpdateUserListDetailsTask task = new UpdateUserListDetailsTask(account_id, list_id, is_public, name,
				description);
		return mAsyncTaskManager.add(task, true);
	}

	private Notification buildNotification(final String title, final String message, final int icon,
			final Intent content_intent, final Intent delete_intent) {
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
		builder.setTicker(message);
		builder.setContentTitle(title);
		builder.setContentText(message);
		builder.setAutoCancel(true);
		builder.setWhen(System.currentTimeMillis());
		builder.setSmallIcon(icon);
		if (delete_intent != null) {
			builder.setDeleteIntent(PendingIntent.getBroadcast(mContext, 0, delete_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		if (content_intent != null) {
			content_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			builder.setContentIntent(PendingIntent.getActivity(mContext, 0, content_intent,
					PendingIntent.FLAG_UPDATE_CURRENT));
		}
		int defaults = 0;
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_SOUND, false)) {
			final Uri def_ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			final String path = mPreferences.getString(PREFERENCE_KEY_NOTIFICATION_RINGTONE, "");
			builder.setSound(isEmpty(path) ? def_ringtone : Uri.parse(path), Notification.STREAM_DEFAULT);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_VIBRATION, false)) {
			defaults |= Notification.DEFAULT_VIBRATE;
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION_HAVE_LIGHTS, false)) {
			final int color_def = mResources.getColor(android.R.color.holo_blue_dark);
			final int color = mPreferences.getInt(PREFERENCE_KEY_NOTIFICATION_LIGHT_COLOR, color_def);
			builder.setLights(color, 1000, 2000);
		}
		builder.setDefaults(defaults);
		return builder.build();
	}

	public static AsyncTwitterWrapper getInstance(final Context context) {
		if (sInstance != null) return sInstance;
		return sInstance = new AsyncTwitterWrapper(context);
	}

	public static class UpdateProfileBannerImageTask extends ManagedAsyncTask<Void, Void, SingleResponse<Boolean>> {

		private final long account_id;
		private final Uri image_uri;
		private final boolean delete_image;
		private final Context mContext;

		public UpdateProfileBannerImageTask(final Context context, final AsyncTaskManager manager,
				final long account_id, final Uri image_uri, final boolean delete_image) {
			super(context, manager);
			mContext = context;
			this.account_id = account_id;
			this.image_uri = image_uri;
			this.delete_image = delete_image;
		}

		@Override
		protected SingleResponse<Boolean> doInBackground(final Void... params) {
			return TwitterWrapper.updateProfileBannerImage(mContext, account_id, image_uri, delete_image);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Boolean> result) {
			if (result != null && result.data != null && result.data) {
				Utils.showOkMessage(mContext, R.string.profile_banner_image_updated, false);
			} else {
				Utils.showErrorMessage(mContext, R.string.updating_profile_banner_image, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_BANNER_UPDATED);
			intent.putExtra(EXTRA_USER_ID, account_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	public static class UpdateProfileImageTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUser>> {

		private final long account_id;
		private final Uri image_uri;
		private final boolean delete_image;
		private final Context context;

		public UpdateProfileImageTask(final Context context, final AsyncTaskManager manager, final long account_id,
				final Uri image_uri, final boolean delete_image) {
			super(context, manager);
			this.context = context;
			this.account_id = account_id;
			this.image_uri = image_uri;
			this.delete_image = delete_image;
		}

		@Override
		protected SingleResponse<ParcelableUser> doInBackground(final Void... params) {
			return TwitterWrapper.updateProfileImage(context, account_id, image_uri, delete_image);
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
			if (result != null && result.data != null) {
				Utils.showOkMessage(context, R.string.profile_image_updated, false);
			} else {
				Utils.showErrorMessage(context, R.string.updating_profile_image, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_UPDATED);
			intent.putExtra(EXTRA_USER_ID, account_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			context.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	public static class UpdateProfileTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUser>> {

		private final long account_id;
		private final String name, url, location, description;
		private final Context context;

		public UpdateProfileTask(final Context context, final AsyncTaskManager manager, final long account_id,
				final String name, final String url, final String location, final String description) {
			super(context, manager);
			this.context = context;
			this.account_id = account_id;
			this.name = name;
			this.url = url;
			this.location = location;
			this.description = description;
		}

		@Override
		protected SingleResponse<ParcelableUser> doInBackground(final Void... params) {
			return updateProfile(context, account_id, name, url, location, description);
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUser> result) {
			if (result != null && result.data != null) {
				Utils.showOkMessage(context, R.string.profile_updated, false);
			} else {
				Utils.showErrorMessage(context, context.getString(R.string.updating_profile), result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_IMAGE_UPDATED);
			intent.putExtra(EXTRA_USER_ID, account_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			context.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class AddUserListMembersTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUserList>> {

		private final long account_id;
		private final int list_id;
		private final long[] user_ids;
		private final String[] screen_names;

		public AddUserListMembersTask(final long account_id, final int list_id, final long[] user_ids,
				final String[] screen_names) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
			this.user_ids = user_ids;
			this.screen_names = screen_names;
		}

		@Override
		protected SingleResponse<ParcelableUserList> doInBackground(final Void... params) {
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final ParcelableUserList list;
					if (user_ids != null) {
						list = new ParcelableUserList(twitter.addUserListMembers(list_id, user_ids), account_id, false);
					} else if (screen_names != null) {
						list = new ParcelableUserList(twitter.addUserListMembers(list_id, screen_names), account_id,
								false);
					} else
						return SingleResponse.nullInstance();
					return SingleResponse.newInstance(list, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.id > 0;
			if (succeed) {
				final String message = mContext.getString(R.string.added_users_to_list, result.data.name);
				mMessagesManager.showOkMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.adding_member, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_MEMBERS_ADDED);
			intent.putExtra(EXTRA_USER_LIST, result.data);
			intent.putExtra(EXTRA_USER_IDS, user_ids);
			intent.putExtra(EXTRA_SCREEN_NAMES, screen_names);
			intent.putExtra(EXTRA_SUCCEED, succeed);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateBlockTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id, user_id;

		public CreateBlockTask(final long account_id, final long user_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.createBlock(user_id);
					for (final Uri uri : STATUSES_URIS) {
						final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.USER_ID
								+ " = " + user_id;
						mResolver.delete(uri, where, null);

					}
					// I bet you don't want to see this user in your auto
					// complete
					// list.
					final String where = CachedUsers.USER_ID + " = " + user_id;
					mResolver.delete(CachedUsers.CONTENT_URI, where, null);
					return SingleResponse.newInstance(user, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<User> result) {
			if (result != null && result.data != null && result.data.getId() > 0) {
				final String message = mContext.getString(R.string.blocked_user, getUserName(mContext, result.data));
				mMessagesManager.showInfoMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.blocking, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(EXTRA_USER_ID, user_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateFavoriteTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableStatus>> {

		private final long account_id, status_id;

		public CreateFavoriteTask(final long account_id, final long status_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<ParcelableStatus> doInBackground(final Void... params) {

			if (account_id < 0) return SingleResponse.nullInstance();

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.createFavorite(status_id);
					final ContentValues values = new ContentValues();
					values.put(Statuses.IS_FAVORITE, true);
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + " = " + account_id);
					where.append(" AND ");
					where.append("(");
					where.append(Statuses.STATUS_ID + " = " + status_id);
					where.append(" OR ");
					where.append(Statuses.RETWEET_ID + " = " + status_id);
					where.append(")");
					for (final Uri uri : TweetStore.STATUSES_URIS) {
						mResolver.update(uri, values, where.toString(), null);
					}
					return SingleResponse.dataOnly(new ParcelableStatus(status, account_id, false, mLargeProfileImage));
				} catch (final TwitterException e) {
					return SingleResponse.exceptionOnly(e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {

			if (result.data != null) {
				final Intent intent = new Intent(BROADCAST_FAVORITE_CHANGED);
				intent.putExtra(EXTRA_STATUS, result.data);
				intent.putExtra(EXTRA_FAVORITED, true);
				mContext.sendBroadcast(intent);
				mMessagesManager.showOkMessage(R.string.status_favorited, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.favoriting, result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	class CreateFriendshipTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id;
		private final long user_id;

		public CreateFriendshipTask(final long account_id, final long user_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.createFriendship(user_id);
					return SingleResponse.newInstance(user, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<User> result) {
			if (result != null && result.data != null) {
				final String message = mContext.getString(R.string.followed_user, getUserName(mContext, result.data));
				mMessagesManager.showOkMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.following, result.exception, false);
			}
			final Intent intent = new Intent(BROADCAST_FRIENDSHIP_CHANGED);
			intent.putExtra(EXTRA_USER_ID, user_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateMultiBlockTask extends ManagedAsyncTask<Void, Void, ListResponse<Long>> {

		private final long account_id;
		private final long[] user_ids;

		public CreateMultiBlockTask(final long account_id, final long[] user_ids) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_ids = user_ids;
		}

		@Override
		protected ListResponse<Long> doInBackground(final Void... params) {
			final List<Long> blocked_users = new ArrayList<Long>();
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				for (final long user_id : user_ids) {
					try {
						final User user = twitter.createBlock(user_id);
						if (user == null || user.getId() <= 0) {
							continue;
						}
						blocked_users.add(user.getId());
					} catch (final TwitterException e) {
						deleteCaches(blocked_users);
						return new ListResponse<Long>(null, e, null);
					}
				}
			}
			deleteCaches(blocked_users);
			return new ListResponse<Long>(blocked_users, null, null);
		}

		@Override
		protected void onPostExecute(final ListResponse<Long> result) {
			if (result.list != null) {
				mMessagesManager.showInfoMessage(R.string.users_blocked, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.blocking, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_MULTI_BLOCKSTATE_CHANGED);
			intent.putExtra(EXTRA_USER_ID, user_ids);
			intent.putExtra(EXTRA_SUCCEED, result.list != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

		private void deleteCaches(final List<Long> list) {
			for (final Uri uri : STATUSES_URIS) {
				bulkDelete(mResolver, uri, Statuses.USER_ID, list, Statuses.ACCOUNT_ID + " = " + account_id, false);
			}
			// I bet you don't want to see these users in your auto complete
			// list.
			bulkDelete(mResolver, CachedUsers.CONTENT_URI, CachedUsers.USER_ID, list, null, false);
		}
	}

	class CreateUserListSubscriptionTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUserList>> {

		private final long account_id;
		private final int list_id;

		public CreateUserListSubscriptionTask(final long account_id, final int list_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
		}

		@Override
		protected SingleResponse<ParcelableUserList> doInBackground(final Void... params) {
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final ParcelableUserList list = new ParcelableUserList(twitter.createUserListSubscription(list_id),
							account_id, false);
					return new SingleResponse<ParcelableUserList>(list, null);
				} catch (final TwitterException e) {
					return SingleResponse.exceptionOnly(e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
			final boolean succeed = result != null && result.data != null;
			if (succeed) {
				final String message = mContext.getString(R.string.subscribed_to_list, result.data.name);
				mMessagesManager.showOkMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.subscribing_to_list, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_SUBSCRIBED);
			intent.putExtra(EXTRA_USER_LIST, result.data);
			intent.putExtra(EXTRA_SUCCEED, succeed);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class CreateUserListTask extends ManagedAsyncTask<Void, Void, SingleResponse<UserList>> {

		private final long account_id;
		private final String list_name, description;
		private final boolean is_public;

		public CreateUserListTask(final long account_id, final String list_name, final boolean is_public,
				final String description) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_name = list_name;
			this.description = description;
			this.is_public = is_public;
		}

		@Override
		protected SingleResponse<UserList> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					if (list_name != null) {
						final UserList list = twitter.createUserList(list_name, is_public, description);
						return SingleResponse.newInstance(list, null);
					}
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<UserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.getId() > 0;
			if (succeed) {
				final String message = mContext.getString(R.string.created_list, result.data.getName());
				mMessagesManager.showOkMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.creating_list, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_CREATED);
			intent.putExtra(EXTRA_SUCCEED, succeed);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DeleteUserListMembersTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUserList>> {

		private final long account_id;
		private final int list_id;
		private final long[] user_ids;
		private final String[] screen_names;

		public DeleteUserListMembersTask(final long account_id, final int list_id, final long[] user_ids,
				final String[] screen_names) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
			this.user_ids = user_ids;
			this.screen_names = screen_names;
		}

		@Override
		protected SingleResponse<ParcelableUserList> doInBackground(final Void... params) {
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final ParcelableUserList list;
					if (user_ids != null) {
						list = new ParcelableUserList(twitter.deleteUserListMembers(list_id, user_ids), account_id,
								false);
					} else if (screen_names != null) {
						list = new ParcelableUserList(twitter.deleteUserListMembers(list_id, screen_names), account_id,
								false);
					} else
						return SingleResponse.nullInstance();
					return new SingleResponse<ParcelableUserList>(list, null);
				} catch (final TwitterException e) {
					return SingleResponse.exceptionOnly(e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
			final boolean succeed = result != null && result.data != null && result.data.id > 0;
			if (succeed) {
				final String message = mContext.getString(R.string.deleted_users_from_list, result.data.name);
				mMessagesManager.showInfoMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.deleting, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_MEMBERS_DELETED);
			intent.putExtra(EXTRA_USER_LIST, result.data);
			intent.putExtra(EXTRA_USER_IDS, user_ids);
			intent.putExtra(EXTRA_SCREEN_NAMES, screen_names);
			intent.putExtra(EXTRA_SUCCEED, succeed);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyBlockTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id;
		private final long user_id;

		public DestroyBlockTask(final long account_id, final long user_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.destroyBlock(user_id);
					return SingleResponse.newInstance(user, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<User> result) {
			if (result != null && result.data != null) {
				final String message = mContext.getString(R.string.unblocked_user, getUserName(mContext, result.data));
				mMessagesManager.showInfoMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.unblocking, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(EXTRA_USER_ID, user_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyDirectMessageTask extends ManagedAsyncTask<Void, Void, SingleResponse<DirectMessage>> {

		private final long message_id;
		private final long account_id;

		public DestroyDirectMessageTask(final long account_id, final long message_id) {
			super(mContext, mAsyncTaskManager);

			this.account_id = account_id;
			this.message_id = message_id;
		}

		@Override
		protected SingleResponse<DirectMessage> doInBackground(final Void... args) {
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter == null) return SingleResponse.nullInstance();
			try {
				final DirectMessage message = twitter.destroyDirectMessage(message_id);
				deleteMessages(message_id);
				return SingleResponse.newInstance(message, null);
			} catch (final TwitterException e) {
				if (e.getErrorCode() == 34) {
					deleteMessages(message_id);
				}
				return SingleResponse.newInstance(null, e);
			}
		}

		@Override
		protected void onPostExecute(final SingleResponse<DirectMessage> result) {
			super.onPostExecute(result);
			if (result == null) return;
			if (result.data != null && result.data.getId() > 0 || result.exception instanceof TwitterException
					&& ((TwitterException) result.exception).getErrorCode() == 34) {
				mMessagesManager.showInfoMessage(R.string.direct_message_deleted, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.deleting, result.exception, true);
			}
		}

		private void deleteMessages(final long message_id) {
			final String where = DirectMessages.MESSAGE_ID + " = " + message_id;
			mResolver.delete(DirectMessages.Inbox.CONTENT_URI, where, null);
			mResolver.delete(DirectMessages.Outbox.CONTENT_URI, where, null);
		}
	}

	class DestroyFavoriteTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableStatus>> {

		private final long account_id;

		private final long status_id;

		public DestroyFavoriteTask(final long account_id, final long status_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<ParcelableStatus> doInBackground(final Void... params) {
			if (account_id < 0) return SingleResponse.nullInstance();
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyFavorite(status_id);
					final ContentValues values = new ContentValues();
					values.put(Statuses.IS_FAVORITE, 0);
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + " = " + account_id);
					where.append(" AND ");
					where.append("(");
					where.append(Statuses.STATUS_ID + " = " + status_id);
					where.append(" OR ");
					where.append(Statuses.RETWEET_ID + " = " + status_id);
					where.append(")");
					for (final Uri uri : TweetStore.STATUSES_URIS) {
						mResolver.update(uri, values, where.toString(), null);
					}
					return new SingleResponse<ParcelableStatus>(new ParcelableStatus(status, account_id, false,
							mLargeProfileImage), null);
				} catch (final TwitterException e) {
					return SingleResponse.exceptionOnly(e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableStatus> result) {
			if (result.data != null) {
				final Intent intent = new Intent(BROADCAST_FAVORITE_CHANGED);
				intent.putExtra(EXTRA_STATUS, result.data);
				intent.putExtra(EXTRA_FAVORITED, false);
				mContext.sendBroadcast(intent);
				mMessagesManager.showInfoMessage(R.string.status_unfavorited, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.unfavoriting, result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	class DestroyFriendshipTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id;
		private final long user_id;

		public DestroyFriendshipTask(final long account_id, final long user_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.destroyFriendship(user_id);
					final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.USER_ID + " = "
							+ user_id;
					mResolver.delete(Statuses.CONTENT_URI, where, null);
					return SingleResponse.newInstance(user, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<User> result) {
			if (result != null && result.data != null) {
				final String message = mContext.getString(R.string.unfollowed_user, getUserName(mContext, result.data));
				mMessagesManager.showInfoMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.unfollowing, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_FRIENDSHIP_CHANGED);
			intent.putExtra(EXTRA_USER_ID, user_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyStatusTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private final long account_id;

		private final long status_id;

		public DestroyStatusTask(final long account_id, final long status_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyStatus(status_id);
					final ContentValues values = new ContentValues();
					values.put(Statuses.MY_RETWEET_ID, -1);
					for (final Uri uri : TweetStore.STATUSES_URIS) {
						mResolver.delete(uri, Statuses.STATUS_ID + " = " + status_id, null);
						mResolver.update(uri, values, Statuses.MY_RETWEET_ID + " = " + status_id, null);
					}
					return SingleResponse.newInstance(status, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<twitter4j.Status> result) {
			final Intent intent = new Intent(BROADCAST_STATUS_DESTROYED);
			if (result != null && result.data != null && result.data.getId() > 0) {
				intent.putExtra(EXTRA_STATUS_ID, status_id);
				intent.putExtra(EXTRA_SUCCEED, true);
				if (result.data.getRetweetedStatus() != null) {
					mMessagesManager.showInfoMessage(R.string.retweet_cancelled, false);
				} else {
					mMessagesManager.showInfoMessage(R.string.status_deleted, false);
				}
			} else {
				mMessagesManager.showErrorMessage(R.string.deleting, result.exception, true);
			}
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyUserListSubscriptionTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUserList>> {

		private final long account_id;
		private final int list_id;

		public DestroyUserListSubscriptionTask(final long account_id, final int list_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
		}

		@Override
		protected SingleResponse<ParcelableUserList> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final ParcelableUserList list = new ParcelableUserList(
							twitter.destroyUserListSubscription(list_id), account_id, false);
					return SingleResponse.newInstance(list, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
			final boolean succeed = result.data != null;
			if (succeed) {
				final String message = mContext.getString(R.string.unsubscribed_from_list, result.data.name);
				mMessagesManager.showOkMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.unsubscribing_from_list, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_UNSUBSCRIBED);
			intent.putExtra(EXTRA_USER_LIST, result.data);
			intent.putExtra(EXTRA_SUCCEED, succeed);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class DestroyUserListTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUserList>> {

		private final long account_id;
		private final int list_id;

		public DestroyUserListTask(final long account_id, final int list_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.list_id = list_id;
		}

		@Override
		protected SingleResponse<ParcelableUserList> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					if (list_id > 0) {
						final ParcelableUserList list = new ParcelableUserList(twitter.destroyUserList(list_id),
								account_id, false);
						return new SingleResponse<ParcelableUserList>(list, null);
					}
				} catch (final TwitterException e) {
					return SingleResponse.exceptionOnly(e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
			final boolean succeed = result.data != null;
			if (succeed) {
				final String message = mContext.getString(R.string.deleted_list, result.data.name);
				mMessagesManager.showInfoMessage(message, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.deleting, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_USER_LIST_DELETED);
			intent.putExtra(EXTRA_SUCCEED, succeed);
			intent.putExtra(EXTRA_USER_LIST, result.data);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	abstract class GetDirectMessagesTask extends ManagedAsyncTask<Void, Void, List<TwitterListResponse<DirectMessage>>> {

		private final long[] account_ids, max_ids, since_ids;

		public GetDirectMessagesTask(final long[] account_ids, final long[] max_ids, final long[] since_ids,
				final String tag) {
			super(mContext, mAsyncTaskManager, tag);
			this.account_ids = account_ids;
			this.max_ids = max_ids;
			this.since_ids = since_ids;
		}

		public abstract ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging)
				throws TwitterException;

		@Override
		protected List<TwitterListResponse<DirectMessage>> doInBackground(final Void... params) {

			final List<TwitterListResponse<DirectMessage>> result = new ArrayList<TwitterListResponse<DirectMessage>>();

			if (account_ids == null) return result;

			int idx = 0;
			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitterInstance(mContext, account_id, true);
				if (twitter != null) {
					try {
						final Paging paging = new Paging();
						paging.setCount(load_item_limit);
						long max_id = -1, since_id = -1;
						if (isMaxIdsValid() && max_ids[idx] > 0) {
							max_id = max_ids[idx];
							paging.setMaxId(max_id);
						}
						if (isSinceIdsValid() && since_ids[idx] > 0) {
							since_id = since_ids[idx];
							paging.setSinceId(since_id);
						}
						final ResponseList<DirectMessage> statuses = getDirectMessages(twitter, paging);

						if (statuses != null) {
							result.add(new TwitterListResponse<DirectMessage>(account_id, max_id, since_id,
									load_item_limit, statuses, null));
						}
					} catch (final TwitterException e) {
						result.add(new TwitterListResponse<DirectMessage>(account_id, -1, -1, load_item_limit, null, e));
					}
				}
				idx++;
			}
			return result;

		}

		@Override
		protected void onPostExecute(final List<TwitterListResponse<DirectMessage>> result) {
			super.onPostExecute(result);
			for (final TwitterListResponse<DirectMessage> response : result) {
				if (response.list == null) {
					mMessagesManager.showErrorMessage(R.string.refreshing_direct_messages, response.exception, true);
				}
			}
		}

		final boolean isMaxIdsValid() {
			return max_ids != null && max_ids.length == account_ids.length;
		}

		final boolean isSinceIdsValid() {
			return since_ids != null && since_ids.length == account_ids.length;
		}

	}

	class GetHomeTimelineTask extends GetStatusesTask {

		public GetHomeTimelineTask(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
			super(account_ids, max_ids, since_ids, TASK_TAG_GET_HOME_TIMELINE);
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(final Twitter twitter, final Paging paging)
				throws TwitterException {
			return twitter.getHomeTimeline(paging);
		}

		@Override
		protected void onPostExecute(final List<StatusListResponse> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreHomeTimelineTask(responses, shouldSetMinId(), !isMaxIdsValid()), true);
			mGetHomeTimelineTaskId = -1;
			for (final StatusListResponse response : responses) {
				if (response.list == null) {
					mMessagesManager.showErrorMessage(R.string.refreshing_home_timeline, response.exception, true);
					break;
				}
			}
		}

		@Override
		protected void onPreExecute() {
			final Intent intent = new Intent(BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING);
			mContext.sendBroadcast(intent);
			super.onPreExecute();
		}

	}

	class GetLocalTrendsTask extends GetTrendsTask {

		private final int woeid;

		public GetLocalTrendsTask(final long account_id, final int woeid) {
			super(account_id);
			this.woeid = woeid;
		}

		@Override
		public List<Trends> getTrends(final Twitter twitter) throws TwitterException {
			final ArrayList<Trends> trends_list = new ArrayList<Trends>();
			if (twitter != null) {
				trends_list.add(twitter.getLocationTrends(woeid));
			}
			return trends_list;
		}

		@Override
		protected void onPostExecute(final ListResponse<Trends> result) {
			mAsyncTaskManager.add(new StoreLocalTrendsTask(result), true);
			super.onPostExecute(result);

		}

	}

	class GetMentionsTask extends GetStatusesTask {

		public GetMentionsTask(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
			super(account_ids, max_ids, since_ids, TASK_TAG_GET_MENTIONS);
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(final Twitter twitter, final Paging paging)
				throws TwitterException {
			return twitter.getMentionsTimeline(paging);
		}

		@Override
		protected void onPostExecute(final List<StatusListResponse> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreMentionsTask(responses, shouldSetMinId(), !isMaxIdsValid()), true);
			mGetMentionsTaskId = -1;
			for (final StatusListResponse response : responses) {
				if (response.list == null) {
					mMessagesManager.showErrorMessage(R.string.refreshing_mentions, response.exception, true);
					break;
				}
			}
		}

		@Override
		protected void onPreExecute() {

			final Intent intent = new Intent(BROADCAST_RESCHEDULE_MENTIONS_REFRESHING);
			mContext.sendBroadcast(intent);
			super.onPreExecute();
		}

	}

	class GetReceivedDirectMessagesTask extends GetDirectMessagesTask {

		public GetReceivedDirectMessagesTask(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
			super(account_ids, max_ids, since_ids, TASK_TAG_GET_RECEIVED_DIRECT_MESSAGES);
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(final Twitter twitter, final Paging paging)
				throws TwitterException {
			return twitter.getDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(final List<TwitterListResponse<DirectMessage>> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreReceivedDirectMessagesTask(responses, !isMaxIdsValid()), true);
			mGetReceivedDirectMessagesTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			final Intent intent = new Intent(BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING);
			mContext.sendBroadcast(intent);
			super.onPreExecute();
		}

	}

	class GetSentDirectMessagesTask extends GetDirectMessagesTask {

		public GetSentDirectMessagesTask(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
			super(account_ids, max_ids, since_ids, TASK_TAG_GET_SENT_DIRECT_MESSAGES);
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(final Twitter twitter, final Paging paging)
				throws TwitterException {
			return twitter.getSentDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(final List<TwitterListResponse<DirectMessage>> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreSentDirectMessagesTask(responses, !isMaxIdsValid()), true);
			mGetSentDirectMessagesTaskId = -1;
		}

	}

	abstract class GetStatusesTask extends ManagedAsyncTask<Void, Void, List<StatusListResponse>> {

		private final long[] account_ids, max_ids, since_ids;

		public GetStatusesTask(final long[] account_ids, final long[] max_ids, final long[] since_ids, final String tag) {
			super(mContext, mAsyncTaskManager, tag);
			this.account_ids = account_ids;
			this.max_ids = max_ids;
			this.since_ids = since_ids;
		}

		public abstract ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging)
				throws TwitterException;

		@Override
		protected List<StatusListResponse> doInBackground(final Void... params) {

			final List<StatusListResponse> result = new ArrayList<StatusListResponse>();

			if (account_ids == null) return result;

			int idx = 0;
			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
					PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitterInstance(mContext, account_id, true);
				if (twitter != null) {
					try {
						final Paging paging = new Paging();
						paging.setCount(load_item_limit);
						long max_id = -1, since_id = -1;
						if (isMaxIdsValid() && max_ids[idx] > 0) {
							max_id = max_ids[idx];
							paging.setMaxId(max_id);
						}
						if (isSinceIdsValid() && since_ids[idx] > 0) {
							since_id = since_ids[idx];
							// paging.setSinceId(since_id);
						}
						final List<twitter4j.Status> statuses = truncateStatuses(getStatuses(twitter, paging), since_id);
						if (statuses != null) {
							result.add(new StatusListResponse(account_id, max_id, since_id, load_item_limit, statuses,
									null));
						}
					} catch (final TwitterException e) {
						result.add(new StatusListResponse(account_id, -1, -1, load_item_limit, null, e));
					}
				}
				idx++;
			}
			return result;
		}

		private List<twitter4j.Status> truncateStatuses(final List<twitter4j.Status> statuses, final long since_id) {
			final List<twitter4j.Status> result = new ArrayList<twitter4j.Status>();
			for (final twitter4j.Status status : statuses) {
				if (since_id > 0 && status.getId() <= since_id) {
					continue;
				}
				result.add(status);

			}
			return result;
		}

		final boolean isMaxIdsValid() {
			return max_ids != null && max_ids.length == account_ids.length;
		}

		final boolean isSinceIdsValid() {
			return since_ids != null && since_ids.length == account_ids.length;
		}

		final boolean shouldSetMinId() {
			return !isMaxIdsValid();
		}

	}

	abstract class GetTrendsTask extends ManagedAsyncTask<Void, Void, ListResponse<Trends>> {

		private final long account_id;

		public GetTrendsTask(final long account_id) {
			super(mContext, mAsyncTaskManager, TASK_TAG_GET_TRENDS);
			this.account_id = account_id;
		}

		public abstract List<Trends> getTrends(Twitter twitter) throws TwitterException;

		@Override
		protected ListResponse<Trends> doInBackground(final Void... params) {
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			final Bundle extras = new Bundle();
			extras.putLong(EXTRA_ACCOUNT_ID, account_id);
			if (twitter != null) {
				try {
					return new ListResponse<Trends>(getTrends(twitter), null, extras);
				} catch (final TwitterException e) {
					return new ListResponse<Trends>(null, e, extras);
				}
			}
			return new ListResponse<Trends>(null, null, extras);
		}

	}

	class ReportMultiSpamTask extends ManagedAsyncTask<Void, Void, ListResponse<Long>> {

		private final long account_id;
		private final long[] user_ids;

		public ReportMultiSpamTask(final long account_id, final long[] user_ids) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_ids = user_ids;
		}

		@Override
		protected ListResponse<Long> doInBackground(final Void... params) {

			final Bundle extras = new Bundle();
			extras.putLong(EXTRA_ACCOUNT_ID, account_id);
			final List<Long> reported_users = new ArrayList<Long>();
			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				for (final long user_id : user_ids) {
					try {
						final User user = twitter.reportSpam(user_id);
						if (user == null || user.getId() <= 0) {
							continue;
						}
						reported_users.add(user.getId());
					} catch (final TwitterException e) {
						return new ListResponse<Long>(null, e, extras);
					}
				}
			}
			return new ListResponse<Long>(reported_users, null, extras);
		}

		@Override
		protected void onPostExecute(final ListResponse<Long> result) {
			if (result != null) {
				final String user_id_where = ListUtils.toString(result.list, ',', false);
				for (final Uri uri : STATUSES_URIS) {
					final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.USER_ID
							+ " IN (" + user_id_where + ")";
					mResolver.delete(uri, where, null);
				}
				mMessagesManager.showInfoMessage(R.string.reported_users_for_spam, false);
			}
			final Intent intent = new Intent(BROADCAST_MULTI_BLOCKSTATE_CHANGED);
			intent.putExtra(EXTRA_USER_IDS, user_ids);
			intent.putExtra(EXTRA_ACCOUNT_ID, account_id);
			intent.putExtra(EXTRA_SUCCEED, result != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class ReportSpamTask extends ManagedAsyncTask<Void, Void, SingleResponse<User>> {

		private final long account_id;
		private final long user_id;

		public ReportSpamTask(final long account_id, final long user_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected SingleResponse<User> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.reportSpam(user_id);
					return SingleResponse.newInstance(user, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<User> result) {
			if (result != null && result.data != null && result.data.getId() > 0) {
				for (final Uri uri : STATUSES_URIS) {
					final String where = Statuses.ACCOUNT_ID + " = " + account_id + " AND " + Statuses.USER_ID + " = "
							+ user_id;
					mResolver.delete(uri, where, null);
				}
				mMessagesManager.showInfoMessage(R.string.reported_user_for_spam, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.reporting_for_spam, result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(EXTRA_USER_ID, user_id);
			intent.putExtra(EXTRA_SUCCEED, result != null && result.data != null);
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	class RetweetStatusTask extends ManagedAsyncTask<Void, Void, SingleResponse<twitter4j.Status>> {

		private final long account_id;

		private final long status_id;

		public RetweetStatusTask(final long account_id, final long status_id) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected SingleResponse<twitter4j.Status> doInBackground(final Void... params) {

			if (account_id < 0) return SingleResponse.nullInstance();

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.retweetStatus(status_id);
					return SingleResponse.newInstance(status, null);
				} catch (final TwitterException e) {
					return SingleResponse.newInstance(null, e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<twitter4j.Status> result) {

			if (result.data != null && result.data.getId() > 0) {
				final ContentValues values = new ContentValues();
				values.put(Statuses.MY_RETWEET_ID, result.data.getId());
				final String where = Statuses.STATUS_ID + " = " + status_id + " OR " + Statuses.RETWEET_ID + " = "
						+ status_id;
				for (final Uri uri : STATUSES_URIS) {
					mResolver.update(uri, values, where, null);
				}
				final Intent intent = new Intent(BROADCAST_RETWEET_CHANGED);
				intent.putExtra(EXTRA_STATUS_ID, status_id);
				intent.putExtra(EXTRA_RETWEETED, true);
				mContext.sendBroadcast(intent);
				mMessagesManager.showOkMessage(R.string.status_retweeted, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.retweeting, result.exception, true);
			}

			super.onPostExecute(result);
		}

	}

	class SendDirectMessageTask extends ManagedAsyncTask<Void, Void, SingleResponse<DirectMessage>> {

		private final long user_id;
		private final String screen_name;
		private final String message;
		private final long account_id;

		public SendDirectMessageTask(final long account_id, final String screen_name, final long user_id,
				final String message) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
			this.screen_name = screen_name;
			this.message = message;
		}

		@Override
		protected SingleResponse<DirectMessage> doInBackground(final Void... args) {
			final Twitter twitter = getTwitterInstance(mContext, account_id, true, true);
			if (twitter == null) return SingleResponse.nullInstance();
			try {
				if (user_id > 0)
					return SingleResponse.newInstance(twitter.sendDirectMessage(user_id, message), null);
				else if (screen_name != null)
					return SingleResponse.newInstance(twitter.sendDirectMessage(screen_name, message), null);
			} catch (final TwitterException e) {
				return SingleResponse.newInstance(null, e);
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<DirectMessage> result) {
			super.onPostExecute(result);
			if (result.data != null && result.data.getId() > 0) {
				final ContentValues values = makeDirectMessageContentValues(result.data, account_id, true,
						mLargeProfileImage);
				final String delete_where = DirectMessages.ACCOUNT_ID + " = " + account_id + " AND "
						+ DirectMessages.MESSAGE_ID + " = " + result.data.getId();
				mResolver.delete(DirectMessages.Outbox.CONTENT_URI, delete_where, null);
				mResolver.insert(DirectMessages.Outbox.CONTENT_URI, values);
				mMessagesManager.showOkMessage(R.string.direct_message_sent, false);
			} else {
				mMessagesManager.showErrorMessage(R.string.sending_direct_message, result.exception, true);
			}
		}

	}

	abstract class StoreDirectMessagesTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final List<TwitterListResponse<DirectMessage>> responses;
		private final Uri uri;

		public StoreDirectMessagesTask(final List<TwitterListResponse<DirectMessage>> result, final Uri uri,
				final boolean notify, final String tag) {
			super(mContext, mAsyncTaskManager, tag);
			responses = result;
			this.uri = uri.buildUpon().appendQueryParameter(QUERY_PARAM_NOTIFY, String.valueOf(notify)).build();
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(final Void... args) {

			boolean succeed = false;
			for (final TwitterListResponse<DirectMessage> response : responses) {
				final long account_id = response.account_id;
				final List<DirectMessage> messages = response.list;
				if (messages != null) {
					final List<ContentValues> values_list = new ArrayList<ContentValues>();
					final List<Long> message_ids = new ArrayList<Long>();

					for (final DirectMessage message : messages) {
						if (message == null || message.getId() <= 0) {
							continue;
						}
						message_ids.add(message.getId());
						values_list.add(makeDirectMessageContentValues(message, account_id, isOutgoing(),
								mLargeProfileImage));
					}

					// Delete all rows conflicting before new data inserted.
					{
						final StringBuilder where = new StringBuilder();
						where.append(DirectMessages.ACCOUNT_ID + " = " + account_id);
						where.append(" AND ");
						where.append(DirectMessages.MESSAGE_ID + " IN ( " + ListUtils.toString(message_ids, ',', true)
								+ " ) ");
						final Uri delete_uri = appendQueryParameters(uri, new NameValuePairImpl(QUERY_PARAM_NOTIFY,
								false));
						mResolver.delete(delete_uri, where.toString(), null);
					}

					// Insert previously fetched items.
					final Uri insert_uri = appendQueryParameters(uri, new NameValuePairImpl(QUERY_PARAM_NOTIFY, false));
					mResolver.bulkInsert(insert_uri, values_list.toArray(new ContentValues[values_list.size()]));

				}
				succeed = true;
			}
			final Bundle bundle = new Bundle();
			bundle.putBoolean(EXTRA_SUCCEED, succeed);
			return SingleResponse.newInstance(bundle, null);
		}

		abstract boolean isOutgoing();

	}

	class StoreHomeTimelineTask extends StoreStatusesTask {

		public StoreHomeTimelineTask(final List<StatusListResponse> result, final boolean should_set_min_id,
				final boolean notify) {
			super(result, Statuses.CONTENT_URI, should_set_min_id, notify, TASK_TAG_STORE_HOME_TIMELINE);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(EXTRA_SUCCEED);
			final Bundle extras = new Bundle();
			extras.putBoolean(EXTRA_SUCCEED, succeed);
			if (shouldSetMinId()) {
				final long min_id = response != null && response.data != null ? response.data.getLong(EXTRA_MIN_ID, -1)
						: -1;
				extras.putLong(EXTRA_MIN_ID, min_id);
			}
			mContext.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtras(extras));
			super.onPostExecute(response);
		}

	}

	class StoreLocalTrendsTask extends StoreTrendsTask {

		public StoreLocalTrendsTask(final ListResponse<Trends> result) {
			super(result, CachedTrends.Local.CONTENT_URI);
		}

	}

	class StoreMentionsTask extends StoreStatusesTask {

		public StoreMentionsTask(final List<StatusListResponse> result, final boolean should_set_min_id,
				final boolean notify) {
			super(result, Mentions.CONTENT_URI, should_set_min_id, notify, TASK_TAG_STORE_MENTIONS);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(EXTRA_SUCCEED);
			final Bundle extras = new Bundle();
			extras.putBoolean(EXTRA_SUCCEED, succeed);
			if (shouldSetMinId()) {
				final long min_id = response != null && response.data != null ? response.data.getLong(EXTRA_MIN_ID, -1)
						: -1;
				extras.putLong(EXTRA_MIN_ID, min_id);
			}
			mContext.sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED).putExtras(extras));
			super.onPostExecute(response);
		}

	}

	class StoreReceivedDirectMessagesTask extends StoreDirectMessagesTask {

		public StoreReceivedDirectMessagesTask(final List<TwitterListResponse<DirectMessage>> result,
				final boolean notify) {
			super(result, DirectMessages.Inbox.CONTENT_URI, notify, TASK_TAG_STORE_RECEIVED_DIRECT_MESSAGES);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(EXTRA_SUCCEED);
			mContext.sendBroadcast(new Intent(BROADCAST_RECEIVED_DIRECT_MESSAGES_REFRESHED).putExtra(EXTRA_SUCCEED,
					succeed));
			super.onPostExecute(response);
		}

		@Override
		boolean isOutgoing() {
			return false;
		}

	}

	class StoreSentDirectMessagesTask extends StoreDirectMessagesTask {

		public StoreSentDirectMessagesTask(final List<TwitterListResponse<DirectMessage>> result, final boolean notify) {
			super(result, DirectMessages.Outbox.CONTENT_URI, notify, TASK_TAG_STORE_SENT_DIRECT_MESSAGES);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(EXTRA_SUCCEED);
			mContext.sendBroadcast(new Intent(BROADCAST_SENT_DIRECT_MESSAGES_REFRESHED)
					.putExtra(EXTRA_SUCCEED, succeed));
			super.onPostExecute(response);
		}

		@Override
		boolean isOutgoing() {
			return true;
		}

	}

	abstract class StoreStatusesTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final List<StatusListResponse> responses;
		private final Uri uri;
		private final boolean should_set_min_id;
		private final ArrayList<ContentValues> all_statuses = new ArrayList<ContentValues>();

		public StoreStatusesTask(final List<StatusListResponse> result, final Uri uri, final boolean should_set_min_id,
				final boolean notify, final String tag) {
			super(mContext, mAsyncTaskManager, tag);
			responses = result;
			this.should_set_min_id = should_set_min_id;
			this.uri = uri.buildUpon().appendQueryParameter(QUERY_PARAM_NOTIFY, String.valueOf(notify)).build();
		}

		public boolean shouldSetMinId() {
			return should_set_min_id;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(final Void... args) {
			boolean succeed = false;
			final ArrayList<Long> newly_inserted_ids = new ArrayList<Long>();
			for (final StatusListResponse response : responses) {
				final long account_id = response.account_id;
				final List<twitter4j.Status> statuses = response.list;
				if (statuses == null || statuses.size() <= 0) {
					continue;
				}
				final ArrayList<Long> ids_in_db = getStatusIdsInDatabase(mContext, uri, account_id);
				final boolean no_items_before = ids_in_db.size() <= 0;
				final List<ContentValues> values_list = new ArrayList<ContentValues>();
				final List<Long> status_ids = new ArrayList<Long>(), retweet_ids = new ArrayList<Long>();
				for (final twitter4j.Status status : statuses) {
					if (status == null) {
						continue;
					}
					final long status_id = status.getId();
					final long retweet_id = status.getRetweetedStatus() != null ? status.getRetweetedStatus().getId()
							: -1;

					status_ids.add(status_id);

					if ((retweet_id <= 0 || !retweet_ids.contains(retweet_id)) && !retweet_ids.contains(status_id)) {
						if (retweet_id > 0) {
							retweet_ids.add(retweet_id);
						}
						values_list.add(makeStatusContentValues(status, account_id, mLargeProfileImage));
					}

				}

				// Delete all rows conflicting before new data inserted.

				final ArrayList<Long> account_newly_inserted = new ArrayList<Long>();
				account_newly_inserted.addAll(status_ids);
				account_newly_inserted.removeAll(ids_in_db);
				newly_inserted_ids.addAll(account_newly_inserted);
				final StringBuilder delete_where = new StringBuilder();
				final String ids_string = ListUtils.toString(status_ids, ',', true);
				delete_where.append(Statuses.ACCOUNT_ID + " = " + account_id);
				delete_where.append(" AND ");
				delete_where.append("(");
				delete_where.append(Statuses.STATUS_ID + " IN ( " + ids_string + " ) ");
				delete_where.append(" OR ");
				delete_where.append(Statuses.RETWEET_ID + " IN ( " + ids_string + " ) ");
				delete_where.append(")");
				final Uri delete_uri = appendQueryParameters(uri, new NameValuePairImpl(QUERY_PARAM_NOTIFY, false));
				final int rows_deleted = mResolver.delete(delete_uri, delete_where.toString(), null);
				// UCD
				final String UCD_new_status_ids = ListUtils.toString(account_newly_inserted, ',', true);
				ProfilingUtil.profile(mContext, account_id, "Download tweets, " + UCD_new_status_ids);
				all_statuses.addAll(values_list);
				// Insert previously fetched items.
				final Uri insert_query = appendQueryParameters(uri, new NameValuePairImpl(QUERY_PARAM_NEW_ITEMS_COUNT,
						newly_inserted_ids.size() - rows_deleted), new NameValuePairImpl(QUERY_PARAM_NOTIFY, false));
				mResolver.bulkInsert(insert_query, values_list.toArray(new ContentValues[values_list.size()]));

				// Insert a gap.
				// TODO make sure it will not have bugs.
				final long min_id = status_ids.size() > 0 ? Collections.min(status_ids) : -1;
				final boolean insert_gap = min_id > 0 && response.load_item_limit <= response.list.size()
						&& !no_items_before;
				if (insert_gap) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.IS_GAP, 1);
					final StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + " = " + account_id);
					where.append(" AND " + Statuses.STATUS_ID + " = " + min_id);
					final Uri update_uri = appendQueryParameters(uri, new NameValuePairImpl(QUERY_PARAM_NOTIFY, false));
					mResolver.update(update_uri, values, where.toString(), null);
					// Ignore gaps
					newly_inserted_ids.remove(min_id);
				}
				succeed = true;
			}
			final Bundle bundle = new Bundle();
			bundle.putBoolean(EXTRA_SUCCEED, succeed);
			getAllStatusesIds(mContext, uri);
			if (should_set_min_id && newly_inserted_ids.size() > 0) {
				bundle.putLong(EXTRA_MIN_ID, Collections.min(newly_inserted_ids));
			}
			return SingleResponse.newInstance(bundle, null);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			final StatusListResponse[] array = new StatusListResponse[responses.size()];
			new CacheUsersStatusesTask(mContext, responses.toArray(array)).execute();
		}

	}

	class StoreTrendsTask extends ManagedAsyncTask<Void, Void, SingleResponse<Bundle>> {

		private final ListResponse<Trends> response;
		private final Uri uri;

		public StoreTrendsTask(final ListResponse<Trends> result, final Uri uri) {
			super(mContext, mAsyncTaskManager, TASK_TAG_STORE_TRENDS);
			response = result;
			this.uri = uri;
		}

		@Override
		protected SingleResponse<Bundle> doInBackground(final Void... args) {
			final Bundle bundle = new Bundle();
			if (response != null) {

				final List<Trends> messages = response.list;
				final ArrayList<String> hashtags = new ArrayList<String>();
				final ArrayList<ContentValues> hashtag_values = new ArrayList<ContentValues>();
				if (messages != null && messages.size() > 0) {
					final ContentValues[] values_array = makeTrendsContentValues(messages);
					for (final ContentValues values : values_array) {
						final String hashtag = values.getAsString(CachedTrends.NAME).replaceFirst("#", "");
						if (hashtags.contains(hashtag)) {
							continue;
						}
						hashtags.add(hashtag);
						final ContentValues hashtag_value = new ContentValues();
						hashtag_value.put(CachedHashtags.NAME, hashtag);
						hashtag_values.add(hashtag_value);
					}
					mResolver.delete(uri, null, null);
					mResolver.bulkInsert(uri, values_array);
					mResolver.delete(CachedHashtags.CONTENT_URI,
							CachedHashtags.NAME + " IN (" + ListUtils.toStringForSQL(hashtags) + ")",
							hashtags.toArray(new String[hashtags.size()]));
					mResolver.bulkInsert(CachedHashtags.CONTENT_URI,
							hashtag_values.toArray(new ContentValues[hashtag_values.size()]));
					bundle.putBoolean(EXTRA_SUCCEED, true);
				}
			}
			return new SingleResponse<Bundle>(bundle, null);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			if (response != null && response.data != null && response.data.getBoolean(EXTRA_SUCCEED)) {
				final Intent intent = new Intent(BROADCAST_TRENDS_UPDATED);
				intent.putExtra(EXTRA_SUCCEED, true);
				mContext.sendBroadcast(intent);
			}
			super.onPostExecute(response);

		}

	}

	class UpdateStatusTask extends ManagedAsyncTask<Void, Void, List<SingleResponse<ParcelableStatus>>> {

		private final ImageUploaderInterface uploader;
		private final TweetShortenerInterface shortener;

		private final Validator validator = new Validator();
		private final long[] account_ids;
		private final String content;

		private final ParcelableLocation location;
		private final Uri image_uri;
		private final long in_reply_to;
		private final boolean use_uploader, use_shortener, is_possibly_sensitive, delete_image;

		public UpdateStatusTask(final long[] account_ids, final String content, final ParcelableLocation location,
				final Uri image_uri, final long in_reply_to, final boolean is_possibly_sensitive,
				final boolean delete_image) {
			super(mContext, mAsyncTaskManager);
			final String uploader_component = mPreferences.getString(PREFERENCE_KEY_IMAGE_UPLOADER, null);
			final String shortener_component = mPreferences.getString(PREFERENCE_KEY_TWEET_SHORTENER, null);
			use_uploader = !isEmpty(uploader_component);
			final TwidereApplication app = TwidereApplication.getInstance(mContext);
			uploader = use_uploader ? ImageUploaderInterface.getInstance(app, uploader_component) : null;
			use_shortener = !isEmpty(shortener_component);
			shortener = use_shortener ? TweetShortenerInterface.getInstance(app, shortener_component) : null;
			this.account_ids = account_ids != null ? account_ids : new long[0];
			this.content = content;
			this.location = location;
			this.image_uri = image_uri;
			this.in_reply_to = in_reply_to;
			this.is_possibly_sensitive = is_possibly_sensitive;
			this.delete_image = delete_image;
		}

		@Override
		protected List<SingleResponse<ParcelableStatus>> doInBackground(final Void... params) {

			final Extractor extractor = new Extractor();
			final ArrayList<ContentValues> hashtag_values = new ArrayList<ContentValues>();
			final List<String> hashtags = extractor.extractHashtags(content);
			for (final String hashtag : hashtags) {
				final ContentValues values = new ContentValues();
				values.put(CachedHashtags.NAME, hashtag);
				hashtag_values.add(values);
			}
			mResolver.bulkInsert(CachedHashtags.CONTENT_URI,
					hashtag_values.toArray(new ContentValues[hashtag_values.size()]));

			final List<SingleResponse<ParcelableStatus>> results = new ArrayList<SingleResponse<ParcelableStatus>>();

			if (account_ids.length == 0) return Collections.emptyList();

			try {
				if (use_uploader && uploader == null) throw new ImageUploaderNotFoundException();
				if (use_shortener && shortener == null) throw new TweetShortenerNotFoundException();

				final String image_path = getImagePathFromUri(mContext, image_uri);
				final File image_file = image_path != null ? new File(image_path) : null;

				final Uri upload_result_uri;
				try {
					if (uploader != null) {
						uploader.waitForService();
					}
					upload_result_uri = image_file != null && image_file.exists() && uploader != null ? uploader
							.upload(Uri.fromFile(image_file), content) : null;
				} catch (final Exception e) {
					throw new ImageUploadException();
				}
				if (use_uploader && image_file != null && image_file.exists() && upload_result_uri == null)
					throw new ImageUploadException();

				final String unshortened_content = use_uploader && upload_result_uri != null ? getImageUploadStatus(
						mContext, upload_result_uri.toString(), content) : content;

				final boolean should_shorten = validator.getTweetLength(unshortened_content) > Validator.MAX_TWEET_LENGTH;
				final String screen_name = getAccountScreenName(mContext, account_ids[0]);
				final String shortened_content;
				try {
					if (shortener != null) {
						shortener.waitForService();
					}
					shortened_content = should_shorten && use_shortener ? shortener.shorten(unshortened_content,
							screen_name, in_reply_to) : null;
				} catch (final Exception e) {
					throw new TweetShortenException();
				}

				if (should_shorten) {
					if (!use_shortener)
						throw new StatusTooLongException();
					else if (unshortened_content == null) throw new TweetShortenException();
				}

				final StatusUpdate status = new StatusUpdate(should_shorten && use_shortener ? shortened_content
						: unshortened_content);
				status.setInReplyToStatusId(in_reply_to);
				if (location != null) {
					status.setLocation(ParcelableLocation.toGeoLocation(location));
				}
				if (!use_uploader && image_file != null && image_file.exists()) {
					try {
						final ContentLengthInputStream is = new ContentLengthInputStream(image_file);
						is.setReadListener(new ReadListener() {

							int percent;

							@Override
							public void onRead(final int length, final int available) {
								final int percent = length > 0 ? (length - available) * 100 / length : 0;
								if (this.percent != percent) {
									Log.d(LOGTAG, "onRead, " + percent + "%");
								}
								this.percent = percent;
							}
						});
						status.setMedia(image_file.getAbsolutePath(), is);
					} catch (final FileNotFoundException e) {
						status.setMedia(image_file);
					}
				}
				status.setPossiblySensitive(is_possibly_sensitive);

				for (final long account_id : account_ids) {
					final Twitter twitter = getTwitterInstance(mContext, account_id, false, true);
					if (twitter == null) {
						results.add(new SingleResponse<ParcelableStatus>(null, new NullPointerException()));
						continue;
					}
					try {
						final ParcelableStatus result = new ParcelableStatus(twitter.updateStatus(status), account_id,
								false, false);
						results.add(new SingleResponse<ParcelableStatus>(result, null));
					} catch (final TwitterException e) {
						e.printStackTrace();
						final SingleResponse<ParcelableStatus> response = SingleResponse.exceptionOnly(e);
						results.add(response);
					}
				}
			} catch (final UpdateStatusException e) {
				final SingleResponse<ParcelableStatus> response = SingleResponse.exceptionOnly(e);
				results.add(response);
			}
			return results;
		}

		@Override
		protected void onCancelled() {
			saveDrafts(ListUtils.fromArray(account_ids));
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(final List<SingleResponse<ParcelableStatus>> result) {

			boolean failed = false;
			Exception exception = null;
			final List<Long> failed_account_ids = ListUtils.fromArray(account_ids);

			for (final SingleResponse<ParcelableStatus> response : result) {
				if (response.data == null) {
					failed = true;
					if (exception == null) {
						exception = response.exception;
					}
				} else if (response.data.account_id > 0) {
					failed_account_ids.remove(response.data.account_id);
				}
			}
			if (result.isEmpty()) {
				saveDrafts(failed_account_ids);
				mMessagesManager.showErrorMessage(R.string.updating_status,
						mContext.getString(R.string.no_account_selected), false);
			} else if (failed) {
				// If the status is a duplicate, there's no need to save it to
				// drafts.
				if (exception instanceof TwitterException
						&& ((TwitterException) exception).getErrorCode() == TwitterErrorCodes.STATUS_IS_DUPLICATE) {
					mMessagesManager.showErrorMessage(mContext.getString(R.string.status_is_duplicate), false);
				} else {
					saveDrafts(failed_account_ids);
					mMessagesManager.showErrorMessage(R.string.updating_status, exception, true);
				}
			} else {
				mMessagesManager.showOkMessage(R.string.status_updated, false);
				if (image_uri != null && delete_image) {
					final String path = getImagePathFromUri(mContext, image_uri);
					if (path != null) {
						new File(path).delete();
					}
				}
			}
			super.onPostExecute(result);
			if (mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_AFTER_TWEET, false)) {
				refreshAll();
			}
		}

		private void saveDrafts(final List<Long> account_ids) {
			final ContentValues values = new ContentValues();
			values.put(Drafts.ACCOUNT_IDS, ListUtils.toString(account_ids, ';', false));
			values.put(Drafts.IN_REPLY_TO_STATUS_ID, in_reply_to);
			values.put(Drafts.TEXT, content);
			if (image_uri != null) {
				final int image_type = delete_image ? ATTACHED_IMAGE_TYPE_PHOTO : ATTACHED_IMAGE_TYPE_IMAGE;
				values.put(Drafts.ATTACHED_IMAGE_TYPE, image_type);
				values.put(Drafts.IMAGE_URI, ParseUtils.parseString(image_uri));
			}
			mResolver.insert(Drafts.CONTENT_URI, values);
			final String title = mContext.getString(R.string.tweet_not_sent);
			final String message = mContext.getString(R.string.tweet_not_sent_summary);
			final Intent intent = new Intent(INTENT_ACTION_DRAFTS);
			final Notification notification = buildNotification(title, message, R.drawable.ic_stat_twitter, intent,
					null);
			mNotificationManager.notify(NOTIFICATION_ID_DRAFTS, notification);
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

			public UpdateStatusException(final int message) {
				super(mContext.getString(message));
			}
		}
	}

	class UpdateUserListDetailsTask extends ManagedAsyncTask<Void, Void, SingleResponse<ParcelableUserList>> {

		private final long account_id;

		private final int list_id;

		private final boolean is_public;
		private final String name, description;

		public UpdateUserListDetailsTask(final long account_id, final int list_id, final boolean is_public,
				final String name, final String description) {
			super(mContext, mAsyncTaskManager);
			this.account_id = account_id;
			this.name = name;
			this.list_id = list_id;
			this.is_public = is_public;
			this.description = description;
		}

		@Override
		protected SingleResponse<ParcelableUserList> doInBackground(final Void... params) {

			final Twitter twitter = getTwitterInstance(mContext, account_id, false);
			if (twitter != null) {
				try {
					final UserList list = twitter.updateUserList(list_id, name, is_public, description);
					return new SingleResponse<ParcelableUserList>(new ParcelableUserList(list, account_id, false), null);
				} catch (final TwitterException e) {
					return SingleResponse.exceptionOnly(e);
				}
			}
			return SingleResponse.nullInstance();
		}

		@Override
		protected void onPostExecute(final SingleResponse<ParcelableUserList> result) {
			final Intent intent = new Intent(BROADCAST_USER_LIST_DETAILS_UPDATED);
			intent.putExtra(EXTRA_LIST_ID, list_id);
			if (result.data != null && result.data.id > 0) {
				final String message = mContext.getString(R.string.updated_list_details, result.data.name);
				mMessagesManager.showOkMessage(message, false);
				intent.putExtra(EXTRA_SUCCEED, true);
			} else {
				mMessagesManager.showErrorMessage(R.string.updating_details, result.exception, true);
			}
			mContext.sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

}
