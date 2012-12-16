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

import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.BitmapPool;
import org.mariotaku.gallery3d.data.MediaItem;

import android.graphics.Bitmap;
import android.graphics.RectF;

// This is a ScreenNail wraps a Bitmap. There are some extra functions:
//
// - If we need to draw before the bitmap is available, we draw a rectange of
// placeholder color (gray).
//
// - When the the bitmap is available, and we have drawn the placeholder color
// before, we will do a fade-in animation.
public class TiledScreenNail implements ScreenNail {
	@SuppressWarnings("unused")
	private static final String TAG = "TiledScreenNail";

	// The duration of the fading animation in milliseconds
	private static final int DURATION = 180;

	private static int sMaxSide = 640;

	// These are special values for mAnimationStartTime
	private static final long ANIMATION_NOT_NEEDED = -1;
	private static final long ANIMATION_NEEDED = -2;
	private static final long ANIMATION_DONE = -3;

	private int mWidth;
	private int mHeight;
	private long mAnimationStartTime = ANIMATION_NOT_NEEDED;

	private Bitmap mBitmap;
	private TiledTexture mTexture;

	// This gets overridden by bitmap_screennail_placeholder
	// in GalleryUtils.initialize
	private static int mPlaceholderColor = 0xFF222222;

	private static boolean mDrawPlaceholder = true;

	public TiledScreenNail(final Bitmap bitmap) {
		mWidth = bitmap.getWidth();
		mHeight = bitmap.getHeight();
		mBitmap = bitmap;
		mTexture = new TiledTexture(bitmap);
	}

	public TiledScreenNail(final int width, final int height) {
		setSize(width, height);
	}

	// Combines the two ScreenNails.
	// Returns the used one and recycle the unused one.
	public ScreenNail combine(final ScreenNail other) {
		if (other == null) return this;

		if (!(other instanceof TiledScreenNail)) {
			recycle();
			return other;
		}

		// Now both are TiledScreenNail. Move over the information about width,
		// height, and Bitmap, then recycle the other.
		final TiledScreenNail newer = (TiledScreenNail) other;
		mWidth = newer.mWidth;
		mHeight = newer.mHeight;
		if (newer.mTexture != null) {
			recycleBitmap(MediaItem.getThumbPool(), mBitmap);
			if (mTexture != null) {
				mTexture.recycle();
			}
			mBitmap = newer.mBitmap;
			mTexture = newer.mTexture;
			newer.mBitmap = null;
			newer.mTexture = null;
		}
		newer.recycle();
		return this;
	}

	@Override
	public void draw(final GLCanvas canvas, final int x, final int y, final int width, final int height) {
		if (mTexture == null || !mTexture.isReady()) {
			if (mAnimationStartTime == ANIMATION_NOT_NEEDED) {
				mAnimationStartTime = ANIMATION_NEEDED;
			}
			if (mDrawPlaceholder) {
				canvas.fillRect(x, y, width, height, mPlaceholderColor);
			}
			return;
		}

		if (mAnimationStartTime == ANIMATION_NEEDED) {
			mAnimationStartTime = AnimationTime.get();
		}

		if (isAnimating()) {
			mTexture.drawMixed(canvas, mPlaceholderColor, getRatio(), x, y, width, height);
		} else {
			mTexture.draw(canvas, x, y, width, height);
		}
	}

	@Override
	public void draw(final GLCanvas canvas, final RectF source, final RectF dest) {
		if (mTexture == null || !mTexture.isReady()) {
			canvas.fillRect(dest.left, dest.top, dest.width(), dest.height(), mPlaceholderColor);
			return;
		}

		mTexture.draw(canvas, source, dest);
	}

	@Override
	public int getHeight() {
		return mHeight;
	}

	public TiledTexture getTexture() {
		return mTexture;
	}

	@Override
	public int getWidth() {
		return mWidth;
	}

	public boolean isAnimating() {
		// The TiledTexture may not be uploaded completely yet.
		// In that case, we count it as animating state and we will draw
		// the placeholder in TileImageView.
		if (mTexture == null || !mTexture.isReady()) return true;
		if (mAnimationStartTime < 0) return false;
		if (AnimationTime.get() - mAnimationStartTime >= DURATION) {
			mAnimationStartTime = ANIMATION_DONE;
			return false;
		}
		return true;
	}

	public boolean isShowingPlaceholder() {
		return mBitmap == null || isAnimating();
	}

	@Override
	public void noDraw() {
	}

	@Override
	public void recycle() {
		if (mTexture != null) {
			mTexture.recycle();
			mTexture = null;
		}
		recycleBitmap(MediaItem.getThumbPool(), mBitmap);
		mBitmap = null;
	}

	public void updatePlaceholderSize(final int width, final int height) {
		if (mBitmap != null) return;
		if (width == 0 || height == 0) return;
		setSize(width, height);
	}

	private float getRatio() {
		final float r = (float) (AnimationTime.get() - mAnimationStartTime) / DURATION;
		return Utils.clamp(1.0f - r, 0.0f, 1.0f);
	}

	private void setSize(int width, int height) {
		if (width == 0 || height == 0) {
			width = sMaxSide;
			height = sMaxSide * 3 / 4;
		}
		final float scale = Math.min(1, (float) sMaxSide / Math.max(width, height));
		mWidth = Math.round(scale * width);
		mHeight = Math.round(scale * height);
	}

	public static void disableDrawPlaceholder() {
		mDrawPlaceholder = false;
	}

	public static void enableDrawPlaceholder() {
		mDrawPlaceholder = true;
	}

	public static void setMaxSide(final int size) {
		sMaxSide = size;
	}

	public static void setPlaceholderColor(final int color) {
		mPlaceholderColor = color;
	}

	private static void recycleBitmap(final BitmapPool pool, final Bitmap bitmap) {
		if (pool == null || bitmap == null) return;
		pool.recycle(bitmap);
	}
}
