package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.view.View;

import org.mariotaku.twidere.Constants;

public class ViewHolder implements Constants {

	public View view;

	public ViewHolder(final View view) {
		if (view == null) throw new NullPointerException();
		this.view = view;
	}

	protected View findViewById(final int id) {
		return view.findViewById(id);
	}

	protected Context getContext() {
		return view.getContext();
	}

	protected String getString(final int resId, final Object... formatArgs) {
		return getContext().getString(resId, formatArgs);
	}

}
