package org.mariotaku.twidere.fragment;

import java.util.ArrayList;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusItemHolder;
import org.mariotaku.twidere.widget.StatusesAdapter;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class HomeTimelineFragment extends BaseFragment implements Constants, OnRefreshListener,
		LoaderCallbacks<Cursor>, OnScrollListener, OnItemClickListener, OnItemLongClickListener,
		ActionMode.Callback {

	private StatusesAdapter mAdapter;
	private ServiceInterface mServiceInterface;
	private PullToRefreshListView mListView;
	private boolean mBusy, mTickerStopped;

	private Handler mHandler;
	private Runnable mTicker;
	private ContentResolver mResolver;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_HOME_TIMELINE_REFRESHED.equals(action)) {
				mListView.onRefreshComplete();
				getLoaderManager().restartLoader(0, null, HomeTimelineFragment.this);
			} else if ((HomeTimelineFragment.this.getClass().getName() + SHUFFIX_SCROLL_TO_TOP)
					.equals(action)) {
				if (mListView != null) {
					mListView.getRefreshableView().setSelection(0);
				}
			}
		}
	};
	private boolean mBottomReached, mDisplayProfileImage;
	private SharedPreferences mPreferences;

	private long mSelectedStatusId;

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		Uri uri = Statuses.CONTENT_URI;
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + mSelectedStatusId;
		Cursor cur = mResolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			int idx = cur.getColumnIndexOrThrow(Statuses.TEXT);
			String text = cur.getString(idx);
			Toast.makeText(
					getSherlockActivity(),
					"Item " + item.getTitle() + " in fragment " + getClass().getSimpleName()
							+ " clicked, tweet content: " + text, Toast.LENGTH_LONG).show();
		}
		if (cur != null) {
			cur.close();
		}
		mode.finish();
		return true;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSherlockActivity().getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		mResolver = getSherlockActivity().getContentResolver();
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication())
				.getServiceInterface();
		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mAdapter = new StatusesAdapter(getSherlockActivity(), imageloader);
		mListView = (PullToRefreshListView) getView().findViewById(R.id.refreshable_list);
		mListView.setOnRefreshListener(this);
		ListView list = mListView.getRefreshableView();
		list.setOnScrollListener(this);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		list.setAdapter(mAdapter);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.action_status, menu);
		return true;
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
		return inflater.inflate(R.layout.refreshable_list, container, false);
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusItemHolder) {
			StatusItemHolder holder = (StatusItemHolder) tag;
			long status_id = holder.status_id;
			long account_id = holder.account_id;
			if (holder.isGap()) {
				mServiceInterface.refreshHomeTimeline(new long[] { account_id },
						new long[] { status_id });
			} else {
				Bundle bundle = new Bundle();
				bundle.putLong(Statuses.ACCOUNT_ID, account_id);
				bundle.putLong(Statuses.STATUS_ID, status_id);
				bundle.putInt(TweetStore.KEY_TYPE, TweetStore.VALUE_TYPE_STATUS);
				Intent intent = new Intent(INTENT_ACTION_VIEW_STATUS).putExtras(bundle);
				startActivity(intent);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusItemHolder) {
			StatusItemHolder holder = (StatusItemHolder) tag;
			if (holder.isGap()) return false;
			mSelectedStatusId = holder.status_id;
			getSherlockActivity().startActionMode(this);
		}
		return true;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		String[] accounts_cols = new String[] { Accounts.USER_ID };
		Cursor accounts_cur = mResolver
				.query(Accounts.CONTENT_URI, accounts_cols, null, null, null);
		ArrayList<Long> ids = new ArrayList<Long>();
		if (accounts_cur != null) {
			accounts_cur.moveToFirst();
			int idx = accounts_cur.getColumnIndexOrThrow(Accounts.USER_ID);
			while (!accounts_cur.isAfterLast()) {
				ids.add(accounts_cur.getLong(idx));
				accounts_cur.moveToNext();
			}
			accounts_cur.close();
		}
		Uri uri = Statuses.CONTENT_URI;
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + mSelectedStatusId;
		Cursor cur = mResolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			int idx = cur.getColumnIndexOrThrow(Statuses.USER_ID);
			long user_id = cur.getLong(idx);
			menu.findItem(MENU_DELETE).setVisible(ids.contains(user_id));
		}
		if (cur != null) {
			cur.close();
		}
		return true;
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
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(
				PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
		if (mDisplayProfileImage != display_profile_image) {
			mDisplayProfileImage = display_profile_image;
			mListView.getRefreshableView().invalidateViews();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		boolean reached = firstVisibleItem + visibleItemCount == totalItemCount;

		if (mBottomReached != reached) {
			mBottomReached = reached;
			if (mBottomReached) {
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
			case SCROLL_STATE_TOUCH_SCROLL:
				mBusy = true;
				break;
			case SCROLL_STATE_IDLE:
				mBusy = false;
				break;
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		mTickerStopped = false;
		IntentFilter filter = new IntentFilter(BROADCAST_HOME_TIMELINE_REFRESHED);
		filter.addAction(getClass().getName() + SHUFFIX_SCROLL_TO_TOP);
		if (getSherlockActivity() != null) {
			getSherlockActivity().registerReceiver(mStatusReceiver, filter);
		}
		mHandler = new Handler();

		mTicker = new Runnable() {

			@Override
			public void run() {
				if (mTickerStopped) return;
				if (mListView != null && !mBusy) {
					mListView.getRefreshableView().invalidateViews();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + 1000 - now % 1000;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		if (getSherlockActivity() != null) {
			getSherlockActivity().unregisterReceiver(mStatusReceiver);
		}
		super.onStop();
	}
}
