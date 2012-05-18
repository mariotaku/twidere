package org.mariotaku.twidere.widget;

import static org.mariotaku.twidere.util.Utils.setViewLayerType;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class RoundCorneredImageView extends ImageView {

	private Path mPath = new Path();

	public RoundCorneredImageView(Context context) {
		this(context, null);
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundCorneredImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
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

	private void init() {
		setViewLayerType(this, View.LAYER_TYPE_SOFTWARE, new Paint());
	}
}
