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

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.support.v4.util.LruCache;

/**
 * This class holds our bitmap caches (memory and disk).
 */
public class ImageCache {

	// Default memory cache size
	public static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 1024 * 5; // 5MB

	// Default disk cache size
	public static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB

	// Compression settings when writing images to disk cache
	private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.JPEG;
	private static final int DEFAULT_COMPRESS_QUALITY = 70;

	// Constants to easily toggle various caches
	private static final boolean DEFAULT_MEM_CACHE_ENABLED = true;
	private static final boolean DEFAULT_DISK_CACHE_ENABLED = true;
	private static final boolean DEFAULT_CLEAR_DISK_CACHE_ON_START = false;

	private DiskLruCache mDiskCache;
	private LruCache<String, Bitmap> mMemoryCache;
	private ImageCacheParams mCacheParams;

	/**
	 * Creating a new ImageCache object using the specified parameters.
	 * 
	 * @param context The context to use
	 * @param cacheParams The cache parameters to use to initialize the cache
	 */
	public ImageCache(final Context context, final ImageCacheParams cacheParams) {
		init(context, cacheParams);
	}

	/**
	 * Creating a new ImageCache object using the default parameters.
	 * 
	 * @param context The context to use
	 * @param uniqueName A unique name that will be appended to the cache
	 *            directory
	 */
	public ImageCache(final Context context, final String uniqueName) {
		init(context, new ImageCacheParams(uniqueName));
	}

	public void addBitmapToCache(final String data, final Bitmap bitmap) {
		if (data == null || bitmap == null) return;

		// Add to memory cache
		if (mMemoryCache != null && mMemoryCache.get(data) == null) {
			mMemoryCache.put(data, bitmap);
		}

		// Add to disk cache
		if (mDiskCache != null && !mDiskCache.containsKey(data)) {
			mDiskCache.put(data, bitmap);
		}
	}

	public void clearCaches() {
		clearDiskCache();
		clearMemoryCache();
	}

	public void clearDiskCache() {
		if (mDiskCache == null) return;
		mDiskCache.clearCache();
	}

	public void clearMemoryCache() {
		if (mMemoryCache == null) return;
		mMemoryCache.evictAll();
	}
	
	public void disableMemoryCache() {
		clearMemoryCache();
		mMemoryCache = null;
	}
	
	public void enableMemoryCache() {		
		// Set up memory cache
		if (mCacheParams.memoryCacheEnabled) {
			mMemoryCache = new MemoryCache(mCacheParams.memCacheSize);
		}
	}

	/**
	 * Get from disk cache.
	 * 
	 * @param data Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromDiskCache(final String data) {
		if (mDiskCache == null) return null;
		return mDiskCache.get(data);
	}

	/**
	 * Get from memory cache.
	 * 
	 * @param data Unique identifier for which item to get
	 * @return The bitmap if found in cache, null otherwise
	 */
	public Bitmap getBitmapFromMemCache(final String data) {
		if (mMemoryCache == null) return null;
		final Bitmap memBitmap = mMemoryCache.get(data);
		if (memBitmap != null) return memBitmap;
		return null;
	}

	public File getFileFromDiskCache(final String data) {
		if (mDiskCache == null) return null;
		return mDiskCache.getFile(data);
	}

	/**
	 * Initialize the cache, providing all parameters.
	 * 
	 * @param context The context to use
	 * @param cacheParams The cache parameters to initialize the cache
	 */
	private void init(final Context context, final ImageCacheParams cacheParams) {
		mCacheParams = cacheParams;
		final File diskCacheDir = DiskLruCache.getDiskCacheDir(context, cacheParams.uniqueName);

		// Set up disk cache
		if (cacheParams.diskCacheEnabled) {
			mDiskCache = DiskLruCache.openCache(context, diskCacheDir, cacheParams.diskCacheSize);
			if (mDiskCache != null) {
				mDiskCache.setCompressParams(cacheParams.compressFormat, cacheParams.compressQuality);
				if (cacheParams.clearDiskCacheOnStart) {
					mDiskCache.clearCache();
				}
			}
		}

		// Set up memory cache
		if (cacheParams.memoryCacheEnabled) {
			mMemoryCache = new MemoryCache(cacheParams.memCacheSize);
		}
	}

	/**
	 * Find and return an existing ImageCache stored in a {@link RetainFragment}
	 * , if not found a new one is created using the supplied params and saved
	 * to a {@link RetainFragment}.
	 * 
	 * @param activity The calling {@link FragmentActivity}
	 * @param cacheParams The cache parameters to use if creating the ImageCache
	 * @return An existing retained ImageCache object or a new one if one did
	 *         not exist
	 */
	public static ImageCache findOrCreateCache(final Context context, final ImageCacheParams cacheParams) {

		// Search for, or create an instance of the non-UI RetainFragment
		// final RetainFragment mRetainFragment =
		// RetainFragment.findOrCreateRetainFragment(
		// activity.getSupportFragmentManager());

		// See if we already have an ImageCache stored in RetainFragment
		// ImageCache imageCache = (ImageCache) mRetainFragment.getObject();
		ImageCache imageCache = null;

		// No existing ImageCache, create one and store it in RetainFragment
		if (imageCache == null) {
			imageCache = new ImageCache(context, cacheParams);
			// mRetainFragment.setObject(imageCache);
		}

		return imageCache;
	}

	/**
	 * Find and return an existing ImageCache stored in a {@link RetainFragment}
	 * , if not found a new one is created with defaults and saved to a
	 * {@link RetainFragment}.
	 * 
	 * @param activity The calling {@link FragmentActivity}
	 * @param uniqueName A unique name to append to the cache directory
	 * @return An existing retained ImageCache object or a new one if one did
	 *         not exist.
	 */
	public static ImageCache findOrCreateCache(final Context context, final String uniqueName) {
		return findOrCreateCache(context, new ImageCacheParams(uniqueName));
	}

	/**
	 * A holder class that contains cache parameters.
	 */
	public static class ImageCacheParams {
		public String uniqueName;
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;
		public boolean memoryCacheEnabled = DEFAULT_MEM_CACHE_ENABLED;
		public boolean diskCacheEnabled = DEFAULT_DISK_CACHE_ENABLED;
		public boolean clearDiskCacheOnStart = DEFAULT_CLEAR_DISK_CACHE_ON_START;

		public ImageCacheParams(final String uniqueName) {
			this.uniqueName = uniqueName;
		}
	}

	private static final class MemoryCache extends LruCache<String, Bitmap> {

		MemoryCache(final int maxSize) {
			super(maxSize);
		}

		/**
		 * Measure item size in bytes rather than units which is more practical
		 * for a bitmap cache
		 */
		@Override
		protected int sizeOf(final String key, final Bitmap bitmap) {
			return ImageLoaderUtils.getBitmapSize(bitmap);
		}
	}
}
