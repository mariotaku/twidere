package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout.TouchInterceptor;

import android.os.Bundle;
import android.support.v4.app.BackStackEntryTrojan;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class DualPaneActivity extends BaseActivity implements OnBackStackChangedListener {

	private ExtendedFrameLayout mLeftPaneContainer, mRightPaneContainer;
	private ViewGroup mLeftPaneLayer, mRightPaneLayer;
	
	private Fragment mDetailsFragment;
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportFragmentManager().addOnBackStackChangedListener(this);
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
	

	public final boolean isDualPaneMode() {
		if (!(findViewById(PANE_LEFT_CONTAINER) instanceof ViewGroup
				&& findViewById(PANE_RIGHT_CONTAINER) instanceof ViewGroup)) return false;
		final View main_container = findViewById(R.id.main_container);
		return main_container != null && main_container instanceof FrameLayout;
	}
	

	public Fragment getDetailsFragment() {
		return mDetailsFragment;
	}
	
	public final void showAtPane(int pane, Fragment fragment, boolean addToBackStack) {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch (pane) {
			case PANE_LEFT: {
				// bringLeftPaneToFront();
				ft.replace(PANE_LEFT, fragment);
				break;
			}
			case PANE_RIGHT: {
				// bringRightPaneToFront();
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

	private int getPaneBackground() {
		final boolean dark = isDarkTheme(), solid = isSolidColorBackground();
		return dark ? solid ? android.R.color.black : R.drawable.background_holo_dark : solid ? android.R.color.white
				: R.drawable.background_holo_light;
	}

	@Override
	public void onBackStackChanged() {
		if (isDualPaneMode()) {
			final FragmentManager fm = getSupportFragmentManager();
			final int count = fm.getBackStackEntryCount();
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
				bringLeftPaneToFront();
			}
		}
	}
}
