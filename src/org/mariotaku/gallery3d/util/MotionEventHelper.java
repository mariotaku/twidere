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
package org.mariotaku.gallery3d.util;

import org.mariotaku.gallery3d.common.ApiHelper;

import android.annotation.TargetApi;
import android.graphics.Matrix;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;

public final class MotionEventHelper {
	private MotionEventHelper() {
	}

	public static MotionEvent transformEvent(final MotionEvent e, final Matrix m) {
		// We try to use the new transform method if possible because it uses
		// less memory.
		if (ApiHelper.HAS_MOTION_EVENT_TRANSFORM)
			return transformEventNew(e, m);
		else
			return transformEventOld(e, m);
	}

	private static PointerCoords[] getPointerCoords(final MotionEvent e) {
		final int n = e.getPointerCount();
		final PointerCoords[] r = new PointerCoords[n];
		for (int i = 0; i < n; i++) {
			r[i] = new PointerCoords();
			e.getPointerCoords(i, r[i]);
		}
		return r;
	}

	private static int[] getPointerIds(final MotionEvent e) {
		final int n = e.getPointerCount();
		final int[] r = new int[n];
		for (int i = 0; i < n; i++) {
			r[i] = e.getPointerId(i);
		}
		return r;
	}

	private static float transformAngle(final Matrix m, final float angleRadians) {
		// Construct and transform a vector oriented at the specified clockwise
		// angle from vertical. Coordinate system: down is increasing Y, right
		// is
		// increasing X.
		final float[] v = new float[2];
		v[0] = FloatMath.sin(angleRadians);
		v[1] = -FloatMath.cos(angleRadians);
		m.mapVectors(v);

		// Derive the transformed vector's clockwise angle from vertical.
		float result = (float) Math.atan2(v[0], -v[1]);
		if (result < -Math.PI / 2) {
			result += Math.PI;
		} else if (result > Math.PI / 2) {
			result -= Math.PI;
		}
		return result;
	}

	@TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
	private static MotionEvent transformEventNew(final MotionEvent e, final Matrix m) {
		final MotionEvent newEvent = MotionEvent.obtain(e);
		newEvent.transform(m);
		return newEvent;
	}

	// This is copied from Input.cpp in the android framework.
	private static MotionEvent transformEventOld(final MotionEvent e, final Matrix m) {
		final long downTime = e.getDownTime();
		final long eventTime = e.getEventTime();
		final int action = e.getAction();
		final int pointerCount = e.getPointerCount();
		final int[] pointerIds = getPointerIds(e);
		final PointerCoords[] pointerCoords = getPointerCoords(e);
		final int metaState = e.getMetaState();
		final float xPrecision = e.getXPrecision();
		final float yPrecision = e.getYPrecision();
		final int deviceId = e.getDeviceId();
		final int edgeFlags = e.getEdgeFlags();
		final int source = e.getSource();
		final int flags = e.getFlags();

		// Copy the x and y coordinates into an array, map them, and copy back.
		final float[] xy = new float[pointerCoords.length * 2];
		for (int i = 0; i < pointerCount; i++) {
			xy[2 * i] = pointerCoords[i].x;
			xy[2 * i + 1] = pointerCoords[i].y;
		}
		m.mapPoints(xy);
		for (int i = 0; i < pointerCount; i++) {
			pointerCoords[i].x = xy[2 * i];
			pointerCoords[i].y = xy[2 * i + 1];
			pointerCoords[i].orientation = transformAngle(m, pointerCoords[i].orientation);
		}

		final MotionEvent n = MotionEvent.obtain(downTime, eventTime, action, pointerCount, pointerIds, pointerCoords,
				metaState, xPrecision, yPrecision, deviceId, edgeFlags, source, flags);

		return n;
	}
}
