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

	public MenuImpl(final Context context) {
		this(context, null);
	}

	public MenuImpl(final Context context, final MenuAdapter adapter) {
		mMenuItems = new Menus(adapter);
		mContext = context;
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
		return add(groupId, itemId, order, mContext.getString(titleRes));
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
	public void close() {

	}

	@Override
	public MenuItem findItem(final int id) {
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
	public MenuItem getItem(final int index) {
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
	public void setGroupEnabled(final int group, final boolean enabled) {
		for (final MenuItem item : mMenuItems) {
			if (item.getGroupId() == group) {
				item.setEnabled(enabled);
			}
		}

	}

	@Override
	public void setGroupVisible(final int group, final boolean visible) {
		for (final MenuItem item : mMenuItems) {
			if (item.getGroupId() == group) {
				item.setVisible(visible);
			}
		}
	}

	@Override
	public void setQwertyMode(final boolean isQwerty) {

	}

	@Override
	public int size() {
		return mMenuItems.size();
	}

}
