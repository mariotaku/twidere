package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ActionBarHomeAsUpIndicator extends ImageView {

	public ActionBarHomeAsUpIndicator(final Context context) {
		this(context, null);
	}

	public ActionBarHomeAsUpIndicator(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarHomeAsUpIndicator(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.homeAsUpIndicator },
				android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
		final Drawable d = a.getDrawable(0);
		a.recycle();
		setImageDrawable(d);
		setScaleType(ScaleType.CENTER);
	}

}
