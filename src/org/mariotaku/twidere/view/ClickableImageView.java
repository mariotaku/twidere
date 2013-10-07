/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;

public class ClickableImageView extends ImageView {

	private final Rect mRect;
	private final Paint mHighlightPaint;

	private boolean mIsDown;
	private boolean mIgnorePadding;

	public ClickableImageView(final Context context) {
		this(context, null);
	}

	public ClickableImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClickableImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Twidere);
		mIgnorePadding = a.getBoolean(R.styleable.Twidere_ignorePadding, false);
		a.recycle();
		final int color = ThemeUtils.getThemeColor(context);
		final int mHighlightColor = Color.argb(0x80, Color.red(color), Color.green(color), Color.blue(color));
		mHighlightPaint = new Paint();
		mHighlightPaint.setColor(mHighlightColor);
		mRect = new Rect();
	}

	public boolean isPaddingsIgnored() {
		return mIgnorePadding;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mRect.set(getLeft(), getTop(), getRight(), getBottom());
				mIsDown = true;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				if (mRect.contains(getLeft() + (int) e.getX(), getTop() + (int) e.getY())) {
					break;
				}
				if (mIsDown) {
					mIsDown = false;
					invalidate();
				}
				break;
			default:
				mIsDown = false;
				invalidate();
				break;
		}
		return super.onTouchEvent(e);
	}

	public void setIgnorePaddings(final boolean ignorePaddings) {
		mIgnorePadding = ignorePaddings;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mIsDown && isClickable() && isEnabled()) {
			final int pl, pt, pr, pb;
			final int w = getWidth(), h = getHeight();
			if (mIgnorePadding) {
				pl = pt = pr = pb = 0;
			} else {
				pl = getPaddingLeft();
				pt = getPaddingTop();
				pr = getPaddingRight();
				pb = getPaddingBottom();
			}
			canvas.drawRect(pl, pt, w - pr, h - pb, mHighlightPaint);
		}
	}

}
