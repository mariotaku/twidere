package org.mariotaku.actionbarcompat;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;

@SuppressLint("Registered")
public class ActionBarFragmentActivity extends FragmentActivity {

	protected ActionBarCompat mActionBarCompat = ActionBarCompat.getInstance(this);
	private boolean mActionBarInitialized = false;

	protected Fragment mAttachedFragment;

	private ActionModeCompat mActionModeCompat;

	private int mWindowFeatureId;

	public MenuInflater getBaseMenuInflater() {
		return super.getMenuInflater();
	}

	@Override
	public MenuInflater getMenuInflater() {
		if (mActionBarCompat instanceof ActionBarCompatBase)
			return mActionBarCompat.getMenuInflater(super.getMenuInflater());
		return super.getMenuInflater();
	}

	public ActionBar getSupportActionBar() {
		return mActionBarCompat.getActionBar();
	}

	public void invalidateSupportOptionsMenu() {
		if (mActionBarCompat instanceof ActionBarCompatNative) {
			ActivityCompat.invalidateOptionsMenu(this);
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
	public void onContentChanged() {
		super.onContentChanged();
		initActionBar();
		checkActionBar();
		if (mActionBarCompat.isAvailable()) {
			switch (mWindowFeatureId) {
				case Window.FEATURE_INDETERMINATE_PROGRESS: {
					if (mActionBarCompat instanceof ActionBarCompatBase) {
						((ActionBarCompatBase) mActionBarCompat).setProgressBarIndeterminateEnabled(true);
					}
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			if (((ActionBarCompatBase) mActionBarCompat).isActionModeShowing()) return false;
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

	public void requestSupportWindowFeature(final int featureId) {
		mWindowFeatureId = featureId;
		if (mActionBarCompat instanceof ActionBarCompatNative) {
			requestWindowFeature(featureId);
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return new ActionModeNative(this, callback);
		else
			return mActionModeCompat = new ActionModeCompat((ActionBarCompatBase) mActionBarCompat, callback);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBarCompat).requestCustomTitleView();
		}
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onTitleChanged(final CharSequence title, final int color) {
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			getSupportActionBar().setTitle(title);
		}
		super.onTitleChanged(title, color);
	}

	private void checkActionBar() {
		if (!mActionBarCompat.isAvailable()) {
			mActionBarCompat = new ActionBarCompatBase(this);
			initActionBar();
		}
	}

	private void initActionBar() {
		if (mActionBarCompat instanceof ActionBarCompatBase && !mActionBarInitialized) {
			mActionBarInitialized = ((ActionBarCompatBase) mActionBarCompat).setCustomTitleView();
		}
	}

}
