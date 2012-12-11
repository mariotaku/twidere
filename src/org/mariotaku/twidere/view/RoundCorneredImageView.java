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

import org.mariotaku.twidere.view.iface.IExtendedView;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundCorneredImageView extends ImageView implements IExtendedView {

	private final Path mPath = new Path();
	private final RectF mRectF = new RectF();
	private float mRadius;

	private OnSizeChangedListener mOnSizeChangedListener;

	public RoundCorneredImageView(final Context context) {
		this(context, null);
	}

	public RoundCorneredImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundCorneredImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			SetLayerTypeAccessor.setLayerType(this, View.LAYER_TYPE_SOFTWARE, null);
		}
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.radius });
		mRadius = a.getDimensionPixelSize(0, (int) (4 * getResources().getDisplayMetrics().density));
		a.recycle();
	}

	@Override
	public void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		if (w > 0 && h > 0) {
			createRectF(w, h);
			createPath(w, h);
		}
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		// Workaround for pre-ICS devices, without anti-alias.
		try {
			canvas.clipPath(mPath);
		} catch (final UnsupportedOperationException e) {
			// This shouldn't happen, but in order to keep app running, I
			// simply ignore this Exception.
		}
		super.onDraw(canvas);
	}

	@Override
	public final void setOnSizeChangedListener(final OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	public void setRadius(final float radius) {
		mRadius = radius;
		final int w = getWidth(), h = getHeight();
		createRectF(w, h);
		createPath(w, h);
		invalidate();
	}

	private void createPath(final int w, final int h) {
		if (w <= 0 || h <= 0) return;
		mPath.reset();
		mPath.addRoundRect(mRectF, mRadius, mRadius, Path.Direction.CW);
	}

	private void createRectF(final int w, final int h) {
		if (w <= 0 || h <= 0) return;
		mRectF.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
	}

	static class SetLayerTypeAccessor {

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public static void setLayerType(final View view, final int layerType, final Paint paint) {
			view.setLayerType(layerType, paint);
		}
	}
}
