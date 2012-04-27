package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class UserTimelineFragment extends TimelineFragment {

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_HOME_TIMELINE_UPDATED.equals(action)) {
				mListView.onRefreshComplete();
				getLoaderManager().restartLoader(0, null, UserTimelineFragment.this);
			} else if ((UserTimelineFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP)
					.equals(action)) {
				if (mListView != null) {
					mListView.getRefreshableView().setSelection(0);
				}
			}
		}
	};

	public UserTimelineFragment() {
		super(Statuses.CONTENT_URI, TweetStore.VALUE_TYPE_STATUS);
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_HOME_TIMELINE_UPDATED);
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
