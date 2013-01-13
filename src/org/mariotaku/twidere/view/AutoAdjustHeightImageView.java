package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class AutoAdjustHeightImageView extends RoundCorneredImageView {

	public AutoAdjustHeightImageView(final Context context) {
		this(context, null);
	}

	public AutoAdjustHeightImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AutoAdjustHeightImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final Drawable d = getDrawable();

		if (d != null) {
			// ceil not round - avoid thin vertical gaps along the left/right
			// edges
			final int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / d.getIntrinsicWidth());
			setMeasuredDimension(width, height);
			setMaxHeight(width * 3);
		} else {
			setMeasuredDimension(width, width);
			// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
