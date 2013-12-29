package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import org.mariotaku.twidere.util.ThemeUtils;

public class ThemedRadioButton extends RadioButton {

	public ThemedRadioButton(final Context context) {
		this(context, null);
	}

	public ThemedRadioButton(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.checkboxStyle);
	}

	public ThemedRadioButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode()) {
			setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
			setHighlightColor(ThemeUtils.getUserHighlightColor(context));
			setTypeface(ThemeUtils.getUserTypeface(context, getTypeface()));
		}
	}

}
