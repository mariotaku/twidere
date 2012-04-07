package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusItemHolder;
import org.mariotaku.twidere.widget.RefreshableListView;
import org.mariotaku.twidere.widget.RefreshableListView.OnRefreshListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

public class HomeTabFragment extends SherlockListFragment implements Constants, OnRefreshListener,
		LoaderCallbacks<Cursor>, OnScrollListener {

	private StatusesAdapter mAdapter;
	private LazyImageLoader mListProfileImageLoader;
	private CommonUtils mCommonUtils;
	private ServiceInterface mServiceInterface;
	private RefreshableListView mListView;
	private int mAccountIdIdx, mStatusIdIdx, mStatusTimestampIdx,
			mScreenNameIdx, mProfileImageUrlIdx, mIsRetweetIdx, mIsFavoriteIdx, mIsGapIdx, mHasLocationIdx, mHasMediaIdx;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
				mListView.completeRefreshing();
				getLoaderManager().restartLoader(0, null, HomeTabFragment.this);
			} else if ((HomeTabFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP)
					.equals(action)) {
				if (mListView != null) {
					mListView.smoothScrollToPosition(0);
				}
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d("debug", "onActivityCreated, value = " + toString());
		super.onActivityCreated(savedInstanceState);
		mListProfileImageLoader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mCommonUtils = ((TwidereApplication) getSherlockActivity().getApplication())
				.getCommonUtils();
		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication())
				.getServiceInterface();
		mAdapter = new StatusesAdapter(getSherlockActivity());
		setListAdapter(mAdapter);
		mListView = (RefreshableListView) getListView();
		mListView.setOnRefreshListener(this);
		mListView.setOnScrollListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = Statuses.COLUMNS;
		Uri uri = Statuses.CONTENT_URI;
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null,
				Statuses.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timeline, container, false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Object tag = v.getTag();
		if (tag instanceof StatusItemHolder) {
			StatusItemHolder holder = (StatusItemHolder) tag;
			long status_id = holder.status_id;
			long account_id = holder.account_id;
			if (holder.isGap()) {
				mServiceInterface.refreshHomeTimeline(new long[] { account_id },
						new long[] { status_id });
			} else {

			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
		mAccountIdIdx = data.getColumnIndexOrThrow(Statuses.ACCOUNT_ID);
		mStatusIdIdx = data.getColumnIndexOrThrow(Statuses.STATUS_ID);
		mStatusTimestampIdx = data.getColumnIndexOrThrow(Statuses.STATUS_TIMESTAMP);
		mScreenNameIdx = data.getColumnIndexOrThrow(Statuses.SCREEN_NAME);
		mProfileImageUrlIdx = data.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
		mIsRetweetIdx = data.getColumnIndexOrThrow(Statuses.IS_RETWEET);
		mIsFavoriteIdx = data.getColumnIndexOrThrow(Statuses.IS_FAVORITE);
		mIsGapIdx = data.getColumnIndexOrThrow(Statuses.IS_GAP);
		mHasLocationIdx = data.getColumnIndexOrThrow(Statuses.HAS_LOCATION);
		mHasMediaIdx = data.getColumnIndexOrThrow(Statuses.HAS_MEDIA);

	}

	@Override
	public void onRefresh() {
		String[] cols = new String[] { Accounts.USER_ID };
		Cursor cur = getSherlockActivity().getContentResolver().query(Accounts.CONTENT_URI, cols,
				null, null, null);

		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
			long[] ids = new long[cur.getCount()];
			for (int i = 0; i < cur.getCount(); i++) {
				cur.moveToPosition(i);
				ids[i] = cur.getLong(idx);
			}
			mServiceInterface.refreshHomeTimeline(ids, null);
			cur.close();
		}

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
			case SCROLL_STATE_IDLE:
			case SCROLL_STATE_TOUCH_SCROLL:
				view.invalidateViews();
				break;
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_HOME_TIMELINE_REFRESHED);
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

	private class StatusesAdapter extends SimpleCursorAdapter {

		public StatusesAdapter(Context context) {
			super(context, R.layout.tweet_list_item, null, new String[] { Statuses.NAME,
					Statuses.TEXT }, new int[] { R.id.user_name, R.id.tweet_content }, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			StatusItemHolder holder = (StatusItemHolder) view.getTag();

			if (holder == null) return;

			boolean is_gap = cursor.getInt(mIsGapIdx) == 1;

			holder.setIsGap(is_gap);
			holder.status_id = cursor.getLong(mStatusIdIdx);
			holder.account_id = cursor.getLong(mAccountIdIdx);

			if (!is_gap) {

				String screen_name = cursor.getString(mScreenNameIdx);
				String profile_image_url = cursor.getString(mProfileImageUrlIdx);
				boolean is_retweet = cursor.getInt(mIsRetweetIdx) == 1;
				boolean is_favorite = cursor.getInt(mIsFavoriteIdx) == 1;
				boolean has_media = cursor.getInt(mHasMediaIdx) == 1;
				boolean has_location = cursor.getInt(mHasLocationIdx) == 1;

				holder.screen_name.setText("@" + screen_name);
				holder.tweet_time.setText(mCommonUtils.formatToShortTimeString(cursor
						.getLong(mStatusTimestampIdx)));
				holder.tweet_time.setCompoundDrawablesWithIntrinsicBounds(0, 0, mCommonUtils.getTypeIcon(is_retweet, is_favorite, has_location, has_media), 0);
				URL url = null;
				try {
					url = new URL(profile_image_url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				if (url != null) {
					mListProfileImageLoader.displayImage(url, holder.profile_image);
				}
			}
			super.bindView(view, context, cursor);

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			StatusItemHolder viewholder = new StatusItemHolder(view);
			view.setTag(viewholder);
			return view;
		}

	}
}
