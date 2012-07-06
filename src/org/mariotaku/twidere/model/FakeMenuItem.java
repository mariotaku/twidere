/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class FakeMenuItem implements MenuItem {

	private final ImageView view;
	private final Context context;

	private OnLongClickListener mLongClickListener = new OnLongClickListener() {

		@TargetApi(14)
		@Override
		public boolean onLongClick(View v) {
			final Toast t = Toast.makeText(view.getContext(), getTitle(), Toast.LENGTH_SHORT);

			final int[] screenPos = new int[2];
			final Rect displayFrame = new Rect();
			view.getLocationOnScreen(screenPos);
			view.getWindowVisibleDisplayFrame(displayFrame);

			final int width = view.getWidth();
			final int height = view.getHeight();
			final int midy = screenPos[1] + height / 2;
			final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

			if (midy < displayFrame.height()) {
				// Show along the top; follow action buttons
				t.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - width / 2, height);
			} else {
				// Show along the bottom center
				t.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
			}
			t.show();
			return true;
		}
	};

	private final View.OnClickListener listener;

	public FakeMenuItem(ImageView view, View.OnClickListener listener) {
		this.view = view;
		context = view.getContext();
		this.listener = listener;
		this.view.setOnClickListener(listener);
		this.view.setOnLongClickListener(mLongClickListener);
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
		return 0;
	}

	@Override
	public Drawable getIcon() {
		return view.getDrawable();
	}

	@TargetApi(14)
	@Override
	public Intent getIntent() {
		return new Intent();
	}

	@Override
	public int getItemId() {
		return view.getId();
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
		return 0;
	}

	@Override
	public SubMenu getSubMenu() {
		return null;
	}

	@Override
	public CharSequence getTitle() {
		return view.getContentDescription();
	}

	@Override
	public CharSequence getTitleCondensed() {
		return null;
	}

	@Override
	public boolean hasSubMenu() {
		return getSubMenu() != null;
	}

	@Override
	public boolean isActionViewExpanded() {
		return false;
	}

	@Override
	public boolean isCheckable() {
		return false;
	}

	@Override
	public boolean isChecked() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return view.isClickable();
	}

	@Override
	public boolean isVisible() {
		return view.getVisibility() == View.VISIBLE;
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

	@TargetApi(14)
	@Override
	public MenuItem setCheckable(boolean checkable) {
		return this;
	}

	@Override
	public MenuItem setChecked(boolean checked) {
		return this;
	}

	@Override
	public MenuItem setEnabled(boolean enabled) {
		view.setClickable(enabled);
		view.setAlpha(enabled ? 0xFF : 0x80);
		view.setOnClickListener(enabled ? listener : null);
		return this;
	}

	@Override
	public MenuItem setIcon(Drawable icon) {
		view.setImageDrawable(icon);
		return this;
	}

	@Override
	public MenuItem setIcon(int iconRes) {
		view.setImageResource(iconRes);
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
		view.setContentDescription(title);
		return this;
	}

	@Override
	public MenuItem setTitle(int title) {
		view.setContentDescription(context.getResources().getString(title));
		return this;
	}

	@Override
	public MenuItem setTitleCondensed(CharSequence title) {
		return this;
	}

	@Override
	public MenuItem setVisible(boolean visible) {
		view.setVisibility(visible ? View.VISIBLE : View.GONE);
		return this;
	}

}
