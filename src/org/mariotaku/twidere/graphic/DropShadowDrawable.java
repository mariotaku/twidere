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
	
	public DropShadowDrawable(final Resources resources, final Bitmap bitmap, final float shadowRadius, final int shadowColor) {
		super(resources, bitmap);
		final float density = resources.getDisplayMetrics().density;
		mShadow = getDropShadow(bitmap, shadowRadius * density, shadowColor);
	}

	public DropShadowDrawable(final Resources resources, final Drawable drawable, final float shadowRadius, final int shadowColor) {
		this(resources, Utils.getBitmap(drawable), shadowRadius, shadowColor);
	}
	
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
