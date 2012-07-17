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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean expandActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	@TargetApi(14)
	@Override
	public ActionProvider getActionProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public View getActionView() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getAlphabeticShortcut() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return new Intent();
	}

	@Override
	public int getItemId() {
		return itemId;
	}

	@Override
	public ContextMenuInfo getMenuInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char getNumericShortcut() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MenuItem setActionView(int resId) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MenuItem setActionView(View view) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MenuItem setAlphabeticShortcut(char alphaChar) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MenuItem setNumericShortcut(char numericChar) {
		// TODO Auto-generated method stub
		return this;
	}

	@TargetApi(14)
	@Override
	public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public MenuItem setShortcut(char numericChar, char alphaChar) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void setShowAsAction(int actionEnum) {
		// TODO Auto-generated method stub

	}

	@Override
	public MenuItem setShowAsActionFlags(int actionEnum) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
