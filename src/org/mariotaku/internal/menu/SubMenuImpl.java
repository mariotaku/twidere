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

	public SubMenuImpl(Context context, MenuItem menuItem) {
		super(context);
		mContext = context;
		mAdapter = new MenuAdapter(context);
		mMenuItems = new Menus(mAdapter);
		this.menuItem = menuItem;
	}

	@Override
	public MenuItem add(CharSequence title) {
		return add(0, 0, 0, title);
	}

	@Override
	public MenuItem add(int titleRes) {
		return add(0, 0, 0, titleRes);
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		final MenuItem item = new MenuItemImpl(mContext).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(title);
		if (order != 0) {
			mMenuItems.add(order, item);
		} else {
			mMenuItems.add(item);
		}
		return item;
	}

	@Override
	public MenuItem add(int groupId, int itemId, int order, int titleRes) {
		return add(0, 0, 0, mContext.getString(titleRes));
	}

	@Override
	public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics,
			Intent intent, int flags, MenuItem[] outSpecificItems) {
		return 0;
	}

	@Override
	public SubMenu addSubMenu(CharSequence title) {
		return addSubMenu(0, 0, 0, title);
	}

	@Override
	public SubMenu addSubMenu(int titleRes) {
		return addSubMenu(0, 0, 0, titleRes);
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
		final MenuItem item = new MenuItemImpl(mContext).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(title);
		final SubMenu subMenu = new SubMenuImpl(mContext, item);
		((MenuItemImpl) item).setSubMenu(subMenu);
		if (order != 0) {
			mMenuItems.add(order, item);
		} else {
			mMenuItems.add(item);
		}
		return subMenu;
	}

	@Override
	public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
		return addSubMenu(groupId, itemId, order, mContext.getString(titleRes));
	}

	@Override
	public void clear() {
		mMenuItems.clear();
	}

	@Override
	public void clearHeader() {
	}

	@Override
	public void close() {
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

	}

	@Override
	public void setGroupEnabled(int group, boolean enabled) {

	}

	@Override
	public void setGroupVisible(int group, boolean visible) {

	}

	@Override
	public SubMenu setHeaderIcon(Drawable icon) {
		return this;
	}

	@Override
	public SubMenu setHeaderIcon(int iconRes) {
		return this;
	}

	@Override
	public SubMenu setHeaderTitle(CharSequence title) {

		return this;
	}

	@Override
	public SubMenu setHeaderTitle(int titleRes) {

		return this;
	}

	@Override
	public SubMenu setHeaderView(View view) {

		return this;
	}

	@Override
	public SubMenu setIcon(Drawable icon) {

		return this;
	}

	@Override
	public SubMenu setIcon(int iconRes) {

		return this;
	}

	@Override
	public void setQwertyMode(boolean isQwerty) {

	}

	@Override
	public int size() {
		return mMenuItems.size();
	}

}
