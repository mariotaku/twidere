package org.mariotaku.actionbarcompat;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Window;

public class ActionBarPreferenceActivity extends PreferenceActivity {

	protected ActionBarCompat mActionBarCompat = ActionBarCompat.getInstance(this);
	private boolean mActionBarInitialized = false;

	private ActionModeCompat mActionModeCompat;

	@Override
	public MenuInflater getMenuInflater() {
		return mActionBarCompat.getMenuInflater(super.getMenuInflater());
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
	public void onContentChanged() {
		super.onContentChanged();
		initActionBar();
		checkActionBar();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		boolean retValue = super.onCreateOptionsMenu(menu);
		if (mActionBarCompat instanceof ActionBarCompatBase) {
			retValue = true;
		}
		return retValue;
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mActionBarCompat instanceof ActionBarCompatBase) {
			if (((ActionBarCompatBase) mActionBarCompat).isActionModeShowing() && mActionModeCompat != null) {
				mActionModeCompat.finish();
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
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
