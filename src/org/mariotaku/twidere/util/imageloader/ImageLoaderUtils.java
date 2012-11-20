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

package org.mariotaku.twidere.util.imageloader;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * Class containing some static utility methods.
 */
public class ImageLoaderUtils {
	public static final int IO_BUFFER_SIZE = 8 * 1024;

	private ImageLoaderUtils() {};

	/**
	 * Workaround for bug pre-Froyo, see here for more info:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 */
	public static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (hasHttpConnectionBug()) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	/**
	 * Get the size in bytes of a bitmap.
	 * @param bitmap
	 * @return size in bytes
	 */
	public static int getBitmapSize(final Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return GetBitmapSizeAccessor.getBitmapSize(bitmap);
		}
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Check how much usable space is available at a given path.
	 *
	 * @param path The path to check
	 * @return The space available in bytes
	 */
	public static long getUsableSpace(final File path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			return GetUsableSpaceAccessor.getUsableSpace(path);
		}
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * Get the memory class of this device (approx. per-app memory limit)
	 *
	 * @param context
	 * @return
	 */
	public static int getMemoryClass(Context context) {
		return ((ActivityManager) context.getSystemService(
				Context.ACTIVITY_SERVICE)).getMemoryClass();
	}

	/**
	 * Check if OS version has a http URLConnection bug. See here for more information:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 *
	 * @return
	 */
	public static boolean hasHttpConnectionBug() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
	}

	static class GetBitmapSizeAccessor {
		
		static int getBitmapSize(final Bitmap bitmap) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
				return bitmap.getByteCount();
			}
			// Pre HC-MR1
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}
	
	static class GetUsableSpaceAccessor {
		
		public static long getUsableSpace(File path) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				return path.getUsableSpace();
			}
			final StatFs stats = new StatFs(path.getPath());
			return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
		}
	}
}
