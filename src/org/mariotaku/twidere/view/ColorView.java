package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {

	private final Paint mPaint;

	public ColorView(final Context context) {
		this(context, null);
	}

	public ColorView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mPaint = new Paint();
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.color });
		setColor(a.getColor(0, Color.TRANSPARENT));
	}

	public int getColor() {
		return mPaint.getColor();
	}

	public void setColor(final int color) {
		mPaint.setColor(color);
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		final int w = getWidth(), h = getHeight();
		canvas.drawRect(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom(), mPaint);
	}

}
