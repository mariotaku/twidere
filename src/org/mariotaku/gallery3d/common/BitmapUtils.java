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

package org.mariotaku.gallery3d.common;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.FloatMath;
import android.util.Log;

public class BitmapUtils {
	private static final String TAG = "BitmapUtils";
	private static final int DEFAULT_JPEG_QUALITY = 90;
	public static final int UNCONSTRAINED = -1;

	private BitmapUtils() {
	}

	public static byte[] compressToBytes(final Bitmap bitmap) {
		return compressToBytes(bitmap, DEFAULT_JPEG_QUALITY);
	}

	public static byte[] compressToBytes(final Bitmap bitmap, final int quality) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
		bitmap.compress(CompressFormat.JPEG, quality, baos);
		return baos.toByteArray();
	}

	// Find the max x that 1 / x <= scale.
	public static int computeSampleSize(final float scale) {
		Utils.assertTrue(scale > 0);
		final int initialSize = Math.max(1, (int) FloatMath.ceil(1 / scale));
		return initialSize <= 8 ? Utils.nextPowerOf2(initialSize) : (initialSize + 7) / 8 * 8;
	}

	/*
	 * Compute the sample size as a function of minSideLength and
	 * maxNumOfPixels. minSideLength is used to specify that minimal width or
	 * height of a bitmap. maxNumOfPixels is used to specify the maximal size in
	 * pixels that is tolerable in terms of memory usage.
	 * 
	 * The function returns a sample size based on the constraints. Both size
	 * and minSideLength can be passed in as UNCONSTRAINED, which indicates no
	 * care of the corresponding constraint. The functions prefers returning a
	 * sample size that generates a smaller bitmap, unless minSideLength =
	 * UNCONSTRAINED.
	 * 
	 * Also, the function rounds up the sample size to a power of 2 or multiple
	 * of 8 because BitmapFactory only honors sample size this way. For example,
	 * BitmapFactory downsamples an image by 2 even though the request is 3. So
	 * we round up the sample size to avoid OOM.
	 */
	public static int computeSampleSize(final int width, final int height, final int minSideLength,
			final int maxNumOfPixels) {
		final int initialSize = computeInitialSampleSize(width, height, minSideLength, maxNumOfPixels);

		return initialSize <= 8 ? Utils.nextPowerOf2(initialSize) : (initialSize + 7) / 8 * 8;
	}

	// Find the min x that 1 / x >= scale
	public static int computeSampleSizeLarger(final float scale) {
		final int initialSize = (int) FloatMath.floor(1f / scale);
		if (initialSize <= 1) return 1;

		return initialSize <= 8 ? Utils.prevPowerOf2(initialSize) : initialSize / 8 * 8;
	}

	// This computes a sample size which makes the longer side at least
	// minSideLength long. If that's not possible, return 1.
	public static int computeSampleSizeLarger(final int w, final int h, final int minSideLength) {
		final int initialSize = Math.max(w / minSideLength, h / minSideLength);
		if (initialSize <= 1) return 1;

		return initialSize <= 8 ? Utils.prevPowerOf2(initialSize) : initialSize / 8 * 8;
	}

	public static Bitmap createVideoThumbnail(final String filePath) {
		// MediaMetadataRetriever is available on API Level 8
		// but is hidden until API Level 10
		Class<?> clazz = null;
		Object instance = null;
		try {
			clazz = Class.forName("android.media.MediaMetadataRetriever");
			instance = clazz.newInstance();

			final Method method = clazz.getMethod("setDataSource", String.class);
			method.invoke(instance, filePath);

			// The method name changes between API Level 9 and 10.
			if (Build.VERSION.SDK_INT <= 9)
				return (Bitmap) clazz.getMethod("captureFrame").invoke(instance);
			else {
				final byte[] data = (byte[]) clazz.getMethod("getEmbeddedPicture").invoke(instance);
				if (data != null) {
					final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					if (bitmap != null) return bitmap;
				}
				return (Bitmap) clazz.getMethod("getFrameAtTime").invoke(instance);
			}
		} catch (final IllegalArgumentException ex) {
			// Assume this is a corrupt video file
		} catch (final RuntimeException ex) {
			// Assume this is a corrupt video file.
		} catch (final InstantiationException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (final InvocationTargetException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (final ClassNotFoundException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (final NoSuchMethodException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} catch (final IllegalAccessException e) {
			Log.e(TAG, "createVideoThumbnail", e);
		} finally {
			try {
				if (instance != null) {
					clazz.getMethod("release").invoke(instance);
				}
			} catch (final Exception ignored) {
			}
		}
		return null;
	}

	public static boolean isRotationSupported(String mimeType) {
		if (mimeType == null) return false;
		mimeType = mimeType.toLowerCase();
		return mimeType.equals("image/jpeg");
	}

	public static boolean isSupportedByRegionDecoder(String mimeType) {
		if (mimeType == null) return false;
		mimeType = mimeType.toLowerCase();
		return mimeType.startsWith("image/") && !mimeType.equals("image/gif") && !mimeType.endsWith("bmp");
	}

	public static void recycleSilently(final Bitmap bitmap) {
		if (bitmap == null) return;
		try {
			bitmap.recycle();
		} catch (final Throwable t) {
			Log.w(TAG, "unable recycle bitmap", t);
		}
	}

	public static Bitmap resizeAndCropCenter(final Bitmap bitmap, final int size, final boolean recycle) {
		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		if (w == size && h == size) return bitmap;

		// scale the image so that the shorter side equals to the target;
		// the longer side will be center-cropped.
		final float scale = (float) size / Math.min(w, h);

		final Bitmap target = Bitmap.createBitmap(size, size, getConfig(bitmap));
		final int width = Math.round(scale * bitmap.getWidth());
		final int height = Math.round(scale * bitmap.getHeight());
		final Canvas canvas = new Canvas(target);
		canvas.translate((size - width) / 2f, (size - height) / 2f);
		canvas.scale(scale, scale);
		final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if (recycle) {
			bitmap.recycle();
		}
		return target;
	}

	public static Bitmap resizeBitmapByScale(final Bitmap bitmap, final float scale, final boolean recycle) {
		final int width = Math.round(bitmap.getWidth() * scale);
		final int height = Math.round(bitmap.getHeight() * scale);
		if (width == bitmap.getWidth() && height == bitmap.getHeight()) return bitmap;
		final Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
		final Canvas canvas = new Canvas(target);
		canvas.scale(scale, scale);
		final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if (recycle) {
			bitmap.recycle();
		}
		return target;
	}

	public static Bitmap resizeDownBySideLength(final Bitmap bitmap, final int maxLength, final boolean recycle) {
		final int srcWidth = bitmap.getWidth();
		final int srcHeight = bitmap.getHeight();
		final float scale = Math.min((float) maxLength / srcWidth, (float) maxLength / srcHeight);
		if (scale >= 1.0f) return bitmap;
		return resizeBitmapByScale(bitmap, scale, recycle);
	}

	public static Bitmap rotateBitmap(final Bitmap source, final int rotation, final boolean recycle) {
		if (rotation == 0) return source;
		final int w = source.getWidth();
		final int h = source.getHeight();
		final Matrix m = new Matrix();
		m.postRotate(rotation);
		final Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, w, h, m, true);
		if (recycle) {
			source.recycle();
		}
		return bitmap;
	}

	private static int computeInitialSampleSize(final int w, final int h, final int minSideLength,
			final int maxNumOfPixels) {
		if (maxNumOfPixels == UNCONSTRAINED && minSideLength == UNCONSTRAINED) return 1;

		final int lowerBound = maxNumOfPixels == UNCONSTRAINED ? 1 : (int) FloatMath.ceil(FloatMath
				.sqrt((float) (w * h) / maxNumOfPixels));

		if (minSideLength == UNCONSTRAINED)
			return lowerBound;
		else {
			final int sampleSize = Math.min(w / minSideLength, h / minSideLength);
			return Math.max(sampleSize, lowerBound);
		}
	}

	private static Bitmap.Config getConfig(final Bitmap bitmap) {
		Bitmap.Config config = bitmap.getConfig();
		if (config == null) {
			config = Bitmap.Config.ARGB_8888;
		}
		return config;
	}
}
