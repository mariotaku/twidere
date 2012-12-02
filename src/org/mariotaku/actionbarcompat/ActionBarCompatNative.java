package org.mariotaku.actionbarcompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ActionBarCompatNative extends ActionBarCompat implements ActionBar {

	private final Activity mActivity;

	public ActionBarCompatNative(final Activity activity) {
		mActivity = activity;
	}

	@Override
	public View getCustomView() {
		if (mActivity == null || mActivity.getActionBar() == null) return null;
		return mActivity.getActionBar().getCustomView();
	}

	@Override
	public int getHeight() {
		if (mActivity == null || mActivity.getActionBar() == null) return 0;
		return mActivity.getActionBar().getHeight();
	}

	@Override
	public CharSequence getSubtitle() {
		if (mActivity == null || mActivity.getActionBar() == null) return null;
		return mActivity.getActionBar().getSubtitle();
	}

	@Override
	public CharSequence getTitle() {
		if (mActivity == null || mActivity.getActionBar() == null) return null;
		return mActivity.getActionBar().getTitle();
	}

	@Override
	public void setBackgroundDrawable(final Drawable background) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setBackgroundDrawable(background);
	}

	@Override
	public void setCustomView(final int resId) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setCustomView(resId);
	}

	@Override
	public void setCustomView(final View view) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setCustomView(view);
	}

	@Override
	public void setDisplayHomeAsUpEnabled(final boolean showHomeAsUp) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
	}

	@Override
	public void setDisplayShowCustomEnabled(final boolean showCustom) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setDisplayShowCustomEnabled(showCustom);
	}

	@Override
	public void setDisplayShowHomeEnabled(final boolean showHome) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setDisplayShowHomeEnabled(showHome);
	}

	@Override
	public void setDisplayShowTitleEnabled(final boolean showTitle) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setDisplayShowTitleEnabled(showTitle);
	}

	@Override
	public void setSubtitle(final CharSequence subtitle) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setSubtitle(subtitle);
	}

	@Override
	public void setSubtitle(final int resId) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setSubtitle(resId);
	}

	@Override
	public void setTitle(final CharSequence title) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setTitle(title);
	}

	@Override
	public void setTitle(final int resId) {
		if (mActivity == null || mActivity.getActionBar() == null) return;
		mActivity.getActionBar().setTitle(resId);
	}

	@Override
	boolean isAvailable() {
		return mActivity != null && mActivity.getActionBar() != null;
	}

}
