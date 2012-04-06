package org.mariotaku.twidere.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundCorneredImageView extends ImageView {

	public RoundCorneredImageView(Context context) {
		super(context);
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onDraw(Canvas canvas) {
		Path path = new Path();
		float density = getResources().getDisplayMetrics().density;
		path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), 4 * density, 4 * density,
				Path.Direction.CW);
		canvas.clipPath(path);
		super.onDraw(canvas);
	}
}
