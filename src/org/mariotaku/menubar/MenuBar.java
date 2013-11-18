package org.mariotaku.menubar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.mariotaku.internal.menu.MenuUtils;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

import java.util.ArrayList;

public class MenuBar extends LinearLayout implements MenuItem.OnMenuItemClickListener {

	private final Menu mMenu;
	private final Context mContext;
	private final LayoutInflater mLayoutInflater;

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
			ViewAccessor.setBackground(this, ThemeUtils.getActionBarSplitBackground(context, true));
		}
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.max });
		mMaxItemsShown = a.getInt(0, getResources().getInteger(R.integer.max_action_buttons));
		a.recycle();
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mMenu = MenuUtils.createMenu(context);
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
		int actionButtonCount = 0;
		final ArrayList<MenuItem> itemsNotShowing = new ArrayList<MenuItem>();
		for (int i = 0, j = mMenu.size(); i < j; i++) {
			final MenuItem item = mMenu.getItem(i);
			final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			params.weight = 1;
			final int showAsActionFlags = MenuUtils.getShowAsActionFlags(item);
			final boolean showIfRoom = (showAsActionFlags & MenuItem.SHOW_AS_ACTION_IF_ROOM) != 0;
			final boolean showAlways = (showAsActionFlags & MenuItem.SHOW_AS_ACTION_ALWAYS) != 0;
			if (showIfRoom && actionButtonCount < mMaxItemsShown || showAlways) {
				if (item.isVisible()) {
					addView(createViewForMenuItem(item), params);
				}
				actionButtonCount++;
			} else {
				itemsNotShowing.add(item);
			}
		}
		if (!itemsNotShowing.isEmpty()) {
			final LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			params.weight = 1;
			addView(createMoreOverflowButton(itemsNotShowing), params);
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

	private View createMoreOverflowButton(final ArrayList<MenuItem> itemsNotShowing) {
		final ImageButton view = new ImageButton(mContext, null, android.R.attr.actionOverflowButtonStyle);
		view.setOnClickListener(new MoreOverflowOnClickListener(itemsNotShowing, this));
		return view;
	}

	private View createViewForMenuItem(final MenuItem item) {
		final View actionView = item.getActionView(), view;
		if (actionView != null) {
			view = actionView;
		} else {
			final int showAsActionFlags = MenuUtils.getShowAsActionFlags(item);
			final Drawable icon = item.getIcon();
			final CharSequence title = item.getTitle();
			final boolean isEnabled = item.isEnabled();
			final boolean showText = (showAsActionFlags & MenuItem.SHOW_AS_ACTION_WITH_TEXT) != 0;
			final boolean hasIcon = icon != null, hasTitle = !TextUtils.isEmpty(title);
			view = mLayoutInflater.inflate(R.layout.menubar_item, this, false);
			view.setOnClickListener(isEnabled ? new ActionViewOnClickListener(item, this) : null);
			view.setOnLongClickListener(isEnabled && !showText ? new OnActionItemLongClickListener(item, this) : null);
			final ImageView iconView = (ImageView) view.findViewById(android.R.id.icon);
			final TextView titleView = (TextView) view.findViewById(android.R.id.title);
			iconView.setVisibility(hasIcon ? View.VISIBLE : View.GONE);
			iconView.setImageDrawable(icon);
			iconView.setContentDescription(item.getTitle());
			titleView.setText(title);
			titleView.setVisibility(hasTitle && (showText || !hasIcon) ? View.VISIBLE : View.GONE);
		}
		return view;
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

	private static class MoreOverflowOnClickListener implements OnClickListener {
		private final ArrayList<MenuItem> menuItems;
		private final MenuBar menuBar;

		private MoreOverflowOnClickListener(final ArrayList<MenuItem> menuItems, final MenuBar menuBar) {
			this.menuItems = menuItems;
			this.menuBar = menuBar;
		}

		@Override
		public void onClick(final View actionView) {
			if (menuItems.isEmpty()) return;
			final PopupMenu popupMenu = PopupMenu.getInstance(actionView.getContext(), actionView);
			popupMenu.setOnMenuItemClickListener(menuBar);
			popupMenu.setMenu(MenuUtils.createMenu(menuBar.getContext(), menuItems));
			menuBar.showPopupMenu(popupMenu);
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
			if ((MenuUtils.getShowAsActionFlags(item) & MenuItem.SHOW_AS_ACTION_WITH_TEXT) != 0) return false;
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
