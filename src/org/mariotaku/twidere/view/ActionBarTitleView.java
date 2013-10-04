package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

public class ActionBarTitleView extends TextView {

	public ActionBarTitleView(final Context context) {
		this(context, null);
	}

	public ActionBarTitleView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarTitleView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(null, new int[] { android.R.attr.titleTextStyle },
				android.R.attr.actionBarStyle, android.R.style.Widget_Holo_ActionBar);
		final int textAppearance = a.getResourceId(0, android.R.style.Widget_Holo_TextView);
		a.recycle();
		setTextAppearance(context, textAppearance);
	}

}
