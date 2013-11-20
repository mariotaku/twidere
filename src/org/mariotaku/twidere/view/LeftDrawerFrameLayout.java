package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LeftDrawerFrameLayout extends FrameLayout {

	private final Paint mClipPaint = new Paint();
	private float mScrollScale, mPercentOpen;
	private boolean mClipEnabled;

	public LeftDrawerFrameLayout(final Context context) {
		this(context, null);
	}

	public LeftDrawerFrameLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LeftDrawerFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		mClipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
	}

	@Override
	public boolean hasOverlappingRendering() {
		return mClipEnabled;
	}

	public void setClipEnabled(final boolean clipEnabled) {
		mClipEnabled = clipEnabled;
	}

	public void setPercentOpen(final float percentOpen) {
		if (mPercentOpen == percentOpen) return;
		mPercentOpen = percentOpen;
		setAlpha(mPercentOpen);
		invalidate();
	}

	public void setScrollScale(final float scrollScale) {
		mScrollScale = scrollScale;
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mClipEnabled && mPercentOpen > 0 && mPercentOpen < 1) {
			final int left = Math.round(getWidth() * (1 - (1 - mPercentOpen) * (1 - mScrollScale)));
			canvas.drawRect(left, getTop(), getRight(), getBottom(), mClipPaint);
		}
	}
}
