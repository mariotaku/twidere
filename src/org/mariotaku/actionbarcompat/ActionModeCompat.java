package org.mariotaku.actionbarcompat;

import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.twidere.R;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

class ActionModeCompat extends ActionMode {

	private final Callback mCallbackProxy;
	private final View mActionModeContainer;

	private final TextView mTitleView, mSubtitleView;
	private final MenuBar mMenuBar;
	private final ActionBarCompatBase mActionBar;
	private final OnMenuItemClickListener mListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(final MenuItem item) {
			if (mCallbackProxy != null) return mCallbackProxy.onActionItemClicked(ActionModeCompat.this, item);
			return false;
		}

	};

	ActionModeCompat(final ActionBarCompatBase action_bar, final Callback callback) {
		mCallbackProxy = callback;
		mActionBar = action_bar;
		mActionModeContainer = action_bar.startActionMode();
		mActionModeContainer.findViewById(R.id.action_mode_cancel).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(final View v) {
				finish();
			}

		});
		mTitleView = (TextView) mActionModeContainer.findViewById(R.id.action_mode_title);
		mSubtitleView = (TextView) mActionModeContainer.findViewById(R.id.action_mode_subtitle);
		mMenuBar = (MenuBar) mActionModeContainer.findViewById(R.id.action_mode_menu);
		mMenuBar.setOnMenuItemClickListener(mListener);
		if (mCallbackProxy != null) {
			final Menu menu = mMenuBar.getMenu();
			if (mCallbackProxy.onCreateActionMode(this, menu) && mCallbackProxy.onPrepareActionMode(this, menu)) {
				mMenuBar.show();
			}
		}
	}

	@Override
	public void finish() {
		mActionBar.stopActionMode();
		if (mCallbackProxy != null) {
			mCallbackProxy.onDestroyActionMode(this);
		}
	}

	@Override
	public Menu getMenu() {
		if (mMenuBar == null) return null;
		return mMenuBar.getMenu();
	}

	@Override
	public MenuInflater getMenuInflater() {
		if (mMenuBar == null) return null;
		return mMenuBar.getMenuInflater();
	}

	@Override
	public CharSequence getSubtitle() {
		if (mSubtitleView == null) return null;
		return mSubtitleView.getText();
	}

	@Override
	public CharSequence getTitle() {
		if (mTitleView == null) return null;
		return mTitleView.getText();
	}

	@Override
	public void invalidate() {
		if (mMenuBar == null || mActionModeContainer == null) return;
		mActionModeContainer.invalidate();
		mMenuBar.show();
	}

	@Override
	public void setSubtitle(final CharSequence subtitle) {
		if (mSubtitleView == null) return;
		mSubtitleView.setText(subtitle);
		mTitleView.setVisibility(subtitle != null ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setSubtitle(final int resId) {
		if (mSubtitleView == null) return;
		mSubtitleView.setText(resId);
		mSubtitleView.setVisibility(resId != 0 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void setTitle(final CharSequence title) {
		if (mTitleView == null) return;
		mTitleView.setText(title);
	}

	@Override
	public void setTitle(final int resId) {
		if (mTitleView == null) return;
		mTitleView.setText(resId);
	}

}
