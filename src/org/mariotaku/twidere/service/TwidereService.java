package org.mariotaku.twidere.service;

import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getRetweetId;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.makeCachedUsersContentValues;
import static org.mariotaku.twidere.util.Utils.makeStatusesContentValues;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

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
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.ManagedAsyncTask;

import twitter4j.GeoLocation;
import twitter4j.Paging;
import twitter4j.ResponseList;
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
import android.os.RemoteException;
import android.widget.Toast;

public class TwidereService extends Service implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);

	private AsyncTaskManager mAsyncTaskManager;

	private int mGetHomeTimelineTaskId, mGetMentionsTaskId;

	public int cancelRetweet(long account_id, long status_id) {
		CancelRetweetTask task = new CancelRetweetTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createFavorite(long[] account_ids, long status_id) {
		CreateFavoriteTask task = new CreateFavoriteTask(account_ids, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int createFriendship(long account_id, long user_id) {
		CreateFriendshipTask task = new CreateFriendshipTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyFavorite(long[] account_ids, long status_id) {
		DestroyFavoriteTask task = new DestroyFavoriteTask(account_ids, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyFriendship(long account_id, long user_id) {
		DestroyFriendshipTask task = new DestroyFriendshipTask(account_id, user_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int destroyStatus(long account_id, long status_id) {
		DestroyStatusTask task = new DestroyStatusTask(account_id, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int getHomeTimeline(long[] account_ids, long[] max_ids) {
		mAsyncTaskManager.cancel(mGetHomeTimelineTaskId);
		GetHomeTimelineTask task = new GetHomeTimelineTask(this, mAsyncTaskManager, account_ids, max_ids);
		return mGetHomeTimelineTaskId = mAsyncTaskManager.add(task, true);
	}

	public int getMentions(long[] account_ids, long[] max_ids) {
		mAsyncTaskManager.cancel(mGetMentionsTaskId);
		GetMentionsTask task = new GetMentionsTask(this, mAsyncTaskManager, account_ids, max_ids);
		return mGetMentionsTaskId = mAsyncTaskManager.add(task, true);
	}

	public boolean hasActivatedTask() {
		return mAsyncTaskManager.hasActivatedTask();
	}

	public boolean isHomeTimelineRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetHomeTimelineTaskId);
	}

	public boolean isMentionsRefreshing() {
		return mAsyncTaskManager.isExcuting(mGetMentionsTaskId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mAsyncTaskManager = ((TwidereApplication) getApplication()).getAsyncTaskManager();
	}

	public int refreshMessages(long[] account_ids, long[] max_ids) {
		return -1;
	}

	public int retweetStatus(long[] account_ids, long status_id) {
		RetweetStatusTask task = new RetweetStatusTask(account_ids, status_id);
		return mAsyncTaskManager.add(task, true);
	}

	public int updateStatus(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to) {
		UpdateStatusTask task = new UpdateStatusTask(account_ids, content, location, image_uri, in_reply_to);
		return mAsyncTaskManager.add(task, true);
	}

	private class CancelRetweetTask extends ManagedAsyncTask<StatusResponse> {

		private long account_id;
		private long status_id, retweeted_id;

		public CancelRetweetTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Object... params) {

			Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					twitter4j.Status status = twitter.destroyStatus(retweeted_id);
					return new StatusResponse(status, null);
				} catch (TwitterException e) {
					return new StatusResponse(null, e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			if (result != null && result.status != null) {
				ContentResolver resolver = getContentResolver();
				User user = result.status.getUser();
				twitter4j.Status retweeted_status = result.status.getRetweetedStatus();
				if (user != null && retweeted_status != null) {
					ContentValues values = new ContentValues();
					values.put(Statuses.RETWEET_COUNT, result.status.getRetweetCount());
					values.put(Statuses.RETWEET_ID, -1);
					values.put(Statuses.RETWEETED_BY_ID, -1);
					values.put(Statuses.RETWEETED_BY_NAME, "");
					values.put(Statuses.RETWEETED_BY_SCREEN_NAME, "");
					values.put(Statuses.IS_RETWEET, 0);
					StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + "=" + result.status.getUser().getId());
					where.append(" AND " + Statuses.STATUS_ID + "=" + retweeted_status.getId());
					for (Uri uri : TweetStore.STATUSES_URIS) {
						resolver.update(uri, values, where.toString(), null);
					}
				}
				Toast.makeText(TwidereService.this, R.string.cancel_retweet_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			retweeted_id = getRetweetId(TwidereService.this, status_id);
		}

	}

	private class CreateFavoriteTask extends ManagedAsyncTask<List<StatusResponse>> {

		private long[] account_ids;

		private long status_id;

		public CreateFavoriteTask(long[] account_ids, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.status_id = status_id;
		}

		@Override
		protected List<StatusResponse> doInBackground(Object... params) {

			if (account_ids == null) return null;

			List<StatusResponse> result = new ArrayList<StatusResponse>();

			for (long account_id : account_ids) {
				Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
				if (twitter != null) {
					try {
						twitter4j.Status status = twitter.createFavorite(status_id);
						result.add(new StatusResponse(status, null));
					} catch (TwitterException e) {
						result.add(new StatusResponse(null, e));
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<StatusResponse> result) {
			ContentResolver resolver = getContentResolver();

			boolean succeed = false;
			TwitterException exception = null;

			for (StatusResponse response : result) {
				if (response.status != null) {
					ContentValues values = new ContentValues();
					values.put(Statuses.IS_FAVORITE, 1);
					StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + "=" + response.status.getUser().getId());
					where.append(" AND " + Statuses.STATUS_ID + "=" + response.status.getId());
					for (Uri uri : TweetStore.STATUSES_URIS) {
						resolver.update(uri, values, where.toString(), null);
					}
					succeed = true;
				} else {
					exception = response.exception;
				}
			}
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.favorite_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, exception, true);
			}
			super.onPostExecute(result);
		}

	}

	private class CreateFriendshipTask extends ManagedAsyncTask<UserResponse> {

		private long account_id;
		private long user_id;

		public CreateFriendshipTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Object... params) {

			Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					User user = twitter.createFriendship(user_id);
					return new UserResponse(user, null);
				} catch (TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.follow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	private class DestroyFavoriteTask extends ManagedAsyncTask<List<StatusResponse>> {

		private long[] account_ids;

		private long status_id;

		public DestroyFavoriteTask(long[] account_ids, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.status_id = status_id;
		}

		@Override
		protected List<StatusResponse> doInBackground(Object... params) {

			if (account_ids == null) return null;

			List<StatusResponse> result = new ArrayList<StatusResponse>();

			for (long account_id : account_ids) {
				Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
				if (twitter != null) {
					try {
						twitter4j.Status status = twitter.destroyFavorite(status_id);
						result.add(new StatusResponse(status, null));
					} catch (TwitterException e) {
						result.add(new StatusResponse(null, e));
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<StatusResponse> result) {
			ContentResolver resolver = getContentResolver();

			boolean succeed = false;
			TwitterException exception = null;

			for (StatusResponse response : result) {
				if (response.status != null) {
					ContentValues values = new ContentValues();
					values.put(Statuses.IS_FAVORITE, 0);
					StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + "=" + response.status.getUser().getId());
					where.append(" AND " + Statuses.STATUS_ID + "=" + response.status.getId());
					for (Uri uri : TweetStore.STATUSES_URIS) {
						resolver.update(uri, values, where.toString(), null);
					}
					succeed = true;
				} else {
					exception = response.exception;
				}
			}
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.unfavorite_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, exception, true);
			}
			super.onPostExecute(result);
		}

	}

	private class DestroyFriendshipTask extends ManagedAsyncTask<UserResponse> {

		private long account_id;
		private long user_id;

		public DestroyFriendshipTask(long account_id, long user_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		protected UserResponse doInBackground(Object... params) {

			Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					User user = twitter.destroyFriendship(user_id);
					return new UserResponse(user, null);
				} catch (TwitterException e) {
					return new UserResponse(null, e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(UserResponse result) {
			if (result != null && result.user != null) {
				Toast.makeText(TwidereService.this, R.string.unfollow_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	private class DestroyStatusTask extends ManagedAsyncTask<StatusResponse> {

		private long account_id;

		private long status_id;

		public DestroyStatusTask(long account_id, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		protected StatusResponse doInBackground(Object... params) {

			Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
			if (twitter != null) {
				try {
					twitter4j.Status status = twitter.destroyStatus(status_id);
					return new StatusResponse(status, null);
				} catch (TwitterException e) {
					return new StatusResponse(null, e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(StatusResponse result) {
			if (result != null && result.status != null) {
				ContentResolver resolver = getContentResolver();
				StringBuilder where = new StringBuilder();
				where.append(Statuses.ACCOUNT_ID + "=" + result.status.getUser().getId());
				where.append(" AND " + Statuses.STATUS_ID + "=" + result.status.getId());
				for (Uri uri : TweetStore.STATUSES_URIS) {
					resolver.delete(uri, where.toString(), null);
				}
				Toast.makeText(TwidereService.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

	}

	private class GetHomeTimelineTask extends GetStatusesTask {

		private Context context;

		public GetHomeTimelineTask(Context context, AsyncTaskManager manager, long[] account_ids, long[] max_ids) {
			super(context, manager, Statuses.CONTENT_URI, account_ids, max_ids);
			this.context = context;
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getHomeTimeline(paging);
		}

		@Override
		protected void onPostExecute(List<GetStatusesTask.AccountResponse> responses) {
			super.onPostExecute(responses);
			context.sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtra(INTENT_KEY_SUCCEED,
					responses.size() > 0));
		}

	}

	private class GetMentionsTask extends GetStatusesTask {

		private Context context;

		public GetMentionsTask(Context context, AsyncTaskManager manager, long[] account_ids, long[] max_ids) {
			super(context, manager, Mentions.CONTENT_URI, account_ids, max_ids);
			this.context = context;
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging) throws TwitterException {
			return twitter.getMentions(paging);
		}

		@Override
		protected void onPostExecute(List<GetStatusesTask.AccountResponse> responses) {
			super.onPostExecute(responses);
			context.sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED).putExtra(INTENT_KEY_SUCCEED,
					responses.size() > 0));
		}

	}

	private static abstract class GetStatusesTask extends ManagedAsyncTask<List<GetStatusesTask.AccountResponse>> {

		private long[] account_ids, max_ids;

		private final Uri uri;
		private Context context;

		public GetStatusesTask(Context context, AsyncTaskManager manager, Uri uri, long[] account_ids, long[] max_ids) {
			super(context, manager);
			this.uri = uri;
			this.context = context;
			this.account_ids = account_ids;
			this.max_ids = max_ids;
		}

		public abstract ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging)
				throws TwitterException;

		@Override
		protected List<AccountResponse> doInBackground(Object... params) {

			List<AccountResponse> result = new ArrayList<AccountResponse>();

			if (account_ids == null) return result;

			boolean max_ids_valid = max_ids != null && max_ids.length == account_ids.length;

			int idx = 0;
			SharedPreferences prefs = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			int load_item_limit = prefs.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT) + 1;
			for (long account_id : account_ids) {
				Twitter twitter = getTwitterInstance(context, account_id, true);
				if (twitter != null) {
					try {
						Paging paging = new Paging();
						paging.setCount(load_item_limit);
						if (max_ids_valid && max_ids[idx] > 0) {
							paging.setMaxId(max_ids[idx]);
						}
						ResponseList<twitter4j.Status> statuses = getStatuses(twitter, paging);

						if (statuses != null) {
							result.add(new AccountResponse(account_id, statuses));
						}
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}
				idx++;
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<AccountResponse> responses) {
			ContentResolver resolver = context.getContentResolver();

			for (AccountResponse response : responses) {
				long account_id = response.account_id;
				ResponseList<twitter4j.Status> statuses = response.responselist;
				Cursor cur = resolver.query(uri, new String[0], Statuses.ACCOUNT_ID + " = " + account_id , null, null);
				boolean no_items_before = false;
				if (cur != null) {
					no_items_before = cur.getCount() > 0;
					cur.close();
				}
				if (statuses == null || statuses.size() <= 0) {
					super.onPostExecute(responses);
					return;
				}
				List<ContentValues> values_list = new ArrayList<ContentValues>();

				long min_id = -1, max_id = -1;
				for (twitter4j.Status status : statuses) {
					if (status == null) {
						continue;
					}
					User user = status.getUser();
					long status_id = status.getId(), user_id = user.getId();

					resolver.delete(CachedUsers.CONTENT_URI, CachedUsers.USER_ID + "=" + user_id, null);
					resolver.insert(CachedUsers.CONTENT_URI, makeCachedUsersContentValues(user));

					if (status_id < min_id || min_id == -1) {
						min_id = status_id;
					}
					if (status_id > max_id || max_id == -1) {
						max_id = status_id;
					}
					values_list.add(makeStatusesContentValues(status, account_id));
				}
				// Delete all rows conflicting before new data inserted.
				int rows_deleted = -1;
				if (min_id != -1 && max_id != -1) {
					StringBuilder where = new StringBuilder();
					where.append(Statuses.STATUS_ID + ">=" + min_id);
					where.append(" AND " + Statuses.STATUS_ID + "<=" + max_id);

					rows_deleted = resolver.delete(uri, where.toString(), null);
				}

				// Insert previously fetched items.
				resolver.bulkInsert(uri, values_list.toArray(new ContentValues[values_list.size()]));

				// No row deleted, so I will insert a gap.
				if (rows_deleted == 0 && (max_ids == null || max_ids.length <= 0) && !no_items_before) {
					ContentValues values = new ContentValues();
					values.put(Statuses.IS_GAP, 1);
					StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + "=" + account_id);
					where.append(" AND " + Statuses.STATUS_ID + "=" + min_id);
					resolver.update(uri, values, where.toString(), null);
				}

			}
			super.onPostExecute(responses);
		}

		private class AccountResponse {

			public long account_id;
			public ResponseList<twitter4j.Status> responselist;

			public AccountResponse(long account_id, ResponseList<twitter4j.Status> responselist) {
				this.account_id = account_id;
				this.responselist = responselist;

			}
		}

	}

	private class RetweetStatusTask extends ManagedAsyncTask<List<StatusResponse>> {

		private long[] account_ids;

		private long status_id;

		public RetweetStatusTask(long[] account_ids, long status_id) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.status_id = status_id;
		}

		@Override
		protected List<StatusResponse> doInBackground(Object... params) {

			if (account_ids == null) return null;

			List<StatusResponse> result = new ArrayList<StatusResponse>();

			for (long account_id : account_ids) {
				Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
				if (twitter != null) {
					try {
						twitter4j.Status status = twitter.retweetStatus(status_id);
						result.add(new StatusResponse(status, null));
					} catch (TwitterException e) {
						result.add(new StatusResponse(null, e));
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<StatusResponse> result) {
			ContentResolver resolver = getContentResolver();

			boolean succeed = false;
			TwitterException exception = null;

			for (StatusResponse response : result) {
				if (response.status != null) {
					User user = response.status.getUser();
					twitter4j.Status retweeted_status = response.status.getRetweetedStatus();
					if (user != null && retweeted_status != null) {
						ContentValues values = new ContentValues();
						values.put(Statuses.RETWEET_COUNT, response.status.getRetweetCount());
						values.put(Statuses.RETWEET_ID, response.status.getId());
						values.put(Statuses.RETWEETED_BY_ID, user.getId());
						values.put(Statuses.RETWEETED_BY_NAME, user.getName());
						values.put(Statuses.RETWEETED_BY_SCREEN_NAME, user.getScreenName());
						values.put(Statuses.IS_RETWEET, 1);
						StringBuilder where = new StringBuilder();
						where.append(Statuses.ACCOUNT_ID + "=" + response.status.getUser().getId());
						where.append(" AND " + Statuses.STATUS_ID + "=" + retweeted_status.getId());
						for (Uri uri : TweetStore.STATUSES_URIS) {
							resolver.update(uri, values, where.toString(), null);
						}
					}
					succeed = true;
				} else {
					exception = response.exception;
				}

			}
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.retweet_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, exception, true);
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

		WeakReference<TwidereService> mService;

		public ServiceStub(TwidereService service) {

			mService = new WeakReference<TwidereService>(service);
		}

		@Override
		public int cancelRetweet(long account_id, long status_id) throws RemoteException {
			return mService.get().cancelRetweet(account_id, status_id);
		}

		@Override
		public int createFavorite(long[] account_ids, long status_id) throws RemoteException {
			return mService.get().createFavorite(account_ids, status_id);
		}

		@Override
		public int createFriendship(long account_id, long user_id) throws RemoteException {
			return mService.get().createFriendship(account_id, user_id);
		}

		@Override
		public int destroyFavorite(long[] account_ids, long status_id) throws RemoteException {
			return mService.get().destroyFavorite(account_ids, status_id);
		}

		@Override
		public int destroyFriendship(long account_id, long user_id) throws RemoteException {
			return mService.get().destroyFriendship(account_id, user_id);
		}

		@Override
		public int destroyStatus(long account_id, long status_id) throws RemoteException {
			return mService.get().destroyStatus(account_id, status_id);
		}

		@Override
		public int getHomeTimeline(long[] account_ids, long[] max_ids) throws RemoteException {
			return mService.get().getHomeTimeline(account_ids, max_ids);
		}

		@Override
		public int getMentions(long[] account_ids, long[] max_ids) throws RemoteException {
			return mService.get().getMentions(account_ids, max_ids);
		}

		@Override
		public int getMessages(long[] account_ids, long[] max_ids) throws RemoteException {
			return mService.get().refreshMessages(account_ids, max_ids);
		}

		@Override
		public boolean hasActivatedTask() throws RemoteException {
			return mService.get().hasActivatedTask();
		}

		@Override
		public boolean isHomeTimelineRefreshing() throws RemoteException {
			return mService.get().isHomeTimelineRefreshing();
		}

		@Override
		public boolean isMentionsRefreshing() throws RemoteException {
			return mService.get().isMentionsRefreshing();
		}

		@Override
		public int retweetStatus(long[] account_ids, long status_id) throws RemoteException {
			return mService.get().retweetStatus(account_ids, status_id);
		}

		@Override
		public boolean test() throws RemoteException {
			return true;
		}

		@Override
		public int updateStatus(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to)
				throws RemoteException {
			return mService.get().updateStatus(account_ids, content, location, image_uri, in_reply_to);

		}

	}

	private static final class StatusResponse {
		public TwitterException exception;
		public twitter4j.Status status;

		public StatusResponse(twitter4j.Status status, TwitterException exception) {
			this.exception = exception;
			this.status = status;
		}
	}

	private class UpdateStatusTask extends ManagedAsyncTask<List<StatusResponse>> {

		private long[] account_ids;
		private String content;
		private Location location;
		private Uri image_uri;
		private long in_reply_to;

		public UpdateStatusTask(long[] account_ids, String content, Location location, Uri image_uri, long in_reply_to) {
			super(TwidereService.this, mAsyncTaskManager);
			this.account_ids = account_ids;
			this.content = content;
			this.location = location;
			this.image_uri = image_uri;
			this.in_reply_to = in_reply_to;
		}

		@Override
		protected List<StatusResponse> doInBackground(Object... params) {

			if (account_ids == null) return null;

			final List<StatusResponse> result = new ArrayList<StatusResponse>();

			for (long account_id : account_ids) {
				Twitter twitter = getTwitterInstance(TwidereService.this, account_id, false);
				if (twitter != null) {
					try {
						StatusUpdate status = new StatusUpdate(content);
						status.setInReplyToStatusId(in_reply_to);
						if (location != null) {
							status.setLocation(new GeoLocation(location.getLatitude(), location.getLongitude()));
						}
						String image_path = getImagePathFromUri(TwidereService.this, image_uri);
						if (image_path != null) {
							status.setMedia(new File(image_path));
						}
						result.add(new StatusResponse(twitter.updateStatus(status), null));
					} catch (TwitterException e) {
						result.add(new StatusResponse(null, e));
					}
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<StatusResponse> result) {

			boolean succeed = false;
			TwitterException exception = null;

			for (StatusResponse response : result) {
				if (response.status != null) {
					succeed = true;
					break;
				} else {
					exception = response.exception;
				}
			}
			if (succeed) {
				Toast.makeText(TwidereService.this, R.string.send_success, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwidereService.this, exception, true);
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
