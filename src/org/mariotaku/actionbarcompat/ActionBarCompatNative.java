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
		return mActivity.getActionBar().getCustomView();
	}

	@Override
	public int getHeight() {
		return mActivity.getActionBar().getHeight();
	}

	@Override
	public CharSequence getSubtitle() {
		return mActivity.getActionBar().getSubtitle();
	}

	@Override
	public CharSequence getTitle() {
		return mActivity.getActionBar().getTitle();
	}

	@Override
	public void setBackgroundDrawable(final Drawable background) {
		mActivity.getActionBar().setBackgroundDrawable(background);
	}

	@Override
	public void setCustomView(final int resId) {
		mActivity.getActionBar().setCustomView(resId);
	}

	@Override
	public void setCustomView(final View view) {
		mActivity.getActionBar().setCustomView(view);
	}

	@Override
	public void setDisplayHomeAsUpEnabled(final boolean showHomeAsUp) {
		mActivity.getActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
	}

	@Override
	public void setDisplayShowCustomEnabled(final boolean showCustom) {
		mActivity.getActionBar().setDisplayShowCustomEnabled(showCustom);
	}

	@Override
	public void setDisplayShowHomeEnabled(final boolean showHome) {
		mActivity.getActionBar().setDisplayShowHomeEnabled(showHome);
	}

	@Override
	public void setDisplayShowTitleEnabled(final boolean showTitle) {
		mActivity.getActionBar().setDisplayShowTitleEnabled(showTitle);
	}

	@Override
	public void setSubtitle(final CharSequence subtitle) {
		mActivity.getActionBar().setSubtitle(subtitle);
	}

	@Override
	public void setSubtitle(final int resId) {
		mActivity.getActionBar().setSubtitle(resId);
	}

	@Override
	public void setTitle(final CharSequence title) {
		mActivity.getActionBar().setTitle(title);
	}

	@Override
	public void setTitle(final int resId) {
		mActivity.getActionBar().setTitle(resId);
	}

}
