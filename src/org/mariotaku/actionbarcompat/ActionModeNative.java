package org.mariotaku.actionbarcompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class ActionModeNative extends ActionMode {

	private final Callback mCallbackProxy;
	private final android.view.ActionMode mActionMode;

	private final android.view.ActionMode.Callback mCallback = new android.view.ActionMode.Callback() {

		@Override
		public boolean onActionItemClicked(final android.view.ActionMode mode, final MenuItem item) {
			if (mCallbackProxy != null) return mCallbackProxy.onActionItemClicked(ActionModeNative.this, item);
			return false;
		}

		@Override
		public boolean onCreateActionMode(final android.view.ActionMode mode, final Menu menu) {
			if (mCallbackProxy != null) return mCallbackProxy.onCreateActionMode(ActionModeNative.this, menu);
			return false;
		}

		@Override
		public void onDestroyActionMode(final android.view.ActionMode mode) {
			if (mCallbackProxy != null) {
				mCallbackProxy.onDestroyActionMode(ActionModeNative.this);
			}

		}

		@Override
		public boolean onPrepareActionMode(final android.view.ActionMode mode, final Menu menu) {
			if (mCallbackProxy != null) return mCallbackProxy.onPrepareActionMode(ActionModeNative.this, menu);
			return false;
		}

	};

	ActionModeNative(final Activity activity, final Callback callback) {
		mCallbackProxy = callback;
		mActionMode = activity.startActionMode(mCallback);
	}

	@Override
	public void finish() {
		if (mActionMode == null) return;
		mActionMode.finish();
	}

	@Override
	public Menu getMenu() {
		if (mActionMode == null) return null;
		return mActionMode.getMenu();
	}

	@Override
	public MenuInflater getMenuInflater() {
		if (mActionMode == null) return null;
		return mActionMode.getMenuInflater();
	}

	@Override
	public CharSequence getSubtitle() {
		if (mActionMode == null) return null;
		return mActionMode.getSubtitle();
	}

	@Override
	public CharSequence getTitle() {
		if (mActionMode == null) return null;
		return mActionMode.getTitle();
	}

	@Override
	public void invalidate() {
		if (mActionMode == null) return;
		mActionMode.invalidate();
	}

	@Override
	public void setSubtitle(final CharSequence subtitle) {
		if (mActionMode == null) return;
		mActionMode.setSubtitle(subtitle);
	}

	@Override
	public void setSubtitle(final int resId) {
		if (mActionMode == null) return;
		mActionMode.setSubtitle(resId);
	}

	@Override
	public void setTitle(final CharSequence title) {
		if (mActionMode == null) return;
		mActionMode.setTitle(title);
	}

	@Override
	public void setTitle(final int resId) {
		if (mActionMode == null) return;
		mActionMode.setTitle(resId);

	}

}
