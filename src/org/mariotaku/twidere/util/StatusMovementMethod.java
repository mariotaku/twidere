package org.mariotaku.twidere.util;

import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public class StatusMovementMethod extends LinkMovementMethod {

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
		return super.onTouchEvent(widget, buffer, event);
	}

}
