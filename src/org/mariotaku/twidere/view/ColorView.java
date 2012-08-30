package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {

	private int mColor = Color.TRANSPARENT;

	public ColorView(Context context) {
		this(context, null);
	}

	public ColorView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setColor(int color) {
		mColor = color;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawColor(mColor);
	}

}
