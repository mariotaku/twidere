package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;

public class TwidereMenuBar extends MenuBar {

	public TwidereMenuBar(final Context context) {
		this(context, null);
	}

	public TwidereMenuBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (!hasBackground(attrs)) {
			ViewAccessor.setBackground(this, ThemeUtils.getActionBarSplitBackground(context, true));
		}
	}

	private static boolean hasBackground(final AttributeSet attrs) {
		final int count = attrs.getAttributeCount();
		for (int i = 0; i < count; i++) {
			if (attrs.getAttributeNameResource(i) == android.R.attr.background) return true;
		}
		return false;
	}

}
