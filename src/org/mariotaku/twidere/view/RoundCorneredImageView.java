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

import org.mariotaku.twidere.util.SetLayerTypeAccessor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundCorneredImageView extends ImageView {

	private Path mPath = new Path();

	public RoundCorneredImageView(Context context) {
		this(context, null);
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		createPath();
	}

	@Override
	public void onDraw(Canvas canvas) {
		try {
			canvas.clipPath(mPath);
		} catch (final UnsupportedOperationException e) {
			// This shouldn't happen, but in order to keep app running, I simply
			// ignore this Exception.
		}
		super.onDraw(canvas);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		createPath();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void createPath() {
		final float density = getResources().getDisplayMetrics().density;
		mPath.reset();
		mPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), 4 * density, 4 * density, Path.Direction.CW);
	}

	private void init() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			SetLayerTypeAccessor.setLayerType(this, View.LAYER_TYPE_SOFTWARE, null);
		}
	}
}
