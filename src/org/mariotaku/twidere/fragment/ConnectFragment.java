package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.provider.TweetStore.Mentions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

public class ConnectFragment extends StatusesFragment {

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_MENTIONS_UPDATED.equals(action)) {
				mListView.onRefreshComplete();
				getLoaderManager().restartLoader(0, null, ConnectFragment.this);
			} else if ((ConnectFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP)
					.equals(action)) if (mListView != null) {
				mListView.getRefreshableView().setSelection(0);
			}
		}
	};

	@Override
	public Uri getContentUri() {
		return Mentions.CONTENT_URI;
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
