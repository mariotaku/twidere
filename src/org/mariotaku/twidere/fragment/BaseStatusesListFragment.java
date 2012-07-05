package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusViewHolder;
import org.mariotaku.twidere.util.StatusesAdapterInterface;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.twitter.Extractor;

abstract class BaseStatusesListFragment<Data> extends PullToRefreshListFragment implements LoaderCallbacks<Data>,
		OnScrollListener, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	private ServiceInterface mServiceInterface;

	private SharedPreferences mPreferences;
	private AsyncTaskManager mAsyncTaskManager;

	private Handler mHandler;
	private Runnable mTicker;
	private ListView mListView;
	private Data mData;

	private PopupMenu mPopupMenu;

	private ParcelableStatus mSelectedStatus;
	private int mRunningTaskId;

	private boolean mLoadMoreAutomatically;

	private volatile boolean mBusy, mTickerStopped, mReachedBottom, mActivityFirstCreated,
			mNotReachedBottomBefore = true;

	private Fragment mDetailFragment;

	private static final long TICKER_DURATION = 5000L;

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public final Data getData() {
		return mData;
	}

	public abstract long[] getLastStatusIds();

	@Override
	public abstract StatusesAdapterInterface getListAdapter();

	public ParcelableStatus getSelectedStatus() {
		return mSelectedStatus;
	}

	public ServiceInterface getServiceInterface() {
		return mServiceInterface;
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public abstract int getStatuses(long[] account_ids, long[] max_ids);

	public boolean isActivityFirstCreated() {
		return mActivityFirstCreated;
	}

	public abstract boolean mustShowLastAsGap();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAsyncTaskManager = AsyncTaskManager.getInstance();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mServiceInterface = getApplication().getServiceInterface();
		setListAdapter(getListAdapter());
		setShowIndicator(false);
		mListView = getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, getArguments(), this);
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
	public abstract Loader<Data> onCreateLoader(int id, Bundle args);

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			final ParcelableStatus status = getListAdapter().findItem(id);
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap || position == adapter.getCount() - 1 && !mLoadMoreAutomatically) {
				getStatuses(new long[] { status.account_id }, new long[] { status.status_id });
			} else {
				openStatus(status);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = getListAdapter().findItem(id);
			mPopupMenu = PopupMenu.getInstance(getActivity(), view);
			mPopupMenu.inflate(R.menu.action_status);
			setMenuForStatus(getActivity(), mPopupMenu.getMenu(), mSelectedStatus);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();

			return true;
		}
		return false;
	}

	@Override
	public void onLoaderReset(Loader<Data> loader) {
		mData = null;
	}

	@Override
	public void onLoadFinished(Loader<Data> loader, Data data) {
		mData = data;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mSelectedStatus != null) {
			final long status_id = mSelectedStatus.status_id;
			final String text_plain = mSelectedStatus.text_plain;
			final String screen_name = mSelectedStatus.screen_name;
			final String name = mSelectedStatus.name;
			final long account_id = mSelectedStatus.account_id;
			switch (item.getItemId()) {
				case MENU_SHARE: {
					final Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, "@" + screen_name + ": " + text_plain);
					startActivity(Intent.createChooser(intent, getString(R.string.share)));
					break;
				}
				case MENU_RETWEET: {
					if (isMyRetweet(getActivity(), account_id, status_id)) {
						mServiceInterface.cancelRetweet(account_id, status_id);
					} else {
						final long id_to_retweet = mSelectedStatus.is_retweet && mSelectedStatus.retweet_id > 0 ? mSelectedStatus.retweet_id
								: mSelectedStatus.status_id;
						mServiceInterface.retweetStatus(account_id, id_to_retweet);
					}
					break;
				}
				case MENU_QUOTE: {
					final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					final Bundle bundle = new Bundle();
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
					bundle.putString(INTENT_KEY_TEXT, getQuoteStatus(getActivity(), screen_name, text_plain));
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_REPLY: {
					final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					final Bundle bundle = new Bundle();
					final List<String> mentions = new Extractor().extractMentionedScreennames(text_plain);
					mentions.add(0, screen_name);
					bundle.putStringArray(INTENT_KEY_MENTIONS, mentions.toArray(new String[mentions.size()]));
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

	public abstract void onPostStart();

	@Override
	public abstract void onRefresh();

	@Override
	public void onResume() {
		super.onResume();
		final StatusesAdapterInterface adapter = getListAdapter();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_LOAD_MORE_AUTOMATICALLY, false);
		adapter.setDisplayProfileImage(display_profile_image);
		adapter.setDisplayName(display_name);
		adapter.setTextSize(text_size);
		adapter.setShowLastItemAsGap(!mLoadMoreAutomatically || mustShowLastAsGap());
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		final boolean reached = firstVisibleItem + visibleItemCount >= totalItemCount
				&& totalItemCount >= visibleItemCount;

		if (mReachedBottom != reached) {
			mReachedBottom = reached;
			if (mReachedBottom && mNotReachedBottomBefore) {
				mNotReachedBottomBefore = false;
				return;
			}
			if (mLoadMoreAutomatically && mReachedBottom && getListAdapter().getCount() > visibleItemCount) {
				if (!mAsyncTaskManager.isExcuting(mRunningTaskId)) {
					mRunningTaskId = getStatuses(getActivatedAccountIds(getActivity()), getLastStatusIds());
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
					getListAdapter().notifyDataSetChanged();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + TICKER_DURATION - now % TICKER_DURATION;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();

		onPostStart();
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

	private void openStatus(ParcelableStatus status) {
		final long account_id = status.account_id, status_id = status.status_id;
		final FragmentActivity activity = getActivity();
		final Bundle bundle = new Bundle();
		bundle.putParcelable(INTENT_KEY_STATUS, status);
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			if (mDetailFragment instanceof ViewStatusFragment && mDetailFragment.isAdded()) {
				((ViewStatusFragment) mDetailFragment).displayStatus(status);
			} else {
				mDetailFragment = new ViewStatusFragment();
				final Bundle args = new Bundle(bundle);
				args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				args.putLong(INTENT_KEY_STATUS_ID, status_id);
				mDetailFragment.setArguments(args);
				home_activity.showAtPane(HomeActivity.PANE_RIGHT, mDetailFragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());

			intent.putExtras(bundle);
			startActivity(intent);
		}
	}
}
