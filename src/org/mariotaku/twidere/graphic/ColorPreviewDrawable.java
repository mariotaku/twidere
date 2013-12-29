package org.mariotaku.twidere.graphic;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class ColorPreviewDrawable extends AlphaPatternDrawable {

	private final int mColor;
	private final Paint mPaint;
	private final float[] mPoints;

	public ColorPreviewDrawable(final int alphaPatternSize, final int color) {
		super(alphaPatternSize);
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(2.0f);
		mColor = color;
		mPoints = new float[16];
	}

	@Override
	public void draw(final Canvas canvas) {
		super.draw(canvas);
		canvas.drawColor(mColor);
		canvas.drawLines(mPoints, mPaint);
	}

	@Override
	protected void onBoundsChange(final Rect bounds) {
		super.onBoundsChange(bounds);
		mPoints[0] = bounds.top;
		mPoints[1] = bounds.top;
		mPoints[2] = bounds.right;
		mPoints[3] = bounds.top;
		mPoints[4] = bounds.top;
		mPoints[5] = bounds.top;
		mPoints[6] = bounds.top;
		mPoints[7] = bounds.bottom;
		mPoints[8] = bounds.right;
		mPoints[9] = bounds.top;
		mPoints[10] = bounds.right;
		mPoints[11] = bounds.bottom;
		mPoints[12] = bounds.top;
		mPoints[13] = bounds.bottom;
		mPoints[14] = bounds.right;
		mPoints[15] = bounds.bottom;
	}
}
