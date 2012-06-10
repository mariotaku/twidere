package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

public class HomeTimelineFragment extends CursorStatusesListFragment {

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
			} else if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
			} else if (BROADCAST_HOME_TIMELINE_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
			} else if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				if (!getServiceInterface().isHomeTimelineRefreshing()){
					getListView().onRefreshComplete();
				}
			} else if ((HomeTimelineFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP).equals(action))
				if (getListView() != null) {
					getListView().getRefreshableView().setSelection(0);
				}
		}
	};

	@Override
	public Uri getContentUri() {
		return Statuses.CONTENT_URI;
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_HOME_TIMELINE_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_HOME_TIMELINE_DATABASE_UPDATED);
		filter.addAction(BROADCAST_REFRESHSTATE_CHANGED);
		filter.addAction(getClass().getName() + SHUFFIX_SCROLL_TO_TOP);
		if (getActivity() != null) {
			getActivity().registerReceiver(mStatusReceiver, filter);
		}
		if (!getServiceInterface().isHomeTimelineRefreshing()){
			getListView().onRefreshComplete();
		}
	}

	@Override
	public void onStop() {
		if (getActivity() != null) {
			getActivity().unregisterReceiver(mStatusReceiver);
		}
		super.onStop();
	}
}
