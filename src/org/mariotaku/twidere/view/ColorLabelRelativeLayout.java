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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.IColorLabelView;

public class ColorLabelRelativeLayout extends RelativeLayout implements IColorLabelView {

	private final Paint mPaintStart = new Paint(), mPaintEnd = new Paint(), mPaintBackground = new Paint();
	private final Rect mRectStart = new Rect(), mRectEnd = new Rect(), mRectBackground = new Rect();
	private final float mDensity;
	private final boolean mIsRTL;

	private boolean mIgnorePadding;

	public ColorLabelRelativeLayout(final Context context) {
		this(context, null);
	}

	public ColorLabelRelativeLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorLabelRelativeLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Twidere);
		mIgnorePadding = a.getBoolean(R.styleable.Twidere_ignorePadding, false);
		a.recycle();
		final Resources res = context.getResources();
		mDensity = res.getDisplayMetrics().density;
		mPaintStart.setColor(Color.TRANSPARENT);
		mPaintEnd.setColor(Color.TRANSPARENT);
		mPaintBackground.setColor(Color.TRANSPARENT);
		mIsRTL = isRTL(context);
	}

	@Override
	public void drawBackground(final int color) {
		drawLabel(mPaintStart.getColor(), mPaintEnd.getColor(), color);
	}

	@Override
	public void drawEnd(final int color) {
		drawLabel(mPaintStart.getColor(), color, mPaintBackground.getColor());
	}

	@Override
	public void drawLabel(final int left, final int right, final int background) {
		mPaintBackground.setColor(background);
		mPaintStart.setColor(left);
		mPaintEnd.setColor(right);
		invalidate();
	}

	@Override
	public void drawStart(final int color) {
		drawLabel(color, mPaintEnd.getColor(), mPaintBackground.getColor());
	}

	@Override
	public boolean isPaddingsIgnored() {
		return mIgnorePadding;
	}

	@Override
	public void setIgnorePaddings(final boolean ignorePaddings) {
		mIgnorePadding = ignorePaddings;
		invalidate();
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		canvas.drawRect(mRectBackground, mPaintBackground);
		super.dispatchDraw(canvas);
		canvas.drawRect(mRectStart, mPaintStart);
		canvas.drawRect(mRectEnd, mPaintEnd);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		final int pl, pt, pr, pb;
		if (mIgnorePadding) {
			pl = pt = pr = pb = 0;
		} else {
			pl = getPaddingLeft();
			pt = getPaddingTop();
			pr = getPaddingRight();
			pb = getPaddingBottom();
		}
		mRectBackground.set(pl, pt, w - pr, h - pb);
		(mIsRTL ? mRectEnd : mRectStart).set(pl, pt, (int) (LABEL_WIDTH * mDensity) + pl, h - pb);
		(mIsRTL ? mRectStart : mRectEnd).set(w - (int) (LABEL_WIDTH * mDensity) - pr, pt, w - pr, h - pb);
		super.onSizeChanged(w, h, oldw, oldh);
	}
}
