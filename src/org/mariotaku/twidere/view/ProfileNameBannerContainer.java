package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

class ProfileNameBannerContainer2 extends ExtendedFrameLayout {

	public ProfileNameBannerContainer2(final Context context) {
		super(context);
	}

	public ProfileNameBannerContainer2(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ProfileNameBannerContainer2(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setBanner(final Bitmap banner) {
		setBackgroundDrawable(banner != null ? new BitmapDrawable(getResources(), banner) : null);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(parentWidth, parentWidth / 2);
		final ViewGroup.LayoutParams lp = getLayoutParams();
		lp.width = parentWidth;
		lp.height = parentWidth / 2;
		setLayoutParams(lp);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
