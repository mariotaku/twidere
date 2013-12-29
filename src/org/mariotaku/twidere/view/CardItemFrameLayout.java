package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.ICardItemView;

public class CardItemFrameLayout extends ColorLabelFrameLayout implements ICardItemView {

	private final DrawingHelper mDrawingHelper;

	public CardItemFrameLayout(final Context context) {
		this(context, null);
	}

	public CardItemFrameLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.attr.cardItemViewStyle);
	}

	public CardItemFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mDrawingHelper = new DrawingHelper(this, context, attrs, defStyleAttr);
	}

	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev) {
		if (mDrawingHelper.isGap()) return false;
		if (mDrawingHelper.isOverflowIconClicked(ev)) {
			mDrawingHelper.handleOverflowTouchEvent(ev);
			return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public View getFakeOverflowButton() {
		return mDrawingHelper.getFakeOverflowButton();
	}

	@Override
	public boolean isGap() {
		return mDrawingHelper.isGap();
	}

	@Override
	public void setActivatedIndicator(final Drawable activatedIndicator) {
		mDrawingHelper.setActivatedIndicator(activatedIndicator);
	}

	@Override
	public void setIsGap(final boolean isGap) {
		mDrawingHelper.setIsGap(isGap);
	}

	@Override
	public void setItemBackground(final Drawable itemBackground) {
		mDrawingHelper.setItemBackground(itemBackground);
	}

	@Override
	public void setItemSelector(final Drawable itemSelector) {
		mDrawingHelper.setItemSelector(itemSelector);
	}

	@Override
	public void setOnOverflowIconClickListener(final OnOverflowIconClickListener listener) {
		mDrawingHelper.setOnOverflowIconClickListener(listener);
	}

	@Override
	public void setOverflowIcon(final Drawable overflowIcon) {
		mDrawingHelper.setOverflowIcon(overflowIcon);
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		if (mDrawingHelper.isGap()) {
			mDrawingHelper.drawGap(canvas);
		} else {
			mDrawingHelper.drawBackground(canvas);
			super.dispatchDraw(canvas);
			mDrawingHelper.drawOverflowIcon(canvas);
		}
		mDrawingHelper.drawSelector(canvas);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		mDrawingHelper.dispatchDrawableStateChanged();
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
		if (mDrawingHelper.isGap()) {
			setMeasuredDimension(measuredWidth, mDrawingHelper.getCardGapHeight());
		} else {
			final int measuredHeight = MeasureSpec.getSize(widthMeasureSpec);
			setMeasuredDimension(measuredWidth, measuredHeight);
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mDrawingHelper.dispatchOnSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || mDrawingHelper.verifyDrawable(who);
	}

}
