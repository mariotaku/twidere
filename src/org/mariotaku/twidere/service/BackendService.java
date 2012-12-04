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
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getNewestMessageIdsFromDatabase;
import static org.mariotaku.twidere.util.Utils.getNewestStatusIdsFromDatabase;

import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BackendService extends Service implements Constants {

	private AsyncTaskManager mAsyncTaskManager;
	private SharedPreferences mPreferences;

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
		if (!checkPermission(this)) {
			stopSelf();
			return;
		}
		final String action = intent.getAction();
		final String command = intent.getStringExtra(INTENT_KEY_COMMAND);
		if (!INTENT_ACTION_SERVICE_COMMAND.equals(action) || isEmpty(command)) {
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

	private boolean checkPermission(final Context context) {
		final String pname = getCallingPackageName(context);
		Log.d(LOGTAG, "Package " + pname + " called.");
		// TODO Stub!
		return true;
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

	private static String getCallingPackageName(final Context context) {
		final int pid = Binder.getCallingPid();
		final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
		if (processes == null) return null;
		for (final RunningAppProcessInfo process : processes) {
			if (process.pid == pid && process.pkgList.length > 0) return process.pkgList[0];
		}
		return null;
	}

	private static boolean isSameCertificate(final Context context) {
		final PackageManager pm = context.getPackageManager();
		return true;
	}
}
