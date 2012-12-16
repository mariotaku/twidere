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

package org.mariotaku.gallery3d.ui;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class CaptureAnimation {
	// The amount of change for zooming out.
	private static final float ZOOM_DELTA = 0.2f;
	// Pre-calculated value for convenience.
	private static final float ZOOM_IN_BEGIN = 1f - ZOOM_DELTA;

	private static final Interpolator sZoomOutInterpolator = new DecelerateInterpolator();
	private static final Interpolator sZoomInInterpolator = new AccelerateInterpolator();
	private static final Interpolator sSlideInterpolator = new AccelerateDecelerateInterpolator();

	// Calculate the scale factor based on the given time fraction.
	public static float calculateScale(final float fraction) {
		float value;
		if (fraction <= 0.5f) {
			// Zoom in for the beginning.
			value = 1f - ZOOM_DELTA * sZoomOutInterpolator.getInterpolation(fraction * 2);
		} else {
			// Zoom out for the ending.
			value = ZOOM_IN_BEGIN + ZOOM_DELTA * sZoomInInterpolator.getInterpolation((fraction - 0.5f) * 2f);
		}
		return value;
	}

	// Calculate the slide factor based on the give time fraction.
	public static float calculateSlide(final float fraction) {
		return sSlideInterpolator.getInterpolation(fraction);
	}
}
