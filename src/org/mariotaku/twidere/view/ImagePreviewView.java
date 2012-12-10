package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.graphic.AlphaPatternDrawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;

public class ImagePreviewView extends RoundCorneredImageView {

	private final float mDensity;

	public ImagePreviewView(final Context context) {
		this(context, null);
	}

	public ImagePreviewView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImagePreviewView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
		mDensity = getResources().getDisplayMetrics().density;
	}

	@Override
	protected boolean setFrame(final int frameLeft, final int frameTop, final int frameRight, final int frameBottom) {
		final float frameWidth = frameRight - frameLeft;
		final float frameHeight = frameBottom - frameTop;

		final Drawable drawable = getDrawable();

		int translatey = 0;
		final Bitmap bitmap = getBitmap(drawable);
		if (bitmap != null) {
			setBackgroundDrawable(new AlphaPatternDrawable((int) (mDensity * 16)));
			final int bw = bitmap.getWidth();
			final int bh = bitmap.getHeight();
			final float ratio = bh / bw;
			if (frameWidth * ratio > frameHeight) {
				final int stepw = limit(bw / 32, 1, bw);
				final int steph = limit(bh / 96, 1, bh);
				for (int x = 0, y = 0; x < bw && y < bh / 2; x += stepw, y += steph) {
					if (!isBorder(bitmap.getPixel(x, y))) {
						translatey = y;
						break;
					}
				}
			}
		} else {
			setBackgroundResource(R.drawable.image_preview_fallback_large);
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

	private static boolean inRange(final int value, final int min, final int max, final int threshold) {
		return Math.abs(value - max) <= threshold || Math.abs(value - min) <= threshold;
	}

	private static boolean isBorder(final int color) {
		final int alpha = color >> 24 & 0xFF;
		final int red = color >> 16 & 0xFF;
		final int green = color >> 8 & 0xFF;
		final int blue = color & 0xFF;
		return inRange(alpha, 0x00, 0xFF, 8) && inRange(red, 0x00, 0xFF, 8) && inRange(green, 0x00, 0xFF, 8)
				&& inRange(blue, 0x00, 0xFF, 8);
	}

	private static int limit(final int value, final int min, final int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static Bitmap getBitmap(final Drawable drawable) {
		if (drawable instanceof NinePatchDrawable) return null;
		if (drawable instanceof BitmapDrawable)
			return ((BitmapDrawable) drawable).getBitmap();
		else if (drawable instanceof TransitionDrawable) {
			final int layer_count = ((TransitionDrawable) drawable).getNumberOfLayers();
			for (int i = 0; i < layer_count; i++) {
				final Drawable layer = ((TransitionDrawable) drawable).getDrawable(i);
				if (layer instanceof BitmapDrawable) return ((BitmapDrawable) layer).getBitmap();
			}
		}
		return null;
	}
}
