package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;

public class StatusImagePreviewItemView extends ImagePreviewView {

	public StatusImagePreviewItemView(final Context context) {
		super(context);
		//setScaleType(ScaleType.CENTER_CROP);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		//setScaleType(ScaleType.CENTER_CROP);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		//setScaleType(ScaleType.CENTER_CROP);
	}

	@Override
	public void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
//		post(new Runnable() {
//				public void run() {
//					final Bitmap b = getBitmap(getDrawable());
//					if (b == null) return;
//					final float ratio = b.getHeight() / b.getWidth();
//					setMinimumHeight((int) (w * ratio));
//				}
//			}
//		);
	}
	

	@Override
	protected boolean setFrame(final int frameLeft, final int frameTop, final int frameRight, final int frameBottom) {
		final boolean ret = super.setFrame(frameLeft, frameTop, frameRight, frameBottom);

		final Bitmap b = getBitmap(getDrawable());
		if (b == null) return ret;
		final float ratio = b.getHeight() / b.getWidth();
		setMinimumHeight((int) ((frameRight - frameLeft) * ratio));
		return ret;
	}

}
