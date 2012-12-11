package org.mariotaku.twidere.view.iface;

import android.view.View;

public interface IExtendedView {

	public void setOnSizeChangedListener(final OnSizeChangedListener listener);

	public static interface OnSizeChangedListener {
		void onSizeChanged(View view, int w, int h, int oldw, int oldh);
	}

}
