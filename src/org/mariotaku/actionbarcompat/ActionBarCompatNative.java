package org.mariotaku.actionbarcompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

@TargetApi(11)
class ActionBarCompatNative extends ActionBarCompat implements ActionBar {

	private Activity mActivity;

	public ActionBarCompatNative(Activity activity) {
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
	public void setBackgroundDrawable(Drawable background) {
		mActivity.getActionBar().setBackgroundDrawable(background);
	}

	@Override
	public void setCustomView(int resId) {
		mActivity.getActionBar().setCustomView(resId);
	}

	@Override
	public void setCustomView(View view) {
		mActivity.getActionBar().setCustomView(view);
	}

	@Override
	public void setDisplayHomeAsUpEnabled(boolean showHomeAsUp) {
		mActivity.getActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
	}

	@Override
	public void setDisplayShowCustomEnabled(boolean showCustom) {
		mActivity.getActionBar().setDisplayShowCustomEnabled(showCustom);
	}

	@Override
	public void setDisplayShowHomeEnabled(boolean showHome) {
		mActivity.getActionBar().setDisplayShowHomeEnabled(showHome);
	}

	@Override
	public void setDisplayShowTitleEnabled(boolean showTitle) {
		mActivity.getActionBar().setDisplayShowTitleEnabled(showTitle);
	}

	@Override
	public void setSubtitle(CharSequence subtitle) {
		mActivity.getActionBar().setSubtitle(subtitle);
	}

	@Override
	public void setSubtitle(int resId) {
		mActivity.getActionBar().setSubtitle(resId);
	}

	@Override
	public void setTitle(CharSequence title) {
		mActivity.getActionBar().setTitle(title);
	}

	@Override
	public void setTitle(int resId) {
		mActivity.getActionBar().setTitle(resId);
	}

}
