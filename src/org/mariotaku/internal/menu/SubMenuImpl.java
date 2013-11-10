package org.mariotaku.internal.menu;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

final class SubMenuImpl extends MenuImpl implements SubMenu {

	private final List<MenuItem> menusList;
	private final MenuAdapter mAdapter;
	private final MenuItem menuItem;
	private final Context context;

	SubMenuImpl(final Context context, final MenuItem menuItem) {
		super(context);
		this.context = context;
		mAdapter = new MenuAdapter(context);
		menusList = new MenusList(mAdapter);
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
		final MenuItem item = new MenuItemImpl(context).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(title);
		if (order != 0) {
			menusList.add(order, item);
		} else {
			menusList.add(item);
		}
		return item;
	}

	@Override
	public MenuItem add(final int groupId, final int itemId, final int order, final int titleRes) {
		return add(0, 0, 0, context.getString(titleRes));
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
		final MenuItem item = new MenuItemImpl(context).setGroupId(groupId).setItemId(itemId).setOrder(order)
				.setTitle(title);
		final SubMenu subMenu = new SubMenuImpl(context, item);
		((MenuItemImpl) item).setSubMenu(subMenu);
		if (order != 0) {
			menusList.add(order, item);
		} else {
			menusList.add(item);
		}
		return subMenu;
	}

	@Override
	public SubMenu addSubMenu(final int groupId, final int itemId, final int order, final int titleRes) {
		return addSubMenu(groupId, itemId, order, context.getString(titleRes));
	}

	@Override
	public void clear() {
		menusList.clear();
	}

	@Override
	public void clearHeader() {
	}

	@Override
	public void close() {
	}

	@Override
	public MenuItem findItem(final int id) {
		for (final MenuItem item : menusList) {
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
		return menusList.get(index);
	}

	@Override
	public List<MenuItem> getMenuItems() {
		return menusList;
	}

	@Override
	public boolean hasVisibleItems() {
		for (final MenuItem item : menusList) {
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
		for (final MenuItem item : menusList) {
			if (item.hasSubMenu()) {
				item.getSubMenu().removeGroup(groupId);
			} else if (item.getGroupId() == groupId) {
				items_to_remove.add(item);
			}
		}
		menusList.removeAll(items_to_remove);
	}

	@Override
	public void removeItem(final int id) {
		final List<MenuItem> items_to_remove = new ArrayList<MenuItem>();
		for (final MenuItem item : menusList) {
			if (item.hasSubMenu()) {
				item.getSubMenu().removeItem(id);
			} else if (item.getItemId() == id) {
				items_to_remove.add(item);
			}
		}
		menusList.removeAll(items_to_remove);
	}

	@Override
	public void setGroupCheckable(final int group, final boolean checkable, final boolean exclusive) {
		for (final MenuItem item : menusList) {
			if (item.hasSubMenu()) {
				item.getSubMenu().setGroupCheckable(group, checkable, exclusive);
			} else if (item.getGroupId() == group) {
				item.setCheckable(checkable);
			}
		}
	}

	@Override
	public void setGroupEnabled(final int group, final boolean enabled) {
		for (final MenuItem item : menusList) {
			if (item.hasSubMenu()) {
				item.getSubMenu().setGroupEnabled(group, enabled);
			} else if (item.getGroupId() == group) {
				item.setEnabled(enabled);
			}
		}
	}

	@Override
	public void setGroupVisible(final int group, final boolean visible) {
		for (final MenuItem item : menusList) {
			if (item.hasSubMenu()) {
				item.getSubMenu().setGroupVisible(group, visible);
			} else if (item.getGroupId() == group) {
				item.setVisible(visible);
			}
		}
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
		menuItem.setIcon(icon);
		return this;
	}

	@Override
	public SubMenu setIcon(final int iconRes) {
		menuItem.setIcon(iconRes);
		return this;
	}

	@Override
	public void setQwertyMode(final boolean isQwerty) {
	}

	@Override
	public int size() {
		return menusList.size();
	}

}
