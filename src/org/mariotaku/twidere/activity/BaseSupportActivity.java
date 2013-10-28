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

package org.mariotaku.twidere.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.PullToRefreshAttacherActivity;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MessagesManager;
import org.mariotaku.twidere.util.ThemeUtils;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

import java.util.HashSet;
import java.util.Set;

@SuppressLint("Registered")
public class BaseSupportActivity extends BaseSupportThemedActivity implements Constants, PullToRefreshAttacherActivity {

	private final Set<String> mEnabledStates = new HashSet<String>();
	private final Set<String> mRefreshingStates = new HashSet<String>();

	private boolean mInstanceStateSaved, mIsVisible, mIsOnTop;
	private PullToRefreshAttacher mPullToRefreshAttacher;

	@Override
	public void addRefreshingState(final IBasePullToRefreshFragment fragment) {
		final String tag = fragment.getPullToRefreshTag();
		if (tag == null) return;
		mEnabledStates.add(tag);
	}

	public MessagesManager getMessagesManager() {
		return getTwidereApplication() != null ? getTwidereApplication().getMessagesManager() : null;
	}

	@Override
	public PullToRefreshAttacher getPullToRefreshAttacher() {
		return mPullToRefreshAttacher;
	}

	public TwidereApplication getTwidereApplication() {
		return (TwidereApplication) getApplication();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return getTwidereApplication() != null ? getTwidereApplication().getTwitterWrapper() : null;
	}

	public boolean isOnTop() {
		return mIsOnTop;
	}

	@Override
	public boolean isRefreshing(final IBasePullToRefreshFragment fragment) {
		if (fragment == null) return false;
		return mRefreshingStates.contains(fragment.getPullToRefreshTag());
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	@Override
	public void setPullToRefreshEnabled(final IBasePullToRefreshFragment fragment, final boolean enabled) {
		final String tag = fragment.getPullToRefreshTag();
		if (tag == null) return;
		if (enabled) {
			mEnabledStates.add(tag);
		} else {
			mEnabledStates.remove(tag);
		}
		final IBasePullToRefreshFragment curr = getCurrentPullToRefreshFragment();
		if (curr != null && tag.equals(curr.getPullToRefreshTag())) {
			mPullToRefreshAttacher.setEnabled(enabled);
		}
	}

	@Override
	public void setRefreshComplete(final IBasePullToRefreshFragment fragment) {
		final String tag = fragment.getPullToRefreshTag();
		if (tag == null) return;
		mRefreshingStates.remove(tag);
		final IBasePullToRefreshFragment curr = getCurrentPullToRefreshFragment();
		if (curr != null && tag.equals(curr.getPullToRefreshTag())) {
			mPullToRefreshAttacher.setRefreshComplete();
		}
	}

	public void setRefreshing(final boolean refreshing) {
		mPullToRefreshAttacher.setRefreshing(refreshing);
	}

	@Override
	public void setRefreshing(final IBasePullToRefreshFragment fragment, final boolean refreshing) {
		final String tag = fragment.getPullToRefreshTag();
		if (tag == null) return;
		if (refreshing) {
			mRefreshingStates.add(tag);
		} else {
			mRefreshingStates.remove(tag);
		}
		final IBasePullToRefreshFragment curr = getCurrentPullToRefreshFragment();
		if (curr != null && tag.equals(curr.getPullToRefreshTag())) {
			mPullToRefreshAttacher.setRefreshing(refreshing);
		}
	}

	@Override
	public void startActivity(final Intent intent) {
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(final Intent intent, final int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	public void updateRefreshingState() {
		setRefreshing(isRefreshing(getCurrentPullToRefreshFragment()));
	}

	protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
		return null;
	}

	@Override
	protected int getThemeResource() {
		return ThemeUtils.getThemeResource(this);
	}

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * Here we create a PullToRefreshAttacher manually without an Options
		 * instance. PullToRefreshAttacher will manually create one using
		 * default values.
		 */
		mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
	}

	@Override
	protected void onPause() {
		mIsOnTop = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mInstanceStateSaved = false;
		mIsOnTop = true;
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mInstanceStateSaved = true;
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mIsVisible = true;
		final MessagesManager croutons = getMessagesManager();
		if (croutons != null) {
			croutons.addMessageCallback(this);
		}
	}

	@Override
	protected void onStop() {
		mIsVisible = false;
		final MessagesManager croutons = getMessagesManager();
		if (croutons != null) {
			croutons.removeMessageCallback(this);
		}
		super.onStop();
	}

}
