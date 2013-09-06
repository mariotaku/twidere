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

import org.mariotaku.twidere.util.ThemeUtils;

import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.HeaderTransformer;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public abstract class BasePullToRefreshListFragment extends BaseSupportListFragment implements
		PullToRefreshAttacher.OnRefreshListener, OnTouchListener, OnGestureListener {

	private PullToRefreshAttacherActivity mPullToRefreshAttacherActivity;
	private PullToRefreshAttacher mPullToRefreshAttacher;
	private GestureDetector mGestureDector;
	private boolean mPulledUp;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if ((BasePullToRefreshListFragment.this.getClass().getName() + SHUFFIX_REFRESH_TAB).equals(action)) {
				onRefreshStarted(getListView());
			}
		}
	};

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
		mPullToRefreshAttacher.setOnTouchListener(getListView(), this);
		final HeaderTransformer transformer = mPullToRefreshAttacher.getHeaderTransformer();
		if (transformer instanceof DefaultHeaderTransformer) {
			final DefaultHeaderTransformer t = (DefaultHeaderTransformer) transformer;
			t.setProgressBarColor(ThemeUtils.getThemeColor(activity));
			t.setProgressBarColorEnabled(ThemeUtils.shouldApplyColorFilter(activity));
		}
		mGestureDector = new GestureDetector(getActivity(), this);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final Activity activity = getActivity();
		if (activity instanceof PullToRefreshAttacherActivity) {
			mPullToRefreshAttacher = ((PullToRefreshAttacherActivity) activity).getPullToRefreshAttacher();
			// Set the Refreshable View to be the ListView and the refresh
			// listener
			// to be this.
			mPullToRefreshAttacher.addRefreshableView(view.findViewById(android.R.id.list), this);
		} else
			throw new IllegalStateException("Activity class must implement PullToRefreshAttacherActivity");
		return view;
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
	public void onStart() {
		super.onStart();
		registerReceiver(mStateReceiver, new IntentFilter(getClass().getName() + SHUFFIX_REFRESH_TAB));
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	@Override
	public final boolean onTouch(final View v, final MotionEvent event) {
		mGestureDector.onTouchEvent(event);
		return false;
	}

	public void setPullToRefreshEnabled(final boolean enabled) {
		if (mPullToRefreshAttacherActivity == null) return;
		mPullToRefreshAttacherActivity.setPullToRefreshEnabled(this, enabled);
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

	protected PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	protected void onPullUp() {
	}

	public static interface PullToRefreshAttacherActivity {
		public void addRefreshingState(BasePullToRefreshListFragment fragment);

		public PullToRefreshAttacher getPullToRefreshAttacher();

		public boolean isRefreshing(BasePullToRefreshListFragment fragment);

		public void setPullToRefreshEnabled(final BasePullToRefreshListFragment fragment, final boolean enabled);

		public void setRefreshComplete(final BasePullToRefreshListFragment fragment);

		public void setRefreshing(final BasePullToRefreshListFragment fragment, final boolean refreshing);
	}

}
