/*
 * Copyright (C) 2010 Daniel Nilsson
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

package org.mariotaku.twidere.graphic;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * This drawable that draws a simple white and gray chessboard pattern. It's
 * pattern you will often see as a background behind a partly transparent image
 * in many applications.
 * 
 * @author Daniel Nilsson
 */
public class AlphaPatternDrawable extends Drawable {

	private int mRectangleSize = 10;

	private int numRectanglesHorizontal;
	private int numRectanglesVertical;

	/**
	 * Bitmap in which the pattern will be cahched.
	 */
	private Bitmap mBitmap;

	public AlphaPatternDrawable(final int rectangleSize) {

		mRectangleSize = rectangleSize;
	}

	@Override
	public void draw(final Canvas canvas) {

		canvas.drawBitmap(mBitmap, null, getBounds(), new Paint());
	}

	@Override
	public int getOpacity() {

		return 0;
	}

	@Override
	public void setAlpha(final int alpha) {

	}

	@Override
	public void setColorFilter(final ColorFilter cf) {

	}

	@Override
	protected void onBoundsChange(final Rect bounds) {

		super.onBoundsChange(bounds);

		final int height = bounds.height();
		final int width = bounds.width();

		numRectanglesHorizontal = (int) Math.ceil(width / mRectangleSize);
		numRectanglesVertical = (int) Math.ceil(height / mRectangleSize);

		generatePatternBitmap();

	}

	/**
	 * This will generate a bitmap with the pattern as big as the rectangle we
	 * were allow to draw on. We do this to chache the bitmap so we don't need
	 * to recreate it each time draw() is called since it takes a few
	 * milliseconds.
	 */
	private void generatePatternBitmap() {

		if (getBounds().width() <= 0 || getBounds().height() <= 0) return;

		mBitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(), Config.ARGB_8888);
		final Canvas canvas = new Canvas(mBitmap);

		final Rect r = new Rect();
		boolean verticalStartWhite = true;
		for (int i = 0; i <= numRectanglesVertical; i++) {

			boolean isWhite = verticalStartWhite;
			for (int j = 0; j <= numRectanglesHorizontal; j++) {

				r.top = i * mRectangleSize;
				r.left = j * mRectangleSize;
				r.bottom = r.top + mRectangleSize;
				r.right = r.left + mRectangleSize;

				final Paint paint = new Paint();
				paint.setColor(isWhite ? Color.WHITE : Color.GRAY);
				canvas.drawRect(r, paint);

				isWhite = !isWhite;
			}

			verticalStartWhite = !verticalStartWhite;

		}

	}

}