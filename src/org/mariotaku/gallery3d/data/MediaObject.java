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

import android.net.Uri;
import android.util.Log;

public abstract class MediaObject {
	private static final String TAG = "MediaObject";
	public static final long INVALID_DATA_VERSION = -1;

	// These are the bits returned from getSupportedOperations():
	public static final int SUPPORT_DELETE = 1 << 0;
	public static final int SUPPORT_ROTATE = 1 << 1;
	public static final int SUPPORT_SHARE = 1 << 2;
	public static final int SUPPORT_CROP = 1 << 3;
	public static final int SUPPORT_SHOW_ON_MAP = 1 << 4;
	public static final int SUPPORT_SETAS = 1 << 5;
	public static final int SUPPORT_FULL_IMAGE = 1 << 6;
	public static final int SUPPORT_PLAY = 1 << 7;
	public static final int SUPPORT_CACHE = 1 << 8;
	public static final int SUPPORT_EDIT = 1 << 9;
	public static final int SUPPORT_INFO = 1 << 10;
	public static final int SUPPORT_IMPORT = 1 << 11;
	public static final int SUPPORT_TRIM = 1 << 12;
	public static final int SUPPORT_UNLOCK = 1 << 13;
	public static final int SUPPORT_BACK = 1 << 14;
	public static final int SUPPORT_ACTION = 1 << 15;
	public static final int SUPPORT_CAMERA_SHORTCUT = 1 << 16;
	public static final int SUPPORT_ALL = 0xffffffff;

	// These are the bits returned from getMediaType():
	public static final int MEDIA_TYPE_UNKNOWN = 1;
	public static final int MEDIA_TYPE_IMAGE = 2;
	public static final int MEDIA_TYPE_VIDEO = 4;
	public static final int MEDIA_TYPE_ALL = MEDIA_TYPE_IMAGE | MEDIA_TYPE_VIDEO;

	public static final String MEDIA_TYPE_IMAGE_STRING = "image";
	public static final String MEDIA_TYPE_VIDEO_STRING = "video";
	public static final String MEDIA_TYPE_ALL_STRING = "all";

	// These are flags for cache() and return values for getCacheFlag():
	public static final int CACHE_FLAG_NO = 0;
	public static final int CACHE_FLAG_SCREENNAIL = 1;
	public static final int CACHE_FLAG_FULL = 2;

	// These are return values for getCacheStatus():
	public static final int CACHE_STATUS_NOT_CACHED = 0;
	public static final int CACHE_STATUS_CACHING = 1;
	public static final int CACHE_STATUS_CACHED_SCREENNAIL = 2;
	public static final int CACHE_STATUS_CACHED_FULL = 3;

	private static long sVersionSerial = 0;

	protected long mDataVersion;

	protected final Path mPath;

	public MediaObject(final Path path, final long version) {
		path.setObject(this);
		mPath = path;
		mDataVersion = version;
	}

	public void cache(final int flag) {
		throw new UnsupportedOperationException();
	}

	public void delete() {
		throw new UnsupportedOperationException();
	}

	public int getCacheFlag() {
		return CACHE_FLAG_NO;
	}

	public long getCacheSize() {
		throw new UnsupportedOperationException();
	}

	public int getCacheStatus() {
		throw new UnsupportedOperationException();
	}

	public Uri getContentUri() {
		final String className = getClass().getName();
		Log.e(TAG, "Class " + className + "should implement getContentUri.");
		Log.e(TAG, "The object was created from path: " + getPath());
		throw new UnsupportedOperationException();
	}

	public long getDataVersion() {
		return mDataVersion;
	}

	public int getMediaType() {
		return MEDIA_TYPE_UNKNOWN;
	}

	public Path getPath() {
		return mPath;
	}

	public Uri getPlayUri() {
		throw new UnsupportedOperationException();
	}

	public int getSupportedOperations() {
		return 0;
	}

	public boolean Import() {
		throw new UnsupportedOperationException();
	}

	public void rotate(final int degrees) {
		throw new UnsupportedOperationException();
	}

	public static int getTypeFromString(final String s) {
		if (MEDIA_TYPE_ALL_STRING.equals(s)) return MediaObject.MEDIA_TYPE_ALL;
		if (MEDIA_TYPE_IMAGE_STRING.equals(s)) return MediaObject.MEDIA_TYPE_IMAGE;
		if (MEDIA_TYPE_VIDEO_STRING.equals(s)) return MediaObject.MEDIA_TYPE_VIDEO;
		throw new IllegalArgumentException(s);
	}

	public static String getTypeString(final int type) {
		switch (type) {
			case MEDIA_TYPE_IMAGE:
				return MEDIA_TYPE_IMAGE_STRING;
			case MEDIA_TYPE_VIDEO:
				return MEDIA_TYPE_VIDEO_STRING;
			case MEDIA_TYPE_ALL:
				return MEDIA_TYPE_ALL_STRING;
		}
		throw new IllegalArgumentException();
	}

	public static synchronized long nextVersionNumber() {
		return ++MediaObject.sVersionSerial;
	}
}
