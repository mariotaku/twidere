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

package org.mariotaku.gallery3d.ui;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.mariotaku.gallery3d.ui.GLRoot.OnGLIdleListener;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.SystemClock;

// This class is similar to BitmapTexture, except the bitmap is
// split into tiles. By doing so, we may increase the time required to
// upload the whole bitmap but we reduce the time of uploading each tile
// so it make the animation more smooth and prevents jank.
public class TiledTexture implements Texture {
	private static final int CONTENT_SIZE = 254;
	private static final int BORDER_SIZE = 1;
	private static final int TILE_SIZE = CONTENT_SIZE + 2 * BORDER_SIZE;
	private static final int INIT_CAPACITY = 8;

	// We are targeting at 60fps, so we have 16ms for each frame.
	// In this 16ms, we use about 4~8 ms to upload tiles.
	private static final long UPLOAD_TILE_LIMIT = 4; // ms

	private static Tile sFreeTileHead = null;
	private static final Object sFreeTileLock = new Object();

	private static Bitmap sUploadBitmap;
	private static Canvas sCanvas;
	private static Paint sBitmapPaint;
	private static Paint sPaint;

	private int mUploadIndex = 0;

	private final Tile[] mTiles;
	private final int mWidth;
	private final int mHeight;
	private final RectF mSrcRect = new RectF();
	private final RectF mDestRect = new RectF();

	public TiledTexture(final Bitmap bitmap) {
		mWidth = bitmap.getWidth();
		mHeight = bitmap.getHeight();
		final ArrayList<Tile> list = new ArrayList<Tile>();

		for (int x = 0, w = mWidth; x < w; x += CONTENT_SIZE) {
			for (int y = 0, h = mHeight; y < h; y += CONTENT_SIZE) {
				final Tile tile = obtainTile();
				tile.offsetX = x;
				tile.offsetY = y;
				tile.bitmap = bitmap;
				tile.setSize(Math.min(CONTENT_SIZE, mWidth - x), Math.min(CONTENT_SIZE, mHeight - y));
				list.add(tile);
			}
		}
		mTiles = list.toArray(new Tile[list.size()]);
	}

	@Override
	public void draw(final GLCanvas canvas, final int x, final int y) {
		draw(canvas, x, y, mWidth, mHeight);
	}

	// Draws the texture on to the specified rectangle.
	@Override
	public void draw(final GLCanvas canvas, final int x, final int y, final int width, final int height) {
		final RectF src = mSrcRect;
		final RectF dest = mDestRect;
		final float scaleX = (float) width / mWidth;
		final float scaleY = (float) height / mHeight;
		for (int i = 0, n = mTiles.length; i < n; ++i) {
			final Tile t = mTiles[i];
			src.set(0, 0, t.contentWidth, t.contentHeight);
			src.offset(t.offsetX, t.offsetY);
			mapRect(dest, src, 0, 0, x, y, scaleX, scaleY);
			src.offset(BORDER_SIZE - t.offsetX, BORDER_SIZE - t.offsetY);
			canvas.drawTexture(t, mSrcRect, mDestRect);
		}
	}

	// Draws a sub region of this texture on to the specified rectangle.
	public void draw(final GLCanvas canvas, final RectF source, final RectF target) {
		final RectF src = mSrcRect;
		final RectF dest = mDestRect;
		final float x0 = source.left;
		final float y0 = source.top;
		final float x = target.left;
		final float y = target.top;
		final float scaleX = target.width() / source.width();
		final float scaleY = target.height() / source.height();

		for (int i = 0, n = mTiles.length; i < n; ++i) {
			final Tile t = mTiles[i];
			src.set(0, 0, t.contentWidth, t.contentHeight);
			src.offset(t.offsetX, t.offsetY);
			if (!src.intersect(source)) {
				continue;
			}
			mapRect(dest, src, x0, y0, x, y, scaleX, scaleY);
			src.offset(BORDER_SIZE - t.offsetX, BORDER_SIZE - t.offsetY);
			canvas.drawTexture(t, src, dest);
		}
	}

	// Draws a mixed color of this texture and a specified color onto the
	// a rectangle. The used color is: from * (1 - ratio) + to * ratio.
	public void drawMixed(final GLCanvas canvas, final int color, final float ratio, final int x, final int y,
			final int width, final int height) {
		final RectF src = mSrcRect;
		final RectF dest = mDestRect;
		final float scaleX = (float) width / mWidth;
		final float scaleY = (float) height / mHeight;
		for (int i = 0, n = mTiles.length; i < n; ++i) {
			final Tile t = mTiles[i];
			src.set(0, 0, t.contentWidth, t.contentHeight);
			src.offset(t.offsetX, t.offsetY);
			mapRect(dest, src, 0, 0, x, y, scaleX, scaleY);
			src.offset(BORDER_SIZE - t.offsetX, BORDER_SIZE - t.offsetY);
			canvas.drawMixed(t, color, ratio, mSrcRect, mDestRect);
		}
	}

	@Override
	public int getHeight() {
		return mHeight;
	}

	@Override
	public int getWidth() {
		return mWidth;
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

	public boolean isReady() {
		return mUploadIndex == mTiles.length;
	}

	public void recycle() {
		for (int i = 0, n = mTiles.length; i < n; ++i) {
			freeTile(mTiles[i]);
		}
	}

	private boolean uploadNextTile(final GLCanvas canvas) {
		if (mUploadIndex == mTiles.length) return true;

		final Tile next = mTiles[mUploadIndex++];

		// Make sure tile has not already been recycled by the time
		// this is called (race condition in onGLIdle)
		if (next.bitmap != null) {
			final boolean hasBeenLoad = next.isLoaded();
			next.updateContent(canvas);

			// It will take some time for a texture to be drawn for the first
			// time. When scrolling, we need to draw several tiles on the screen
			// at the same time. It may cause a UI jank even these textures has
			// been uploaded.
			if (!hasBeenLoad) {
				next.draw(canvas, 0, 0);
			}
		}
		return mUploadIndex == mTiles.length;
	}

	public static void freeResources() {
		sUploadBitmap = null;
		sCanvas = null;
		sBitmapPaint = null;
		sPaint = null;
	}

	public static void prepareResources() {
		sUploadBitmap = Bitmap.createBitmap(TILE_SIZE, TILE_SIZE, Config.ARGB_8888);
		sCanvas = new Canvas(sUploadBitmap);
		sBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
		sBitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		sPaint = new Paint();
		sPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
		sPaint.setColor(Color.TRANSPARENT);
	}

	private static void freeTile(final Tile tile) {
		tile.invalidateContent();
		tile.bitmap = null;
		synchronized (sFreeTileLock) {
			tile.nextFreeTile = sFreeTileHead;
			sFreeTileHead = tile;
		}
	}

	// We want to draw the "source" on the "target".
	// This method is to find the "output" rectangle which is
	// the corresponding area of the "src".
	// (x,y) target
	// (x0,y0) source +---------------+
	// +----------+ | |
	// | src | | output |
	// | +--+ | linear map | +----+ |
	// | +--+ | ----------> | | | |
	// | | by (scaleX, scaleY) | +----+ |
	// +----------+ | |
	// Texture +---------------+
	// Canvas
	private static void mapRect(final RectF output, final RectF src, final float x0, final float y0, final float x,
			final float y, final float scaleX, final float scaleY) {
		output.set(x + (src.left - x0) * scaleX, y + (src.top - y0) * scaleY, x + (src.right - x0) * scaleX, y
				+ (src.bottom - y0) * scaleY);
	}

	private static Tile obtainTile() {
		synchronized (sFreeTileLock) {
			final Tile result = sFreeTileHead;
			if (result == null) return new Tile();
			sFreeTileHead = result.nextFreeTile;
			result.nextFreeTile = null;
			return result;
		}
	}

	public static class Uploader implements OnGLIdleListener {
		private final ArrayDeque<TiledTexture> mTextures = new ArrayDeque<TiledTexture>(INIT_CAPACITY);

		private final GLRoot mGlRoot;
		private boolean mIsQueued = false;

		public Uploader(final GLRoot glRoot) {
			mGlRoot = glRoot;
		}

		public synchronized void addTexture(final TiledTexture t) {
			if (t.isReady()) return;
			mTextures.addLast(t);

			if (mIsQueued) return;
			mIsQueued = true;
			mGlRoot.addOnGLIdleListener(this);
		}

		public synchronized void clear() {
			mTextures.clear();
		}

		@Override
		public boolean onGLIdle(final GLCanvas canvas, final boolean renderRequested) {
			final ArrayDeque<TiledTexture> deque = mTextures;
			synchronized (this) {
				long now = SystemClock.uptimeMillis();
				final long dueTime = now + UPLOAD_TILE_LIMIT;
				while (now < dueTime && !deque.isEmpty()) {
					final TiledTexture t = deque.peekFirst();
					if (t.uploadNextTile(canvas)) {
						deque.removeFirst();
						mGlRoot.requestRender();
					}
					now = SystemClock.uptimeMillis();
				}
				mIsQueued = !mTextures.isEmpty();

				// return true to keep this listener in the queue
				return mIsQueued;
			}
		}
	}

	private static class Tile extends UploadedTexture {
		public int offsetX;
		public int offsetY;
		public Bitmap bitmap;
		public Tile nextFreeTile;
		public int contentWidth;
		public int contentHeight;

		@Override
		public void setSize(final int width, final int height) {
			contentWidth = width;
			contentHeight = height;
			mWidth = width + 2 * BORDER_SIZE;
			mHeight = height + 2 * BORDER_SIZE;
			mTextureWidth = TILE_SIZE;
			mTextureHeight = TILE_SIZE;
		}

		@Override
		protected void onFreeBitmap(final Bitmap bitmap) {
			// do nothing
		}

		@Override
		protected Bitmap onGetBitmap() {
			final int x = BORDER_SIZE - offsetX;
			final int y = BORDER_SIZE - offsetY;
			final int r = bitmap.getWidth() + x;
			final int b = bitmap.getHeight() + y;
			sCanvas.drawBitmap(bitmap, x, y, sBitmapPaint);
			bitmap = null;

			// draw borders if need
			if (x > 0) {
				sCanvas.drawLine(x - 1, 0, x - 1, TILE_SIZE, sPaint);
			}
			if (y > 0) {
				sCanvas.drawLine(0, y - 1, TILE_SIZE, y - 1, sPaint);
			}
			if (r < CONTENT_SIZE) {
				sCanvas.drawLine(r, 0, r, TILE_SIZE, sPaint);
			}
			if (b < CONTENT_SIZE) {
				sCanvas.drawLine(0, b, TILE_SIZE, b, sPaint);
			}

			return sUploadBitmap;
		}
	}
}
