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

final class SubMenuImpl extends MenuImpl implements SubMenu {

	private final List<MenuItem> mMenuItems;
	private final MenuAdapter mAdapter;
	private final MenuItem menuItem;
	private final Context mContext;

	SubMenuImpl(final Context context, final MenuItem menuItem) {
		super(context);
		mContext = context;
		mAdapter = new MenuAdapter(context);
		mMenuItems = new Menus(mAdapter);
		this.menuItem = menuItem;
	}

	@Override
	public MenuItem add(final CharSequence title) {
		return add(0, 0, 0, title);
	}

	@Override
	public MenuItem add(final int titleRes) {
		return add(0, 0, 0, titleRes);
	}

	@Override
	public MenuItem add(final int groupId, final int itemId, final int order, final CharSequence title) {
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
	public MenuItem add(final int groupId, final int itemId, final int order, final int titleRes) {
		return add(0, 0, 0, mContext.getString(titleRes));
	}

	@Override
	public int addIntentOptions(final int groupId, final int itemId, final int order, final ComponentName caller,
			final Intent[] specifics, final Intent intent, final int flags, final MenuItem[] outSpecificItems) {
		return 0;
	}

	@Override
	public SubMenu addSubMenu(final CharSequence title) {
		return addSubMenu(0, 0, 0, title);
	}

	@Override
	public SubMenu addSubMenu(final int titleRes) {
		return addSubMenu(0, 0, 0, titleRes);
	}

	@Override
	public SubMenu addSubMenu(final int groupId, final int itemId, final int order, final CharSequence title) {
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
	public SubMenu addSubMenu(final int groupId, final int itemId, final int order, final int titleRes) {
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
	public MenuItem findItem(final int id) {
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
	public MenuItem getItem(final int index) {
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
	public boolean isShortcutKey(final int keyCode, final KeyEvent event) {
		return false;
	}

	@Override
	public boolean performIdentifierAction(final int id, final int flags) {
		return false;
	}

	@Override
	public boolean performShortcut(final int keyCode, final KeyEvent event, final int flags) {
		return false;
	}

	@Override
	public void removeGroup(final int groupId) {
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
	public void removeItem(final int id) {
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
	public void setGroupCheckable(final int group, final boolean checkable, final boolean exclusive) {

	}

	@Override
	public void setGroupEnabled(final int group, final boolean enabled) {

	}

	@Override
	public void setGroupVisible(final int group, final boolean visible) {

	}

	@Override
	public SubMenu setHeaderIcon(final Drawable icon) {
		return this;
	}

	@Override
	public SubMenu setHeaderIcon(final int iconRes) {
		return this;
	}

	@Override
	public SubMenu setHeaderTitle(final CharSequence title) {

		return this;
	}

	@Override
	public SubMenu setHeaderTitle(final int titleRes) {

		return this;
	}

	@Override
	public SubMenu setHeaderView(final View view) {

		return this;
	}

	@Override
	public SubMenu setIcon(final Drawable icon) {

		return this;
	}

	@Override
	public SubMenu setIcon(final int iconRes) {

		return this;
	}

	@Override
	public void setQwertyMode(final boolean isQwerty) {

	}

	@Override
	public int size() {
		return mMenuItems.size();
	}

}
