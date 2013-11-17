package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class LeftDrawerFrameLayout extends FrameLayout {

	private static final int[] COLORS = new int[] { 0, 0 };
	private static final PorterDuffXfermode DST_IN = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
	private final Shader mShader;
	private final Paint mPaint = new Paint();
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
		mShader = new LinearGradient(0, 0, 0, 0, COLORS, null, Shader.TileMode.CLAMP);
	}

	public void setClipEnabled(final boolean clipEnabled) {
		mClipEnabled = clipEnabled;
	}

	public void setPercentOpen(final float percentOpen) {
		if (mPercentOpen == percentOpen) return;
		mPercentOpen = percentOpen;
		invalidate();
	}

	public void setScrollScale(final float scrollScale) {
		mScrollScale = scrollScale;
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mClipEnabled && mPercentOpen > 0 && mPercentOpen < 1) {
			final int left = (int) Math.floor(getWidth() * (1 - (1 - mPercentOpen) * (1 - mScrollScale)));
			mPaint.setShader(mShader);
			mPaint.setXfermode(DST_IN);
			canvas.drawRect(left, getTop(), getRight(), getBottom(), mPaint);
		}
	}
}
