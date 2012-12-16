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

package org.mariotaku.gallery3d.ui;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Rect;

// See "frameworks/base/include/utils/ResourceTypes.h" for the format of
// NinePatch chunk.
class NinePatchChunk {

	public static final int NO_COLOR = 0x00000001;
	public static final int TRANSPARENT_COLOR = 0x00000000;

	public Rect mPaddings = new Rect();

	public int mDivX[];
	public int mDivY[];
	public int mColor[];

	public static NinePatchChunk deserialize(final byte[] data) {
		final ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());

		final byte wasSerialized = byteBuffer.get();
		if (wasSerialized == 0) return null;

		final NinePatchChunk chunk = new NinePatchChunk();
		chunk.mDivX = new int[byteBuffer.get()];
		chunk.mDivY = new int[byteBuffer.get()];
		chunk.mColor = new int[byteBuffer.get()];

		checkDivCount(chunk.mDivX.length);
		checkDivCount(chunk.mDivY.length);

		// skip 8 bytes
		byteBuffer.getInt();
		byteBuffer.getInt();

		chunk.mPaddings.left = byteBuffer.getInt();
		chunk.mPaddings.right = byteBuffer.getInt();
		chunk.mPaddings.top = byteBuffer.getInt();
		chunk.mPaddings.bottom = byteBuffer.getInt();

		// skip 4 bytes
		byteBuffer.getInt();

		readIntArray(chunk.mDivX, byteBuffer);
		readIntArray(chunk.mDivY, byteBuffer);
		readIntArray(chunk.mColor, byteBuffer);

		return chunk;
	}

	private static void checkDivCount(final int length) {
		if (length == 0 || (length & 0x01) != 0) throw new RuntimeException("invalid nine-patch: " + length);
	}

	private static void readIntArray(final int[] data, final ByteBuffer buffer) {
		for (int i = 0, n = data.length; i < n; ++i) {
			data[i] = buffer.getInt();
		}
	}
}