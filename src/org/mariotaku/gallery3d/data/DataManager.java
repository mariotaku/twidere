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

import java.util.HashMap;
import java.util.WeakHashMap;

import org.mariotaku.gallery3d.app.IGalleryApplication;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
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
	public static final int INCLUDE_IMAGE = 1;
	public static final int INCLUDE_LOCAL_ONLY = 4;

	// Any one who would like to access data should require this lock
	// to prevent concurrency issue.
	public static final Object LOCK = new Object();

	private static final String TAG = "DataManager";

	private final Handler mDefaultMainHandler;

	private final IGalleryApplication mApplication;

	private final HashMap<Uri, NotifyBroker> mNotifierMap = new HashMap<Uri, NotifyBroker>();

	private final MediaSource source;

	public DataManager(final IGalleryApplication application) {
		mApplication = application;
		source = new MediaSource(application);
		mDefaultMainHandler = new Handler(application.getMainLooper());
	}

	public Path findPathByUri(final Uri uri, final String type) {
		if (uri == null) return null;
		final Path path = source.findPathByUri(uri, type);
		if (path != null) return path;
		return null;
	}

	public MediaObject getMediaObject(final Path path) {
		synchronized (LOCK) {
			final MediaObject obj = path.getObject();
			if (obj != null) return obj;

			try {
				final MediaObject object = source.createMediaObject(path);
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

	public void registerChangeNotifier(final Uri uri, final ChangeNotifier notifier) {
		NotifyBroker broker = null;
		synchronized (mNotifierMap) {
			broker = mNotifierMap.get(uri);
			if (broker == null) {
				broker = new NotifyBroker(mDefaultMainHandler);
				mApplication.getContentResolver().registerContentObserver(uri, true, broker);
				mNotifierMap.put(uri, broker);
			}
		}
		broker.registerNotifier(notifier);
	}

	private static class NotifyBroker extends ContentObserver {
		private final WeakHashMap<ChangeNotifier, Object> mNotifiers = new WeakHashMap<ChangeNotifier, Object>();

		public NotifyBroker(final Handler handler) {
			super(handler);
		}

		@Override
		public synchronized void onChange(final boolean selfChange) {
			for (final ChangeNotifier notifier : mNotifiers.keySet()) {
				notifier.onChange(selfChange);
			}
		}

		public synchronized void registerNotifier(final ChangeNotifier notifier) {
			mNotifiers.put(notifier, null);
		}
	}
}
