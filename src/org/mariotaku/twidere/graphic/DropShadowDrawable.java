package org.mariotaku.twidere.graphic;

import android.graphics.drawable.BitmapDrawable;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;

public final class DropShadowDrawable extends BitmapDrawable {

	private final Bitmap mShadow;
	
	public DropShadowDrawable(final Resources resources, final Bitmap bitmap, final float shadowRadius, final int shadowColor) {
		super(resources, bitmap);
		final float density = resources.getDisplayMetrics().density;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		mShadow = getDropShadow(bitmap, paint, shadowRadius * density, shadowColor);
	}

	public void draw(final Canvas canvas) {		
		canvas.drawBitmap(mShadow, 0, 0, null);
		super.draw(canvas);
	}
	
	private static Bitmap getDropShadow(final Bitmap src, final Paint paint, final float radius, final int color) {
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
