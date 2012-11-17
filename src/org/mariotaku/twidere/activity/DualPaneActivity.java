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

package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.view.ExtendedFrameLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.BackStackEntryTrojan;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import org.mariotaku.twidere.util.ExtendedViewGroupInterface;
import org.mariotaku.twidere.view.SlidingPanel;

@SuppressLint("Registered")
public class DualPaneActivity extends BaseActivity implements OnBackStackChangedListener {

	private SharedPreferences mPreferences;

	private SlidingPanel mSlidingPanel;
	private FrameLayout mFragmentContainerLeft;
	private ExtendedFrameLayout mPanelAnchor, mFragmentContainerRight;

	private Fragment mDetailsFragment;

	private boolean mDualPaneInPortrait, mDualPaneInLandscape;

	private ExtendedViewGroupInterface.TouchInterceptor mTouchInterceptorRight = new ExtendedFrameLayout.TouchInterceptor() {

		public boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event) {
			final int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					showRightPane();
					break;
				}
			}
			return false;
		}

		public boolean onTouchEvent(ViewGroup view, MotionEvent event) {
			return true;
		}
	
	};

	private ExtendedViewGroupInterface.TouchInterceptor mTouchInterceptorLeft = new ExtendedFrameLayout.TouchInterceptor() {

		public boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event) {
			final int action = event.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN: {
					showLeftPane();
					break;
				}
			}
			return false;
		}

		public boolean onTouchEvent(ViewGroup view, MotionEvent event) {
			return false;
		}

	};
	
	public final void showLeftPane() {
		mSlidingPanel.close();
	}

	public final void showRightPane() {
		mSlidingPanel.open();
	}

	public Fragment getDetailsFragment() {
		return mDetailsFragment;
	}

	public final boolean isDualPaneMode() {
		return findViewById(PANE_LEFT) instanceof ViewGroup && findViewById(PANE_RIGHT) instanceof ViewGroup;
	}

	@Override
	public void onBackStackChanged() {
		if (isDualPaneMode()) {
			final FragmentManager fm = getSupportFragmentManager();
			final int count = fm.getBackStackEntryCount();
			final Fragment left_pane_fragment = fm.findFragmentById(PANE_LEFT);
			final Fragment right_pane_fragment = fm.findFragmentById(PANE_RIGHT);
			final View main_view = findViewById(R.id.main);
			final boolean left_pane_used = left_pane_fragment != null && left_pane_fragment.isAdded();
			final boolean right_pane_used = right_pane_fragment != null && right_pane_fragment.isAdded();
			if (count > 0) {
				final BackStackEntry entry = fm.getBackStackEntryAt(count - 1);
				if (entry == null) return;
				final Fragment fragment = BackStackEntryTrojan.getFragmentInBackStackRecord(entry);
				if (fragment instanceof Panes.Right) {
					showRightPane();
				} else if (fragment instanceof Panes.Left) {
					showLeftPane();
				}
			} else {
				if (fm.findFragmentById(R.id.content) != null || left_pane_used) {
					showLeftPane();
				} else if (right_pane_used) {
					showRightPane();
				}
			}
			if (main_view != null) {
				main_view.setVisibility(left_pane_used ? View.GONE : View.VISIBLE);
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		if (isDualPaneMode()) {
			mSlidingPanel = (SlidingPanel) findViewById(R.id.main_container);
			mFragmentContainerLeft = (FrameLayout) findViewById(PANE_LEFT);
			mFragmentContainerRight = (ExtendedFrameLayout) findViewById(PANE_RIGHT);
			mPanelAnchor = (ExtendedFrameLayout) findViewById(R.id.panel_anchor);
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		final Resources res = getResources();
		final int orientation = res.getConfiguration().orientation;
		final int layout;
		mDualPaneInPortrait = mPreferences.getBoolean(PREFERENCE_KEY_DUAL_PANE_IN_PORTRAIT, false);
		mDualPaneInLandscape = mPreferences.getBoolean(PREFERENCE_KEY_DUAL_PANE_IN_LANDSCAPE, false);
		switch (orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				layout = mDualPaneInLandscape ? getDualPaneLayoutRes() : getNormalLayoutRes();
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				layout = mDualPaneInPortrait ? getDualPaneLayoutRes() : getNormalLayoutRes();
				break;
			default:
				layout = getNormalLayoutRes();
				break;
		}
		setContentView(layout);
		mFragmentContainerRight.setBackgroundResource(getPaneBackground());
		mFragmentContainerRight.setTouchInterceptor(mTouchInterceptorRight);
		mPanelAnchor.setTouchInterceptor(mTouchInterceptorLeft);
		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}

	public final void showAtPane(final int pane, final Fragment fragment, final boolean addToBackStack) {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch (pane) {
			case PANE_LEFT: {
				showLeftPane();
				ft.replace(PANE_LEFT, fragment);
				break;
			}
			case PANE_RIGHT: {
				showRightPane();
				ft.replace(PANE_RIGHT, fragment);
				break;
			}
		}
		if (addToBackStack) {
			ft.addToBackStack(null);
		}
		ft.setTransitionStyle(FragmentTransaction.TRANSIT_NONE);
		ft.commit();
		mDetailsFragment = fragment;
	}

	public final void showFragment(final Fragment fragment, final boolean add_to_backstack) {
		if (fragment instanceof Panes.Right) {
			showAtPane(PANE_RIGHT, fragment, add_to_backstack);
		} else {
			showAtPane(PANE_LEFT, fragment, add_to_backstack);
		}
	}

	@Override
	protected void onStart() {
		final FragmentManager fm = getSupportFragmentManager();
		if (!isDualPaneMode() && !FragmentManagerTrojan.isStateSaved(fm)) {
			final int count = fm.getBackStackEntryCount();
			for (int i = 0; i < count; i++) {
				fm.popBackStackImmediate();
			}
		}
		super.onStart();
		final boolean dual_pane_in_portrait = mPreferences.getBoolean(PREFERENCE_KEY_DUAL_PANE_IN_PORTRAIT, false);
		final boolean dual_pane_in_landscape = mPreferences.getBoolean(PREFERENCE_KEY_DUAL_PANE_IN_LANDSCAPE, false);
		final Resources res = getResources();
		final int orientation = res.getConfiguration().orientation;
		switch (orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				if (mDualPaneInLandscape != dual_pane_in_landscape) {
					restart();
				}
				break;
			case Configuration.ORIENTATION_PORTRAIT:
				if (mDualPaneInPortrait != dual_pane_in_portrait) {
					restart();
				}
				break;
		}
	}

	private int getPaneBackground() {
		final boolean dark = isDarkTheme(), solid = isSolidColorBackground();
		return dark ? solid ? android.R.color.black : R.drawable.background_holo_dark : solid ? android.R.color.white
				: R.drawable.background_holo_light;
	}

	int getDualPaneLayoutRes() {
		return R.layout.base_dual_pane;
	}

	int getNormalLayoutRes() {
		return R.layout.base;
	}
}
