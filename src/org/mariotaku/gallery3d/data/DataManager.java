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

import org.mariotaku.gallery3d.app.IGalleryApplication;

import android.net.Uri;
import android.util.Log;

// DataManager manages all media sets and media items in the system.
//
// Each MediaSet and MediaItem has a unique 64 bits id. The most significant
// 32 bits represents its parent, and the least significant 32 bits represents
// the self id. For MediaSet the self id is is globally unique, but for
// MediaItem it's unique only relative to its parent.
//
// To make sure the id is the same when the MediaSet is re-created, a child key
// is provided to obtainSetId() to make sure the same self id will be used as
// when the parent and key are the same. A sequence of child keys is called a
// path. And it's used to identify a specific media set even if the process is
// killed and re-created, so child keys should be stable identifiers.

public class DataManager {

	// Any one who would like to access data should require this lock
	// to prevent concurrency issue.
	public static final Object LOCK = new Object();

	private static final String TAG = "DataManager";

	private final MediaSource source;

	public DataManager(final IGalleryApplication application) {
		source = new MediaSource(application);
	}

	public Path findPathByUri(final Uri uri, final String type) {
		if (uri == null) return null;
		final Path path = source.findPathByUri(uri, type);
		return path;
	}

	public MediaItem getMediaItem(final Path path) {
		synchronized (LOCK) {
			final MediaItem obj = path.getItem();
			if (obj != null) return obj;

			try {
				final MediaItem object = source.createMediaItem(path);
				if (object == null) {
					Log.w(TAG, "cannot create media object: " + path);
				}
				return object;
			} catch (final Throwable t) {
				Log.w(TAG, "exception in creating media object: " + path, t);
				return null;
			}
		}
	}
}
