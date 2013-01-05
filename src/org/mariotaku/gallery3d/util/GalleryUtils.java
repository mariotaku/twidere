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

package org.mariotaku.gallery3d.util;

import org.mariotaku.gallery3d.data.MediaItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class GalleryUtils {

	private static float sPixelDensity = -1f;

	public static int dpToPixel(final int dp) {
		return Math.round(dpToPixel((float) dp));
	}

	public static void initialize(final Context context) {
		final DisplayMetrics metrics = new DisplayMetrics();
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		sPixelDensity = metrics.density;
		final Resources r = context.getResources();
		initializeThumbnailSizes(metrics, r);
	}

	public static float[] intColorToFloatARGBArray(final int from) {
		return new float[] { Color.alpha(from) / 255f, Color.red(from) / 255f, Color.green(from) / 255f,
				Color.blue(from) / 255f };
	}

	public static boolean isHighResolution(final Context context) {
		final DisplayMetrics metrics = new DisplayMetrics();
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels > 2048 || metrics.widthPixels > 2048;
	}

	private static float dpToPixel(final float dp) {
		return sPixelDensity * dp;
	}

	private static void initializeThumbnailSizes(final DisplayMetrics metrics, final Resources r) {
		final int maxPixels = Math.max(metrics.heightPixels, metrics.widthPixels);

		// For screen-nails, we never need to completely fill the screen
		MediaItem.setThumbnailSizes(maxPixels / 2, maxPixels / 5);
	}

}
