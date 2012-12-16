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

package org.mariotaku.gallery3d.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class IdentityCache<K, V> {

	private final HashMap<K, Entry<K, V>> mWeakMap = new HashMap<K, Entry<K, V>>();
	private final ReferenceQueue<V> mQueue = new ReferenceQueue<V>();

	public IdentityCache() {
	}

	public synchronized V get(final K key) {
		cleanUpWeakMap();
		final Entry<K, V> entry = mWeakMap.get(key);
		return entry == null ? null : entry.get();
	}

	// This is for debugging only
	public synchronized ArrayList<K> keys() {
		final Set<K> set = mWeakMap.keySet();
		final ArrayList<K> result = new ArrayList<K>(set);
		return result;
	}

	public synchronized V put(final K key, final V value) {
		cleanUpWeakMap();
		final Entry<K, V> entry = mWeakMap.put(key, new Entry<K, V>(key, value, mQueue));
		return entry == null ? null : entry.get();
	}

	@SuppressWarnings("unchecked")
	private void cleanUpWeakMap() {
		Entry<K, V> entry = (Entry<K, V>) mQueue.poll();
		while (entry != null) {
			mWeakMap.remove(entry.mKey);
			entry = (Entry<K, V>) mQueue.poll();
		}
	}

	// This is currently unused.
	/*
	 * public synchronized void clear() { mWeakMap.clear(); mQueue = new
	 * ReferenceQueue<V>(); }
	 */

	private static class Entry<K, V> extends WeakReference<V> {
		K mKey;

		public Entry(final K key, final V value, final ReferenceQueue<V> queue) {
			super(value, queue);
			mKey = key;
		}
	}
}
