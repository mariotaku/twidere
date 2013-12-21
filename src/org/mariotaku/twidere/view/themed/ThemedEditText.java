package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import org.mariotaku.twidere.util.ThemeUtils;

public class ThemedEditText extends EditText {

	public ThemedEditText(final Context context) {
		this(context, null);
	}

	public ThemedEditText(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.editTextStyle);
	}

	public ThemedEditText(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode()) {
			setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
			setHighlightColor(ThemeUtils.getUserHighlightColor(context));
		}
	}

}
