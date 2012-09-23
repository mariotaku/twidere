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
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {

	private int mColor = Color.TRANSPARENT;

	public ColorView(final Context context) {
		this(context, null);
	}

	public ColorView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setColor(final int color) {
		mColor = color;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(mColor);
	}

}
