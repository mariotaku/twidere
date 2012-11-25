package org.mariotaku.twidere.view;

import org.mariotaku.twidere.util.InvalidateProgressBarRunnable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class HoloProgressBar extends ProgressBar {

	public HoloProgressBar(final Context context) {
		super(context);
		init();
	}

	public HoloProgressBar(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HoloProgressBar(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	void init() {
		post(new InvalidateProgressBarRunnable(this));
	}

}
