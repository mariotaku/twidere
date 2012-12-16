/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.util.ArrayList;

import org.mariotaku.gallery3d.common.Utils;

import android.graphics.Bitmap;

public class BitmapPool {
	@SuppressWarnings("unused")
	private static final String TAG = "BitmapPool";

	private final ArrayList<Bitmap> mPool;
	private final int mPoolLimit;

	// mOneSize is true if the pool can only cache Bitmap with one size.
	private final boolean mOneSize;
	private final int mWidth, mHeight; // only used if mOneSize is true

	// Construct a BitmapPool which caches bitmap with any size;
	public BitmapPool(final int poolLimit) {
		mWidth = -1;
		mHeight = -1;
		mPoolLimit = poolLimit;
		mPool = new ArrayList<Bitmap>(poolLimit);
		mOneSize = false;
	}

	// Construct a BitmapPool which caches bitmap with the specified size.
	public BitmapPool(final int width, final int height, final int poolLimit) {
		mWidth = width;
		mHeight = height;
		mPoolLimit = poolLimit;
		mPool = new ArrayList<Bitmap>(poolLimit);
		mOneSize = true;
	}

	public synchronized void clear() {
		mPool.clear();
	}

	// Get a Bitmap from the pool.
	public synchronized Bitmap getBitmap() {
		Utils.assertTrue(mOneSize);
		final int size = mPool.size();
		return size > 0 ? mPool.remove(size - 1) : null;
	}

	// Get a Bitmap from the pool with the specified size.
	public synchronized Bitmap getBitmap(final int width, final int height) {
		Utils.assertTrue(!mOneSize);
		for (int i = mPool.size() - 1; i >= 0; i--) {
			final Bitmap b = mPool.get(i);
			if (b.getWidth() == width && b.getHeight() == height) return mPool.remove(i);
		}
		return null;
	}

	public boolean isOneSize() {
		return mOneSize;
	}

	// Put a Bitmap into the pool, if the Bitmap has a proper size. Otherwise
	// the Bitmap will be recycled. If the pool is full, an old Bitmap will be
	// recycled.
	public void recycle(final Bitmap bitmap) {
		if (bitmap == null) return;
		if (mOneSize && (bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight)) {
			bitmap.recycle();
			return;
		}
		synchronized (this) {
			if (mPool.size() >= mPoolLimit) {
				mPool.remove(0);
			}
			mPool.add(bitmap);
		}
	}
}
