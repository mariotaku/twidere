package org.mariotaku.internal.menu;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public final class SubMenuImpl extends MenuImpl implements SubMenu {

	private final List<MenuItem> mMenuItems;
	private final MenuAdapter mAdapter;
	private final MenuItem menuItem;
	private final Context mContext;

	private Drawable mHeaderIcon;

	public SubMenuImpl(Context context, MenuItem menuItem) {
		super(context);
		mContext = context;
		mAdapter = new MenuAdapter(context);

		mMenuItems = new Menus(mAdapter);
		this.menuItem = menuItem;
	}

	@Override
	public MenuItem add(CharSequence title) {
		final MenuItem item = new MenuItemImpl(mContext).setTitle(title);
		mMenuItems.add(item);
		return item;
	}

	@Override
	public MenuItem add(int titleRes) {
		final MenuItem item = new MenuItemImpl(mContext).setTitle(titleRes);
		mMenuItems.add(item);
		return item;
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		final MenuItem item = new MenuItemImpl(mContext).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(title);
		mMenuItems.add(order, item);
		return item;
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, int titleRes) {
		final MenuItem item = new MenuItemImpl(mContext).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(titleRes);
		mMenuItems.add(order, item);
		return item;
	}

	@Override
	public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics,
			Intent intent, int flags, MenuItem[] outSpecificItems) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SubMenu addSubMenu(CharSequence title) {
		final MenuItem item = new MenuItemImpl(mContext).setTitle(title);
		final SubMenu subMenu = new SubMenuImpl(mContext, item);
		((MenuItemImpl) item).setSubMenu(subMenu);
		mMenuItems.add(item);
		return subMenu;
	}

	@Override
	public SubMenu addSubMenu(int titleRes) {
		final MenuItem item = new MenuItemImpl(mContext).setTitle(titleRes);
		final SubMenu subMenu = new SubMenuImpl(mContext, item);
		((MenuItemImpl) item).setSubMenu(subMenu);
		mMenuItems.add(item);
		return subMenu;
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
		final MenuItem item = new MenuItemImpl(mContext).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(title);
		final SubMenu subMenu = new SubMenuImpl(mContext, item);
		((MenuItemImpl) item).setSubMenu(subMenu);
		mMenuItems.add(order, item);
		return subMenu;
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
		final MenuItem item = new MenuItemImpl(mContext).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(titleRes);
		final SubMenu subMenu = new SubMenuImpl(mContext, item);
		((MenuItemImpl) item).setSubMenu(subMenu);
		mMenuItems.add(order, item);
		return subMenu;
	}

	@Override
	public void clear() {
		mMenuItems.clear();
	}

	@Override
	public void clearHeader() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public MenuItem findItem(int id) {
		for (final MenuItem item : mMenuItems) {
			if (item.getItemId() == id) return item;
		}
		return null;
	}

	@Override
	public MenuItem getItem() {
		return menuItem;
	}

	@Override
	public MenuItem getItem(int index) {
		return mMenuItems.get(index);
	}

	@Override
	public List<MenuItem> getMenuItems() {
		return mMenuItems;
	}

	@Override
	public boolean hasVisibleItems() {
		for (final MenuItem item : mMenuItems) {
			if (item.isVisible()) return true;
		}
		return false;
	}

	@Override
	public boolean isShortcutKey(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean performIdentifierAction(int id, int flags) {
		return false;
	}

	@Override
	public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
		return false;
	}

	@Override
	public void removeGroup(int groupId) {
		final List<MenuItem> items_to_remove = new ArrayList<MenuItem>();
		for (final MenuItem item : mMenuItems) {
			if (item.hasSubMenu()) {
				item.getSubMenu().removeGroup(groupId);
			} else if (item.getGroupId() == groupId) {
				items_to_remove.add(item);
			}
		}
		mMenuItems.removeAll(items_to_remove);
	}

	@Override
	public void removeItem(int id) {
		final List<MenuItem> items_to_remove = new ArrayList<MenuItem>();
		for (final MenuItem item : mMenuItems) {
			if (item.hasSubMenu()) {
				item.getSubMenu().removeItem(id);
			} else if (item.getItemId() == id) {
				items_to_remove.add(item);
			}
		}
		mMenuItems.removeAll(items_to_remove);
	}

	@Override
	public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroupEnabled(int group, boolean enabled) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroupVisible(int group, boolean visible) {
		// TODO Auto-generated method stub

	}

	@Override
	public SubMenu setHeaderIcon(Drawable icon) {
		mHeaderIcon = icon;
		return this;
	}

	@Override
	public SubMenu setHeaderIcon(int iconRes) {
		mHeaderIcon = mContext.getResources().getDrawable(iconRes);
		return this;
	}

	@Override
	public SubMenu setHeaderTitle(CharSequence title) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public SubMenu setHeaderTitle(int titleRes) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public SubMenu setHeaderView(View view) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public SubMenu setIcon(Drawable icon) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public SubMenu setIcon(int iconRes) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void setQwertyMode(boolean isQwerty) {
		// TODO Auto-generated method stub

	}

	@Override
	public int size() {
		return mMenuItems.size();
	}

}
