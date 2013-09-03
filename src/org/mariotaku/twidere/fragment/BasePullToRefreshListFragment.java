/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment;

import static android.support.v4.app.ListFragmentTrojan.INTERNAL_EMPTY_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_LIST_CONTAINER_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_PROGRESS_CONTAINER_ID;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.extras.AccessibilityPullEventListener;

public abstract class BasePullToRefreshListFragment extends BaseListFragment implements OnRefreshListener2<ListView> {

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if ((BasePullToRefreshListFragment.this.getClass().getName() + SHUFFIX_REFRESH_TAB).equals(action)) {
				onPullDownToRefresh(mPullToRefreshListView);
			}
		}
	};

	private PullToRefreshListView mPullToRefreshListView;

	public final PullToRefreshListView getPullToRefreshListView() {
		return mPullToRefreshListView;
	}

	/**
	 * Returns whether the Widget is currently in the Refreshing mState
	 * 
	 * @return true if the Widget is currently refreshing
	 */
	public boolean isRefreshing() {
		if (mPullToRefreshListView == null) return false;
		return mPullToRefreshListView.isRefreshing();
	}

	/**
	 * Provide default implementation to return a simple list view. Subclasses
	 * can override to replace with their own layout. If doing so, the returned
	 * view hierarchy <em>must</em> have a ListView whose id is
	 * {@link android.R.id#list android.R.id.list} and can optionally have a
	 * sibling view id {@link android.R.id#empty android.R.id.empty} that is to
	 * be shown when the list is empty.
	 * 
	 * <p>
	 * If you are overriding this method with your own custom content, consider
	 * including the standard layout {@link android.R.layout#list_content} in
	 * your layout file, so that you continue to retain all of the standard
	 * behavior of ListFragment. In particular, this is currently the only way
	 * to have the built-in indeterminant progress state be shown.
	 */
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final Context context = getActivity();

		final FrameLayout root = new FrameLayout(context);

		// ------------------------------------------------------------------

		final LinearLayout pframe = new LinearLayout(context);
		pframe.setId(INTERNAL_PROGRESS_CONTAINER_ID);
		pframe.setOrientation(LinearLayout.VERTICAL);
		pframe.setVisibility(View.GONE);
		pframe.setGravity(Gravity.CENTER);

		final ProgressBar progress = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
		pframe.addView(progress, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		root.addView(pframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		// ------------------------------------------------------------------

		final FrameLayout lframe = new FrameLayout(context);
		lframe.setId(INTERNAL_LIST_CONTAINER_ID);

		final TextView tv = new TextView(getActivity());
		tv.setId(INTERNAL_EMPTY_ID);
		tv.setGravity(Gravity.CENTER);
		lframe.addView(tv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		final PullToRefreshListView plv = new PullToRefreshListView(context);
		plv.setOnRefreshListener(this);
		plv.setShowIndicator(false);
		plv.setOnPullEventListener(new AccessibilityPullEventListener<ListView>(context));
		plv.setPullToRefreshOverScrollEnabled(false);
		mPullToRefreshListView = plv;

		final ListView lv = plv.getRefreshableView();
		lv.setId(android.R.id.list);
		lv.setDrawSelectorOnTop(false);
		// ViewCompat.setOverScrollMode(lv, ViewCompat.OVER_SCROLL_NEVER);
		lframe.addView(plv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		root.addView(lframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		// ------------------------------------------------------------------

		root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		return root;
	}

	/**
	 * onPullDownToRefresh will be called only when the user has Pulled from the
	 * start, and released.
	 */
	@Override
	public final void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
		onPullDownToRefresh();
	}

	/**
	 * onPullUpToRefresh will be called only when the user has Pulled from the
	 * end, and released.
	 */
	@Override
	public final void onPullUpToRefresh(final PullToRefreshBase<ListView> refreshView) {
		onPullUpToRefresh();
	}

	/**
	 * Mark the current Refresh as complete. Will Reset the UI and hide the
	 * Refreshing View
	 */
	public final void onRefreshComplete() {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.onRefreshComplete();
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(getClass().getName() + SHUFFIX_REFRESH_TAB);
		registerReceiver(mStateReceiver, filter);
		onPostStart();
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	/**
	 * Set the mode of Pull-to-Refresh that this view will use.
	 * 
	 * @param mode - Mode to set the View to
	 */
	public final void setMode(final Mode mode) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setMode(mode);
	}

	/**
	 * Set Text to show when the Widget is being Pulled
	 * 
	 * @param pullLabel - String to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setPullLabel(final String pullLabel, final Mode mode) {
		if (mPullToRefreshListView == null) return;
		final ILoadingLayout layout;
		if (mode != null) {
			layout = mPullToRefreshListView.getLoadingLayoutProxy(mode.showHeaderLoadingLayout(),
					mode.showFooterLoadingLayout());
		} else {
			layout = mPullToRefreshListView.getLoadingLayoutProxy();
		}
		if (layout == null) return;
		layout.setPullLabel(pullLabel);
	}

	/**
	 * Sets the Widget to be in the refresh State. The UI will be updated to
	 * show the 'Refreshing' view.
	 * 
	 * @param doScroll - true if you want to force a scroll to the Refreshing
	 *            view.
	 */
	public final void setRefreshing(final boolean doScroll) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setRefreshing(doScroll);

	}

	/**
	 * Set Text to show when the Widget is refreshing
	 * <code>setRefreshingLabel(releaseLabel, Mode.BOTH)</code>
	 * 
	 * @param releaseLabel - String to display
	 */
	public void setRefreshingLabel(final String refreshingLabel) {
		if (mPullToRefreshListView == null) return;
		final ILoadingLayout layout = mPullToRefreshListView.getLoadingLayoutProxy();
		if (layout == null) return;
		layout.setRefreshingLabel(refreshingLabel);
	}

	/**
	 * Set Text to show when the Widget is refreshing
	 * 
	 * @param refreshingLabel - String to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setRefreshingLabel(final String refreshingLabel, final Mode mode) {
		if (mPullToRefreshListView == null) return;
		final ILoadingLayout layout;
		if (mode != null) {
			layout = mPullToRefreshListView.getLoadingLayoutProxy(mode.showHeaderLoadingLayout(),
					mode.showFooterLoadingLayout());
		} else {
			layout = mPullToRefreshListView.getLoadingLayoutProxy();
		}
		if (layout == null) return;
		layout.setRefreshingLabel(refreshingLabel);
	}

	/**
	 * Set Text to show when the Widget is being pulled, and will refresh when
	 * released. This is the same as calling
	 * <code>setReleaseLabel(releaseLabel, Mode.BOTH)</code>
	 * 
	 * @param releaseLabel - String to display
	 */
	public void setReleaseLabel(final String releaseLabel) {
		if (mPullToRefreshListView == null) return;
		final ILoadingLayout layout = mPullToRefreshListView.getLoadingLayoutProxy();
		if (layout == null) return;
		layout.setReleaseLabel(releaseLabel);
	}

	/**
	 * Set Text to show when the Widget is being pulled, and will refresh when
	 * released
	 * 
	 * @param releaseLabel - String to display
	 * @param mode - Controls which Header/Footer Views will be updated.
	 *            <code>Mode.BOTH</code> will update all available, other values
	 *            will update the relevant View.
	 */
	public void setReleaseLabel(final String releaseLabel, final Mode mode) {
		if (mPullToRefreshListView == null) return;
		final ILoadingLayout layout;
		if (mode != null) {
			layout = mPullToRefreshListView.getLoadingLayoutProxy(mode.showHeaderLoadingLayout(),
					mode.showFooterLoadingLayout());
		} else {
			layout = mPullToRefreshListView.getLoadingLayoutProxy();
		}
		if (layout == null) return;
		layout.setReleaseLabel(releaseLabel);
	}

	/**
	 * A mutator to enable/disable whether the 'Refreshing' View should be
	 * automatically shown when refreshing.
	 * 
	 * @param showView
	 */
	public final void setShowViewWhileRefreshing(final boolean showView) {
		if (mPullToRefreshListView == null) return;
		mPullToRefreshListView.setShowViewWhileRefreshing(showView);
	}

	/**
	 * onPullDownToRefresh will be called only when the user has Pulled from the
	 * start, and released.
	 */
	protected abstract void onPullDownToRefresh();

	/**
	 * onPullUpToRefresh will be called only when the user has Pulled from the
	 * end, and released.
	 */
	protected abstract void onPullUpToRefresh();
}
