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

package org.mariotaku.gallery3d.util;

// This is an array whose index ranges from min to max (inclusive).
public class RangeIntArray {
	private final int[] mData;
	private final int mOffset;

	public RangeIntArray(final int min, final int max) {
		mData = new int[max - min + 1];
		mOffset = min;
	}

	// Wraps around an existing array
	public RangeIntArray(final int[] src, final int min, final int max) {
		mData = src;
		mOffset = min;
	}

	public int get(final int i) {
		return mData[i - mOffset];
	}

	public int indexOf(final int object) {
		for (int i = 0; i < mData.length; i++) {
			if (mData[i] == object) return i + mOffset;
		}
		return Integer.MAX_VALUE;
	}

	public void put(final int i, final int object) {
		mData[i - mOffset] = object;
	}
}
