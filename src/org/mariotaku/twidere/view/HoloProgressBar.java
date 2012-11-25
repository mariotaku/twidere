package org.mariotaku.twidere.view;

import org.mariotaku.twidere.util.InvalidateProgressBarRunnable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class HoloProgressBar extends ProgressBar {

	public HoloProgressBar(Context context) {
		super(context);
		init();
	}

	public HoloProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HoloProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	void init() {
		post(new InvalidateProgressBarRunnable(this));
	}

}
