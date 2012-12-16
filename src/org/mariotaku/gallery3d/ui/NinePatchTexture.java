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
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL11;

import org.mariotaku.gallery3d.common.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

// NinePatchTexture is a texture backed by a NinePatch resource.
//
// getPaddings() returns paddings specified in the NinePatch.
// getNinePatchChunk() returns the layout data specified in the NinePatch.
//
public class NinePatchTexture extends ResourceTexture {
	@SuppressWarnings("unused")
	private static final String TAG = "NinePatchTexture";
	private NinePatchChunk mChunk;
	private final SmallCache mInstanceCache = new SmallCache();

	public NinePatchTexture(final Context context, final int resId) {
		super(context, resId);
	}

	@Override
	public void draw(final GLCanvas canvas, final int x, final int y, final int w, final int h) {
		if (!isLoaded()) {
			mInstanceCache.clear();
		}

		if (w != 0 && h != 0) {
			findInstance(canvas, w, h).draw(canvas, this, x, y);
		}
	}

	public NinePatchChunk getNinePatchChunk() {
		if (mChunk == null) {
			onGetBitmap();
		}
		return mChunk;
	}

	public Rect getPaddings() {
		// get the paddings from nine patch
		if (mChunk == null) {
			onGetBitmap();
		}
		return mChunk.mPaddings;
	}

	@Override
	public void recycle() {
		super.recycle();
		final GLCanvas canvas = mCanvasRef;
		if (canvas == null) return;
		final int n = mInstanceCache.size();
		for (int i = 0; i < n; i++) {
			final NinePatchInstance instance = mInstanceCache.valueAt(i);
			instance.recycle(canvas);
		}
		mInstanceCache.clear();
	}

	@Override
	protected Bitmap onGetBitmap() {
		if (mBitmap != null) return mBitmap;

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), mResId, options);
		mBitmap = bitmap;
		setSize(bitmap.getWidth(), bitmap.getHeight());
		final byte[] chunkData = bitmap.getNinePatchChunk();
		mChunk = chunkData == null ? null : NinePatchChunk.deserialize(bitmap.getNinePatchChunk());
		if (mChunk == null) throw new RuntimeException("invalid nine-patch image: " + mResId);
		return bitmap;
	}

	private NinePatchInstance findInstance(final GLCanvas canvas, final int w, final int h) {
		int key = w;
		key = key << 16 | h;
		NinePatchInstance instance = mInstanceCache.get(key);

		if (instance == null) {
			instance = new NinePatchInstance(this, w, h);
			final NinePatchInstance removed = mInstanceCache.put(key, instance);
			if (removed != null) {
				removed.recycle(canvas);
			}
		}

		return instance;
	}

	// This is a simple cache for a small number of things. Linear search
	// is used because the cache is small. It also tries to remove less used
	// item when the cache is full by moving the often-used items to the front.
	private static class SmallCache {
		private static final int CACHE_SIZE = 16;
		private static final int CACHE_SIZE_START_MOVE = CACHE_SIZE / 2;
		private final int[] mKey = new int[CACHE_SIZE];
		private final NinePatchInstance[] mValue = new NinePatchInstance[CACHE_SIZE];
		private int mCount; // number of items in this cache

		public void clear() {
			for (int i = 0; i < mCount; i++) {
				mValue[i] = null; // make sure it's can be garbage-collected.
			}
			mCount = 0;
		}

		public NinePatchInstance get(final int key) {
			for (int i = 0; i < mCount; i++) {
				if (mKey[i] == key) {
					// Move the accessed item one position to the front, so it
					// will less likely to be removed when cache is full. Only
					// do this if the cache is starting to get full.
					if (mCount > CACHE_SIZE_START_MOVE && i > 0) {
						final int tmpKey = mKey[i];
						mKey[i] = mKey[i - 1];
						mKey[i - 1] = tmpKey;

						final NinePatchInstance tmpValue = mValue[i];
						mValue[i] = mValue[i - 1];
						mValue[i - 1] = tmpValue;
					}
					return mValue[i];
				}
			}
			return null;
		}

		// Puts a value into the cache. If the cache is full, also returns
		// a less used item, otherwise returns null.
		public NinePatchInstance put(final int key, final NinePatchInstance value) {
			if (mCount == CACHE_SIZE) {
				final NinePatchInstance old = mValue[CACHE_SIZE - 1]; // remove
																		// the
																		// last
																		// item
				mKey[CACHE_SIZE - 1] = key;
				mValue[CACHE_SIZE - 1] = value;
				return old;
			} else {
				mKey[mCount] = key;
				mValue[mCount] = value;
				mCount++;
				return null;
			}
		}

		public int size() {
			return mCount;
		}

		public NinePatchInstance valueAt(final int i) {
			return mValue[i];
		}
	}
}

// This keeps data for a specialization of NinePatchTexture with the size
// (width, height). We pre-compute the coordinates for efficiency.
class NinePatchInstance {

	@SuppressWarnings("unused")
	private static final String TAG = "NinePatchInstance";

	// We need 16 vertices for a normal nine-patch image (the 4x4 vertices)
	private static final int VERTEX_BUFFER_SIZE = 16 * 2;

	// We need 22 indices for a normal nine-patch image, plus 2 for each
	// transparent region. Current there are at most 1 transparent region.
	private static final int INDEX_BUFFER_SIZE = 22 + 2;

	private FloatBuffer mXyBuffer;
	private FloatBuffer mUvBuffer;
	private ByteBuffer mIndexBuffer;

	// Names for buffer names: xy, uv, index.
	private int[] mBufferNames;

	private int mIdxCount;

	public NinePatchInstance(final NinePatchTexture tex, final int width, final int height) {
		final NinePatchChunk chunk = tex.getNinePatchChunk();

		if (width <= 0 || height <= 0) throw new RuntimeException("invalid dimension");

		// The code should be easily extended to handle the general cases by
		// allocating more space for buffers. But let's just handle the only
		// use case.
		if (chunk.mDivX.length != 2 || chunk.mDivY.length != 2) throw new RuntimeException("unsupported nine patch");

		final float divX[] = new float[4];
		final float divY[] = new float[4];
		final float divU[] = new float[4];
		final float divV[] = new float[4];

		final int nx = stretch(divX, divU, chunk.mDivX, tex.getWidth(), width);
		final int ny = stretch(divY, divV, chunk.mDivY, tex.getHeight(), height);

		prepareVertexData(divX, divY, divU, divV, nx, ny, chunk.mColor);
	}

	public void draw(final GLCanvas canvas, final NinePatchTexture tex, final int x, final int y) {
		if (mBufferNames == null) {
			prepareBuffers(canvas);
		}
		canvas.drawMesh(tex, x, y, mBufferNames[0], mBufferNames[1], mBufferNames[2], mIdxCount);
	}

	public void recycle(final GLCanvas canvas) {
		if (mBufferNames != null) {
			canvas.deleteBuffer(mBufferNames[0]);
			canvas.deleteBuffer(mBufferNames[1]);
			canvas.deleteBuffer(mBufferNames[2]);
			mBufferNames = null;
		}
	}

	private void prepareBuffers(final GLCanvas canvas) {
		mBufferNames = new int[3];
		final GL11 gl = canvas.getGLInstance();
		GLId.glGenBuffers(3, mBufferNames, 0);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, mBufferNames[0]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, mXyBuffer.capacity() * (Float.SIZE / Byte.SIZE), mXyBuffer,
				GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ARRAY_BUFFER, mBufferNames[1]);
		gl.glBufferData(GL11.GL_ARRAY_BUFFER, mUvBuffer.capacity() * (Float.SIZE / Byte.SIZE), mUvBuffer,
				GL11.GL_STATIC_DRAW);

		gl.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, mBufferNames[2]);
		gl.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER, mIndexBuffer.capacity(), mIndexBuffer, GL11.GL_STATIC_DRAW);

		// These buffers are never used again.
		mXyBuffer = null;
		mUvBuffer = null;
		mIndexBuffer = null;
	}

	private void prepareVertexData(final float x[], final float y[], final float u[], final float v[], final int nx,
			final int ny, final int[] color) {
		/*
		 * Given a 3x3 nine-patch image, the vertex order is defined as the
		 * following graph:
		 * 
		 * (0) (1) (2) (3) | /| /| /| | / | / | / | (4) (5) (6) (7) | \ | \ | \
		 * | | \| \| \| (8) (9) (A) (B) | /| /| /| | / | / | / | (C) (D) (E) (F)
		 * 
		 * And we draw the triangle strip in the following index order:
		 * 
		 * index: 04152637B6A5948C9DAEBF
		 */
		int pntCount = 0;
		final float xy[] = new float[VERTEX_BUFFER_SIZE];
		final float uv[] = new float[VERTEX_BUFFER_SIZE];
		for (int j = 0; j < ny; ++j) {
			for (int i = 0; i < nx; ++i) {
				final int xIndex = pntCount++ << 1;
				final int yIndex = xIndex + 1;
				xy[xIndex] = x[i];
				xy[yIndex] = y[j];
				uv[xIndex] = u[i];
				uv[yIndex] = v[j];
			}
		}

		int idxCount = 1;
		boolean isForward = false;
		final byte index[] = new byte[INDEX_BUFFER_SIZE];
		for (int row = 0; row < ny - 1; row++) {
			--idxCount;
			isForward = !isForward;

			int start, end, inc;
			if (isForward) {
				start = 0;
				end = nx;
				inc = 1;
			} else {
				start = nx - 1;
				end = -1;
				inc = -1;
			}

			for (int col = start; col != end; col += inc) {
				final int k = row * nx + col;
				if (col != start) {
					int colorIdx = row * (nx - 1) + col;
					if (isForward) {
						colorIdx--;
					}
					if (color[colorIdx] == NinePatchChunk.TRANSPARENT_COLOR) {
						index[idxCount] = index[idxCount - 1];
						++idxCount;
						index[idxCount++] = (byte) k;
					}
				}

				index[idxCount++] = (byte) k;
				index[idxCount++] = (byte) (k + nx);
			}
		}

		mIdxCount = idxCount;

		final int size = pntCount * 2 * (Float.SIZE / Byte.SIZE);
		mXyBuffer = allocateDirectNativeOrderBuffer(size).asFloatBuffer();
		mUvBuffer = allocateDirectNativeOrderBuffer(size).asFloatBuffer();
		mIndexBuffer = allocateDirectNativeOrderBuffer(mIdxCount);

		mXyBuffer.put(xy, 0, pntCount * 2).position(0);
		mUvBuffer.put(uv, 0, pntCount * 2).position(0);
		mIndexBuffer.put(index, 0, idxCount).position(0);
	}

	private static ByteBuffer allocateDirectNativeOrderBuffer(final int size) {
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}

	/**
	 * Stretches the texture according to the nine-patch rules. It will linearly
	 * distribute the strechy parts defined in the nine-patch chunk to the
	 * target area.
	 * 
	 * <pre>
	 *                      source
	 *          /--------------^---------------\
	 *         u0    u1       u2  u3     u4   u5
	 * div ---> |fffff|ssssssss|fff|ssssss|ffff| ---> u
	 *          |    div0    div1 div2   div3  |
	 *          |     |       /   /      /    /
	 *          |     |      /   /     /    /
	 *          |     |     /   /    /    /
	 *          |fffff|ssss|fff|sss|ffff| ---> x
	 *         x0    x1   x2  x3  x4   x5
	 *          \----------v------------/
	 *                  target
	 * 
	 * f: fixed segment
	 * s: stretchy segment
	 * </pre>
	 * 
	 * @param div the stretch parts defined in nine-patch chunk
	 * @param source the length of the texture
	 * @param target the length on the drawing plan
	 * @param u output, the positions of these dividers in the texture
	 *            coordinate
	 * @param x output, the corresponding position of these dividers on the
	 *            drawing plan
	 * @return the number of these dividers.
	 */
	private static int stretch(final float x[], final float u[], final int div[], final int source, final int target) {
		final int textureSize = Utils.nextPowerOf2(source);
		final float textureBound = (float) source / textureSize;

		float stretch = 0;
		for (int i = 0, n = div.length; i < n; i += 2) {
			stretch += div[i + 1] - div[i];
		}

		float remaining = target - source + stretch;

		float lastX = 0;
		float lastU = 0;

		x[0] = 0;
		u[0] = 0;
		for (int i = 0, n = div.length; i < n; i += 2) {
			// Make the stretchy segment a little smaller to prevent sampling
			// on neighboring fixed segments.
			// fixed segment
			x[i + 1] = lastX + (div[i] - lastU) + 0.5f;
			u[i + 1] = Math.min((div[i] + 0.5f) / textureSize, textureBound);

			// stretchy segment
			final float partU = div[i + 1] - div[i];
			final float partX = remaining * partU / stretch;
			remaining -= partX;
			stretch -= partU;

			lastX = x[i + 1] + partX;
			lastU = div[i + 1];
			x[i + 2] = lastX - 0.5f;
			u[i + 2] = Math.min((lastU - 0.5f) / textureSize, textureBound);
		}
		// the last fixed segment
		x[div.length + 1] = target;
		u[div.length + 1] = textureBound;

		// remove segments with length 0.
		int last = 0;
		for (int i = 1, n = div.length + 2; i < n; ++i) {
			if (x[i] - x[last] < 1f) {
				continue;
			}
			x[++last] = x[i];
			u[last] = u[i];
		}
		return last + 1;
	}
}
