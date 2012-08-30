package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.handleReplyAll;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.mariotaku.actionbarcompat.ActionMode;
import org.mariotaku.menubar.MenuBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;

public class MultiSelectActivity extends DualPaneActivity implements ActionMode.Callback {

	private TwidereApplication mApplication;

	private ActionMode mActionMode;
	
	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_MULTI_SELECT_STATE_CHANGED.equals(action)) {
				updateMultiSelectState();
			} else if (BROADCAST_MULTI_SELECT_ITEM_CHANGED.equals(action)) {
				updateMultiSelectCount();
			}
		}

	};

	public static class MultiSelectMenuClickHandler implements MenuBar.OnMenuItemClickListener {
		private final Context context;
		public MultiSelectMenuClickHandler(Context context) {
			this.context = context;
		}
		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
				case MENU_REPLY: {
					handleReplyAll(context);
					break;
				}
			}
			return true;
		}

	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mApplication = getTwidereApplication();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_MULTI_SELECT_STATE_CHANGED);
		filter.addAction(BROADCAST_MULTI_SELECT_ITEM_CHANGED);
		registerReceiver(mStateReceiver, filter);

		updateMultiSelectState();
		updateMultiSelectCount();
	}
	
	@Override
	protected void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}
	

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		new MenuInflater(this).inflate(R.menu.menu_multi_select, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return true;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case MENU_REPLY: {
				handleReplyAll(this);
				break;
			}
		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mApplication.stopMultiSelect();
		mActionMode = null;
	}
	
	private void updateMultiSelectCount() {
		if (mActionMode != null) {
			final int count = mApplication.getSelectedItems().size();
			mActionMode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
		}
	}

	private void updateMultiSelectState() {
		if (mApplication.isMultiSelectActive()) {
			if (mActionMode == null) {
				mActionMode = startActionMode(this);
			}
		} else {
			if (mActionMode != null) {
				mActionMode.finish();
				mActionMode = null;
			}
		}
	}

}
