package org.mariotaku.twidere.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.IUpdateService;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;

public class UpdateService extends Service implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);
	private RefreshHomeTimelineTask mRefreshHomeTimelineTask;

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public void refreshHomeTimeline(long[] account_ids, int count) {
		if (mRefreshHomeTimelineTask != null) {
			mRefreshHomeTimelineTask.cancel(true);
		}
		mRefreshHomeTimelineTask = new RefreshHomeTimelineTask(account_ids, count);
		mRefreshHomeTimelineTask.execute();
	}

	public void refreshMentions(long[] account_ids, int count) {
	}

	public void refreshMessages(long[] account_ids, int count) {
	}

	private class RefreshHomeTimelineTask extends
			AsyncTask<Void, Void, List<RefreshHomeTimelineTask.AccountResponce>> {

		private long[] account_ids;

		public RefreshHomeTimelineTask(long[] account_ids, int count) {
			this.account_ids = account_ids;
		}

		@Override
		protected List<AccountResponce> doInBackground(Void... params) {

			List<AccountResponce> result = new ArrayList<AccountResponce>();

			for (long account_id : account_ids) {
				StringBuilder where = new StringBuilder();
				where.append(Accounts.USER_ID + "='" + account_id + "'");
				Cursor cur = getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS,
						where.toString(), null, null);
				if (cur != null) {
					if (cur.getCount() == 1) {
						cur.moveToFirst();
						ConfigurationBuilder cb = new ConfigurationBuilder();
						cb.setRestBaseURL(cur.getString(cur
								.getColumnIndexOrThrow(Accounts.REST_API_BASE)));
						cb.setSearchBaseURL(cur.getString(cur
								.getColumnIndexOrThrow(Accounts.SEARCH_API_BASE)));
						Twitter twitter = null;
						switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
							case Accounts.AUTH_TYPE_OAUTH:
								cb.setOAuthConsumerKey(CONSUMER_KEY);
								cb.setOAuthConsumerSecret(CONSUMER_SECRET);
								twitter = new TwitterFactory(cb.build())
										.getInstance(new AccessToken(
												cur.getString(cur
														.getColumnIndexOrThrow(Accounts.OAUTH_TOKEN)),
												cur.getString(cur
														.getColumnIndexOrThrow(Accounts.TOKEN_SECRET))));
								break;
							case Accounts.AUTH_TYPE_BASIC:
								twitter = new TwitterFactory(cb.build())
										.getInstance(new BasicAuthorization(
												cur.getString(cur
														.getColumnIndexOrThrow(Accounts.USERNAME)),
												cur.getString(cur
														.getColumnIndexOrThrow(Accounts.BASIC_AUTH_PASSWORD))));
								break;
							default:
						}
						if (twitter != null) {
							try {
								result.add(new AccountResponce(account_id, twitter
										.getHomeTimeline()));
							} catch (TwitterException e) {
								e.printStackTrace();
							}
						}
					}
					cur.close();
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<AccountResponce> responces) {
			for (AccountResponce responce : responces) {
				ResponseList<twitter4j.Status> statuses = responce.responselist;
				long account_id = responce.account_id;
				if (statuses == null) return;
				ContentValues[] values_array = new ContentValues[statuses.size()];
				int idx = 0;
				for (twitter4j.Status status : statuses) {
					User user = status.getUser();
					ContentValues values = new ContentValues();
					values.put(Statuses.ACCOUNT_ID, account_id);
					values.put(Statuses.STATUS_ID, status.getId());
					values.put(Statuses.USER_ID, user.getId());
					values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
					values.put(Statuses.TEXT, status.getText());
					values.put(Statuses.NAME, user.getName());
					values.put(Statuses.SCREEN_NAME, user.getScreenName());
					values.put(Statuses.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
					values.put(Statuses.IS_RETWEET, status.isRetweet() ? 1 : 0);
					values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);
					values_array[idx] = values;
					idx++;
				}
				getContentResolver().bulkInsert(Statuses.CONTENT_URI, values_array);
			}
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED));
			mRefreshHomeTimelineTask = null;
		}

		public class AccountResponce {

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
		public void refreshHomeTimeline(long[] account_ids, int count) {
			mService.get().refreshHomeTimeline(account_ids, count);
		}

		@Override
		public void refreshMentions(long[] account_ids, int count) {
			mService.get().refreshMentions(account_ids, count);
		}

		@Override
		public void refreshMessages(long[] account_ids, int count) {
			mService.get().refreshMessages(account_ids, count);
		}

	}

}
