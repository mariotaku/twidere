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

import java.util.Arrays;

import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.data.DataManager;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.ui.TiledScreenNail;
import org.mariotaku.gallery3d.util.ThreadPool.CancelListener;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;
import org.mariotaku.twidere.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class GalleryUtils {
	private static final String TAG = "GalleryUtils";

	public static final String MIME_TYPE_IMAGE = "image/*";
	public static final String MIME_TYPE_VIDEO = "video/*";
	public static final String MIME_TYPE_PANORAMA360 = "application/vnd.google.panorama360+jpg";
	public static final String MIME_TYPE_ALL = "*/*";

	private static final String DIR_TYPE_IMAGE = "vnd.android.cursor.dir/image";

	private static float sPixelDensity = -1f;

	private static volatile Thread sCurrentThread;

	private static volatile boolean sWarned;

	private static final double RAD_PER_DEG = Math.PI / 180.0;

	private static final double EARTH_RADIUS_METERS = 6367000.0;

	public static double accurateDistanceMeters(final double lat1, final double lng1, final double lat2,
			final double lng2) {
		final double dlat = Math.sin(0.5 * (lat2 - lat1));
		final double dlng = Math.sin(0.5 * (lng2 - lng1));
		final double x = dlat * dlat + dlng * dlng * Math.cos(lat1) * Math.cos(lat2);
		return 2 * Math.atan2(Math.sqrt(x), Math.sqrt(Math.max(0.0, 1.0 - x))) * EARTH_RADIUS_METERS;
	}

	public static void assertNotInRenderThread() {
		if (!sWarned) {
			if (Thread.currentThread() == sCurrentThread) {
				sWarned = true;
				Log.w(TAG, new Throwable("Should not do this in render thread"));
			}
		}
	}

	@TargetApi(ApiHelper.VERSION_CODES.HONEYCOMB)
	public static int determineTypeBits(final Context context, final Intent intent) {
		int typeBits = 0;
		final String type = intent.resolveType(context);

		if (MIME_TYPE_IMAGE.equals(type) || DIR_TYPE_IMAGE.equals(type)) {
			typeBits = DataManager.INCLUDE_IMAGE;
		}
		if (ApiHelper.HAS_INTENT_EXTRA_LOCAL_ONLY) {
			if (intent.getBooleanExtra(Intent.EXTRA_LOCAL_ONLY, false)) {
				typeBits |= DataManager.INCLUDE_LOCAL_ONLY;
			}
		}

		return typeBits;
	}

	public static float dpToPixel(final float dp) {
		return sPixelDensity * dp;
	}

	// Below are used the detect using database in the render thread. It only
	// works most of the time, but that's ok because it's for debugging only.

	public static int dpToPixel(final int dp) {
		return Math.round(dpToPixel((float) dp));
	}

	// For debugging, it will block the caller for timeout millis.
	public static void fakeBusy(final JobContext jc, final int timeout) {
		final ConditionVariable cv = new ConditionVariable();
		jc.setCancelListener(new CancelListener() {
			@Override
			public void onCancel() {
				cv.open();
			}
		});
		cv.block(timeout);
		jc.setCancelListener(null);
	}

	public static double fastDistanceMeters(final double latRad1, final double lngRad1, final double latRad2,
			final double lngRad2) {
		if (Math.abs(latRad1 - latRad2) > RAD_PER_DEG || Math.abs(lngRad1 - lngRad2) > RAD_PER_DEG)
			return accurateDistanceMeters(latRad1, lngRad1, latRad2, lngRad2);
		// Approximate sin(x) = x.
		final double sineLat = latRad1 - latRad2;

		// Approximate sin(x) = x.
		final double sineLng = lngRad1 - lngRad2;

		// Approximate cos(lat1) * cos(lat2) using
		// cos((lat1 + lat2)/2) ^ 2
		double cosTerms = Math.cos((latRad1 + latRad2) / 2.0);
		cosTerms = cosTerms * cosTerms;
		double trigTerm = sineLat * sineLat + cosTerms * sineLng * sineLng;
		trigTerm = Math.sqrt(trigTerm);

		// Approximate arcsin(x) = x
		return EARTH_RADIUS_METERS * trigTerm;
	}

	public static int getBucketId(final String path) {
		return path.toLowerCase().hashCode();
	}

	public static byte[] getBytes(final String in) {
		final byte[] result = new byte[in.length() * 2];
		int output = 0;
		for (final char ch : in.toCharArray()) {
			result[output++] = (byte) (ch & 0xFF);
			result[output++] = (byte) (ch >> 8);
		}
		return result;
	}

	public static boolean hasSpaceForSize(final long size) {
		final String state = Environment.getExternalStorageState();
		if (!Environment.MEDIA_MOUNTED.equals(state)) return false;

		final String path = Environment.getExternalStorageDirectory().getPath();
		try {
			final StatFs stat = new StatFs(path);
			return stat.getAvailableBlocks() * (long) stat.getBlockSize() > size;
		} catch (final Exception e) {
			Log.i(TAG, "Fail to access external storage", e);
		}
		return false;
	}

	public static void initialize(final Context context) {
		final DisplayMetrics metrics = new DisplayMetrics();
		final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		sPixelDensity = metrics.density;
		final Resources r = context.getResources();
		TiledScreenNail.setPlaceholderColor(r.getColor(R.color.bitmap_screennail_placeholder));
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

	public static int meterToPixel(final float meter) {
		// 1 meter = 39.37 inches, 1 inch = 160 dp.
		return Math.round(dpToPixel(meter * 39.37f * 160));
	}

	public static void setRenderThread() {
		sCurrentThread = Thread.currentThread();
	}

	public static void setViewPointMatrix(final float matrix[], final float x, final float y, final float z) {
		// The matrix is
		// -z, 0, x, 0
		// 0, -z, y, 0
		// 0, 0, 1, 0
		// 0, 0, 1, -z
		Arrays.fill(matrix, 0, 16, 0);
		matrix[0] = matrix[5] = matrix[15] = -z;
		matrix[8] = x;
		matrix[9] = y;
		matrix[10] = matrix[11] = 1;
	}

	public static final double toMile(final double meter) {
		return meter / 1609;
	}

	private static void initializeThumbnailSizes(final DisplayMetrics metrics, final Resources r) {
		final int maxPixels = Math.max(metrics.heightPixels, metrics.widthPixels);

		// For screen-nails, we never need to completely fill the screen
		MediaItem.setThumbnailSizes(maxPixels / 2, maxPixels / 5);
		TiledScreenNail.setMaxSide(maxPixels / 2);
	}

}
