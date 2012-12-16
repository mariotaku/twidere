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

package org.mariotaku.gallery3d.app;

import java.util.HashMap;

public class TransitionStore {
	private final HashMap<Object, Object> mStorage = new HashMap<Object, Object>();

	public void clear() {
		mStorage.clear();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(final Object key) {
		return (T) mStorage.get(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(final Object key, final T valueIfNull) {
		final T value = (T) mStorage.get(key);
		return value == null ? valueIfNull : value;
	}

	public void put(final Object key, final Object value) {
		mStorage.put(key, value);
	}

}
