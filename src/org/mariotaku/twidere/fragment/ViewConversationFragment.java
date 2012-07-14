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

package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.*;

import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.DummyParcelableStatusLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.Loader;

public class ViewConversationFragment extends ParcelableStatusesListFragment {

	private static final int ADD_STATUS = 1;
	private static final long INVALID_ID = -1;
	
	private ShowConversationTask mShowConversationTask;
	private StatusHandler mStatusHandler;
	
	@Override
	public boolean isListLoadFinished() {
		return true;
	}
	
	public boolean isLoaderUsed() {
		return false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setPullToRefreshEnabled(false);
		Bundle bundle = getArguments();
		if (bundle == null) {
			bundle = new Bundle();
		}
		final long account_id = bundle.getLong(INTENT_KEY_ACCOUNT_ID, INVALID_ID);
		final long status_id = bundle.getLong(INTENT_KEY_STATUS_ID, INVALID_ID);

		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		mStatusHandler = new StatusHandler(getListAdapter(), account_id);
		mShowConversationTask = new ShowConversationTask(mStatusHandler, account_id, status_id);

		if (account_id != INVALID_ID && status_id != INVALID_ID) {
			mShowConversationTask.execute();
		}
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(Bundle args) {
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		return new DummyParcelableStatusLoader(getActivity(), account_id, getData());
	}

	@Override
	public void onDataLoaded(Loader<List<ParcelableStatus>> loader, ParcelableStatusesAdapter adapter) {

	}

	private class ShowConversationTask extends AsyncTask<Void, Void, TwitterException> {

		private final long mAccountId, mStatusId;
		private final StatusHandler mHandler;

		public ShowConversationTask(StatusHandler handler, long account_id, long status_id) {
			mHandler = handler;
			mAccountId = account_id;
			mStatusId = status_id;
		}

		@Override
		protected TwitterException doInBackground(Void... params) {
			final Twitter twitter = getTwitterInstance(getActivity(), mAccountId, true);
			try {
				twitter4j.Status status = twitter.showStatus(mStatusId);
				mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, status));
				long in_reply_to_id = status.getInReplyToStatusId();
				while (in_reply_to_id != -1) {
					status = twitter.showStatus(in_reply_to_id);
					if (status.getId() <= 0) {
						break;
					}
					mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, status));
					in_reply_to_id = status.getInReplyToStatusId();
				}
			} catch (final TwitterException e) {
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(TwitterException result) {
			if (result != null) {
				showErrorToast(getActivity(), result, true);
			}
			setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

	}

	private static class StatusHandler extends Handler {

		private final ParcelableStatusesAdapter mAdapter;
		private final long mAccountId;

		public StatusHandler(ParcelableStatusesAdapter adapter, long account_id) {
			mAdapter = adapter;
			mAccountId = account_id;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ADD_STATUS:
					final Object obj = msg.obj;
					if (obj instanceof Status) {
						mAdapter.add(new ParcelableStatus((Status) obj, mAccountId, false));
					}
					break;
			}
			super.handleMessage(msg);
		}
	}
}