package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.buildActivatedStatsWhereClause;
import static org.mariotaku.twidere.util.Utils.buildFilterWhereClause;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getLastSortIds;
import static org.mariotaku.twidere.util.Utils.getMentionedNames;
import static org.mariotaku.twidere.util.Utils.getTableId;
import static org.mariotaku.twidere.util.Utils.getTableNameForContentUri;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.StatusesCursorAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusViewHolder;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public abstract class StatusesListFragment extends BaseFragment implements OnRefreshListener, LoaderCallbacks<Cursor>,
		OnScrollListener, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	public ServiceInterface mServiceInterface;
	public PullToRefreshListView mListView;
	public ContentResolver mResolver;
	private SharedPreferences mPreferences;
	private AsyncTaskManager mAsyncTaskManager;
	private StatusesCursorAdapter mAdapter;

	private Handler mHandler;
	private Runnable mTicker;
	private PopupMenu mPopupMenu;

	public ParcelableStatus mSelectedStatus;
	private int mRunningTaskId;
	private boolean mBusy, mTickerStopped;
	private boolean mDisplayProfileImage, mDisplayName, mReachedBottom, mActivityFirstCreated;
	private float mTextSize;
	private boolean mLoadMoreAutomatically, mNotReachedBottomBefore = true;

	public abstract Uri getContentUri();

	public HomeActivity getHomeActivity() {
		FragmentActivity activity = getActivity();
		if (activity instanceof HomeActivity) return (HomeActivity) activity;
		return null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAsyncTaskManager = AsyncTaskManager.getInstance();
		mPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = getActivity().getContentResolver();
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		mTextSize = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mServiceInterface = ((TwidereApplication) getActivity().getApplication()).getServiceInterface();
		LazyImageLoader imageloader = ((TwidereApplication) getActivity().getApplication()).getListProfileImageLoader();
		mAdapter = new StatusesCursorAdapter(getActivity(), imageloader);
		mListView = (PullToRefreshListView) getView().findViewById(R.id.refreshable_list);
		mListView.setOnRefreshListener(this);
		ListView list = mListView.getRefreshableView();
		list.setAdapter(mAdapter);
		list.setOnScrollListener(this);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivityFirstCreated = true;
		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = new String[] { Statuses._ID, Statuses.ACCOUNT_ID, Statuses.STATUS_ID,
				Statuses.STATUS_TIMESTAMP, Statuses.TEXT_PLAIN, Statuses.NAME, Statuses.SCREEN_NAME,
				Statuses.PROFILE_IMAGE_URL, Statuses.IN_REPLY_TO_STATUS_ID, Statuses.IN_REPLY_TO_SCREEN_NAME,
				Statuses.LOCATION, Statuses.RETWEET_COUNT, Statuses.RETWEET_ID, Statuses.RETWEETED_BY_NAME,
				Statuses.RETWEETED_BY_SCREEN_NAME, Statuses.IS_RETWEET, Statuses.IS_FAVORITE, Statuses.HAS_MEDIA,
				Statuses.IS_PROTECTED, Statuses.IS_GAP };
		Uri uri = getContentUri();
		String where = buildActivatedStatsWhereClause(getActivity(), null);
		if (mPreferences.getBoolean(PREFERENCE_KEY_ENABLE_FILTER, false)) {
			String table = getTableNameForContentUri(uri);
			where = buildFilterWhereClause(table, where);
		}
		return new CursorLoader(getActivity(), uri, cols, where, null, Statuses.DEFAULT_SORT_ORDER);
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
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			ParcelableStatus status = mAdapter.findItem(id);
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap || position == adapter.getCount() - 1 && !mLoadMoreAutomatically) {
				getStatuses(new long[] { status.account_id }, new long[] { status.status_id });
			} else {
				showSatus(status);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = mAdapter.findItem(id);
			mPopupMenu = new PopupMenu(getActivity(), view);
			mPopupMenu.inflate(R.menu.action_status);
			setMenuForStatus(getActivity(), mPopupMenu.getMenu(), mSelectedStatus);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();

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
		mAdapter.setShowAccountColor(getActivatedAccountIds(getActivity()).length > 1);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mSelectedStatus != null) {
			long status_id = mSelectedStatus.status_id;
			String text_plain = mSelectedStatus.text_plain;
			String screen_name = mSelectedStatus.screen_name;
			String name = mSelectedStatus.name;
			long account_id = mSelectedStatus.account_id;
			switch (item.getItemId()) {
				case MENU_SHARE: {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, "@" + screen_name + ": " + text_plain);
					startActivity(Intent.createChooser(intent, getString(R.string.share)));
					break;
				}
				case MENU_RETWEET: {
					if (isMyRetweet(getActivity(), account_id, status_id)) {
						mServiceInterface.cancelRetweet(account_id, status_id);
					} else {
						long id_to_retweet = mSelectedStatus.is_retweet && mSelectedStatus.retweet_id > 0 ? mSelectedStatus.retweet_id
								: mSelectedStatus.status_id;
						mServiceInterface.retweetStatus(account_id, id_to_retweet);
					}
					break;
				}
				case MENU_QUOTE: {
					Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					Bundle bundle = new Bundle();
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
					bundle.putString(INTENT_KEY_TEXT, "RT @" + screen_name + ": " + text_plain);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_REPLY: {
					Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					Bundle bundle = new Bundle();
					bundle.putStringArray(INTENT_KEY_MENTIONS, getMentionedNames(screen_name, text_plain, false, true));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_FAV: {
					if (mSelectedStatus.is_favorite) {
						mServiceInterface.destroyFavorite(account_id, status_id);
					} else {
						mServiceInterface.createFavorite(account_id, status_id);
					}
					break;
				}
				case MENU_DELETE: {
					mServiceInterface.destroyStatus(account_id, status_id);
					break;
				}
				default:
					return false;
			}
		}
		return true;
	}

	@Override
	public void onRefresh() {
		long[] account_ids = getActivatedAccountIds(getActivity());
		mRunningTaskId = getStatuses(account_ids, null);

	}

	@Override
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_LOAD_MORE_AUTOMATICALLY, false);
		mAdapter.setShowLastItemAsGap(!mLoadMoreAutomatically);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setStatusesTextSize(text_size);
		if (mDisplayProfileImage != display_profile_image || mDisplayName != display_name || mTextSize != text_size) {
			mDisplayProfileImage = display_profile_image;
			mDisplayName = display_name;
			mTextSize = text_size;
			mListView.getRefreshableView().invalidateViews();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		boolean reached = firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount >= visibleItemCount;

		if (mReachedBottom != reached) {
			mReachedBottom = reached;
			if (mReachedBottom && mNotReachedBottomBefore) {
				mNotReachedBottomBefore = false;
				return;
			}
			if (mLoadMoreAutomatically && mReachedBottom) {
				if (!mAsyncTaskManager.isExcuting(mRunningTaskId)) {
					mRunningTaskId = getStatuses(getActivatedAccountIds(getActivity()),
							getLastSortIds(getActivity(), getContentUri()));
				}
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
			if (!mAsyncTaskManager.isExcuting(mRunningTaskId)) {
				mListView.onRefreshComplete();
			}
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		mActivityFirstCreated = false;
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private int getStatuses(long[] account_ids, long[] max_ids) {
		switch (getTableId(getContentUri())) {
			case URI_STATUSES:
				return mServiceInterface.getHomeTimeline(account_ids, max_ids);
			case URI_MENTIONS:
				return mServiceInterface.getMentions(account_ids, max_ids);
			case URI_FAVORITES:
				break;
		}
		return -1;
	}

	private void showSatus(ParcelableStatus status) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_TWIDERE);
		builder.authority(AUTHORITY_STATUS);
		builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_id));
		builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.status_id));
		startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
	}
}
