package org.mariotaku.twidere.util;

import android.os.Build;
import android.view.View;
import android.widget.ProgressBar;

public final class InvalidateProgressBarRunnable implements Runnable {

	private final View view;

	public InvalidateProgressBarRunnable(final View view) {
		this.view = view;
	}

	@Override
	public void run() {
		if (!(view instanceof ProgressBar) || Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
		if (((ProgressBar) view).isIndeterminate()) {
			view.invalidate();
		}
		view.postDelayed(this, 16);
	}

}