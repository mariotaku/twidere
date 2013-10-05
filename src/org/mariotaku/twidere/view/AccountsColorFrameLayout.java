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
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class AccountsColorFrameLayout extends RelativeLayout {

	private int[] mColors;
	private final Paint mPaint = new Paint();
	private final float mColorsWidth;

	public AccountsColorFrameLayout(final Context context) {
		this(context, null);
	}

	public AccountsColorFrameLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AccountsColorFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		final Resources res = getResources();
		mColorsWidth = 3 * res.getDisplayMetrics().density;
	}

	public void setColors(final int... colors) {
		mColors = colors;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mColors == null || mColors.length == 0) {
			canvas.drawColor(Color.TRANSPARENT);
			return;
		}
		final int width = getWidth(), height = getHeight();
		final int length = mColors.length;
		for (int i = 0; i < length; i++) {
			final int color = mColors[i];
			mPaint.setColor(color);
			canvas.drawRect(width - mColorsWidth, i * (height / length), width, (i + 1) * (height / length), mPaint);
		}

	}

}
