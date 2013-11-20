package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewConfiguration;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ThemeUtils;

public class CardItemLinearLayout extends ColorLabelLinearLayout {

	private Drawable mItemSelector;

	public CardItemLinearLayout(final Context context) {
		this(context, null);
	}

	public CardItemLinearLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardItemLinearLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		if (isInEditMode()) return;
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { R.attr.cardItemSelector });
		setItemSelector(a.getDrawable(0));
		ThemeUtils.applyThemeAlphaToDrawable(context, getBackground());
		a.recycle();
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mItemSelector != null) {
			mItemSelector.jumpToCurrentState();
		}
	}

	public void setItemSelector(final Drawable drawable) {
		if (mItemSelector != null) {
			unscheduleDrawable(mItemSelector);
			mItemSelector.setCallback(null);
		}
		mItemSelector = drawable;
		setWillNotDraw(drawable != null);
		if (drawable != null) {
			if (drawable.isStateful()) {
				drawable.setState(getDrawableState());
			}
			drawable.setCallback(this);
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mItemSelector != null && mItemSelector.isStateful()) {
			final int[] state = getDrawableState();
			mItemSelector.setState(state);
			final Drawable layer = mItemSelector instanceof LayerDrawable ? ((LayerDrawable) mItemSelector)
					.findDrawableByLayerId(R.id.card_item_selector) : null;
			final Drawable current = layer != null ? layer.getCurrent() : mItemSelector.getCurrent();
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

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mItemSelector != null) {
			mItemSelector.draw(canvas);
		}
		super.onDraw(canvas);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mItemSelector != null) {
			mItemSelector.setBounds(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
		}
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || who == mItemSelector;
	}

}
