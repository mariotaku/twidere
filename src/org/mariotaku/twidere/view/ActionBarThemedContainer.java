package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import org.mariotaku.twidere.util.ThemeUtils;

public class ActionBarThemedContainer extends FrameLayout {

	public ActionBarThemedContainer(final Context context) {
		this(context, null);
	}

	public ActionBarThemedContainer(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarThemedContainer(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.layout });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) throw new IllegalArgumentException("You must specify a layout resource in layout XML file.");
		inflate(getThemedContext(context), resId, this);
	}

	private static Context getThemedContext(final Context context) {
		return ThemeUtils.getActionBarContext(context);
	}

}
