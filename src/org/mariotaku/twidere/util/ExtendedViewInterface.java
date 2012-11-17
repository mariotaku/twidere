package org.mariotaku.twidere.util;

import android.view.MotionEvent;
import android.view.View;

public interface ExtendedViewInterface {

	public void setOnSizeChangedListener(final OnSizeChangedListener listener);

	public void setTouchInterceptor(final TouchInterceptor listener);
	
	public static interface OnSizeChangedListener {
		void onSizeChanged(View view, int w, int h, int oldw, int oldh);
	}

	public static interface TouchInterceptor {

		boolean onInterceptTouchEvent(View view, MotionEvent event);

		boolean onTouchEvent(View view, MotionEvent event);

	}

}
