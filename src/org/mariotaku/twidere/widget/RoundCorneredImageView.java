package org.mariotaku.twidere.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundCorneredImageView extends ImageView {

	private Path mPath = new Path();

	public RoundCorneredImageView(Context context) {
		super(context);
		createPath();
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		createPath();
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		createPath();
	}

	@Override
	public void onDraw(Canvas canvas) {
		canvas.clipPath(mPath);
		super.onDraw(canvas);
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		createPath();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private void createPath() {
		float density = getResources().getDisplayMetrics().density;
		mPath.reset();
		mPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), 4 * density, 4 * density, Path.Direction.CW);
	}
}
