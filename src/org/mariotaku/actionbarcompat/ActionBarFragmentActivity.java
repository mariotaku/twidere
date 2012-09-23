package org.mariotaku.actionbarcompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

@SuppressLint("Registered")
public class ActionBarFragmentActivity extends FragmentActivity {

	private final ActionBarCompat mActionBarCompat = ActionBarCompat.getInstance(this);
	private boolean mActionBarInitialized = false;

	private Fragment mAttachedFragment;

	private ActionModeCompat mActionModeCompat;

	public MenuInflater getBaseMenuInflater() {
		return super.getMenuInflater();
	}

	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarCompat.getMenuInflater(super.getMenuInflater());
	}

	public ActionBar getSupportActionBar() {
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase) mActionBarCompat).setCustomTitleView();
		}
		return mActionBarCompat.getActionBar();

	}

	public void invalidateSupportOptionsMenu() {
		if (mActionBarCompat instanceof ActionBarCompatNative) {
			MethodsCompat.invalidateOptionsMenu(this);
		} else if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).invalidateOptionsMenu();
		}
	}

	@Override
	public void onAttachFragment(final Fragment fragment) {
		super.onAttachFragment(fragment);
		mAttachedFragment = fragment;
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).createActionBarMenu();
		}
	}

	@Override
	public void onBackPressed() {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			if (((ActionBarCompatBase) mActionBarCompat).isActionModeShowing() && mActionModeCompat != null) {
				mActionModeCompat.finish();
			}
		}
		super.onBackPressed();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).requestCustomTitleView();
		}
		super.onCreate(savedInstanceState);
	}

	/**
	 * Base action bar-aware implementation for
	 * {@link Activity#onCreateOptionsMenu(android.view.Menu)}.
	 * 
	 * Note: marking menu items as invisible/visible is not currently supported.
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			if (((ActionBarCompatBase) mActionBarCompat).isActionModeShowing() && mActionModeCompat != null)
				return false;
		}
		boolean retValue = super.onCreateOptionsMenu(menu);
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			if (mAttachedFragment != null) {
				mAttachedFragment.onCreateOptionsMenu(menu, getMenuInflater());
			}
			retValue = true;
		}
		return retValue;
	}

	@Override
	public void onPostCreate(final Bundle savedInstanceState) {
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase) mActionBarCompat).setCustomTitleView();
		}
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			if (((ActionBarCompatBase) mActionBarCompat).isActionModeShowing() && mActionModeCompat != null)
				return false;
		}
		super.onPrepareOptionsMenu(menu);
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).hideInRealMenu(menu);
		}
		return true;
	}

	@Override
	public void onTitleChanged(final CharSequence title, final int color) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			getSupportActionBar().setTitle(title);
		}
		super.onTitleChanged(title, color);
	}

	public void requestSupportWindowFeature(final int featureId) {
		if (mActionBarCompat instanceof ActionBarCompatNative) {
			requestWindowFeature(featureId);
		} else {
			switch (featureId) {
				case Window.FEATURE_INDETERMINATE_PROGRESS: {
					if (mActionBarCompat instanceof ActionBarCompatBase) {
						((ActionBarCompatBase) mActionBarCompat).setProgressBarIndeterminateEnabled(true);
					}
				}
			}
		}
	}

	@Override
	public void setContentView(final int layoutResID) {
		super.setContentView(layoutResID);
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase) mActionBarCompat).setCustomTitleView();
		}
	}

	@Override
	public void setContentView(final View view) {
		super.setContentView(view);
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase) mActionBarCompat).setCustomTitleView();
		}
	}

	@Override
	public void setContentView(final View view, final LayoutParams params) {
		super.setContentView(view, params);
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase) mActionBarCompat).setCustomTitleView();
		}
	}

	public void setSupportProgressBarIndeterminateVisibility(final boolean visible) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).setProgressBarIndeterminateVisibility(visible);
		} else {
			setProgressBarIndeterminateVisibility(visible);
		}
	}

	public final ActionMode startActionMode(final ActionMode.Callback callback) {
		if (mActionBarCompat instanceof ActionBarCompatBase)
			return mActionModeCompat = new ActionModeCompat((ActionBarCompatBase) mActionBarCompat, callback);
		else
			return new ActionModeNative(this, callback);
	}

}
