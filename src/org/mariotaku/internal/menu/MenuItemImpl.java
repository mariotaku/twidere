package org.mariotaku.internal.menu;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class MenuItemImpl implements MenuItem {

	private CharSequence title, titleCondensed;
	private int groupId, itemId, order;
	private Drawable icon;
	private Intent intent;
	private SubMenu subMenu;
	private final Context context;
	private boolean visible, enabled, checkable, checked;
	private View actionView;
	private ActionProvider actionProvider;
	private int showAsActionFlags;
	private char alphabeticShortcut;
	private OnMenuItemClickListener onMenuItemClickListener;
	private OnActionExpandListener onActionExpandListener;
	private char numericShortcut;

	MenuItemImpl(final Context context) {
		this.context = context;
		setVisible(true);
		setEnabled(true);
	}

	@Override
	public boolean collapseActionView() {
		return false;
	}

	@Override
	public boolean expandActionView() {
		return false;
	}

	@Override
	public ActionProvider getActionProvider() {
		return actionProvider;
	}

	@Override
	public View getActionView() {
		return actionView;
	}

	@Override
	public char getAlphabeticShortcut() {
		return alphabeticShortcut;
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
		return intent;
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
		return numericShortcut;
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
		return titleCondensed;
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

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public MenuItem setActionProvider(final ActionProvider actionProvider) {
		this.actionProvider = actionProvider;
		return this;
	}

	@Override
	public MenuItem setActionView(final int resId) {
		actionView = LayoutInflater.from(context).inflate(resId, null);
		return this;
	}

	@Override
	public MenuItem setActionView(final View view) {
		actionView = view;
		return this;
	}

	@Override
	public MenuItem setAlphabeticShortcut(final char alphaChar) {
		alphabeticShortcut = alphaChar;
		return this;
	}

	@Override
	public MenuItem setCheckable(final boolean checkable) {
		this.checkable = checkable;
		return this;
	}

	@Override
	public MenuItem setChecked(final boolean checked) {
		this.checked = checked;
		return this;
	}

	@Override
	public MenuItem setEnabled(final boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	@Override
	public MenuItem setIcon(final Drawable icon) {
		this.icon = icon;
		return this;
	}

	@Override
	public MenuItem setIcon(final int iconRes) {
		icon = iconRes == 0 ? null : context.getResources().getDrawable(iconRes);
		return this;
	}

	@Override
	public MenuItem setIntent(final Intent intent) {
		this.intent = intent;
		return this;
	}

	@Override
	public MenuItem setNumericShortcut(final char numericChar) {
		numericShortcut = numericChar;
		return this;
	}

	@Override
	public MenuItem setOnActionExpandListener(final OnActionExpandListener listener) {
		onActionExpandListener = listener;
		return this;
	}

	@Override
	public MenuItem setOnMenuItemClickListener(final OnMenuItemClickListener menuItemClickListener) {
		onMenuItemClickListener = menuItemClickListener;
		return this;
	}

	@Override
	public MenuItem setShortcut(final char numericChar, final char alphaChar) {
		setNumericShortcut(numericChar);
		setAlphabeticShortcut(alphaChar);
		return this;
	}

	@Override
	public void setShowAsAction(final int actionEnum) {
		showAsActionFlags = actionEnum;
	}

	@Override
	public MenuItem setShowAsActionFlags(final int actionEnum) {
		setShowAsAction(actionEnum);
		return this;
	}

	@Override
	public MenuItem setTitle(final CharSequence title) {
		this.title = title;
		return this;
	}

	@Override
	public MenuItem setTitle(final int titleRes) {
		title = context.getString(titleRes);
		return this;
	}

	@Override
	public MenuItem setTitleCondensed(final CharSequence titleCondensed) {
		this.titleCondensed = titleCondensed;
		return this;
	}

	@Override
	public MenuItem setVisible(final boolean visible) {
		this.visible = visible;
		return this;
	}

	@Override
	public String toString() {
		return getTitle() != null ? getTitle().toString() : "";
	}

	OnMenuItemClickListener getMenuItemClickListener() {
		return onMenuItemClickListener;
	}

	OnActionExpandListener getOnActionExpandListener() {
		return onActionExpandListener;
	}

	int getShowAsActionFlags() {
		return showAsActionFlags;
	}

	MenuItemImpl setGroupId(final int groupId) {
		this.groupId = groupId;
		return this;
	}

	MenuItemImpl setItemId(final int itemId) {
		this.itemId = itemId;
		return this;
	}

	MenuItemImpl setOrder(final int order) {
		this.order = order;
		return this;
	}

	MenuItemImpl setSubMenu(final SubMenu subMenu) {
		this.subMenu = subMenu;
		this.subMenu.setHeaderTitle(getTitle());
		return this;
	}

	public static MenuItem createItem(final Context context, final int id) {
		return new MenuItemImpl(context).setItemId(id);
	}

}
