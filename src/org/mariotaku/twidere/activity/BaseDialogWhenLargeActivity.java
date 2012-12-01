package org.mariotaku.twidere.activity;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.actionbarcompat.ActionBarCompatBase;
import org.mariotaku.twidere.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class BaseDialogWhenLargeActivity extends BaseActivity {

	protected ActionBar mActionBar;

	@Override
	public MenuInflater getMenuInflater() {
		if (!mActionBarCompat.isAvailable() && mActionBar instanceof ActionBarCompatBase)
			return ((ActionBarCompatBase) mActionBar).getMenuInflater(super.getBaseMenuInflater());
		return super.getMenuInflater();
	}

	@Override
	public ActionBar getSupportActionBar() {
		return mActionBar;
	}

	@Override
	public void invalidateSupportOptionsMenu() {
		if (!mActionBarCompat.isAvailable() && mActionBar instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBar).invalidateOptionsMenu();
		} else {
			super.invalidateSupportOptionsMenu();
		}
	}

	@Override
	public void onAttachFragment(final Fragment fragment) {
		super.onAttachFragment(fragment);
		if (!mActionBarCompat.isAvailable() && mActionBar instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBar).createActionBarMenu();
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionBar = mActionBarCompat.isAvailable() ? mActionBarCompat : new ActionBarCompatBase(this);
	}

	@Override
	public void setContentView(final int layoutResID) {
		super.setContentView(layoutResID);
		if (!mActionBarCompat.isAvailable() && mActionBar instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBar).initViews();
		}
	}

	@Override
	public void setContentView(final View view) {
		super.setContentView(view);
		if (!mActionBarCompat.isAvailable() && mActionBar instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBar).initViews();
		}
	}

	@Override
	public void setContentView(final View view, final LayoutParams params) {
		super.setContentView(view, params);
		if (!mActionBarCompat.isAvailable() && mActionBar instanceof ActionBarCompatBase) {
			((ActionBarCompatBase) mActionBar).initViews();
		}
	}

	@Override
	protected int getDarkThemeRes() {
		return R.style.Theme_Twidere_DialogWhenLarge;
	}

	@Override
	protected int getLightThemeRes() {
		return R.style.Theme_Twidere_Light_DialogWhenLarge;
	}

	@Override
	protected boolean isSetBackgroundEnabled() {
		return getResources().getBoolean(R.bool.should_set_background);
	}
}
