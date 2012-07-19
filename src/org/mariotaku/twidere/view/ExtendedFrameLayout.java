package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ExtendedFrameLayout extends FrameLayout {

	private TouchInterceptor mListener;

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
		if (mListener != null) {
			mListener.onInterceptTouchEvent(event);
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mListener != null) {
			mListener.onInterceptTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public void setTouchInterceptor(TouchInterceptor listener) {
		mListener = listener;
	}

	public interface TouchInterceptor {
		public void onInterceptTouchEvent(MotionEvent event);
	}

}
