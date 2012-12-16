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

public abstract class MediaSource {
	private static final String TAG = "MediaSource";
	private final String mPrefix;

	protected MediaSource(final String prefix) {
		mPrefix = prefix;
	}

	public abstract MediaObject createMediaObject(Path path);

	public Path findPathByUri(final Uri uri, final String type) {
		return null;
	}

	public Path getDefaultSetOf(final Path item) {
		return null;
	}

	public String getPrefix() {
		return mPrefix;
	}

	public long getTotalTargetCacheSize() {
		return 0;
	}

	public long getTotalUsedCacheSize() {
		return 0;
	}

	public void pause() {
	}

	public void resume() {
	}

	public static class PathId {
		public Path path;
		public int id;

		public PathId(final Path path, final int id) {
			this.path = path;
			this.id = id;
		}
	}
}
