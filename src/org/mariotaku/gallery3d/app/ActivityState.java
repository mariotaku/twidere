/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.app;

import org.mariotaku.gallery3d.anim.StateTransitionAnimation;
import org.mariotaku.gallery3d.ui.GLView;
import org.mariotaku.gallery3d.ui.PreparePageFadeoutTexture;
import org.mariotaku.gallery3d.ui.RawTexture;
import org.mariotaku.gallery3d.util.GalleryUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

abstract public class ActivityState {
	protected static final int FLAG_HIDE_ACTION_BAR = 1;
	protected static final int FLAG_HIDE_STATUS_BAR = 2;
	protected static final int FLAG_SCREEN_ON_WHEN_PLUGGED = 4;
	protected static final int FLAG_SCREEN_ON_ALWAYS = 8;
	protected static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON = 16;
	protected static final int FLAG_SHOW_WHEN_LOCKED = 32;

	protected GalleryActivity mActivity;
	protected Bundle mData;
	protected int mFlags;

	protected ResultEntry mReceivedResults;
	protected ResultEntry mResult;

	private boolean mDestroyed = false;

	private boolean mPlugged = false;
	boolean mIsFinishing = false;
	private static final String KEY_TRANSITION_IN = "transition-in";

	private StateTransitionAnimation.Transition mNextTransition = StateTransitionAnimation.Transition.None;

	private StateTransitionAnimation mIntroAnimation;
	private GLView mContentPane;
	protected float[] mBackgroundColor;

	BroadcastReceiver mPowerIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				final boolean plugged = 0 != intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

				if (plugged != mPlugged) {
					mPlugged = plugged;
					setScreenFlags();
				}
			}
		}
	};

	protected ActivityState() {
	}

	public Bundle getData() {
		return mData;
	}

	public boolean isFinishing() {
		return mIsFinishing;
	}

	protected void clearStateResult() {
	}

	protected float[] getBackgroundColor() {
		return mBackgroundColor;
	}

	//XXX
	protected int getBackgroundColorId() {
		//return R.color.default_background;
		return android.R.color.transparent;
	}

	protected void onBackPressed() {
		mActivity.getStateManager().finishState(this);
	}

	protected void onConfigurationChanged(final Configuration config) {
	}

	protected void onCreate(final Bundle data, final Bundle storedState) {
		mBackgroundColor = GalleryUtils.intColorToFloatARGBArray(mActivity.getResources().getColor(
				getBackgroundColorId()));
	}

	protected boolean onCreateActionBar(final Menu menu) {
		// TODO: we should return false if there is no menu to show
		// this is a workaround for a bug in system
		return true;
	}

	protected void onDestroy() {
		mDestroyed = true;
	}

	protected boolean onItemSelected(final MenuItem item) {
		return false;
	}

	protected void onPause() {
		if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
			((Activity) mActivity).unregisterReceiver(mPowerIntentReceiver);
		}
		if (mNextTransition != StateTransitionAnimation.Transition.None) {
			mActivity.getTransitionStore().put(KEY_TRANSITION_IN, mNextTransition);
			PreparePageFadeoutTexture.prepareFadeOutTexture(mActivity, mContentPane);
			mNextTransition = StateTransitionAnimation.Transition.None;
		}
	}

	// a subclass of ActivityState should override the method to resume itself
	protected void onResume() {
		final RawTexture fade = mActivity.getTransitionStore().get(PreparePageFadeoutTexture.KEY_FADE_TEXTURE);
		mNextTransition = mActivity.getTransitionStore().get(KEY_TRANSITION_IN,
				StateTransitionAnimation.Transition.None);
		if (mNextTransition != StateTransitionAnimation.Transition.None) {
			mIntroAnimation = new StateTransitionAnimation(mNextTransition, fade);
			mNextTransition = StateTransitionAnimation.Transition.None;
		}
	}

	protected void onSaveState(final Bundle outState) {
	}

	protected void onStateResult(final int requestCode, final int resultCode, final Intent data) {
	}

	protected void setContentPane(final GLView content) {
		mContentPane = content;
		if (mIntroAnimation != null) {
			mContentPane.setIntroAnimation(mIntroAnimation);
			mIntroAnimation = null;
		}
		mContentPane.setBackgroundColor(getBackgroundColor());
		mActivity.getGLRoot().setContentPane(mContentPane);
	}

	protected void setStateResult(final int resultCode, final Intent data) {
		if (mResult == null) return;
		mResult.resultCode = resultCode;
		mResult.resultData = data;
	}

	protected void transitionOnNextPause(final Class<? extends ActivityState> outgoing,
			final Class<? extends ActivityState> incoming, final StateTransitionAnimation.Transition hint) {
		if (outgoing == PhotoPage.class) {
			mNextTransition = StateTransitionAnimation.Transition.Outgoing;
		} else if (incoming == PhotoPage.class) {
			mNextTransition = StateTransitionAnimation.Transition.PhotoIncoming;
		} else {
			mNextTransition = hint;
		}
	}

	private void setScreenFlags() {
		final Window win = mActivity.getWindow();
		final WindowManager.LayoutParams params = win.getAttributes();
		if (0 != (mFlags & FLAG_SCREEN_ON_ALWAYS) || mPlugged && 0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
			params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		} else {
			params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		}
		if (0 != (mFlags & FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)) {
			params.flags |= WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
		} else {
			params.flags &= ~WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
		}
		if (0 != (mFlags & FLAG_SHOW_WHEN_LOCKED)) {
			params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		} else {
			params.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		}
		win.setAttributes(params);
	}

	void initialize(final GalleryActivity activity, final Bundle data) {
		mActivity = activity;
		mData = data;
	}

	boolean isDestroyed() {
		return mDestroyed;
	}

	// should only be called by StateManager
	void resume() {
		final GalleryActivity activity = mActivity;

		activity.invalidateOptionsMenu();

		setScreenFlags();

		final ResultEntry entry = mReceivedResults;
		if (entry != null) {
			mReceivedResults = null;
			onStateResult(entry.requestCode, entry.resultCode, entry.resultData);
		}

		if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
			// we need to know whether the device is plugged in to do this
			// correctly
			final IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			activity.registerReceiver(mPowerIntentReceiver, filter);
		}

		onResume();

		// the transition store should be cleared after resume;
		mActivity.getTransitionStore().clear();
	}

	protected static class ResultEntry {
		public int requestCode;
		public int resultCode = Activity.RESULT_CANCELED;
		public Intent resultData;
	}
}
