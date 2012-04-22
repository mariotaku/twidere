package org.mariotaku.twidere.fragment;

import java.util.ArrayList;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusItemHolder;
import org.mariotaku.twidere.widget.StatusesAdapter;

import roboguice.inject.InjectResource;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public abstract class TimelineFragment extends BaseFragment implements OnRefreshListener,
		LoaderCallbacks<Cursor>, OnScrollListener, OnItemClickListener, OnItemLongClickListener,
		ActionMode.Callback {

	@InjectResource(R.color.holo_blue_bright) private int mActivedMenuColor;

	private StatusesAdapter mAdapter;
	public ServiceInterface mServiceInterface;
	public PullToRefreshListView mListView;
	private boolean mBusy, mTickerStopped;

	private Handler mHandler;
	private Runnable mTicker;
	public ContentResolver mResolver;
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
			int textIdx = cur.getColumnIndexOrThrow(Statuses.TEXT);
			String text = cur.getString(textIdx);
			long status_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.STATUS_ID));
			long account_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.ACCOUNT_ID));
			switch (item.getItemId()) {
				case MENU_REPLY:
					break;
				case MENU_RETWEET:
					break;
				case MENU_QUOTE:
					break;
				case MENU_FAV:
					mServiceInterface.favStatus(new long[] { account_id }, status_id);
					break;
			}
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
			return true;
		}
		return false;
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
			long user_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));
			menu.findItem(MENU_DELETE).setVisible(ids.contains(user_id));
			MenuItem itemFav = menu.findItem(MENU_FAV);
			if (cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1) {
				itemFav.getIcon().setColorFilter(mActivedMenuColor, Mode.MULTIPLY);
				itemFav.setTitle(R.string.unfav);
			} else {
				itemFav.getIcon().clearColorFilter();
				itemFav.setTitle(R.string.fav);
			}
		}
		if (cur != null) {
			cur.close();
		}
		return true;
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
		super.onStop();
	}
}
