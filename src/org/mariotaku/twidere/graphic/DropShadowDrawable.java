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

package org.mariotaku.twidere.graphic;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.util.Utils;

public final class DropShadowDrawable extends BitmapDrawable {

	private final Bitmap mShadow;

	public DropShadowDrawable(final Resources resources, final Bitmap bitmap, final float shadowRadius,
			final int shadowColor) {
		super(resources, bitmap);
		final float density = resources.getDisplayMetrics().density;
		mShadow = getDropShadow(bitmap, shadowRadius * density, shadowColor);
	}

	public DropShadowDrawable(final Resources resources, final Drawable drawable, final float shadowRadius,
			final int shadowColor) {
		this(resources, Utils.getBitmap(drawable), shadowRadius, shadowColor);
	}

	public DropShadowDrawable(final Resources resources, final int res, final float shadowRadius, final int shadowColor) {
		this(resources, Utils.getBitmap(resources.getDrawable(res)), shadowRadius, shadowColor);
	}

	@Override
	public void draw(final Canvas canvas) {
		canvas.drawBitmap(mShadow, 0, 0, null);
		super.draw(canvas);
	}

	private static Bitmap getDropShadow(final Bitmap src, final float radius, final int color) {
		if (src == null) return null;
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		final int width = src.getWidth(), height = src.getHeight();
		final Bitmap dest = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(dest);
		// Create background
		final Bitmap alpha = src.extractAlpha();
		paint.setColor(color);
		canvas.drawBitmap(alpha, 0, 0, paint);
		// Create outer blur
		final BlurMaskFilter filter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER);
		paint.setMaskFilter(filter);
		canvas.drawBitmap(alpha, 0, 0, paint);
		return dest;
	}
}
