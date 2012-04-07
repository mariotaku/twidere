package org.mariotaku.twidere.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.IUpdateService;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;

public class UpdateService extends Service implements Constants {

	private final ServiceStub mBinder = new ServiceStub(this);
	private RefreshHomeTimelineTask mRefreshHomeTimelineTask;
	private RefreshMentionsTask mRefreshMentionsTask;

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
		if (mRefreshMentionsTask != null) {
			mRefreshMentionsTask.cancel(true);
		}
		mRefreshMentionsTask = new RefreshMentionsTask(account_ids, count);
		mRefreshMentionsTask.execute();
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
						String rest_api_base = cur.getString(cur
								.getColumnIndexOrThrow(Accounts.REST_API_BASE));
						String search_api_base = cur.getString(cur
								.getColumnIndexOrThrow(Accounts.SEARCH_API_BASE));
						if (rest_api_base == null || "".equals(rest_api_base)) {
							rest_api_base = DEFAULT_REST_API_BASE;
						}
						if (search_api_base == null || "".equals(search_api_base)) {
							search_api_base = DEFAULT_SEARCH_API_BASE;
						}
						cb.setRestBaseURL(rest_api_base);
						cb.setSearchBaseURL(search_api_base);
						Twitter twitter = null;
						switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
							case Accounts.AUTH_TYPE_OAUTH:
							case Accounts.AUTH_TYPE_XAUTH:
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
			ContentResolver resolver = getContentResolver();

			for (AccountResponce responce : responces) {
				ResponseList<twitter4j.Status> statuses = responce.responselist;
				long account_id = responce.account_id;
				if (statuses == null || statuses.size() <= 0) return;
				List<ContentValues> values_list = new ArrayList<ContentValues>();
				int idx = 0;
				for (twitter4j.Status status : statuses) {
					ContentValues values = new ContentValues();
					User user = status.getUser();
					values.put(Statuses.STATUS_ID, status.getId());
					values.put(Statuses.ACCOUNT_ID, account_id);
					values.put(Statuses.USER_ID, user.getId());
					values.put(Statuses.STATUS_TIMESTAMP, status.getCreatedAt().getTime());
					values.put(Statuses.TEXT, status.getText());
					values.put(Statuses.NAME, user.getName());
					values.put(Statuses.SCREEN_NAME, user.getScreenName());
					values.put(Statuses.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
					values.put(Statuses.IS_RETWEET, status.isRetweet() ? 1 : 0);
					values.put(Statuses.IS_FAVORITE, status.isFavorited() ? 1 : 0);

					StringBuilder where = new StringBuilder();
					where.append(Statuses.STATUS_ID + "='" + status.getId() + "'");
					where.append(" AND " + Statuses.ACCOUNT_ID + "='" + account_id + "'");
					Cursor cur = resolver.query(Statuses.CONTENT_URI, new String[] {},
							where.toString(), null, null);

					if (cur != null) {
						if (idx == statuses.size() - 1) {
							if (cur.getCount() > 0) {
								resolver.delete(Statuses.CONTENT_URI, where.toString(), null);
							} else {
								values.put(Statuses.IS_GAP, 1);
							}
							values_list.add(values);
						} else if (cur.getCount() <= 0) {
							values_list.add(values);
						}
						cur.close();
					} else {
						values_list.add(values);
					}
					idx++;
				}
				resolver.bulkInsert(Statuses.CONTENT_URI,
						values_list.toArray(new ContentValues[values_list.size()]));
			}
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED));
			mRefreshHomeTimelineTask = null;
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

	private class RefreshMentionsTask extends
			AsyncTask<Void, Void, List<RefreshMentionsTask.AccountResponce>> {

		private long[] account_ids;

		public RefreshMentionsTask(long[] account_ids, int count) {
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
						String rest_api_base = cur.getString(cur
								.getColumnIndexOrThrow(Accounts.REST_API_BASE));
						String search_api_base = cur.getString(cur
								.getColumnIndexOrThrow(Accounts.SEARCH_API_BASE));
						if (rest_api_base == null || "".equals(rest_api_base)) {
							rest_api_base = DEFAULT_REST_API_BASE;
						}
						if (search_api_base == null || "".equals(search_api_base)) {
							search_api_base = DEFAULT_SEARCH_API_BASE;
						}
						cb.setRestBaseURL(rest_api_base);
						cb.setSearchBaseURL(search_api_base);
						Twitter twitter = null;
						switch (cur.getInt(cur.getColumnIndexOrThrow(Accounts.AUTH_TYPE))) {
							case Accounts.AUTH_TYPE_OAUTH:
							case Accounts.AUTH_TYPE_XAUTH:
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
								result.add(new AccountResponce(account_id, twitter.getMentions()));
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
			ContentResolver resolver = getContentResolver();
			for (AccountResponce responce : responces) {
				ResponseList<twitter4j.Status> mentions = responce.responselist;
				long account_id = responce.account_id;
				if (mentions == null) return;
				List<ContentValues> values_list = new ArrayList<ContentValues>();
				int idx = 0;
				for (twitter4j.Status mention : mentions) {
					ContentValues values = new ContentValues();
					User user = mention.getUser();
					values.put(Mentions.ACCOUNT_ID, account_id);
					values.put(Mentions.STATUS_ID, mention.getId());
					values.put(Mentions.USER_ID, user.getId());
					values.put(Mentions.STATUS_TIMESTAMP, mention.getCreatedAt().getTime());
					values.put(Mentions.TEXT, mention.getText());
					values.put(Mentions.NAME, user.getName());
					values.put(Mentions.SCREEN_NAME, user.getScreenName());
					values.put(Mentions.PROFILE_IMAGE_URL, user.getProfileImageURL().toString());
					values.put(Mentions.IS_RETWEET, mention.isRetweet() ? 1 : 0);
					values.put(Mentions.IS_FAVORITE, mention.isFavorited() ? 1 : 0);

					StringBuilder where = new StringBuilder();
					where.append(Mentions.STATUS_ID + "='" + mention.getId() + "'");
					where.append(" AND " + Mentions.ACCOUNT_ID + "='" + account_id + "'");
					Cursor cur = resolver.query(Mentions.CONTENT_URI, new String[] {},
							where.toString(), null, null);

					if (cur != null) {
						if (idx == mentions.size() - 1) {
							if (cur.getCount() > 0) {
								resolver.delete(Mentions.CONTENT_URI, where.toString(), null);
							} else {
								values.put(Mentions.IS_GAP, 1);
							}
							values_list.add(values);
						} else if (cur.getCount() <= 0) {
							values_list.add(values);
						}
						cur.close();
					} else {
						values_list.add(values);
					}
					idx++;
				}
				resolver.bulkInsert(Mentions.CONTENT_URI,
						values_list.toArray(new ContentValues[values_list.size()]));
			}
			sendBroadcast(new Intent(BROADCAST_MENTIONS_REFRESHED));
			mRefreshHomeTimelineTask = null;
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
