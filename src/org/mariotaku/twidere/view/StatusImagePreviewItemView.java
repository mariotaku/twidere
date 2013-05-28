package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;

import static org.mariotaku.twidere.util.Utils.getBitmap;

public class StatusImagePreviewItemView extends ClickableImageView {
	
	public StatusImagePreviewItemView(final Context context) {
		this(context, null);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setAdjustViewBounds(true);
		setScaleType(ScaleType.FIT_CENTER);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final Bitmap b = getBitmap(getDrawable());
		if (b != null) {
			final int height = (int) Math.floor((float) width * (float) b.getHeight() / b.getWidth());
			setMeasuredDimension(width, height);
			setMinimumHeight(height);
		} else {
			setMeasuredDimension(width, width);
			// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
