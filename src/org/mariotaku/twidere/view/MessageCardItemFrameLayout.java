package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import org.mariotaku.twidere.R;
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
			final Drawable layer = d instanceof LayerDrawable ? ((LayerDrawable) d)
					.findDrawableByLayerId(R.id.card_item_selector) : null;
			final Drawable current = layer != null ? layer.getCurrent() : d.getCurrent();
			if (current instanceof TransitionDrawable && ArrayUtils.contains(state, android.R.attr.state_pressed)) {
				((TransitionDrawable) current).startTransition(ViewConfiguration.getLongPressTimeout());
			}
		}
	}
}
