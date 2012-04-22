package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class ConnectFragment extends TimelineFragment {

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_MENTIONS_UPDATED.equals(action)) {
				mListView.onRefreshComplete();
				getLoaderManager().restartLoader(0, null, ConnectFragment.this);
			} else if ((ConnectFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP)
					.equals(action)) {
				if (mListView != null) {
					mListView.getRefreshableView().setSelection(0);
				}
			}
		}
	};

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = Mentions.COLUMNS;
		Uri uri = Mentions.CONTENT_URI;
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null,
				Mentions.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onRefresh() {
		String[] cols = new String[] { Accounts.USER_ID };
		Cursor cur = mResolver.query(Accounts.CONTENT_URI, cols, null, null, null);

		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
			long[] ids = new long[cur.getCount()];
			for (int i = 0; i < cur.getCount(); i++) {
				cur.moveToPosition(i);
				ids[i] = cur.getLong(idx);
			}
			mServiceInterface.refreshMentions(ids, null);
			cur.close();
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_MENTIONS_UPDATED);
		filter.addAction(getClass().getName() + SHUFFIX_SCROLL_TO_TOP);
		if (getSherlockActivity() != null) {
			getSherlockActivity().registerReceiver(mStatusReceiver, filter);
		}
	}

	@Override
	public void onStop() {
		if (getSherlockActivity() != null) {
			getSherlockActivity().unregisterReceiver(mStatusReceiver);
		}
		super.onStop();
	}
}
