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
	public static final int SUPPORT_FULL_IMAGE = 1 << 6;
	public static final int SUPPORT_ACTION = 1 << 15;

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

	private static long sVersionSerial = 0;

	protected long mDataVersion;

	protected final Path mPath;

	public MediaObject(final Path path, final long version) {
		path.setObject(this);
		mPath = path;
		mDataVersion = version;
	}

	public int getCacheFlag() {
		return CACHE_FLAG_NO;
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

	public int getSupportedOperations() {
		return 0;
	}

	public static synchronized long nextVersionNumber() {
		return ++MediaObject.sVersionSerial;
	}
}
