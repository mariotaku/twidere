/*
 * Copyright (C) 2012 The Android Open Source Project
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

import java.util.ArrayList;

import org.mariotaku.gallery3d.ui.OrientationSource;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

public class OrientationManager implements OrientationSource {
	private static final String TAG = "OrientationManager";

	// Orientation hysteresis amount used in rounding, in degrees
	private static final int ORIENTATION_HYSTERESIS = 5;

	private final Activity mActivity;

	private final ArrayList<Listener> mListeners;
	private final MyOrientationEventListener mOrientationListener;
	// The degrees of the device rotated clockwise from its natural orientation.
	private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
	// If the framework orientation is locked.
	private boolean mOrientationLocked = false;
	// The orientation compensation: if the framwork orientation is locked, the
	// device orientation and the framework orientation may be different, so we
	// need to rotate the UI. For example, if this value is 90, the UI
	// components should be rotated 90 degrees counter-clockwise.
	private int mOrientationCompensation = 0;
	// This is true if "Settings -> Display -> Rotation Lock" is checked. We
	// don't allow the orientation to be unlocked if the value is true.
	private boolean mRotationLockedSetting = false;

	public OrientationManager(final Activity activity) {
		mActivity = activity;
		mListeners = new ArrayList<Listener>();
		mOrientationListener = new MyOrientationEventListener(activity);
	}

	public void addListener(final Listener listener) {
		synchronized (mListeners) {
			mListeners.add(listener);
		}
	}

	@Override
	public int getCompensation() {
		return mOrientationCompensation;
	}

	@Override
	public int getDisplayRotation() {
		return getDisplayRotation(mActivity);
	}

	public void pause() {
		mOrientationListener.disable();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Orientation handling
	//
	// We can choose to lock the framework orientation or not. If we lock the
	// framework orientation, we calculate a a compensation value according to
	// current device orientation and send it to listeners. If we don't lock
	// the framework orientation, we always set the compensation value to 0.
	// //////////////////////////////////////////////////////////////////////////

	public void removeListener(final Listener listener) {
		synchronized (mListeners) {
			mListeners.remove(listener);
		}
	}

	public void resume() {
		final ContentResolver resolver = mActivity.getContentResolver();
		mRotationLockedSetting = Settings.System.getInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 0) != 1;
		mOrientationListener.enable();
	}

	// Unlock the framework orientation, so it can change when the device
	// rotates.
	public void unlockOrientation() {
		if (!mOrientationLocked) return;
		if (mRotationLockedSetting) return;
		mOrientationLocked = false;
		Log.d(TAG, "unlock orientation");
		mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		disableCompensation();
	}

	// Make the compensation value 0 and send it to listeners.
	private void disableCompensation() {
		if (mOrientationCompensation != 0) {
			mOrientationCompensation = 0;
			notifyListeners();
		}
	}

	private void notifyListeners() {
		synchronized (mListeners) {
			for (int i = 0, n = mListeners.size(); i < n; i++) {
				mListeners.get(i).onOrientationCompensationChanged();
			}
		}
	}

	// Calculate the compensation value and send it to listeners.
	private void updateCompensation() {
		if (mOrientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;

		final int orientationCompensation = (mOrientation + getDisplayRotation(mActivity)) % 360;

		if (mOrientationCompensation != orientationCompensation) {
			mOrientationCompensation = orientationCompensation;
			notifyListeners();
		}
	}

	private static int getDisplayRotation(final Activity activity) {
		final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
			case Surface.ROTATION_0:
				return 0;
			case Surface.ROTATION_90:
				return 90;
			case Surface.ROTATION_180:
				return 180;
			case Surface.ROTATION_270:
				return 270;
		}
		return 0;
	}

	private static int roundOrientation(final int orientation, final int orientationHistory) {
		boolean changeOrientation = false;
		if (orientationHistory == OrientationEventListener.ORIENTATION_UNKNOWN) {
			changeOrientation = true;
		} else {
			int dist = Math.abs(orientation - orientationHistory);
			dist = Math.min(dist, 360 - dist);
			changeOrientation = dist >= 45 + ORIENTATION_HYSTERESIS;
		}
		if (changeOrientation) return (orientation + 45) / 90 * 90 % 360;
		return orientationHistory;
	}

	public interface Listener {
		public void onOrientationCompensationChanged();
	}

	// This listens to the device orientation, so we can update the
	// compensation.
	private class MyOrientationEventListener extends OrientationEventListener {
		public MyOrientationEventListener(final Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(final int orientation) {
			// We keep the last known orientation. So if the user first orient
			// the camera then point the camera to floor or sky, we still have
			// the correct orientation.
			if (orientation == ORIENTATION_UNKNOWN) return;
			mOrientation = roundOrientation(orientation, mOrientation);
			// If the framework orientation is locked, we update the
			// compensation value and notify the listeners.
			if (mOrientationLocked) {
				updateCompensation();
			}
		}
	}
}
