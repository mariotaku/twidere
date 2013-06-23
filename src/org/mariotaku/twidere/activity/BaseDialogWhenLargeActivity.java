/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

@SuppressLint("Registered")
public class BaseDialogWhenLargeActivity extends BaseActivity {

	private View mActivityContent;

	@Override
	public void addContentView(final View view, final LayoutParams params) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.addContentView(view, params);
			return;
		}
		final ViewGroup content = (ViewGroup) super.findViewById(R.id.activity_content);
		content.addView(view, params);
	}

	@Override
	public View findViewById(final int id) {
		if (shouldDisableDialogWhenLargeMode()) return super.findViewById(id);
		return mActivityContent.findViewById(id);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
	}

	@Override
	public void setContentView(final int layoutResID) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.setContentView(layoutResID);
			return;
		}
		final LayoutInflater inflater = getLayoutInflater();
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dialogwhenlarge, null);
		final ViewGroup content = (ViewGroup) root.findViewById(R.id.activity_content);
		mActivityContent = inflater.inflate(layoutResID, content, true);
		super.setContentView(root);
		setActionBarBackground();
	}

	@Override
	public void setContentView(final View view) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.setContentView(view);
			return;
		}
		setContentView(view, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}

	@Override
	public void setContentView(final View view, final LayoutParams params) {
		if (shouldDisableDialogWhenLargeMode()) {
			super.setContentView(view, params);
			return;
		}
		final LayoutInflater inflater = getLayoutInflater();
		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.dialogwhenlarge, null);
		final ViewGroup content = (ViewGroup) root.findViewById(R.id.activity_content);
		content.addView(mActivityContent = view, params);
		super.setContentView(root);
		setActionBarBackground();
	}

	@Override
	protected int getDarkThemeRes() {
		if (shouldDisableDialogWhenLargeMode()) return super.getDarkThemeRes();
		return R.style.Theme_Twidere_DialogWhenLarge;
	}

	@Override
	protected int getLightThemeRes() {
		if (shouldDisableDialogWhenLargeMode()) return super.getLightThemeRes();
		return R.style.Theme_Twidere_Light_DialogWhenLarge;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarBackground();
	}

	protected boolean shouldDisableDialogWhenLargeMode() {
		return false;
	}

	@Override
	protected boolean shouldSetBackground() {
		if (shouldDisableDialogWhenLargeMode()) return super.shouldSetBackground();
		return getResources().getBoolean(R.bool.should_set_background);
	}
}
