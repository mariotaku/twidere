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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.mariotaku.gallery3d.common.BlobCache;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class CacheManager {
	private static final String TAG = "CacheManager";
	private static final String KEY_CACHE_UP_TO_DATE = "cache-up-to-date";
	private static HashMap<String, BlobCache> sCacheMap = new HashMap<String, BlobCache>();
	private static boolean sOldCheckDone = false;

	// Return null when we cannot instantiate a BlobCache, e.g.:
	// there is no SD card found.
	// This can only be called from data thread.
	public static BlobCache getCache(final Context context, final String filename, final int maxEntries,
			final int maxBytes, final int version) {
		synchronized (sCacheMap) {
			if (!sOldCheckDone) {
				removeOldFilesIfNecessary(context);
				sOldCheckDone = true;
			}
			BlobCache cache = sCacheMap.get(filename);
			if (cache == null) {
				final File cacheDir = context.getExternalCacheDir();
				final String path = cacheDir.getAbsolutePath() + "/" + filename;
				try {
					cache = new BlobCache(path, maxEntries, maxBytes, false, version);
					sCacheMap.put(filename, cache);
				} catch (final IOException e) {
					Log.e(TAG, "Cannot instantiate cache!", e);
				}
			}
			return cache;
		}
	}

	// Removes the old files if the data is wiped.
	private static void removeOldFilesIfNecessary(final Context context) {
		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		int n = 0;
		try {
			n = pref.getInt(KEY_CACHE_UP_TO_DATE, 0);
		} catch (final Throwable t) {
			// ignore.
		}
		if (n != 0) return;
		pref.edit().putInt(KEY_CACHE_UP_TO_DATE, 1).commit();

		final File cacheDir = context.getExternalCacheDir();
		final String prefix = cacheDir.getAbsolutePath() + "/";

		BlobCache.deleteFiles(prefix + "imgcache");
		BlobCache.deleteFiles(prefix + "rev_geocoding");
		BlobCache.deleteFiles(prefix + "bookmark");
	}
}
