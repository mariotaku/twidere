package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.openUserProfile;

import java.util.List;

import org.mariotaku.twidere.adapter.UsersAdapter;
import org.mariotaku.twidere.util.ParcelableUser;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public abstract class BaseUsersListFragment extends BaseListFragment implements LoaderCallbacks<List<ParcelableUser>>,
		OnItemClickListener, OnScrollListener {

	private UsersAdapter mAdapter;
	private SharedPreferences mPreferences;
	private boolean mLoadMoreAutomatically;
	private ListView mListView;
	private long mAccountId;

	private volatile boolean mReachedBottom, mNotReachedBottomBefore = true;

	public abstract Loader<List<ParcelableUser>> newLoaderInstance();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

		final Bundle args = getArguments() != null ? getArguments() : new Bundle();
		mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID);
		mAdapter = new UsersAdapter(getActivity());
		mListView = getListView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			SetLayerTypeAccessor.setLayerType(mListView, View.LAYER_TYPE_SOFTWARE, new Paint());
		}
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	private static class SetLayerTypeAccessor {

		@TargetApi(11)
		public static void setLayerType(View view, int layerType, Paint paint) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				view.setLayerType(layerType, paint);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public Loader<List<ParcelableUser>> onCreateLoader(int id, Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return newLoaderInstance();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final ParcelableUser user = mAdapter.getItem(position);
		if (user == null) return;
		if (mAdapter.isGap(position) && !mLoadMoreAutomatically) {
			final Bundle args = getArguments();
			if (args != null) {
				args.putLong(INTENT_KEY_MAX_ID, user.user_id);
			}
			if (!getLoaderManager().hasRunningLoaders()) {
				getLoaderManager().restartLoader(0, args, this);
			}
		} else {
			openUserProfile(getActivity(), mAccountId, user.user_id, user.screen_name);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<ParcelableUser>> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.clear();
		if (data != null) {
			for (ParcelableUser user : data) {
				mAdapter.add(user);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_LOAD_MORE_AUTOMATICALLY, false);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setTextSize(text_size);
		mAdapter.setDisplayName(display_name);
		mAdapter.setShowLastItemAsGap(!mLoadMoreAutomatically);
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
			int count = mAdapter.getCount();
			if (mLoadMoreAutomatically && mReachedBottom && count > visibleItemCount && count - 1 > 0) {
				final Bundle args = getArguments();
				if (args != null) {
					args.putLong(INTENT_KEY_MAX_ID, mAdapter.getItem(count -1 ).user_id);
				}
				if (!getLoaderManager().hasRunningLoaders()) {
					getLoaderManager().restartLoader(0, args, this);
				}
			}
		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

}
