package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.mariotaku.twidere.util.ThemeUtils;

public class ListMenuOverflowButton extends ImageView {

	private final int mHighlightColor;
	private final Rect mRect;
	private boolean mIsDown;

	public ListMenuOverflowButton(final Context context) {
		this(context, null);
	}

	public ListMenuOverflowButton(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ListMenuOverflowButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.CENTER_INSIDE);
		mHighlightColor = ThemeUtils.getThemeColor(context);
		mRect = new Rect();
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.src });
		if (a.getDrawable(0) == null) {
			setImageDrawable(ThemeUtils.getListMenuOverflowButtonDrawable(context));
		}
		a.recycle();
	}

	@Override
	public boolean onTouchEvent(final MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mRect.set(getLeft(), getTop(), getRight(), getBottom());
				mIsDown = true;
				updateColorFilter();
				break;
			case MotionEvent.ACTION_MOVE:
				if (mRect.contains(getLeft() + (int) e.getX(), getTop() + (int) e.getY())) {
					break;
				}
				if (mIsDown) {
					mIsDown = false;
					updateColorFilter();
				}
				break;
			default:
				mIsDown = false;
				updateColorFilter();
				break;
		}
		return super.onTouchEvent(e);
	}

	private void updateColorFilter() {
		if (mIsDown && isClickable() && isEnabled()) {
			setColorFilter(mHighlightColor, Mode.SRC_ATOP);
		} else {
			clearColorFilter();
		}
	}
}
