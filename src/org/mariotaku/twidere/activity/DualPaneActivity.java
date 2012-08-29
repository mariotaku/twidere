package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout.TouchInterceptor;

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

public class DualPaneActivity extends BaseActivity implements OnBackStackChangedListener {

	private SharedPreferences mPreferences;

	private ExtendedFrameLayout mLeftPaneContainer, mRightPaneContainer;
	private ViewGroup mLeftPaneLayer, mRightPaneLayer;

	private Fragment mDetailsFragment;

	private boolean mDualPaneInPortrait, mDualPaneInLandscape;

	private final TouchInterceptor mLeftPaneTouchInterceptor = new TouchInterceptor() {

		@Override
		public void onInterceptTouchEvent(MotionEvent event) {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				bringLeftPaneToFront();
			}
		}
	};
	private final TouchInterceptor mRightPaneTouchInterceptor = new TouchInterceptor() {

		@Override
		public void onInterceptTouchEvent(MotionEvent event) {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				bringRightPaneToFront();
			}
		}
	};

	public final void bringLeftPaneToFront() {
		if (mLeftPaneLayer == null || mRightPaneLayer == null || mLeftPaneContainer == null
				|| mRightPaneContainer == null) return;
		mLeftPaneLayer.bringToFront();
		mLeftPaneContainer.setBackgroundResource(getPaneBackground());
		mRightPaneContainer.setBackgroundResource(0);
	}

	public final void bringRightPaneToFront() {
		if (mLeftPaneLayer == null || mRightPaneLayer == null || mLeftPaneContainer == null
				|| mRightPaneContainer == null) return;
		mRightPaneLayer.bringToFront();
		mRightPaneContainer.setBackgroundResource(getPaneBackground());
		mLeftPaneContainer.setBackgroundResource(0);
	}

	public Fragment getDetailsFragment() {
		return mDetailsFragment;
	}

	public final boolean isDualPaneMode() {
		if (!(findViewById(PANE_LEFT_CONTAINER) instanceof ViewGroup && findViewById(PANE_RIGHT_CONTAINER) instanceof ViewGroup))
			return false;
		final View main_container = findViewById(R.id.main_container);
		return main_container != null && main_container instanceof FrameLayout;
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
				if (fragment instanceof Panes.Left) {
					bringLeftPaneToFront();
				} else if (fragment instanceof Panes.Right) {
					bringRightPaneToFront();
				}
			} else {
				if (fm.findFragmentById(R.id.content) != null || left_pane_used) {
					bringLeftPaneToFront();
				} else if (right_pane_used) {
					bringRightPaneToFront();
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
			mLeftPaneContainer = (ExtendedFrameLayout) findViewById(R.id.left_pane_container);
			mLeftPaneContainer.setTouchInterceptor(mLeftPaneTouchInterceptor);
			mLeftPaneLayer = (ViewGroup) findViewById(R.id.left_pane_layer);
			mRightPaneContainer = (ExtendedFrameLayout) findViewById(R.id.right_pane_container);
			mRightPaneContainer.setTouchInterceptor(mRightPaneTouchInterceptor);
			mRightPaneLayer = (ViewGroup) findViewById(R.id.right_pane_layer);
			bringLeftPaneToFront();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
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
		getSupportFragmentManager().addOnBackStackChangedListener(this);
	}

	public final void showAtPane(int pane, Fragment fragment, boolean addToBackStack) {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch (pane) {
			case PANE_LEFT: {
				bringLeftPaneToFront();
				ft.replace(PANE_LEFT, fragment);
				break;
			}
			case PANE_RIGHT: {
				bringRightPaneToFront();
				ft.replace(PANE_RIGHT, fragment);
				break;
			}
		}
		if (addToBackStack) {
			ft.addToBackStack(null);
		}
		ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
		mDetailsFragment = fragment;
	}

	public final void showFragment(Fragment fragment, boolean add_to_backstack) {
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
