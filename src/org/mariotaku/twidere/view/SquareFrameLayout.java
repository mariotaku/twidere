package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class SquareFrameLayout extends FrameLayout {

	public SquareFrameLayout(final Context context) {
		this(context, null);
	}

	public SquareFrameLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SquareFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec), height = MeasureSpec.getSize(heightMeasureSpec);
		final ViewGroup.LayoutParams lp = getLayoutParams();
		if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT && lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
			super.onMeasure(heightMeasureSpec, heightMeasureSpec);
			setMeasuredDimension(height, height);
		} else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
			super.onMeasure(widthMeasureSpec, widthMeasureSpec);
			setMeasuredDimension(width, width);
		} else {
			if (width > height) {
				super.onMeasure(heightMeasureSpec, heightMeasureSpec);
				setMeasuredDimension(height, height);
			} else {
				super.onMeasure(widthMeasureSpec, widthMeasureSpec);
				setMeasuredDimension(width, width);
			}
		}
	}

}
