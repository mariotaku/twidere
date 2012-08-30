package org.mariotaku.internal.menu;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

public class MenuImpl implements Menu {

	private final List<MenuItem> mMenuItems;
	private final Context mContext;

	public MenuImpl(Context context) {
		this(context, null);
	}

	public MenuImpl(Context context, MenuAdapter adapter) {
		mMenuItems = new Menus(adapter);
		mContext = context;
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
		return add(groupId, itemId, order, mContext.getString(titleRes));
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
	public void close() {

	}

	@Override
	public MenuItem findItem(int id) {
		for (final MenuItem item : mMenuItems) {
			if (item.getItemId() == id)
				return item;
			else if (item.hasSubMenu()) {
				final MenuItem possibleItem = item.getSubMenu().findItem(id);

				if (possibleItem != null) return possibleItem;
			}
		}
		return null;
	}

	@Override
	public MenuItem getItem(int index) {
		return mMenuItems.get(index);
	}

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
		for (final MenuItem item : mMenuItems) {
			if (item.getGroupId() == group) {
				item.setCheckable(checkable);
				if (exclusive) {
					break;
				}
			}
		}
	}

	@Override
	public void setGroupEnabled(int group, boolean enabled) {
		for (final MenuItem item : mMenuItems) {
			if (item.getGroupId() == group) {
				item.setEnabled(enabled);
			}
		}

	}

	@Override
	public void setGroupVisible(int group, boolean visible) {
		for (final MenuItem item : mMenuItems) {
			if (item.getGroupId() == group) {
				item.setVisible(visible);
			}
		}
	}

	@Override
	public void setQwertyMode(boolean isQwerty) {

	}

	@Override
	public int size() {
		return mMenuItems.size();
	}

}
