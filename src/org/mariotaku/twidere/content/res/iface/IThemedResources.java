package org.mariotaku.twidere.content.res.iface;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import org.mariotaku.twidere.util.theme.ActionIconsInterceptor;
import org.mariotaku.twidere.util.theme.ActivityIconsInterceptor;
import org.mariotaku.twidere.util.theme.WhiteDrawableInterceptor;

import java.util.ArrayList;

public interface IThemedResources {

	public static final String RESOURCES_LOGTAG = "Twidere.Resources";

	public void addDrawableInterceptor(final DrawableInterceptor interceptor);

	public static interface DrawableInterceptor {
		public Drawable getDrawable(int id);

		public boolean shouldIntercept(int id);
	}

	public static final class Helper {

		private final ArrayList<DrawableInterceptor> mDrawableInterceptors = new ArrayList<DrawableInterceptor>();

		public Helper(final Resources res, final Context context) {
			final DisplayMetrics dm = res.getDisplayMetrics();
			addDrawableInterceptor(new ActionIconsInterceptor(context, dm));
			addDrawableInterceptor(new ActivityIconsInterceptor(context, dm));
			addDrawableInterceptor(new WhiteDrawableInterceptor(res));
		}

		public void addDrawableInterceptor(final DrawableInterceptor interceptor) {
			mDrawableInterceptors.add(interceptor);
		}

		public Drawable getDrawable(final int id) throws NotFoundException {
			for (final DrawableInterceptor interceptor : mDrawableInterceptors) {
				if (interceptor.shouldIntercept(id)) return interceptor.getDrawable(id);
			}
			return null;
		}
	}

}
