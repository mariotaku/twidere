package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.mariotaku.twidere.util.Utils;

public class AutoAdjustHeightImageView extends ImageView {

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
		final Bitmap b = d instanceof BitmapDrawable ? ((BitmapDrawable) d).getBitmap() : null;

		if (b != null) {
			final int height = (int) Math.floor((float) width * (float) b.getHeight() / b.getWidth());
			setMeasuredDimension(width, height);
		} else {
			setMeasuredDimension(width, width);
			// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

}
