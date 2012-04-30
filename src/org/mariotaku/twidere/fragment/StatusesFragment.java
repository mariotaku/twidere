package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusItemHolder;
import org.mariotaku.twidere.widget.StatusesAdapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
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

public abstract class StatusesFragment extends BaseFragment implements OnRefreshListener,
		LoaderCallbacks<Cursor>, OnScrollListener, OnItemClickListener, OnItemLongClickListener,
		ActionMode.Callback {

	public ServiceInterface mServiceInterface;
	public PullToRefreshListView mListView;
	public ContentResolver mResolver;
	public long mSelectedStatusId;

	private StatusesAdapter mAdapter;
	private boolean mBusy, mTickerStopped;

	private Handler mHandler;
	private Runnable mTicker;
	private boolean mBottomReached, mDisplayProfileImage, mActivityFirstCreated;
	private SharedPreferences mPreferences;

	public abstract Uri getContentUri();

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		Uri uri = getContentUri();
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + mSelectedStatusId;
		Cursor cur = mResolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			String text = Html.fromHtml(cur.getString(cur.getColumnIndexOrThrow(Statuses.TEXT)))
					.toString();
			long status_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.STATUS_ID));
			long account_id = cur.getLong(cur.getColumnIndexOrThrow(Statuses.ACCOUNT_ID));
			switch (item.getItemId()) {
				case MENU_REPLY:
					Bundle bundle = new Bundle();
					bundle.putStringArray(INTENT_KEY_MENTIONS,
							CommonUtils.getMentionedNames(text, true));
					startActivity(new Intent(INTENT_ACTION_COMPOSE).putExtras(bundle));
					break;
				case MENU_RETWEET:
					mServiceInterface.retweetStatus(new long[] { account_id }, status_id);
					break;
				case MENU_QUOTE:
					break;
				case MENU_FAV:
					boolean is_favorite = cur.getInt(cur
							.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1;
					if (is_favorite) {
						mServiceInterface.destroyFavorite(new long[] { account_id }, status_id);
					} else {
						mServiceInterface.createFavorite(new long[] { account_id }, status_id);
					}
					break;
				case MENU_DELETE:
					mServiceInterface.destroyStatus(account_id, status_id);
					break;
				default:
					cur.close();
					return false;
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivityFirstCreated = true;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.action_status, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = Statuses.COLUMNS;
		Uri uri = getContentUri();
		String where = CommonUtils.buildActivatedStatsWhereClause(getSherlockActivity(), null);
		return new CursorLoader(getSherlockActivity(), uri, cols, where, null,
				Statuses.DEFAULT_SORT_ORDER);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.refreshable_list, container, false);
	}

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
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
				doRefresh(new long[] { account_id }, new long[] { status_id });
			} else {
				Bundle bundle = new Bundle();
				bundle.putLong(Statuses.ACCOUNT_ID, account_id);
				bundle.putLong(Statuses.STATUS_ID, status_id);
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
		CommonUtils.setMenuForStatus(getSherlockActivity(), menu, mSelectedStatusId,
				getContentUri());
		return true;
	}

	@Override
	public void onRefresh() {

		long[] account_ids = CommonUtils.getActivatedAccounts(getSherlockActivity());
		doRefresh(account_ids, null);

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

		if (!mActivityFirstCreated) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		mActivityFirstCreated = false;
		super.onStop();
	}

	private void doRefresh(long[] account_ids, long[] max_ids) {
		switch (CommonUtils.getTableId(getContentUri())) {
			case URI_STATUSES:
				mServiceInterface.getHomeTimeline(account_ids, max_ids);
				break;
			case URI_MENTIONS:
				mServiceInterface.getMentions(account_ids, max_ids);
				break;
			case URI_FAVORITES:
				break;
		}
	}
}
