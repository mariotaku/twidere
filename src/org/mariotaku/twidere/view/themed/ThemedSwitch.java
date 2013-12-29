package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.util.AttributeSet;

import com.negusoft.holoaccent.R;
import com.negusoft.holoaccent.widget.AccentSwitch;

import org.mariotaku.twidere.util.ThemeUtils;

public class ThemedSwitch extends AccentSwitch {

	public ThemedSwitch(final Context context) {
		this(context, null);
	}

	public ThemedSwitch(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.attr.accentSwitchStyle);
	}

	public ThemedSwitch(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode()) {
			setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
			setHighlightColor(ThemeUtils.getUserHighlightColor(context));
			setTypeface(ThemeUtils.getUserTypeface(context, getTypeface()));
		}
	}

}
