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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.*;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import org.mariotaku.twidere.util.ManagedAsyncTask;
import java.util.List;
import java.util.Arrays;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.Twitter;
import twitter4j.Paging;
import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.R;
import java.util.ArrayList;
import android.content.ContentValues;
import org.mariotaku.twidere.model.SingleResponse;
import android.net.Uri;
import android.os.Bundle;
import android.content.ContentResolver;
import org.mariotaku.twidere.util.ListUtils;
import edu.ucdavis.earlybird.ProfilingUtil;
import org.mariotaku.twidere.util.NameValuePairImpl;
import java.util.Collections;
import org.mariotaku.twidere.util.CacheUsersStatusesTask;

public class ApplicationBackendService extends Service implements Constants {

	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;
	private ContentResolver mResolver;

	private int mGetHomeTimelineTaskId, mGetMentionsTaskId;
	private int mGetReceivedDirectMessagesTaskId, mGetSentDirectMessagesTaskId;

	private final BroadcastReceiver mTaskStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				if (!mAsyncTaskManager.hasRunningTask()) {
					stopSelf();
				}
			}
		}
	};

	@Override
	public IBinder onBind(final Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mAsyncTaskManager = ((TwidereApplication) getApplication()).getAsyncTaskManager();
		mResolver = getContentResolver();
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mTaskStateReceiver, filter);
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mTaskStateReceiver);
		super.onDestroy();
	}

	@Override
	public void onStart(final Intent intent, final int startId) {
		final String command = intent.getStringExtra(INTENT_KEY_COMMAND);
		if (INTENT_ACTION_SERVICE_COMMAND.equals(intent.getAction()) || isEmpty(command)) {
			stopSelf();
			return;
		}
		if (SERVICE_COMMAND_REFRESH_ALL.equals(command)) {
			refreshAll();
		} else if (SERVICE_COMMAND_GET_HOME_TIMELINE.equals(command)) {
			final long[] account_ids = intent.getLongArrayExtra(INTENT_KEY_ACCOUNT_IDS);
			final long[] max_ids = intent.getLongArrayExtra(INTENT_KEY_MAX_IDS);
			final long[] since_ids = intent.getLongArrayExtra(INTENT_KEY_SINCE_IDS);
			getHomeTimeline(account_ids, max_ids, since_ids);
		} else if (SERVICE_COMMAND_GET_MENTIONS.equals(command)) {
			final long[] account_ids = intent.getLongArrayExtra(INTENT_KEY_ACCOUNT_IDS);
			final long[] max_ids = intent.getLongArrayExtra(INTENT_KEY_MAX_IDS);
			final long[] since_ids = intent.getLongArrayExtra(INTENT_KEY_SINCE_IDS);
			getMentions(account_ids, max_ids, since_ids);
		} else if (SERVICE_COMMAND_GET_SENT_DIRECT_MESSAGES.equals(command)) {
			final long[] account_ids = intent.getLongArrayExtra(INTENT_KEY_ACCOUNT_IDS);
			final long[] max_ids = intent.getLongArrayExtra(INTENT_KEY_MAX_IDS);
			final long[] since_ids = intent.getLongArrayExtra(INTENT_KEY_SINCE_IDS);
			getSentDirectMessages(account_ids, max_ids, since_ids);
		} else if (SERVICE_COMMAND_GET_RECEIVED_DIRECT_MESSAGES.equals(command)) {
			final long[] account_ids = intent.getLongArrayExtra(INTENT_KEY_ACCOUNT_IDS);
			final long[] max_ids = intent.getLongArrayExtra(INTENT_KEY_MAX_IDS);
			final long[] since_ids = intent.getLongArrayExtra(INTENT_KEY_SINCE_IDS);
			getReceivedDirectMessages(account_ids, max_ids, since_ids);
		}
	}

	private int getHomeTimeline(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetHomeTimelineTaskId);
		// final GetHomeTimelineTask task = new GetHomeTimelineTask(account_ids,
		// max_ids, since_ids);
		// return mGetHomeTimelineTaskId = mAsyncTaskManager.add(task, true);
		return -1;
	}

	private int getMentions(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetMentionsTaskId);
		// final GetMentionsTask task = new GetMentionsTask(account_ids,
		// max_ids, since_ids);
		// return mGetMentionsTaskId = mAsyncTaskManager.add(task, true);
		return -1;
	}

	private int getReceivedDirectMessages(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetReceivedDirectMessagesTaskId);
		// final GetReceivedDirectMessagesTask task = new
		// GetReceivedDirectMessagesTask(account_ids, max_ids, since_ids);
		// return mGetReceivedDirectMessagesTaskId = mAsyncTaskManager.add(task,
		// true);
		return -1;
	}

	private int getSentDirectMessages(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
		mAsyncTaskManager.cancel(mGetSentDirectMessagesTaskId);
		// final GetSentDirectMessagesTask task = new
		// GetSentDirectMessagesTask(account_ids, max_ids, since_ids);
		// return mGetSentDirectMessagesTaskId = mAsyncTaskManager.add(task,
		// true);
		return -1;
	}

	private int refreshAll() {
		final long[] account_ids = getActivatedAccountIds(this);
		if (mPreferences.getBoolean(PREFERENCE_KEY_HOME_REFRESH_MENTIONS, false)) {
			final long[] since_ids = getNewestStatusIdsFromDatabase(this, Mentions.CONTENT_URI);
			getMentions(account_ids, null, since_ids);
		}
		if (mPreferences.getBoolean(PREFERENCE_KEY_HOME_REFRESH_DIRECT_MESSAGES, false)) {
			final long[] since_ids = getNewestMessageIdsFromDatabase(this, DirectMessages.Inbox.CONTENT_URI);
			getReceivedDirectMessages(account_ids, null, since_ids);
			getSentDirectMessages(account_ids, null, null);
		}
		final long[] since_ids = getNewestStatusIdsFromDatabase(this, Statuses.CONTENT_URI);
		return getHomeTimeline(account_ids, null, since_ids);
	}
	

	abstract class GetStatusesTask extends BaseAsyncTask<List<StatusesListResponse<twitter4j.Status>>> {

		private final long[] account_ids, max_ids, since_ids;

		public GetStatusesTask(final long[] account_ids, final long[] max_ids, final long[] since_ids, final String tag) {
			super(tag);
			this.account_ids = account_ids;
			this.max_ids = max_ids;
			this.since_ids = since_ids;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) return true;
			if (!super.equals(obj)) return false;
			if (!(obj instanceof GetStatusesTask)) return false;
			final GetStatusesTask other = (GetStatusesTask) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (!Arrays.equals(account_ids, other.account_ids)) return false;
			if (!Arrays.equals(max_ids, other.max_ids)) return false;
			return true;
		}

		public abstract ResponseList<twitter4j.Status> getStatuses(Twitter twitter, Paging paging)
		throws TwitterException;

		public abstract Twitter getTwitter(long account_id);

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + getOuterType().hashCode();
			result = prime * result + Arrays.hashCode(account_ids);
			result = prime * result + Arrays.hashCode(max_ids);
			return result;
		}

		@Override
		protected List<StatusesListResponse<twitter4j.Status>> doInBackground(final Void... params) {

			final List<StatusesListResponse<twitter4j.Status>> result = new ArrayList<StatusesListResponse<twitter4j.Status>>();

			if (account_ids == null) return result;

			int idx = 0;
			final int load_item_limit = mPreferences.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT,
															PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			for (final long account_id : account_ids) {
				final Twitter twitter = getTwitter(account_id);
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
						final ResponseList<twitter4j.Status> statuses = getStatuses(twitter, paging);
						if (statuses != null) {
							result.add(new StatusesListResponse<twitter4j.Status>(account_id, max_id, since_id,
																				  load_item_limit, statuses, null));
						}
					} catch (final TwitterException e) {
						result.add(new StatusesListResponse<twitter4j.Status>(account_id, -1, -1, load_item_limit,
																			  null, e));
					}
				}
				idx++;
			}
			return result;
		}

		@Override
		protected void onPostExecute(final List<StatusesListResponse<twitter4j.Status>> result) {
			super.onPostExecute(result);
			for (final StatusesListResponse<twitter4j.Status> response : result) {
				if (response.list == null) {
					showErrorToast(service, R.string.refreshing_timelines, response.exception, true);
				}
			}
		}

		private ApplicationBackendService getOuterType() {
			return ApplicationBackendService.this;
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
		public Twitter getTwitter(final long account_id) {
			return getTwitterInstance(service, account_id, true);
		}

		@Override
		protected void onPostExecute(final List<StatusesListResponse<twitter4j.Status>> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreHomeTimelineTask(responses, shouldSetMinId(), !isMaxIdsValid()), true);
			mGetHomeTimelineTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			sendBroadcast(new Intent(BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING));
			super.onPreExecute();
		}

	}
	

	class GetMentionsTask extends GetStatusesTask {

		public GetMentionsTask(final long[] account_ids, final long[] max_ids, final long[] since_ids) {
			super(account_ids, max_ids, since_ids, TASK_TAG_GET_MENTIONS);
		}

		@Override
		public ResponseList<twitter4j.Status> getStatuses(final Twitter twitter, final Paging paging)
		throws TwitterException {
			return twitter.getMentions(paging);
		}

		@Override
		public Twitter getTwitter(final long account_id) {
			return getTwitterInstance(service, account_id, true);
		}

		@Override
		protected void onPostExecute(final List<StatusesListResponse<twitter4j.Status>> responses) {
			super.onPostExecute(responses);
			mAsyncTaskManager.add(new StoreMentionsTask(responses, shouldSetMinId(), !isMaxIdsValid()), true);
			mGetMentionsTaskId = -1;
		}

		@Override
		protected void onPreExecute() {
			sendBroadcast(new Intent(BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING));
			super.onPreExecute();
		}

	}
	
	
	class StoreHomeTimelineTask extends StoreStatusesTask {

		public StoreHomeTimelineTask(final List<StatusesListResponse<twitter4j.Status>> result,
				final boolean should_set_min_id, final boolean notify) {
			super(result, Statuses.CONTENT_URI, should_set_min_id, notify, TASK_TAG_STORE_HOME_TIMELINE);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			final boolean succeed = response != null && response.data != null
					&& response.data.getBoolean(INTENT_KEY_SUCCEED);
			final Bundle extras = new Bundle();
			extras.putBoolean(INTENT_KEY_SUCCEED, succeed);
			if (shouldSetMinId()) {
				final long min_id = response != null && response.data != null ? response.data.getLong(
						INTENT_KEY_MIN_ID, -1) : -1;
				extras.putLong(INTENT_KEY_MIN_ID, min_id);
				mPreferences.edit().putLong(PREFERENCE_KEY_SAVED_HOME_TIMELINE_ID, min_id).commit();
			}
			sendBroadcast(new Intent(BROADCAST_HOME_TIMELINE_REFRESHED).putExtras(extras));
			super.onPostExecute(response);
		}

	}
	
	abstract class BaseAsyncTask<Result> extends ManagedAsyncTask<Void, Void, Result> {

		protected final ApplicationBackendService service;
		
		public BaseAsyncTask(final String tag) {
			super(ApplicationBackendService.this, mAsyncTaskManager, tag);
			this.service = ApplicationBackendService.this;
		}
	}
	

	abstract class StoreStatusesTask extends BaseAsyncTask<SingleResponse<Bundle>> {

		private final List<StatusesListResponse<twitter4j.Status>> responses;
		private final Uri uri;
		private final boolean should_set_min_id;
		private final ArrayList<ContentValues> all_statuses = new ArrayList<ContentValues>();

		public StoreStatusesTask(final List<StatusesListResponse<twitter4j.Status>> result, final Uri uri,
								 final boolean should_set_min_id, final boolean notify, final String tag) {
			super(tag);
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
			for (final StatusesListResponse<twitter4j.Status> response : responses) {
				final long account_id = response.account_id;
				final List<twitter4j.Status> statuses = response.list;
				if (statuses == null || statuses.size() <= 0) {
					continue;
				}
				final ArrayList<Long> ids_in_db = getStatusIdsInDatabase(service, uri, account_id);
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
						values_list.add(makeStatusContentValues(status, account_id));
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
				ProfilingUtil.profile(service, account_id, "Download tweets, " + UCD_new_status_ids);
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
			bundle.putBoolean(INTENT_KEY_SUCCEED, succeed);
			getAllStatusesIds(service, uri);
			if (should_set_min_id && newly_inserted_ids.size() > 0) {
				bundle.putLong(INTENT_KEY_MIN_ID, Collections.min(newly_inserted_ids));
			}
			return new SingleResponse<Bundle>(-1, bundle, null);
		}

		@Override
		protected void onPostExecute(final SingleResponse<Bundle> response) {
			super.onPostExecute(response);
			new CacheUsersStatusesTask(service, all_statuses).execute();
		}

	}
	
	
	static final class StatusesListResponse<Data> extends ListResponse<Data> {

		public final long max_id, since_id;
		public final int load_item_limit;

		public StatusesListResponse(final long account_id, final long max_id, final long since_id,
				final int load_item_limit, final List<Data> list, final Exception exception) {
			super(account_id, list, exception);
			this.max_id = max_id;
			this.since_id = since_id;
			this.load_item_limit = load_item_limit;
		}

	}

}
