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

package org.mariotaku.gallery3d.data;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.mariotaku.gallery3d.common.BlobCache;
import org.mariotaku.gallery3d.common.BlobCache.LookupRequest;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.BytesBufferPool.BytesBuffer;
import org.mariotaku.gallery3d.util.CacheManager;
import org.mariotaku.gallery3d.util.GalleryUtils;

import android.content.Context;

public class ImageCacheService {
	@SuppressWarnings("unused")
	private static final String TAG = "ImageCacheService";

	private static final String IMAGE_CACHE_FILE = "imgcache";
	private static final int IMAGE_CACHE_MAX_ENTRIES = 5000;
	private static final int IMAGE_CACHE_MAX_BYTES = 200 * 1024 * 1024;
	private static final int IMAGE_CACHE_VERSION = 7;

	private final BlobCache mCache;

	public ImageCacheService(final Context context) {
		mCache = CacheManager.getCache(context, IMAGE_CACHE_FILE, IMAGE_CACHE_MAX_ENTRIES, IMAGE_CACHE_MAX_BYTES,
				IMAGE_CACHE_VERSION);
	}

	public void clearImageData(final Path path, final int type) {
		final byte[] key = makeKey(path, type);
		final long cacheKey = Utils.crc64Long(key);
		synchronized (mCache) {
			try {
				mCache.clearEntry(cacheKey);
			} catch (final IOException ex) {
				// ignore.
			}
		}
	}

	/**
	 * Gets the cached image data for the given <code>path</code> and
	 * <code>type</code>.
	 * 
	 * The image data will be stored in <code>buffer.data</code>, started from
	 * <code>buffer.offset</code> for <code>buffer.length</code> bytes. If the
	 * buffer.data is not big enough, a new byte array will be allocated and
	 * returned.
	 * 
	 * @return true if the image data is found; false if not found.
	 */
	public boolean getImageData(final Path path, final int type, final BytesBuffer buffer) {
		final byte[] key = makeKey(path, type);
		final long cacheKey = Utils.crc64Long(key);
		try {
			final LookupRequest request = new LookupRequest();
			request.key = cacheKey;
			request.buffer = buffer.data;
			synchronized (mCache) {
				if (!mCache.lookup(request)) return false;
			}
			if (isSameKey(key, request.buffer)) {
				buffer.data = request.buffer;
				buffer.offset = key.length;
				buffer.length = request.length - buffer.offset;
				return true;
			}
		} catch (final IOException ex) {
			// ignore.
		}
		return false;
	}

	public void putImageData(final Path path, final int type, final byte[] value) {
		final byte[] key = makeKey(path, type);
		final long cacheKey = Utils.crc64Long(key);
		final ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
		buffer.put(key);
		buffer.put(value);
		synchronized (mCache) {
			try {
				mCache.insert(cacheKey, buffer.array());
			} catch (final IOException ex) {
				// ignore.
			}
		}
	}

	private static boolean isSameKey(final byte[] key, final byte[] buffer) {
		final int n = key.length;
		if (buffer.length < n) return false;
		for (int i = 0; i < n; ++i) {
			if (key[i] != buffer[i]) return false;
		}
		return true;
	}

	private static byte[] makeKey(final Path path, final int type) {
		return GalleryUtils.getBytes(path.toString() + "+" + type);
	}
}
