/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {

	private int mOrientation;
	private int[] mColors;
	private final Paint mPaint = new Paint();

	public static final int HORIZONTAL = 1, VERTICAL = 2;

	public ColorView(final Context context) {
		this(context, null);
	}

	public ColorView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setColor(final int... colors) {
		mColors = colors;
		invalidate();
	}

	public void setOrientation(final int orientation) {
		mOrientation = orientation;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mColors == null || mColors.length == 0) {
			canvas.drawColor(Color.TRANSPARENT);
			return;
		}
		final boolean draw_vertical = mOrientation == VERTICAL;
		final int width = getWidth(), height = getHeight();
		final int length = mColors.length;
		for (int i = 0; i < length; i++) {
			final int color = mColors[i];
			mPaint.setColor(color);
			if (draw_vertical) {
				canvas.drawRect(0, i * (height / length), width, (i + 1) * (height / length), mPaint);
			} else {
				canvas.drawRect(i * (width / length), 0, (i + 1) * (width / length), height, mPaint);
			}
		}

	}

}
