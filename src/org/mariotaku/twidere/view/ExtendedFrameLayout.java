package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ExtendedFrameLayout extends FrameLayout {

	private TouchInterceptor mTouchInterceptor;
	private OnSizeChangedListener mOnSizeChangedListener;

	public ExtendedFrameLayout(final Context context) {
		super(context);
	}

	public ExtendedFrameLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public ExtendedFrameLayout(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		if (mTouchInterceptor != null) {
			mTouchInterceptor.onInterceptTouchEvent(event);
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (mTouchInterceptor != null) {
			mTouchInterceptor.onInterceptTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public void setOnSizeChangedListener(final OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	public void setTouchInterceptor(final TouchInterceptor listener) {
		mTouchInterceptor = listener;
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onSizeChanged(this, w, h, oldw, oldh);
		}
	}

	public interface OnSizeChangedListener {
		void onSizeChanged(FrameLayout view, int w, int h, int oldw, int oldh);
	}

	public interface TouchInterceptor {
		void onInterceptTouchEvent(MotionEvent event);
	}

}
