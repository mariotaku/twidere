package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;

public class StatusListPreviewImageView extends RoundCorneredImageView {

	public StatusListPreviewImageView(final Context context) {
		super(context);
		setScaleType(ScaleType.MATRIX);
	}

	public StatusListPreviewImageView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		setScaleType(ScaleType.MATRIX);
	}

	public StatusListPreviewImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
	}

	@Override
	protected boolean setFrame(final int frameLeft, final int frameTop, final int frameRight, final int frameBottom) {
		final float frameWidth = frameRight - frameLeft;
		final float frameHeight = frameBottom - frameTop;

		final Drawable drawable = getDrawable();

		int translatey = 0;
		final Bitmap bitmap = drawableToBitmap(drawable);
		if (bitmap != null) {
			final int bw = bitmap.getWidth();
			final int bh = bitmap.getHeight();
			final int stepw = limit(bw / 32, 1, bw);
			final int steph = limit(bh / 96, 1, bh);
			for (int x = 0, y = 0; x < bw && y < bh / 2; x += stepw, y += steph) {
				if (!nearlyWhite(bitmap.getPixel(x, y))) {
					translatey = y;
					break;
				}
			}
		}

		final float originalImageWidth = drawable.getIntrinsicWidth();
		final float originalImageHeight = drawable.getIntrinsicHeight();

		float usedScaleFactor = 1;

		if (frameWidth > originalImageWidth || frameHeight > originalImageHeight) {
			// If frame is bigger than image => Crop it, keep aspect ratio and
			// position it at the bottom and center horizontally

			final float fitHorizontallyScaleFactor = frameWidth / originalImageWidth;
			final float fitVerticallyScaleFactor = frameHeight / originalImageHeight;

			usedScaleFactor = Math.max(fitHorizontallyScaleFactor, fitVerticallyScaleFactor);
		}

		final float newImageWidth = originalImageWidth * usedScaleFactor;

		final Matrix matrix = getImageMatrix();
		// Replaces the old matrix completly
		matrix.setScale(usedScaleFactor, usedScaleFactor, 0, 0);
		matrix.postTranslate((frameWidth - newImageWidth) / 2, translatey > 0 ? -translatey * usedScaleFactor : 0);
		setImageMatrix(matrix);
		return super.setFrame(frameLeft, frameTop, frameRight, frameBottom);
	}

	private static Bitmap drawableToBitmap(final Drawable drawable) {
		if (drawable instanceof BitmapDrawable)
			return ((BitmapDrawable) drawable).getBitmap();
		else if (drawable instanceof TransitionDrawable) {
			final int layer_count = ((TransitionDrawable) drawable).getNumberOfLayers();
			for (int i = 0; i < layer_count; i++) {
				final Drawable layer = ((TransitionDrawable) drawable).getDrawable(i);
				if (layer instanceof BitmapDrawable) return ((BitmapDrawable) layer).getBitmap();
			}
		}
		final int w = drawable.getIntrinsicWidth(), h = drawable.getIntrinsicHeight();
		if (w <= 0 || h <= 0) return null;
		final Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	private static boolean inRange(final int value, final int target, final int threshold) {
		return Math.abs(value - target) <= threshold;
	}

	private static int limit(final int value, final int min, final int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	private static boolean nearlyWhite(final int color) {
		final int alpha = color >> 24 & 0xFF;
		final int red = color >> 16 & 0xFF;
		final int green = color >> 8 & 0xFF;
		final int blue = color & 0xFF;
		return (inRange(alpha, 0x00, 12) || inRange(alpha, 0xFF, 12)) && inRange(red, 0xFF, 12)
				&& inRange(green, 0xFF, 12) && inRange(blue, 0xFF, 12);
	}
}
