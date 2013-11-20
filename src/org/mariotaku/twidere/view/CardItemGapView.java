package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ArrayUtils;

public class CardItemGapView extends TextView {

	public CardItemGapView(final Context context) {
		this(context, null);
	}

	public CardItemGapView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardItemGapView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		return false;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		final Drawable bg = getBackground();
		if (bg != null && bg.isStateful()) {
			final int[] state = getDrawableState();
			// bg.setState(state);
			final Drawable layer = bg instanceof LayerDrawable ? ((LayerDrawable) bg)
					.findDrawableByLayerId(R.id.card_item_selector) : null;
			final Drawable current = layer != null ? layer.getCurrent() : bg.getCurrent();
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
