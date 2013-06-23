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

import org.mariotaku.twidere.util.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ClickableImageView extends ImageView {

	private final int mHightlightColor;
	private final Rect mRect;
	private boolean mIsDown;

	public ClickableImageView(final Context context) {
		this(context, null);
	}

	public ClickableImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClickableImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final int color = Utils.getThemeColor(context);
		mHightlightColor = Color.argb(0x80, Color.red(color), Color.green(color), Color.blue(color));
		mRect = new Rect();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mRect.set(getLeft(), getTop(), getRight(), getBottom());
				mIsDown = true;
				invalidate();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mIsDown = false;
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
		}
		return super.onTouchEvent(e);
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mIsDown && isClickable() && isEnabled()) {
			canvas.drawColor(mHightlightColor);
		}
	}

}
