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

import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.ui.ScreenNail;
import org.mariotaku.gallery3d.util.ThreadPool.Job;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;

// MediaItem represents an image or a video item.
public abstract class MediaItem extends MediaObject {
	// NOTE: These type numbers are stored in the image cache, so it should not
	// not be changed without resetting the cache.
	public static final int TYPE_THUMBNAIL = 1;
	public static final int TYPE_MICROTHUMBNAIL = 2;

	public static final int CACHED_IMAGE_QUALITY = 95;

	public static final int IMAGE_READY = 0;
	public static final int IMAGE_WAIT = 1;
	public static final int IMAGE_ERROR = -1;

	public static final String MIME_TYPE_JPEG = "image/jpeg";

	private static final int BYTESBUFFE_POOL_SIZE = 4;
	private static final int BYTESBUFFER_SIZE = 200 * 1024;

	private static int sMicrothumbnailTargetSize = 200;
	private static BitmapPool sMicroThumbPool;
	private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(BYTESBUFFE_POOL_SIZE,
			BYTESBUFFER_SIZE);

	private static int sThumbnailTargetSize = 640;
	private static final BitmapPool sThumbPool = ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY ? new BitmapPool(4)
			: null;

	// TODO: fix default value for latlng and change this.
	public static final double INVALID_LATLNG = 0f;

	public MediaItem(final Path path, final long version) {
		super(path, version);
	}

	public String getFilePath() {
		return "";
	}

	// The rotation of the full-resolution image. By default, it returns the
	// value of
	// getRotation().
	public int getFullImageRotation() {
		return getRotation();
	}

	public abstract int getHeight();

	public abstract String getMimeType();

	public int getRotation() {
		return 0;
	}

	// This is an alternative for requestImage() in PhotoPage. If this
	// is implemented, you don't need to implement requestImage().
	public ScreenNail getScreenNail() {
		return null;
	}

	public long getSize() {
		return 0;
	}

	// Returns width and height of the media item.
	// Returns 0, 0 if the information is not available.
	public abstract int getWidth();

	public abstract Job<Bitmap> requestImage(int type);

	public abstract Job<BitmapRegionDecoder> requestLargeImage();

	public static BytesBufferPool getBytesBufferPool() {
		return sMicroThumbBufferPool;
	}

	public static BitmapPool getMicroThumbPool() {
		if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY && sMicroThumbPool == null) {
			initializeMicroThumbPool();
		}
		return sMicroThumbPool;
	}

	public static int getTargetSize(final int type) {
		switch (type) {
			case TYPE_THUMBNAIL:
				return sThumbnailTargetSize;
			case TYPE_MICROTHUMBNAIL:
				return sMicrothumbnailTargetSize;
			default:
				throw new RuntimeException("should only request thumb/microthumb from cache");
		}
	}

	public static BitmapPool getThumbPool() {
		return sThumbPool;
	}

	public static void setThumbnailSizes(final int size, final int microSize) {
		sThumbnailTargetSize = size;
		if (sMicrothumbnailTargetSize != microSize) {
			sMicrothumbnailTargetSize = microSize;
			initializeMicroThumbPool();
		}
	}

	private static void initializeMicroThumbPool() {
		sMicroThumbPool = ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY ? new BitmapPool(sMicrothumbnailTargetSize,
				sMicrothumbnailTargetSize, 16) : null;
	}
}
