package org.mariotaku.internal.menu;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class MenuItemImpl implements MenuItem {

	private CharSequence title;
	private int groupId, itemId, order;
	private Drawable icon;
	private SubMenu subMenu;
	private final Context context;
	private boolean visible = true, enabled = true, checkable, checked;

	public MenuItemImpl(Context context) {
		this.context = context;
	}

	@Override
	public boolean collapseActionView() {
		return false;
	}

	@Override
	public boolean expandActionView() {
		return false;
	}

	@TargetApi(14)
	@Override
	public ActionProvider getActionProvider() {
		return null;
	}

	@Override
	public View getActionView() {
		return null;
	}

	@Override
	public char getAlphabeticShortcut() {
		return 0;
	}

	@Override
	public int getGroupId() {
		return groupId;
	}

	@Override
	public Drawable getIcon() {
		return icon;
	}

	@Override
	public Intent getIntent() {
		return new Intent();
	}

	@Override
	public int getItemId() {
		return itemId;
	}

	@Override
	public ContextMenuInfo getMenuInfo() {
		return null;
	}

	@Override
	public char getNumericShortcut() {
		return 0;
	}

	@Override
	public int getOrder() {
		return order;
	}

	@Override
	public SubMenu getSubMenu() {
		return subMenu;
	}

	@Override
	public CharSequence getTitle() {
		return title;
	}

	@Override
	public CharSequence getTitleCondensed() {
		return null;
	}

	@Override
	public boolean hasSubMenu() {
		return subMenu != null;
	}

	@Override
	public boolean isActionViewExpanded() {
		return false;
	}

	@Override
	public boolean isCheckable() {
		return checkable;
	}

	@Override
	public boolean isChecked() {
		return checked;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@TargetApi(14)
	@Override
	public boolean isVisible() {
		return visible;
	}

	@TargetApi(14)
	@Override
	public MenuItem setActionProvider(ActionProvider actionProvider) {
		return this;
	}

	@Override
	public MenuItem setActionView(int resId) {
		return this;
	}

	@Override
	public MenuItem setActionView(View view) {
		return this;
	}

	@Override
	public MenuItem setAlphabeticShortcut(char alphaChar) {
		return this;
	}

	@Override
	public MenuItem setCheckable(boolean checkable) {
		this.checkable = checkable;
		return this;
	}

	@Override
	public MenuItem setChecked(boolean checked) {
		this.checked = checked;
		return this;
	}

	@Override
	public MenuItem setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public MenuItem setIcon(Drawable icon) {
		this.icon = icon;
		return this;
	}

	@Override
	public MenuItem setIcon(int iconRes) {
		icon = iconRes == 0 ? null : context.getResources().getDrawable(iconRes);
		return this;
	}

	@Override
	public MenuItem setIntent(Intent intent) {
		return this;
	}

	@Override
	public MenuItem setNumericShortcut(char numericChar) {
		return this;
	}

	@TargetApi(14)
	@Override
	public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
		return this;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
		return this;
	}

	@Override
	public MenuItem setShortcut(char numericChar, char alphaChar) {
		return this;
	}

	@Override
	public void setShowAsAction(int actionEnum) {

	}

	@Override
	public MenuItem setShowAsActionFlags(int actionEnum) {
		return this;
	}

	@Override
	public MenuItem setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

	@Override
	public MenuItem setTitle(int titleRes) {
		title = context.getString(titleRes);
		return this;
	}

	@Override
	public MenuItem setTitleCondensed(CharSequence title) {
		return this;
	}

	@Override
	public MenuItem setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	@Override
	public String toString() {
		return getTitle() == null ? null : getTitle().toString();
	}

	MenuItemImpl setGroupId(int groupId) {
		this.groupId = groupId;
		return this;
	}

	MenuItemImpl setItemId(int itemId) {
		this.itemId = itemId;
		return this;
	}

	MenuItemImpl setOrder(int order) {
		this.order = order;
		return this;
	}

	MenuItemImpl setSubMenu(SubMenu subMenu) {
		this.subMenu = subMenu;
		this.subMenu.setHeaderTitle(getTitle());
		return this;
	}

	public static MenuItem createItem(Context context, int id) {
		return new MenuItemImpl(context).setItemId(id);
	}

}
