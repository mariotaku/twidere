package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;

public class AssetsImageGetter implements ImageGetter {

	private final Resources res;
	private final Context context;

	public AssetsImageGetter(final Context context) {
		this.context = context;
		res = context.getResources();
	}

	@Override
	public Drawable getDrawable(final String source) {
		if (source == null) return null;
		final int resId = res.getIdentifier(source, "drawable", context.getPackageName());
		if (resId < 0) return null;
		final Drawable d;
		try {
			d = res.getDrawable(resId);
		} catch (final Resources.NotFoundException e) {
			return null;
		}
		d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		return d;
	}
}
