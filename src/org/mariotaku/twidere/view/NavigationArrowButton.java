package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

import org.mariotaku.twidere.util.ThemeUtils;

public class NavigationArrowButton extends ImageButton {

	private final int mHighlightColor;
	private final Rect mRect;
	private boolean mIsDown;

	public NavigationArrowButton(final Context context) {
		this(context, null);
	}

	public NavigationArrowButton(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.imageButtonStyle);
	}

	public NavigationArrowButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mHighlightColor = ThemeUtils.getThemeColor(context);
		mRect = new Rect();
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
			setColorFilter(mHighlightColor, Mode.MULTIPLY);
		} else {
			clearColorFilter();
		}
	}
}
