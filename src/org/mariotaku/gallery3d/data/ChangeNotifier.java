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

import java.util.concurrent.atomic.AtomicBoolean;

// This handles change notification for media sets.
public class ChangeNotifier {

	private final AtomicBoolean mContentDirty = new AtomicBoolean(true);

	// Returns the dirty flag and clear it.
	public boolean isDirty() {
		return mContentDirty.compareAndSet(true, false);
	}

	protected void onChange(final boolean selfChange) {
	}
}
