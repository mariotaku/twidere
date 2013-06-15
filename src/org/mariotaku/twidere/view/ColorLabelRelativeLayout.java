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

import static org.mariotaku.twidere.util.Utils.isRTL;

import org.mariotaku.twidere.view.iface.IColorLabelView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class ColorLabelRelativeLayout extends RelativeLayout implements IColorLabelView {

	private final Paint mPaintLeft = new Paint(), mPaintRight = new Paint(), mPaintBackground = new Paint();
	private final Rect mRectLeft = new Rect(), mRectRight = new Rect(), mRectBackground = new Rect();
	private final float mDensity;
	private final boolean mIsRTL;

	public ColorLabelRelativeLayout(final Context context) {
		this(context, null);
	}

	public ColorLabelRelativeLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorLabelRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = context.getResources();
		mDensity = res.getDisplayMetrics().density;
		mPaintLeft.setColor(Color.TRANSPARENT);
		mPaintRight.setColor(Color.TRANSPARENT);
		mPaintBackground.setColor(Color.TRANSPARENT);
		mIsRTL = isRTL(context);
	}

	@Override
	public void drawBackground(final int color) {
		drawLabel(mPaintLeft.getColor(), mPaintRight.getColor(), color);
	}

	@Override
	public void drawLabel(final int left, final int right, final int background) {
		mPaintBackground.setColor(background);
		mPaintLeft.setColor(left);
		mPaintRight.setColor(right);
		invalidate();
	}

	@Override
	public void drawLeft(final int color) {
		drawLabel(color, mPaintRight.getColor(), mPaintBackground.getColor());
	}

	@Override
	public void drawRight(final int color) {
		drawLabel(mPaintLeft.getColor(), color, mPaintBackground.getColor());
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		canvas.drawRect(mRectBackground, mPaintBackground);
		canvas.drawRect(mRectLeft, mPaintLeft);
		canvas.drawRect(mRectRight, mPaintRight);
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		mRectBackground.set(0, 0, w, h);
		if (mIsRTL) {
			mRectRight.set(0, 0, (int) (LABEL_WIDTH * mDensity), h);
			mRectLeft.set(w - (int) (LABEL_WIDTH * mDensity), 0, w, h);
		} else {
			mRectLeft.set(0, 0, (int) (LABEL_WIDTH * mDensity), h);
			mRectRight.set(w - (int) (LABEL_WIDTH * mDensity), 0, w, h);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
