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

import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.util.List;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.loader.DummyParcelableStatusesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.Loader;
import org.mariotaku.twidere.R;

public class ConversationFragment extends ParcelableStatusesListFragment {

	private static final int ADD_STATUS = 1;
	private static final long INVALID_ID = -1;

	private ShowConversationTask mShowConversationTask;
	private StatusHandler mStatusHandler;
	private ParcelableStatusesAdapter mAdapter;

	@Override
	public boolean isLoaderUsed() {
		return false;
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Bundle args) {
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		return new DummyParcelableStatusesLoader(getActivity(), account_id, getData());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setPullToRefreshEnabled(false);
		mAdapter = getListAdapter();
		mAdapter.setGapDisallowed(true);
		Bundle bundle = getArguments();
		if (bundle == null) {
			bundle = new Bundle();
		}
		final long account_id = bundle.getLong(INTENT_KEY_ACCOUNT_ID, INVALID_ID);
		final long status_id = bundle.getLong(INTENT_KEY_STATUS_ID, INVALID_ID);

		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		mStatusHandler = new StatusHandler(mAdapter, account_id);
		mShowConversationTask = new ShowConversationTask(mStatusHandler, account_id, status_id);

		if (account_id != INVALID_ID && status_id != INVALID_ID) {
			mShowConversationTask.execute();
		}
	}

	@Override
	public void onDataLoaded(final Loader<List<ParcelableStatus>> loader, final ParcelableStatusesAdapter adapter) {

	}

	@Override
	public void onDestroyView() {
		setProgressBarIndeterminateVisibility(false);
		super.onDestroyView();
	}

	class ShowConversationTask extends AsyncTask<Void, Void, TwitterException> {

		private final long mAccountId, mStatusId;
		private final StatusHandler mHandler;

		public ShowConversationTask(final StatusHandler handler, final long account_id, final long status_id) {
			mHandler = handler;
			mAccountId = account_id;
			mStatusId = status_id;
		}

		@Override
		protected TwitterException doInBackground(final Void... params) {
			final Twitter twitter = getTwitterInstance(getActivity(), mAccountId, true);
			if (twitter == null) return null;
			try {
				ParcelableStatus p_status = findStatusInDatabases(getActivity(), mAccountId, mStatusId);
				twitter4j.Status status = null;
				if (p_status == null) {
					status = twitter.showStatus(mStatusId);
					if (status == null) return null;
					p_status = new ParcelableStatus(status, mAccountId, false);
				}
				mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, p_status));
				long in_reply_to_id = p_status.in_reply_to_status_id;
				while (in_reply_to_id != -1) {
					p_status = findStatusInDatabases(getActivity(), mAccountId, in_reply_to_id);
					if (p_status == null) {
						status = twitter.showStatus(in_reply_to_id);
						if (status == null) {
							break;
						}
						p_status = new ParcelableStatus(status, mAccountId, false);
					}
					if (p_status.status_id <= 0) {
						break;
					}
					mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, p_status));
					in_reply_to_id = p_status.in_reply_to_status_id;
				}
			} catch (final TwitterException e) {
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(final TwitterException result) {
			if (result != null) {
				showErrorToast(getActivity(), getString(R.string.get_status), result, true);
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

	static class StatusHandler extends Handler {

		private final ParcelableStatusesAdapter mAdapter;

		public StatusHandler(final ParcelableStatusesAdapter adapter, final long account_id) {
			mAdapter = adapter;
		}

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
				case ADD_STATUS:
					final Object obj = msg.obj;
					if (obj instanceof ParcelableStatus) {
						mAdapter.add((ParcelableStatus) obj);
					}
					break;
			}
			super.handleMessage(msg);
		}
	}

}
