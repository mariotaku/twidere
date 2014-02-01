package org.mariotaku.twidere.content.res;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.content.res.iface.IThemedResources;

public class TwidereResources extends Resources implements IThemedResources {

	private final Helper mHelper;

	public TwidereResources(final Context context, final Resources res) {
		super(res.getAssets(), res.getDisplayMetrics(), res.getConfiguration());
		mHelper = new Helper(this, context);
	}

	@Override
	public void addDrawableInterceptor(final DrawableInterceptor interceptor) {
		mHelper.addDrawableInterceptor(interceptor);
	}

	@Override
	public Drawable getDrawable(final int id) throws NotFoundException {
		final Drawable d = mHelper.getDrawable(id);
		if (d != null) return d;
		return super.getDrawable(id);
	}

}
