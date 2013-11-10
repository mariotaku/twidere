package org.mariotaku.menubar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.mariotaku.internal.menu.MenuImpl;
import org.mariotaku.internal.menu.MenuItemUtils;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class MenuBar extends LinearLayout implements MenuItem.OnMenuItemClickListener {

	private final Menu mMenu;
	private final Context mContext;

	private OnMenuItemClickListener mItemClickListener;
	private PopupMenu mPopupMenu;
	private boolean mIsBottomBar;
	private int mMaxItemsShown;

	public MenuBar(final Context context) {
		this(context, null);
	}

	public MenuBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (!hasBackground(attrs)) {
			final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.backgroundSplit },
					android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
			ViewAccessor.setBackground(this, a.getDrawable(0));
			a.recycle();
		}
		mContext = context;
		mMenu = new MenuImpl(context);
		mMaxItemsShown = getResources().getInteger(R.integer.max_action_buttons);
		setOrientation(HORIZONTAL);
	}

	public Menu getMenu() {
		return mMenu;
	}

	public MenuInflater getMenuInflater() {
		return new MenuInflater(mContext);
	}

	/**
	 * Get listener for action item clicked.
	 * 
	 */
	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return mItemClickListener;
	}

	public void inflate(final int menuRes) {
		mMenu.clear();
		new MenuInflater(mContext).inflate(menuRes, mMenu);
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mItemClickListener != null) return mItemClickListener.onMenuItemClick(item);
		return false;
	}

	public void setIsBottomBar(final boolean isBottomBar) {
		mIsBottomBar = isBottomBar;
	}

	public void setMaxItemsShown(final int maxItemsShown) {
		mMaxItemsShown = maxItemsShown;
	}

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
		mItemClickListener = listener;
	}

	public void show() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		removeAllViews();
		for (final MenuItem item : ((MenuImpl) mMenu).getMenuItems()) {
			final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			params.weight = 1;

			final View actionView = item.getActionView(), view;
			if (actionView != null) {
				view = actionView;
			} else {
				final ImageButton actionButton = new ImageButton(mContext, null, android.R.attr.actionButtonStyle);
				actionButton.setImageDrawable(item.getIcon());
				actionButton.setScaleType(ScaleType.CENTER);
				actionButton.setContentDescription(item.getTitle());
				actionButton.setEnabled(item.isEnabled());
				actionButton.setOnClickListener(new ActionViewOnClickListener(item, this));
				actionButton.setOnLongClickListener(new OnActionItemLongClickListener(item, this));
				view = actionButton;
			}
			view.setVisibility(item.isVisible() ? View.VISIBLE : View.GONE);
			addView(view, params);
		}
		invalidate();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onDetachedFromWindow();
	}

	private boolean isBottomBar() {
		return mIsBottomBar;
	}

	private void showPopupMenu(final PopupMenu popupMenu) {
		if (popupMenu == null) return;
		if (mPopupMenu != null && mPopupMenu.isShowing()) {
			mPopupMenu.dismiss();
		}
		mPopupMenu = popupMenu;
		if (!popupMenu.isShowing()) {
			mPopupMenu.show();
		}
	}

	private static boolean hasBackground(final AttributeSet attrs) {
		final int count = attrs.getAttributeCount();
		for (int i = 0; i < count; i++) {
			if (attrs.getAttributeNameResource(i) == android.R.attr.background) return true;
		}
		return false;
	}

	private static class ActionViewOnClickListener implements OnClickListener {
		private final MenuItem menuItem;
		private final MenuBar menuBar;

		private ActionViewOnClickListener(final MenuItem menuItem, final MenuBar menuBar) {
			this.menuItem = menuItem;
			this.menuBar = menuBar;
		}

		@Override
		public void onClick(final View actionView) {
			if (!menuItem.isEnabled()) return;
			if (menuItem.hasSubMenu()) {
				final PopupMenu popupMenu = PopupMenu.getInstance(actionView.getContext(), actionView);
				popupMenu.setOnMenuItemClickListener(menuBar);
				popupMenu.setMenu(menuItem.getSubMenu());
				menuBar.showPopupMenu(popupMenu);
			} else {
				final OnMenuItemClickListener listener = menuBar.getOnMenuItemClickListener();
				if (listener != null) {
					listener.onMenuItemClick(menuItem);
				}
			}
		}
	}

	private static class OnActionItemLongClickListener implements OnLongClickListener {

		private final MenuItem item;
		private final MenuBar menuBar;

		private OnActionItemLongClickListener(final MenuItem item, final MenuBar menuBar) {
			this.item = item;
			this.menuBar = menuBar;
		}

		@Override
		public boolean onLongClick(final View v) {
			// Don't show the cheat sheet for items that already show text.
			if ((MenuItemUtils.getShowAsActionFlags(item) & MenuItem.SHOW_AS_ACTION_WITH_TEXT) != 0) return false;
			final int[] screenPos = new int[2];
			final Rect displayFrame = new Rect();
			v.getLocationOnScreen(screenPos);
			v.getWindowVisibleDisplayFrame(displayFrame);
			final int width = v.getWidth();
			final int height = v.getHeight();
			final int midy = screenPos[1] + height / 2;
			final int screenWidth = menuBar.getResources().getDisplayMetrics().widthPixels;
			final Toast cheatSheet = Toast.makeText(menuBar.getContext(), item.getTitle(), Toast.LENGTH_SHORT);
			if (midy >= displayFrame.height() || menuBar.isBottomBar()) {
				// Show along the bottom center
				cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
			} else {
				// Show along the top; follow action buttons
				cheatSheet.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - width / 2, height);
			}
			cheatSheet.show();
			return true;
		}
	}

}
