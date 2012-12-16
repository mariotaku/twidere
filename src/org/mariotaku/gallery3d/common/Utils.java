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

import java.io.Closeable;
import java.io.InterruptedIOException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class Utils {
	private static final String TAG = "Utils";
	private static final String DEBUG_TAG = "GalleryDebug";

	private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
	private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;

	private static long[] sCrcTable = new long[256];

	private static final boolean IS_DEBUG_BUILD = Build.TYPE.equals("eng") || Build.TYPE.equals("userdebug");

	private static final String MASK_STRING = "********************************";

	static {
		// http://bioinf.cs.ucl.ac.uk/downloads/crc64/crc64.c
		long part;
		for (int i = 0; i < 256; i++) {
			part = i;
			for (int j = 0; j < 8; j++) {
				final long x = ((int) part & 1) != 0 ? POLY64REV : 0;
				part = part >> 1 ^ x;
			}
			sCrcTable[i] = part;
		}
	}

	// Throws AssertionError if the input is false.
	public static void assertTrue(final boolean cond) {
		if (!cond) throw new AssertionError();
	}

	public static int ceilLog2(final float value) {
		int i;
		for (i = 0; i < 31; i++) {
			if (1 << i >= value) {
				break;
			}
		}
		return i;
	}

	// Throws NullPointerException if the input is null.
	public static <T> T checkNotNull(final T object) {
		if (object == null) throw new NullPointerException();
		return object;
	}

	// Returns the input value x clamped to the range [min, max].
	public static float clamp(final float x, final float min, final float max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	// Returns the input value x clamped to the range [min, max].
	public static int clamp(final int x, final int min, final int max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	// Returns the input value x clamped to the range [min, max].
	public static long clamp(final long x, final long min, final long max) {
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}

	public static void closeSilently(final Closeable c) {
		if (c == null) return;
		try {
			c.close();
		} catch (final Throwable t) {
			Log.w(TAG, "close fail", t);
		}
	}

	public static void closeSilently(final Cursor cursor) {
		try {
			if (cursor != null) {
				cursor.close();
			}
		} catch (final Throwable t) {
			Log.w(TAG, "fail to close", t);
		}
	}

	public static void closeSilently(final ParcelFileDescriptor fd) {
		try {
			if (fd != null) {
				fd.close();
			}
		} catch (final Throwable t) {
			Log.w(TAG, "fail to close", t);
		}
	}

	public static int compare(final long a, final long b) {
		return a < b ? -1 : a == b ? 0 : 1;
	}

	public static String[] copyOf(final String[] source, int newSize) {
		final String[] result = new String[newSize];
		newSize = Math.min(source.length, newSize);
		System.arraycopy(source, 0, result, 0, newSize);
		return result;
	}

	public static final long crc64Long(final byte[] buffer) {
		long crc = INITIALCRC;
		for (int k = 0, n = buffer.length; k < n; ++k) {
			crc = sCrcTable[((int) crc ^ buffer[k]) & 0xff] ^ crc >> 8;
		}
		return crc;
	}

	/**
	 * A function thats returns a 64-bit crc for string
	 * 
	 * @param in input string
	 * @return a 64-bit crc value
	 */
	public static final long crc64Long(final String in) {
		if (in == null || in.length() == 0) return 0;
		return crc64Long(getBytes(in));
	}

	// This method should be ONLY used for debugging.
	public static void debug(final String message, final Object... args) {
		Log.v(DEBUG_TAG, String.format(message, args));
	}

	public static String ensureNotNull(final String value) {
		return value == null ? "" : value;
	}

	// Returns true if two input Object are both null or equal
	// to each other.
	public static boolean equals(final Object a, final Object b) {
		return a == b || (a == null ? false : a.equals(b));
	}

	/**
	 * @return String with special XML characters escaped.
	 */
	public static String escapeXml(final String s) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, len = s.length(); i < len; ++i) {
			final char c = s.charAt(i);
			switch (c) {
				case '<':
					sb.append("&lt;");
					break;
				case '>':
					sb.append("&gt;");
					break;
				case '\"':
					sb.append("&quot;");
					break;
				case '\'':
					sb.append("&#039;");
					break;
				case '&':
					sb.append("&amp;");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	// Throws AssertionError with the message. We had a method having the form
	// assertTrue(boolean cond, String message, Object ... args);
	// However a call to that method will cause memory allocation even if the
	// condition is false (due to autoboxing generated by "Object ... args"),
	// so we don't use that anymore.
	public static void fail(final String message, final Object... args) {
		throw new AssertionError(args.length == 0 ? message : String.format(message, args));
	}

	public static int floorLog2(final float value) {
		int i;
		for (i = 0; i < 31; i++) {
			if (1 << i > value) {
				break;
			}
		}
		return i - 1;
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

	public static String getUserAgent(final Context context) {
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			throw new IllegalStateException("getPackageInfo failed");
		}
		return String.format("%s/%s; %s/%s/%s/%s; %s/%s/%s", packageInfo.packageName, packageInfo.versionName,
				Build.BRAND, Build.DEVICE, Build.MODEL, Build.ID, Build.VERSION.SDK_INT, Build.VERSION.RELEASE,
				Build.VERSION.INCREMENTAL);
	}

	public static boolean handleInterrruptedException(final Throwable e) {
		// A helper to deal with the interrupt exception
		// If an interrupt detected, we will setup the bit again.
		if (e instanceof InterruptedIOException || e instanceof InterruptedException) {
			Thread.currentThread().interrupt();
			return true;
		}
		return false;
	}

	public static float interpolateAngle(final float source, final float target, final float progress) {
		// interpolate the angle from source to target
		// We make the difference in the range of [-179, 180], this is the
		// shortest path to change source to target.
		float diff = target - source;
		if (diff < 0) {
			diff += 360f;
		}
		if (diff > 180) {
			diff -= 360f;
		}

		final float result = source + diff * progress;
		return result < 0 ? result + 360f : result;
	}

	public static float interpolateScale(final float source, final float target, final float progress) {
		return source + progress * (target - source);
	}

	public static boolean isNullOrEmpty(final String exifMake) {
		return TextUtils.isEmpty(exifMake);
	}

	public static boolean isOpaque(final int color) {
		return color >>> 24 == 0xFF;
	}

	// Mask information for debugging only. It returns
	// <code>info.toString()</code> directly
	// for debugging build (i.e., 'eng' and 'userdebug') and returns a mask
	// ("****")
	// in release build to protect the information (e.g. for privacy issue).
	public static String maskDebugInfo(final Object info) {
		if (info == null) return null;
		final String s = info.toString();
		final int length = Math.min(s.length(), MASK_STRING.length());
		return IS_DEBUG_BUILD ? s : MASK_STRING.substring(0, length);
	}

	// Returns the next power of two.
	// Returns the input if it is already power of 2.
	// Throws IllegalArgumentException if the input is <= 0 or
	// the answer overflows.
	public static int nextPowerOf2(int n) {
		if (n <= 0 || n > 1 << 30) throw new IllegalArgumentException("n is invalid: " + n);
		n -= 1;
		n |= n >> 16;
		n |= n >> 8;
		n |= n >> 4;
		n |= n >> 2;
		n |= n >> 1;
		return n + 1;
	}

	public static float parseFloatSafely(final String content, final float defaultValue) {
		if (content == null) return defaultValue;
		try {
			return Float.parseFloat(content);
		} catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	public static int parseIntSafely(final String content, final int defaultValue) {
		if (content == null) return defaultValue;
		try {
			return Integer.parseInt(content);
		} catch (final NumberFormatException e) {
			return defaultValue;
		}
	}

	// Returns the previous power of two.
	// Returns the input if it is already power of 2.
	// Throws IllegalArgumentException if the input is <= 0
	public static int prevPowerOf2(final int n) {
		if (n <= 0) throw new IllegalArgumentException();
		return Integer.highestOneBit(n);
	}

	public static void swap(final int[] array, final int i, final int j) {
		final int temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

	public static void waitWithoutInterrupt(final Object object) {
		try {
			object.wait();
		} catch (final InterruptedException e) {
			Log.w(TAG, "unexpected interrupt: " + object);
		}
	}
}
