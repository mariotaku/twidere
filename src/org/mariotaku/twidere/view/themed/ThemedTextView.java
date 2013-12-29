package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import org.mariotaku.twidere.util.ThemeUtils;

public class ThemedTextView extends TextView {

	public ThemedTextView(final Context context) {
		this(context, null);
	}

	public ThemedTextView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public ThemedTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode()) {
			setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
			setHighlightColor(ThemeUtils.getUserHighlightColor(context));
			setTypeface(ThemeUtils.getUserTypeface(context, getTypeface()));
		}
	}

}
