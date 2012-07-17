package org.mariotaku.popupmenu;

import static android.widget.ListPopupWindow.INPUT_METHOD_NOT_NEEDED;

import org.mariotaku.internal.menu.MenuAdapter;
import org.mariotaku.internal.menu.MenuImpl;
import org.mariotaku.twidere.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow.OnDismissListener;

@TargetApi(11)
public class PopupMenuNative extends PopupMenu implements OnDismissListener, OnItemClickListener, OnTouchListener {

	private OnMenuItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;

	private Menu mMenu;
	private final Context mContext;
	private final View mView;
	private ListPopupWindow mWindow;

	private boolean mDidAction;

	private MenuAdapter mAdapter;

	/**
	 * Constructor for default vertical layout
	 * 
	 * @param context Context
	 */
	public PopupMenuNative(Context context, View view) {
		mContext = context;
		mView = view;
		mAdapter = new MenuAdapter(context);
		mMenu = new MenuImpl(mContext, mAdapter);
		mWindow = new ListPopupWindow(context);
		mWindow.setInputMethodMode(INPUT_METHOD_NOT_NEEDED);
		mWindow.setAnchorView(mView);
		mWindow.setWidth(mContext.getResources().getDimensionPixelSize(R.dimen.popup_window_width));
		mWindow.setAdapter(mAdapter);
		mWindow.setOnItemClickListener(this);
		mWindow.setModal(true);
	}

	/**
	 * Dismiss the popup window.
	 */
	@Override
	public void dismiss() {
		if (isPopupWindowShowing()) {
			mWindow.dismiss();
		}
	}

	@Override
	public Menu getMenu() {
		return mMenu;
	}

	public MenuInflater getMenuInflater() {
		return new MenuInflater(mContext);
	}

	@Override
	public void inflate(int menuRes) {
		new MenuInflater(mContext).inflate(menuRes, mMenu);
	}

	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss(this);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		mDidAction = true;
		dismiss();
		final MenuItem item = mAdapter.getItem(position);
		if (item.hasSubMenu()) {
			showMenu(item.getSubMenu());
		} else {
			if (mItemClickListener != null) {
				mItemClickListener.onMenuItemClick(item);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			mWindow.dismiss();

			return true;
		}

		return false;
	}

	@Override
	public void setMenu(Menu menu) {
		mMenu = menu;
	}

	/**
	 * Set listener for window dismissed. This listener will only be fired if
	 * the quickaction dialog is dismissed by clicking outside the dialog or
	 * clicking on sticky item.
	 */
	@Override
	public void setOnDismissListener(PopupMenu.OnDismissListener listener) {
		mWindow.setOnDismissListener(listener != null ? this : null);

		mDismissListener = listener;
	}

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	@Override
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
		mItemClickListener = listener;
	}

	@Override
	public void show() {
		if (isPopupWindowShowing()) {
			dismiss();
		}
		showMenu(getMenu());
	}

	private boolean isPopupWindowShowing() {
		if (mWindow == null) return false;
		return mWindow.isShowing();
	}

	private void showMenu(Menu menu) {
		mAdapter.setMenu(menu);
		mWindow.show();
	}

}
