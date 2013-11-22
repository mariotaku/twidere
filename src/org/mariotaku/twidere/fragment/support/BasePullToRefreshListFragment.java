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

package org.mariotaku.twidere.fragment.support;

import static android.support.v4.app.ListFragmentTrojan.INTERNAL_EMPTY_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_LIST_CONTAINER_ID;
import static android.support.v4.app.ListFragmentTrojan.INTERNAL_PROGRESS_CONTAINER_ID;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.pulltorefresh.TwidereHeaderTransformer;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh.SetupWizard;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class BasePullToRefreshListFragment extends BaseSupportListFragment implements
		IBasePullToRefreshFragment, OnRefreshListener, OnTouchListener, OnGestureListener {

	private GestureDetector mGestureDector;
	private boolean mPulledUp;
	private PullToRefreshLayout mPullToRefreshLayout;

	@Override
	public PullToRefreshLayout getPullToRefreshLayout() {
		return mPullToRefreshLayout;
	}

	@Override
	public boolean isPullToRefreshEnabled() {
		return mPullToRefreshLayout != null && mPullToRefreshLayout.isEnabled();
	}

	@Override
	public boolean isRefreshing() {
		return mPullToRefreshLayout != null && mPullToRefreshLayout.isRefreshing();
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnTouchListener(this);
		mGestureDector = new GestureDetector(getActivity(), this);
	}

	/**
	 * Provide default implementation to return a simple list view. Subclasses
	 * can override to replace with their own layout. If doing so, the returned
	 * view hierarchy <em>must</em> have a ListView whose id is
	 * {@link android.R.id#list android.R.id.list} and can optionally have a
	 * sibling view id {@link android.R.id#empty android.R.id.empty} that is to
	 * be shown when the list is empty.
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
		tv.setTextAppearance(context, ThemeUtils.getTextAppearanceLarge(context));
		tv.setId(INTERNAL_EMPTY_ID);
		tv.setGravity(Gravity.CENTER);
		lframe.addView(tv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		final PullToRefreshLayout plv = new PullToRefreshLayout(context);
		mPullToRefreshLayout = plv;

		final ListView lv = new ListView(context);
		lv.setId(android.R.id.list);
		lv.setDrawSelectorOnTop(false);
		plv.addView(lv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		final Options.Builder builder = new Options.Builder();
		builder.refreshOnUp(true);
		builder.scrollDistance(DEFAULT_PULL_TO_REFRESH_SCROLL_DISTANCE);
		builder.headerTransformer(new TwidereHeaderTransformer());
		if (!isDetached()) {
			final SetupWizard wizard = ActionBarPullToRefresh.from(getActivity());
			wizard.allChildrenArePullable();
			wizard.listener(this);
			wizard.options(builder.build());
			wizard.setup(mPullToRefreshLayout);
		}
		lframe.addView(plv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		root.addView(lframe, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		// ------------------------------------------------------------------

		root.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		return root;
	}

	@Override
	public boolean onDown(final MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
		return true;
	}

	@Override
	public void onLongPress(final MotionEvent e) {

	}

	@Override
	public void onRefreshStarted() {
	}

	@Override
	public final void onRefreshStarted(final View view) {
		onRefreshStarted();
	}

	@Override
	public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
		if (isReachedBottom() && distanceY > 0 && !mPulledUp && !isRefreshing()) {
			mPulledUp = true;
			onPullUp();
			return true;
		}
		if (distanceY < 0) {
			mPulledUp = false;
		}
		return true;
	}

	@Override
	public void onShowPress(final MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(final MotionEvent e) {
		return true;
	}

	@Override
	public final boolean onTouch(final View v, final MotionEvent event) {
		mGestureDector.onTouchEvent(event);
		return false;
	}

	@Override
	public void setPullToRefreshEnabled(final boolean enabled) {
		if (mPullToRefreshLayout == null) return;
		mPullToRefreshLayout.setEnabled(enabled);
	}

	@Override
	public void setRefreshComplete() {
		mPulledUp = false;
		mPullToRefreshLayout.setRefreshComplete();
	}

	@Override
	public void setRefreshing(final boolean refreshing) {
		if (!refreshing) {
			mPulledUp = false;
		}
		if (mPullToRefreshLayout != null) {
			mPullToRefreshLayout.setRefreshing(refreshing);
		}
	}

	@Override
	public boolean triggerRefresh() {
		onRefreshStarted(getListView());
		return true;
	}

	protected void onPullUp() {
	}

}