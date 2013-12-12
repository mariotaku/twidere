package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class ActionBarSplitThemedContainer extends FrameLayout {

	public ActionBarSplitThemedContainer(final Context context) {
		this(context, null);
	}

	public ActionBarSplitThemedContainer(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionBarSplitThemedContainer(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.layout });
		final int resId = a.getResourceId(0, 0);
		a.recycle();
		if (resId == 0) throw new IllegalArgumentException("You must specify a layout resource in layout XML file.");
		final View view = LayoutInflater.from(getThemedContext(context)).inflate(resId, this, false);
		ViewAccessor.setBackground(view, ThemeUtils.getActionBarSplitBackground(context, false));
		addView(view);
	}

	private static Context getThemedContext(final Context context) {
		return ThemeUtils.getActionBarContext(context);
	}

}
