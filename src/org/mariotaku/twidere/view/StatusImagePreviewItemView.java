package org.mariotaku.twidere.view;

import static org.mariotaku.twidere.util.Utils.getBitmap;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

public class StatusImagePreviewItemView extends AutoAdjustHeightImageView {

	public StatusImagePreviewItemView(final Context context) {
		super(context);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public StatusImagePreviewItemView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean setFrame(final int frameLeft, final int frameTop, final int frameRight, final int frameBottom) {
		final boolean ret = super.setFrame(frameLeft, frameTop, frameRight, frameBottom);

		final Bitmap bitmap = getBitmap(getDrawable());
		if (bitmap != null) {
			setBackgroundDrawable(null);
		} else {
			setBackgroundResource(R.drawable.image_preview_fallback_large);
		}
		return ret;
	}
}
