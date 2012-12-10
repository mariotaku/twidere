package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.graphic.AlphaPatternDrawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class StatusImagePreviewItemView extends RoundCorneredImageView {

	private final float mDensity;

	public StatusImagePreviewItemView(final Context context) {
		this(context, null);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mDensity = getResources().getDisplayMetrics().density;
	}

	@Override
	protected boolean setFrame(final int frameLeft, final int frameTop, final int frameRight, final int frameBottom) {
		final boolean ret = super.setFrame(frameLeft, frameTop, frameRight, frameBottom);

		final Bitmap bitmap = ImagePreviewView.getBitmap(getDrawable());
		if (bitmap != null) {
			setBackgroundDrawable(new AlphaPatternDrawable((int) (mDensity * 16)));
		} else {
			setBackgroundResource(R.drawable.image_preview_fallback_large);
		}
		return ret;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final Drawable d = getDrawable();

		if (d != null) {
			// ceil not round - avoid thin vertical gaps along the left/right
			// edges
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = (int) Math
					.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());
			setMeasuredDimension(width, height);
		} else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
