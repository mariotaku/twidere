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

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huewu.pla.lib.MultiColumnListView;

import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.PullToRefreshAttacherActivity;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.pulltorefresh.viewdelegates.PLAAbsListViewDelegate;

import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.HeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

public abstract class BasePullToRefreshMultiColumnListFragment extends BaseSupportMultiColumnListFragment implements
		IBasePullToRefreshFragment, PullToRefreshAttacher.OnRefreshListener, OnTouchListener, OnGestureListener {

	private PullToRefreshAttacherActivity mPullToRefreshAttacherActivity;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private GestureDetector mGestureDector;
	private boolean mPulledUp;
	private PullToRefreshLayout mPullToRefreshLayout;

	@Override
	public String getPullToRefreshTag() {
		return getTag();
	}

	public boolean isRefreshing() {
		if (mPullToRefreshAttacherActivity == null) return false;
		return mPullToRefreshAttacherActivity.isRefreshing(this);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity activity = getActivity();
		if (activity instanceof PullToRefreshAttacherActivity) {
			mPullToRefreshAttacherActivity = (PullToRefreshAttacherActivity) activity;
		} else
			throw new IllegalStateException("Activity class must implement PullToRefreshAttacherActivity");
		getListView().setOnTouchListener(this);
		final HeaderTransformer transformer = mPullToRefreshAttacher.getHeaderTransformer();
		if (transformer instanceof DefaultHeaderTransformer) {
			final DefaultHeaderTransformer t = (DefaultHeaderTransformer) transformer;
			t.setProgressBarColor(ThemeUtils.getThemeColor(activity));
			t.setProgressBarColorEnabled(ThemeUtils.shouldApplyColorFilter(activity));
		}
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
		if (!(context instanceof PullToRefreshAttacherActivity))
			throw new IllegalStateException("Activity class must implement PullToRefreshAttacherActivity");
		mPullToRefreshAttacher = ((PullToRefreshAttacherActivity) context).getPullToRefreshAttacher();
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

		final PullToRefreshLayout plv = new PullToRefreshLayout(context);
		mPullToRefreshLayout = plv;

		final MultiColumnListView lv = createMultiColumnListView(context, inflater);
		lv.setId(android.R.id.list);
		lv.setDrawSelectorOnTop(false);
		plv.addView(lv, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		plv.setPullToRefreshAttacher(mPullToRefreshAttacher, this);
		mPullToRefreshAttacher.addRefreshableView(lv, new PLAAbsListViewDelegate(), this);
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

	public void onRefreshStarted() {
	}

	@Override
	public final void onRefreshStarted(final View view) {
		if (mPullToRefreshAttacherActivity != null) {
			mPullToRefreshAttacherActivity.addRefreshingState(this);
		}
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

	public void setPullToRefreshEnabled(final boolean enabled) {
		// if (mPullToRefreshAttacherActivity == null) return;
		// mPullToRefreshAttacherActivity.setPullToRefreshEnabled(this,
		// enabled);
		if (mPullToRefreshLayout == null) return;
		mPullToRefreshLayout.setEnabled(enabled);
	}

	public void setRefreshComplete() {
		if (mPullToRefreshAttacherActivity == null) return;
		mPulledUp = false;
		mPullToRefreshAttacherActivity.setRefreshComplete(this);
	}

	public void setRefreshing(final boolean refreshing) {
		if (mPullToRefreshAttacherActivity == null) return;
		if (!refreshing) {
			mPulledUp = false;
		}
		mPullToRefreshAttacherActivity.setRefreshing(this, refreshing);
	}

	@Override
	public boolean triggerRefresh() {
		onRefreshStarted(getListView());
		return true;
	}

	protected PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	protected PullToRefreshLayout getPullToRefreshLayout() {
		return mPullToRefreshLayout;
	}

	protected void onPullUp() {
	}

}
