package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class MessageCardItemFrameLayout extends FrameLayout {

	public MessageCardItemFrameLayout(final Context context) {
		this(context, null);
	}

	public MessageCardItemFrameLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MessageCardItemFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		ThemeUtils.applyThemeAlphaToDrawable(context, getBackground());
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		final Drawable d = getBackground();
		if (d != null && d.isStateful()) {
			final int[] state = getDrawableState();
			d.setState(state);
			final Drawable current = d.getCurrent();
			if (current instanceof TransitionDrawable) {
				final TransitionDrawable td = (TransitionDrawable) current;
				if (ArrayUtils.contains(state, android.R.attr.state_pressed)) {
					td.startTransition(ViewConfiguration.getLongPressTimeout());
				} else {
					td.resetTransition();
				}
			}
		}
	}
}
