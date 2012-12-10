package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class ProfileNameBannerContainer extends ExtendedFrameLayout {

	public ProfileNameBannerContainer(final Context context) {
		super(context);
	}

	public ProfileNameBannerContainer(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ProfileNameBannerContainer(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
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