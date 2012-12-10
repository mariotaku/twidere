package org.mariotaku.twidere.view;

import android.view.MotionEvent;
import android.view.ViewGroup;

public interface ExtendedViewGroupInterface extends ExtendedViewInterface {

	public void setTouchInterceptor(final TouchInterceptor listener);

	public static interface TouchInterceptor {

		void dispatchTouchEvent(ViewGroup view, MotionEvent event);

		boolean onInterceptTouchEvent(ViewGroup view, MotionEvent event);

		boolean onTouchEvent(ViewGroup view, MotionEvent event);

	}

}
