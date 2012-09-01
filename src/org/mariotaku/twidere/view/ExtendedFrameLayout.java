package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ExtendedFrameLayout extends FrameLayout {

	private TouchInterceptor mTouchInterceptor;
	private OnSizeChangedListener mOnSizeChangedListener;

	public ExtendedFrameLayout(Context context) {
		super(context);
	}

	public ExtendedFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExtendedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mTouchInterceptor != null) {
			mTouchInterceptor.onInterceptTouchEvent(event);
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mTouchInterceptor != null) {
			mTouchInterceptor.onInterceptTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public void setOnSizeChangedListener(OnSizeChangedListener listener) {
		mOnSizeChangedListener = listener;
	}

	public void setTouchInterceptor(TouchInterceptor listener) {
		mTouchInterceptor = listener;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
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
