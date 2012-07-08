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
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getRetweetId;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.makeDirectMessageContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusContentValues;
import static org.mariotaku.twidere.util.Utils.notifyForUpdatedUri;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.ITwidereService;
import org.mariotaku.twidere.R;
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
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.widget.Toast;

public class TwidereService extends Service implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);

	private AsyncTaskManager mAsyncTaskManager;

	private int mGetHomeTimelineTaskId, mGetMentionsTaskId;

	private boolean mStoreStatusesFinished = true, mStoreMentionsFinished = true;

	private SharedPreferences mPreferences;

	private int mGetReceivedDirectMessagesTaskId;

	private int mGetSentDirectMessagesTaskId;

	private boolean mStoreReceivedDirectMessagesFinished;

	private boolean mStoreSentDirectMessagesFinished;

	public int cancelRetweet(long account_id, long status_id) {
		final CancelRetweetTask task = new CancelRetweetTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
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
		mAsyncTaskManager.cancel(mGetHomeTimelineTaskId);
		final GetHomeTimelineTask task = new GetHomeTimelineTask(account_ids, max_ids);
		return mGetHomeTimelineTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getMentions(long[] account_ids, long[] max_ids) {
		mAsyncTaskManager.cancel(mGetMentionsTaskId);
		final GetMentionsTask task = new GetMentionsTask(account_ids, max_ids);
		return mGetMentionsTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getReceivedDirectMessages(long account_id, long max_id) {
		final GetReceivedDirectMessagesTask task = new GetReceivedDirectMessagesTask(account_id, max_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int getSentDirectMessages(long account_id, long max_id) {
		final GetSentDirectMessagesTask task = new GetSentDirectMessagesTask(account_id, max_id);
		return mAsyncTaskManager.add(task, true);
	}

	public boolean hasActivatedTask() {
		return mAsyncTaskManager.hasActivatedTask();
	}

	public boolean isHomeTimelineRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetHomeTimelineTaskId) || !mStoreStatusesFinished;
	}

	public boolean isMentionsRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetMentionsTaskId) || !mStoreMentionsFinished;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mAsyncTaskManager = ((TwidereApplication) getApplication()).getAsyncTaskManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
	}

	public int reportSpam(long account_id, long user_id) {
		final ReportSpamTask task = new ReportSpamTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int retweetStatus(long account_id, long status_id) {
		final RetweetStatusTask task = new RetweetStatusTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
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

	private void showErrorToast(Exception e, boolean long_message) {
		Utils.showErrorToast(this, e, long_message);
	}

	private class CancelRetweetTask extends ManagedAsyncTask<Void, Void, StatusResponse> {

		private long account_id;
		private long status_id, retweeted_id;

		public CancelRetweetTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyStatus(retweeted_id);
					return new StatusResponse(account_id, status, null);
				} catch (final TwitterException e) {
					return new StatusResponse(account_id, null, e);
				}
			}
			return new StatusResponse(account_id, null, null);
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			if (result != null && result.status != null) {
				final ContentResolver resolver = getContentResolver();
				final User user = result.status.getUser();
				final twitter4j.Status retweeted_status = result.status.getRetweetedStatus();
				if (user != null && retweeted_status != null) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.RETWEET_COUNT, result.status.getRetweetCount());
					values.put(Statuses.RETWEET_ID, -1);
					values.put(Statuses.RETWEETED_BY_ID, -1);
					values.put(Statuses.RETWEETED_BY_NAME, "");
					values.put(Statuses.RETWEETED_BY_SCREEN_NAME, "");
					values.put(Statuses.IS_RETWEET, 0);
					final String status_where = Statuses.STATUS_ID + " = " + result.status.getId();
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

	private class CreateBlockTask extends ManagedAsyncTask<Void, Void, UserResponse> {

		private long account_id;
		private long user_id;

		public CreateBlockTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.createBlock(user_id);
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.user_blocked, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class CreateFavoriteTask extends ManagedAsyncTask<Void, Void, StatusResponse> {

		private long account_id;

		private long status_id;

		public CreateFavoriteTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Void... params) {

			if (account_id < 0) return new StatusResponse(account_id, null, null);

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.createFavorite(status_id);
					return new StatusResponse(account_id, status, null);
				} catch (final TwitterException e) {
					return new StatusResponse(account_id, null, e);
				}
			}
			return new StatusResponse(account_id, null, null);
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			final ContentResolver resolver = getContentResolver();

			if (result.status != null) {
				final long status_id = result.status.getId();
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

	private class CreateFriendshipTask extends ManagedAsyncTask<Void, Void, UserResponse> {

		private long account_id;
		private long user_id;

		public CreateFriendshipTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.createFriendship(user_id);
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.follow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_FRIENDSHIP_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class DestroyBlockTask extends ManagedAsyncTask<Void, Void, UserResponse> {

		private long account_id;
		private long user_id;

		public DestroyBlockTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.destroyBlock(user_id);
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.user_unblocked, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class DestroyFavoriteTask extends ManagedAsyncTask<Void, Void, StatusResponse> {

		private long account_id;

		private long status_id;

		public DestroyFavoriteTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Void... params) {

			if (account_id < 0) {
				new StatusResponse(account_id, null, null);
			}

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyFavorite(status_id);
					return new StatusResponse(account_id, status, null);
				} catch (final TwitterException e) {
					return new StatusResponse(account_id, null, e);
				}
			}
			return new StatusResponse(account_id, null, null);
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			final ContentResolver resolver = getContentResolver();

			if (result.status != null) {
				final long status_id = result.status.getId();
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

	private class DestroyFriendshipTask extends ManagedAsyncTask<Void, Void, UserResponse> {

		private long account_id;
		private long user_id;

		public DestroyFriendshipTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.destroyFriendship(user_id);
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.unfollow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_FRIENDSHIP_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class DestroyStatusTask extends ManagedAsyncTask<Void, Void, StatusResponse> {

		private long account_id;

		private long status_id;

		public DestroyStatusTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.destroyStatus(status_id);
					return new StatusResponse(account_id, status, null);
				} catch (final TwitterException e) {
					return new StatusResponse(account_id, null, e);
				}
			}
			return new StatusResponse(account_id, null, null);
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			if (result != null && result.status != null) {
				final long status_id = result.status.getId();
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

	private static final class DirectMessagesResponse {

		public final long account_id, max_id;
		public final ResponseList<DirectMessage> responselist;

		public DirectMessagesResponse(long account_id, long max_id, ResponseList<DirectMessage> responselist) {
			this.account_id = account_id;
			this.max_id = max_id;
			this.responselist = responselist;

		}
	}

	private abstract class GetDirectMessagesTask extends ManagedAsyncTask<Void, Void, DirectMessagesResponse> {

		private long account_id, max_id;

		public GetDirectMessagesTask(Uri uri, long account_id, long max_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.max_id = max_id;
		}

		public abstract ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging)
				throws TwitterException;

		@Override
		protected DirectMessagesResponse doInBackground(Void... params) {

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

					if (statuses != null) return new DirectMessagesResponse(account_id, max_id, statuses);
				} catch (final TwitterException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}

	private class GetHomeTimelineTask extends GetStatusesTask {

		public GetHomeTimelineTask(long[] account_ids, long[] max_ids) {
			super(Statuses.CONTENT_URI, account_ids, max_ids);
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
		protected void onPostExecute(List<StatusesResponse> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreHomeTimelineTask(responses), true);
			mGetHomeTimelineTaskId = -1;
		}

	}

	private class GetMentionsTask extends GetStatusesTask {

		public GetMentionsTask(long[] account_ids, long[] max_ids) {
			super(Mentions.CONTENT_URI, account_ids, max_ids);
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
		protected void onPostExecute(List<StatusesResponse> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreMentionsTask(responses), true);
			mGetMentionsTaskId = -1;
		}

	}

	private class GetReceivedDirectMessagesTask extends GetDirectMessagesTask {

		public GetReceivedDirectMessagesTask(long account_ids, long max_ids) {
			super(DirectMessages.Inbox.CONTENT_URI, account_ids, max_ids);
		}

		@Override
		public ResponseList<DirectMessage> getDirectMessages(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getDirectMessages(paging);
		}

		@Override
		protected void onPostExecute(DirectMessagesResponse responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreReceivedDirectMessagesTask(responses), true);
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
		protected void onPostExecute(DirectMessagesResponse responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreSentDirectMessagesTask(responses), true);
			mGetSentDirectMessagesTaskId = -1;
		}

	}

	private abstract class GetStatusesTask extends ManagedAsyncTask<Void, Void, List<StatusesResponse>> {

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
		protected List<StatusesResponse> doInBackground(Void... params) {

			final List<StatusesResponse> result = new ArrayList<StatusesResponse>();

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
							result.add(new StatusesResponse(account_id, max_id, statuses));
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

	private class ReportSpamTask extends ManagedAsyncTask<Void, Void, UserResponse> {

		private long account_id;
		private long user_id;

		public ReportSpamTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.reportSpam(user_id);
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.reported_user_for_spam, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_BLOCKSTATE_CHANGED);
			intent.putExtra(INTENT_KEY_USER_ID, user_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class RetweetStatusTask extends ManagedAsyncTask<Void, Void, StatusResponse> {

		private long account_id;

		private long status_id;

		public RetweetStatusTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Void... params) {

			if (account_id < 0) return new StatusResponse(account_id, null, null);

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final twitter4j.Status status = twitter.retweetStatus(status_id);
					return new StatusResponse(account_id, status, null);
				} catch (final TwitterException e) {
					return new StatusResponse(account_id, null, e);
				}
			}
			return new StatusResponse(account_id, null, null);
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			final ContentResolver resolver = getContentResolver();

			if (result.status != null) {
				final User user = result.status.getUser();
				final twitter4j.Status retweeted_status = result.status.getRetweetedStatus();
				if (user != null && retweeted_status != null) {
					final ContentValues values = new ContentValues();
					values.put(Statuses.RETWEET_ID, result.status.getId());
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
		public int reportSpam(long account_id, long user_id) {
			return mService.get().reportSpam(account_id, user_id);
		}

		@Override
		public int retweetStatus(long account_id, long status_id) {
			return mService.get().retweetStatus(account_id, status_id);
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

	private static final class StatusesResponse {

		public final long account_id, max_id;
		public final ResponseList<Status> responselist;

		public StatusesResponse(long account_id, long max_id, ResponseList<twitter4j.Status> responselist) {
			this.account_id = account_id;
			this.max_id = max_id;
			this.responselist = responselist;

		}
	}

	private static final class StatusResponse {
		public final TwitterException exception;
		public final Status status;
		public final long account_id;

		public StatusResponse(long account_id, Status status, TwitterException exception) {
			this.exception = exception;
			this.status = status;
			this.account_id = account_id;
		}
	}

	private class StoreDirectMessagesTask extends ManagedAsyncTask<Void, Void, Boolean> {

		private final DirectMessagesResponse response;
		private final Uri uri;

		public StoreDirectMessagesTask(DirectMessagesResponse result, Uri uri) {
			super(TwidereService.this, mAsyncTaskManager);
			response = result;
			this.uri = uri;
		}

		@Override
		protected Boolean doInBackground(Void... args) {
			final ContentResolver resolver = getContentResolver();
			final Uri query_uri = buildQueryUri(uri, false);

			final long account_id = response.account_id;
			final ResponseList<DirectMessage> messages = response.responselist;
			final Cursor cur = resolver.query(uri, new String[0], DirectMessages.ACCOUNT_ID + " = " + account_id, null,
					null);
			boolean no_items_before = false;
			if (cur != null) {
				no_items_before = cur.getCount() <= 0;
				cur.close();
			}
			if (messages == null) return false;
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
			final boolean insert_gap = rows_deleted == 1 && message_ids.contains(response.max_id) || rows_deleted == 0
					&& response.max_id == -1 && !no_items_before;
			if (insert_gap) {
				final ContentValues values = new ContentValues();
				values.put(DirectMessages.IS_GAP, 1);
				final StringBuilder where = new StringBuilder();
				where.append(DirectMessages.ACCOUNT_ID + "=" + account_id);
				where.append(" AND " + DirectMessages.MESSAGE_ID + "=" + min_id);
				resolver.update(query_uri, values, where.toString(), null);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			if (succeed) {
				notifyForUpdatedUri(TwidereService.this, uri);
			}
			super.onPostExecute(succeed);
		}

	}

	private class StoreHomeTimelineTask extends StoreStatusesTask {

		public StoreHomeTimelineTask(List<StatusesResponse> result) {
			super(result, Statuses.CONTENT_URI);
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			mStoreStatusesFinished = true;
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed != null
					&& succeed));
			super.onPostExecute(succeed);
		}

	}

	private class StoreMentionsTask extends StoreStatusesTask {

		public StoreMentionsTask(List<StatusesResponse> result) {
			super(result, Mentions.CONTENT_URI);
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			mStoreMentionsFinished = true;
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed != null
					&& succeed));
			super.onPostExecute(succeed);
		}

	}

	private class StoreReceivedDirectMessagesTask extends StoreDirectMessagesTask {

		public StoreReceivedDirectMessagesTask(DirectMessagesResponse result) {
			super(result, DirectMessages.Inbox.CONTENT_URI);
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			mStoreReceivedDirectMessagesFinished = true;
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed != null
					&& succeed));
			super.onPostExecute(succeed);
		}

	}

	private class StoreSentDirectMessagesTask extends StoreDirectMessagesTask {

		public StoreSentDirectMessagesTask(DirectMessagesResponse result) {
			super(result, DirectMessages.Outbox.CONTENT_URI);
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			mStoreSentDirectMessagesFinished = true;
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtra(INTENT_KEY_SUCCEED, succeed != null
					&& succeed));
			super.onPostExecute(succeed);
		}

	}

	private class StoreStatusesTask extends ManagedAsyncTask<Void, Void, Boolean> {

		private final List<StatusesResponse> responses;
		private final Uri uri;

		public StoreStatusesTask(List<StatusesResponse> result, Uri uri) {
			super(TwidereService.this, mAsyncTaskManager);
			responses = result;
			this.uri = uri;
		}

		@Override
		protected Boolean doInBackground(Void... args) {
			final ContentResolver resolver = getContentResolver();
			boolean succeed = false;
			final Uri query_uri = buildQueryUri(uri, false);

			for (final StatusesResponse response : responses) {
				final long account_id = response.account_id;
				final ResponseList<twitter4j.Status> statuses = response.responselist;
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

					user_ids.add(user_id);
					if (!user_ids.contains(user_id)) {
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
			return succeed;
		}

		@Override
		protected void onPostExecute(Boolean succeed) {
			if (succeed) {
				notifyForUpdatedUri(TwidereService.this, uri);
			}
			super.onPostExecute(succeed);
		}

	}

	private class UpdateProfileImageTask extends ManagedAsyncTask<Void, Void, UserResponse> {

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
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null && image_uri != null && "file".equals(image_uri.getScheme())) {
				try {
					final User user = twitter.updateProfileImage(new File(image_uri.getPath()));
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.profile_image_update_success, Toast.LENGTH_SHORT).show();
				if (delete_image) {
					new File(image_uri.getPath()).delete();
				}
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_UPDATED);
			intent.putExtra(INTENT_KEY_USER_ID, account_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class UpdateProfileTask extends ManagedAsyncTask<Void, Void, UserResponse> {

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
		protected UserResponse doInBackground(Void... params) {

			final Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					final User user = twitter.updateProfile(name, url, location, description);
					return new UserResponse(user, null);
				} catch (final TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return new UserResponse(null, null);
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.profile_update_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(result.exception, true);
			}
			final Intent intent = new Intent(BROADCAST_PROFILE_UPDATED);
			intent.putExtra(INTENT_KEY_USER_ID, account_id);
			intent.putExtra(INTENT_KEY_SUCCEED, result != null && result.user != null);
			sendBroadcast(intent);
			super.onPostExecute(result);
		}

	}

	private class UpdateStatusTask extends ManagedAsyncTask<Void, Void, List<StatusResponse>> {

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
		protected List<StatusResponse> doInBackground(Void... params) {

			if (account_ids == null) return null;

			final List<StatusResponse> result = new ArrayList<StatusResponse>();

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
						result.add(new StatusResponse(account_id, twitter.updateStatus(status), null));
					} catch (final TwitterException e) {
						e.printStackTrace();
						result.add(new StatusResponse(account_id, null, e));
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<StatusResponse> result) {

			boolean succeed = false;
			TwitterException exception = null;
			final List<Long> failed_account_ids = new ArrayList<Long>();

			for (final StatusResponse response : result) {
				if (response.status != null) {
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

	private static final class UserResponse {
		public TwitterException exception;
		public User user;

		public UserResponse(User user, TwitterException exception) {
			this.exception = exception;
			this.user = user;
		}
	}

}
