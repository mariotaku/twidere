package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.InputDevice;
import android.view.MotionEvent;

public class MotionEventAccessor {

	public static float getAxisValue(final MotionEvent event, final int axis) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			return GetAxisValueAccessorHoneyComb.getAxisValue(event, axis);
		return 0;
	}

	public static int getSource(final MotionEvent event) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			return GetSourceAccessorGingerbread.getSource(event);
		return InputDevice.SOURCE_TOUCHSCREEN;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private static class GetAxisValueAccessorHoneyComb {
		private static float getAxisValue(final MotionEvent event, final int axis) {
			return event.getAxisValue(axis);
		}
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static class GetSourceAccessorGingerbread {
		private static int getSource(final MotionEvent event) {
			return event.getSource();
		}
	}
}
