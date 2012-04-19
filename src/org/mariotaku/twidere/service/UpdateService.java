package org.mariotaku.twidere.service;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.IUpdateService;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;

import roboguice.service.RoboService;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;

public class UpdateService extends RoboService implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);
	private RefreshHomeTimelineTask mRefreshHomeTimelineTask;
	private RefreshMentionsTask mRefreshMentionsTask;
	private UpdateStatusTask mUpdateStatusTask;

	public boolean isHomeTimelineRefreshing() {
		return mRefreshHomeTimelineTask != null && !mRefreshHomeTimelineTask.isCancelled();
	}

	public boolean isMentionsRefreshing() {
		return mRefreshMentionsTask != null && !mRefreshMentionsTask.isCancelled();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void refreshHomeTimeline(long[] account_ids, long[] max_ids) {
		if (mRefreshHomeTimelineTask != null) {
			mRefreshHomeTimelineTask.cancel();
		}
		mRefreshHomeTimelineTask = new RefreshHomeTimelineTask(account_ids, max_ids);
		mRefreshHomeTimelineTask.execute();
	}

	public void refreshMentions(long[] account_ids, long[] max_ids) {
		if (mRefreshMentionsTask != null) {
			mRefreshMentionsTask.cancel();
		}
		mRefreshMentionsTask = new RefreshMentionsTask(account_ids, max_ids);
		mRefreshMentionsTask.execute();
	}

	public void refreshMessages(long[] account_ids, long[] max_ids) {

	}

	public void updateStatus(long[] account_ids, String content, Location location, Uri image_uri,
			long in_reply_to) {
		if (mUpdateStatusTask != null) {
			mUpdateStatusTask.cancel();
		}
		mUpdateStatusTask = new UpdateStatusTask(account_ids, content, location, image_uri,
				in_reply_to);
		mUpdateStatusTask.execute();
	}

	private abstract class AbstractTask extends AsyncTask<Void, Void, Object> {

		protected void cancel() {
			sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
			cancel(true);
		}

		@Override
		protected void onPostExecute(Object result_obj) {
			sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
			super.onPostExecute(result_obj);
		}

		@Override
		protected void onPreExecute() {
			sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
			super.onPreExecute();
		}

	}

	private class RefreshHomeTimelineTask extends AbstractTask {

		private long[] account_ids, max_ids;

		public RefreshHomeTimelineTask(long[] account_ids, long[] max_ids) {

			this.account_ids = account_ids;
			this.max_ids = max_ids;
		}

		@Override
		protected void cancel() {
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED));
			mRefreshHomeTimelineTask = null;
			super.cancel();
		}

		@Override
		protected List<AccountResponce> doInBackground(Void... params) {

			List<AccountResponce> result = new ArrayList<AccountResponce>();

			if (account_ids == null) return result;

			boolean since_valid = max_ids != null && max_ids.length == account_ids.length;

			int idx = 0;
			for (long account_id : account_ids) {
				Twitter twitter = CommonUtils.getTwitterInstance(UpdateService.this, account_id);
				if (twitter != null) {
					try {
						Paging paging = new Paging();
						if (since_valid) {
							paging.setMaxId(max_ids[idx]);
						}
						result.add(new AccountResponce(account_id, twitter.getHomeTimeline(paging)));
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}
				idx++;
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result_obj) {
			List<AccountResponce> responces = (List<AccountResponce>) result_obj;
			ContentResolver resolver = getContentResolver();

			for (AccountResponce responce : responces) {
				ResponseList<twitter4j.Status> statuses = responce.responselist;
				long account_id = responce.account_id;
				if (statuses == null || statuses.size() <= 0) return;
				List<ContentValues> values_list = new ArrayList<ContentValues>();

				long min_id = -1, max_id = -1;
				for (twitter4j.Status status : statuses) {
					ContentValues values = new ContentValues();
					User user = status.getUser();
					long status_id = status.getId();
					MediaEntity[] medias = status.getMediaEntities();
					values.put(Statuses.STATUS_ID, status_id);
					values.put(Statuses.ACCOUNT_ID, account_id);
					values.put(Statuses.USER_ID, user.getId());
					values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
					values.put(Statuses.TEXT, status.getText());
					values.put(Statuses.NAME, user.getName());
					values.put(Statuses.SCREEN_NAME, user.getScreenName());
					values.put(Statuses.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
					values.put(Statuses.IS_RETWEET, status.isRetweet() ? 1 : 0);
					values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);
					values.put(Statuses.IN_REPLY_TO_SCREEN_NAME, status.getInReplyToScreenName());
					values.put(Statuses.IN_REPLY_TO_STATUS_ID, status.getInReplyToStatusId());
					values.put(Statuses.IN_REPLY_TO_USER_ID, status.getInReplyToUserId());
					values.put(Statuses.HAS_MEDIA, medias != null && medias.length > 0 ? 1 : 0);
					values.put(Statuses.HAS_LOCATION, status.getGeoLocation() != null ? 1 : 0);
					values.put(Statuses.IS_TWEET_BY_ME, user.getId() == account_id ? 1 : 0);

					if (status_id < min_id || min_id == -1) {
						min_id = status_id;
					}
					if (status_id > max_id || max_id == -1) {
						max_id = status_id;
					}

					values_list.add(values);
				}
				// Delete all rows conflicting before new data inserted.
				int rows_deleted = -1;
				if (min_id != -1 && max_id != -1) {
					StringBuilder where = new StringBuilder();
					where.append(Statuses.STATUS_ID + ">=" + min_id);
					where.append(" AND " + Statuses.STATUS_ID + "<=" + max_id);

					rows_deleted = resolver.delete(Statuses.CONTENT_URI, where.toString(), null);
				}

				// Insert previously fetched items.
				resolver.bulkInsert(Statuses.CONTENT_URI,
						values_list.toArray(new ContentValues[values_list.size()]));

				// No row deleted, so I will insert a gap.
				if (rows_deleted == 0) {
					ContentValues values = new ContentValues();
					values.put(Statuses.IS_GAP, 1);
					StringBuilder where = new StringBuilder();
					where.append(Statuses.ACCOUNT_ID + "=" + account_id);
					where.append(" AND " + Statuses.STATUS_ID + "=" + min_id);
					resolver.update(Statuses.CONTENT_URI, values, where.toString(), null);
				}

			}
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED));
			mRefreshHomeTimelineTask = null;
			super.onPostExecute(result_obj);
		}

		private class AccountResponce {

			public long account_id;
			public ResponseList<twitter4j.Status> responselist;

			public AccountResponce(long account_id, ResponseList<twitter4j.Status> responselist) {
				this.account_id = account_id;
				this.responselist = responselist;

			}
		}

	}

	private class RefreshMentionsTask extends AbstractTask {

		private long[] account_ids, max_ids;

		public RefreshMentionsTask(long[] account_ids, long[] max_ids) {
			this.account_ids = account_ids;
			this.max_ids = max_ids;
		}

		@Override
		protected void cancel() {
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED));
			mRefreshMentionsTask = null;
			super.cancel();
		}

		@Override
		protected List<AccountResponce> doInBackground(Void... params) {

			List<AccountResponce> result = new ArrayList<AccountResponce>();

			if (account_ids == null) return result;

			boolean since_valid = max_ids != null && max_ids.length == account_ids.length;

			int idx = 0;

			for (long account_id : account_ids) {
				Twitter twitter = CommonUtils.getTwitterInstance(UpdateService.this, account_id);
				if (twitter != null) {
					try {
						Paging paging = new Paging();
						if (since_valid) {
							paging.setMaxId(max_ids[idx]);
						}
						result.add(new AccountResponce(account_id, twitter.getMentions(paging)));
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}
				idx++;
			}
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result_obj) {
			List<AccountResponce> responces = (List<AccountResponce>) result_obj;
			ContentResolver resolver = getContentResolver();
			for (AccountResponce responce : responces) {
				ResponseList<twitter4j.Status> mentions = responce.responselist;
				long account_id = responce.account_id;
				if (mentions == null) return;
				List<ContentValues> values_list = new ArrayList<ContentValues>();

				long min_id = -1, max_id = -1;
				for (twitter4j.Status mention : mentions) {
					ContentValues values = new ContentValues();
					long status_id = mention.getId();
					MediaEntity[] medias = mention.getMediaEntities();
					User user = mention.getUser();
					values.put(Mentions.ACCOUNT_ID, account_id);
					values.put(Mentions.STATUS_ID, status_id);
					values.put(Mentions.USER_ID, user.getId());
					values.put(Mentions.STATUS_TIMESTAMP, mention.getCreatedAt().getTime());
					values.put(Mentions.TEXT, mention.getText());
					values.put(Mentions.NAME, user.getName());
					values.put(Mentions.SCREEN_NAME, user.getScreenName());
					values.put(Mentions.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
					values.put(Mentions.IS_RETWEET, mention.isRetweet() ? 1 : 0);
					values.put(Mentions.IS_FAVORITE, mention.isFavorited() ? 1 : 0);
					values.put(Mentions.IN_REPLY_TO_SCREEN_NAME, mention.getInReplyToScreenName());
					values.put(Mentions.IN_REPLY_TO_STATUS_ID, mention.getInReplyToStatusId());
					values.put(Mentions.IN_REPLY_TO_USER_ID, mention.getInReplyToUserId());
					values.put(Mentions.HAS_MEDIA, medias != null && medias.length > 0 ? 1 : 0);
					values.put(Mentions.HAS_LOCATION, mention.getGeoLocation() != null ? 1 : 0);
					values.put(Mentions.IS_TWEET_BY_ME, user.getId() == account_id ? 1 : 0);

					if (status_id < min_id || min_id == -1) {
						min_id = status_id;
					}
					if (status_id > max_id || max_id == -1) {
						max_id = status_id;
					}

					values_list.add(values);
				}

				// Delete all rows conflicting before new data inserted.
				int rows_deleted = -1;
				if (min_id != -1 && max_id != -1) {
					StringBuilder where = new StringBuilder();
					where.append(Mentions.STATUS_ID + ">=" + min_id);
					where.append(" AND " + Mentions.STATUS_ID + "<=" + max_id);

					rows_deleted = resolver.delete(Mentions.CONTENT_URI, where.toString(), null);
				}

				resolver.bulkInsert(Mentions.CONTENT_URI,
						values_list.toArray(new ContentValues[values_list.size()]));

				// No row deleted, so I will insert a gap.
				if (rows_deleted == 0) {
					ContentValues values = new ContentValues();
					values.put(Mentions.IS_GAP, 1);
					StringBuilder where = new StringBuilder();
					where.append(Mentions.ACCOUNT_ID + "=" + account_id);
					where.append(" AND " + Mentions.STATUS_ID + "=" + min_id);
					resolver.update(Mentions.CONTENT_URI, values, where.toString(), null);
				}
			}
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED));
			mRefreshMentionsTask = null;
			super.onPostExecute(result_obj);
		}

		private class AccountResponce {

			public long account_id;
			public ResponseList<twitter4j.Status> responselist;

			public AccountResponce(long account_id, ResponseList<twitter4j.Status> responselist) {
				this.account_id = account_id;
				this.responselist = responselist;

			}
		}

	}

	/*
	 * By making this a static class with a WeakReference to the Service, we
	 * ensure that the Service can be GCd even when the system process still has
	 * a remote reference to the stub.
	 */
	private class ServiceStub extends IUpdateService.Stub {

		WeakReference<UpdateService> mService;

		public ServiceStub(UpdateService service) {

			mService = new WeakReference<UpdateService>(service);
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
		public void refreshHomeTimeline(long[] account_ids, long[] max_ids) throws RemoteException {
			mService.get().refreshHomeTimeline(account_ids, max_ids);
		}

		@Override
		public void refreshMentions(long[] account_ids, long[] max_ids) throws RemoteException {
			mService.get().refreshMentions(account_ids, max_ids);
		}

		@Override
		public void refreshMessages(long[] account_ids, long[] max_ids) throws RemoteException {
			mService.get().refreshMessages(account_ids, max_ids);
		}

		@Override
		public void updateStatus(long[] account_ids, String content, Location location,
				Uri image_uri, long in_reply_to) throws RemoteException {
			mService.get().updateStatus(account_ids, content, location, image_uri, in_reply_to);

		}

	}

	private class UpdateStatusTask extends AbstractTask {

		private long[] account_ids;
		private String content;
		private Location location;
		private Uri image_uri;
		private long in_reply_to;

		public UpdateStatusTask(long[] account_ids, String content, Location location,
				Uri image_uri, long in_reply_to) {
			this.account_ids = account_ids;
			this.content = content;
			this.location = location;
			this.image_uri = image_uri;
			this.in_reply_to = in_reply_to;
		}

		@Override
		protected Void doInBackground(Void... params) {

			if (account_ids == null) return null;

			for (long account_id : account_ids) {
				Twitter twitter = CommonUtils.getTwitterInstance(UpdateService.this, account_id);
				if (twitter != null) {
					try {
						StatusUpdate status = new StatusUpdate(content);
						status.setInReplyToStatusId(in_reply_to);
						if (location != null) {
							status.setLocation(new GeoLocation(location.getLatitude(), location
									.getLongitude()));
						}
						String image_path = CommonUtils.getImagePathFromUri(UpdateService.this,
								image_uri);
						if (image_path != null) {
							status.setMedia(new File(image_path));
						}
						twitter.updateStatus(status);
					} catch (TwitterException e) {
						e.printStackTrace();
					}
				}
			}
			return null;
		}

	}

}
