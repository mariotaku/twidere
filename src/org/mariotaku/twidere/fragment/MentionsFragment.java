package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.provider.TweetStore.Mentions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

public class MentionsFragment extends CursorStatusesListFragment {

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, MentionsFragment.this);
			} else if (BROADCAST_MENTIONS_REFRESHED.equals(action)) {
				if (!intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					getListView().onRefreshComplete();
				}
			} else if (BROADCAST_MENTIONS_DATABASE_UPDATED.equals(action)) {
				getListView().onRefreshComplete();
				getLoaderManager().restartLoader(0, null, MentionsFragment.this);
			} else if ((MentionsFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP).equals(action))
				if (getListView() != null) {
					getListView().getRefreshableView().setSelection(0);
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
		IntentFilter filter = new IntentFilter(BROADCAST_MENTIONS_REFRESHED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_MENTIONS_DATABASE_UPDATED);
		filter.addAction(getClass().getName() + SHUFFIX_SCROLL_TO_TOP);
		if (getActivity() != null) {
			getActivity().registerReceiver(mStatusReceiver, filter);
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
