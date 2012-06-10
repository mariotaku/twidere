package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getMentionedNames;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.AsyncTaskManager;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusViewHolder;
import org.mariotaku.twidere.util.StatusesAdapterInterface;

import twitter4j.User;

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

public abstract class BaseUsersListFragment extends BaseFragment implements OnRefreshListener,
		LoaderCallbacks<List<User>>, OnScrollListener, OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	private ServiceInterface mServiceInterface;
	private PullToRefreshListView mListView;

	private SharedPreferences mPreferences;
	private AsyncTaskManager mAsyncTaskManager;

	private Handler mHandler;
	private Runnable mTicker;

	private PopupMenu mPopupMenu;

	private ParcelableStatus mSelectedStatus;
	private int mRunningTaskId;
	private boolean mBusy, mTickerStopped;

	private boolean mDisplayProfileImage, mDisplayName, mReachedBottom, mActivityFirstCreated;
	private float mTextSize;
	private boolean mLoadMoreAutomatically, mNotReachedBottomBefore = true;

	public AsyncTaskManager getAsyncTaskManager() {
		return mAsyncTaskManager;
	}

	public abstract long[] getLastUserIds();

	public abstract StatusesAdapterInterface getListAdapter();

	public final PullToRefreshListView getListView() {
		return mListView;
	}

	public ParcelableStatus getSelectedStatus() {
		return mSelectedStatus;
	}

	public ServiceInterface getServiceInterface() {
		return mServiceInterface;
	}

	public SharedPreferences getSharedPreferences() {
		return mPreferences;
	}

	public abstract int getUsers(long[] account_ids, long[] max_ids);

	public boolean isActivityFirstCreated() {
		return mActivityFirstCreated;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAsyncTaskManager = AsyncTaskManager.getInstance();
		mPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		mTextSize = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mServiceInterface = ((TwidereApplication) getActivity().getApplication()).getServiceInterface();
		mListView = (PullToRefreshListView) getView().findViewById(R.id.refreshable_list);
		mListView.setOnRefreshListener(this);
		ListView list = mListView.getRefreshableView();
		list.setAdapter(getListAdapter());
		list.setOnScrollListener(this);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
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
	public abstract Loader<List<User>> onCreateLoader(int id, Bundle args);

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
			ParcelableStatus status = getListAdapter().findItem(id);
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap || position == adapter.getCount() - 1 && !mLoadMoreAutomatically) {
				getUsers(new long[] { status.account_id }, new long[] { status.status_id });
			} else {
				//openUserProfile(status);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = getListAdapter().findItem(id);
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
	public abstract void onLoaderReset(Loader<List<User>> loader);

	@Override
	public abstract void onLoadFinished(Loader<List<User>> loader, List<User> data);

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
					bundle.putString(INTENT_KEY_TEXT, getQuoteStatus(getActivity(), screen_name, text_plain));
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

	public abstract void onPostStart();

	@Override
	public abstract void onRefresh();

	@Override
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_LOAD_MORE_AUTOMATICALLY, false);
		getListAdapter().setShowLastItemAsGap(!mLoadMoreAutomatically);
		getListAdapter().setDisplayProfileImage(display_profile_image);
		getListAdapter().setDisplayName(display_name);
		getListAdapter().setStatusesTextSize(text_size);
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
			if (mLoadMoreAutomatically && mReachedBottom && getListAdapter().getCount() > visibleItemCount) {
				if (!mAsyncTaskManager.isExcuting(mRunningTaskId)) {
					mRunningTaskId = getUsers(getActivatedAccountIds(getActivity()), getLastUserIds());
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

	private void openUserProfile(User status) {
//		final long account_id = status.account_id, status_id = status.status_id;
//		FragmentActivity activity = getActivity();
//		Bundle bundle = new Bundle();
//		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
//			HomeActivity home_activity = (HomeActivity) activity;
//			Fragment fragment = new ViewStatusFragment();
//			Bundle args = new Bundle(bundle);
//			args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
//			args.putLong(INTENT_KEY_STATUS_ID, status_id);
//			fragment.setArguments(args);
//			home_activity.showAtPane(HomeActivity.PANE_RIGHT, fragment, true);
//		} else {
//			Uri.Builder builder = new Uri.Builder();
//			builder.scheme(SCHEME_TWIDERE);
//			builder.authority(AUTHORITY_STATUS);
//			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
//			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
//			Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());
//
//			intent.putExtras(bundle);
//			startActivity(intent);
//		}
	}
}
