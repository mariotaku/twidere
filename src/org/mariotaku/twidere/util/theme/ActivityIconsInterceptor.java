package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.mariotaku.twidere.content.res.iface.IThemedResources.DrawableInterceptor;
import org.mariotaku.twidere.graphic.icon.TwidereIcon;
import org.mariotaku.twidere.util.ThemeUtils;

public class ActivityIconsInterceptor implements DrawableInterceptor {

	private static final SparseArray<ActivityIconsInterceptor.IconSpec> sIconMap = new SparseArray<ActivityIconsInterceptor.IconSpec>();

	static {
		sIconMap.put(R.drawable.ic_iconic_twidere, new IconSpec(TwidereIcon.TWIDERE, 0.875f));
	}

	private static int MENU_ICON_SIZE_DP = 48;
	private final Context mContext;
	private final int mIconSize;
	private final int mIconColor;
	private final float mDensity;

	public ActivityIconsInterceptor(final Context context, final DisplayMetrics dm, final int overrideThemeRes) {
		mContext = context;
		if (overrideThemeRes != 0) {
			mIconColor = ThemeUtils.getActionIconColor(overrideThemeRes);
		} else if (context instanceof TwidereContextThemeWrapper) {
			final int resId = ((TwidereContextThemeWrapper) context).getThemeResourceId();
			mIconColor = ThemeUtils.getActionIconColor(resId);
		} else {
			mIconColor = ThemeUtils.getActionIconColor(context);
		}
		mDensity = dm.density;
		mIconSize = Math.round(mDensity * MENU_ICON_SIZE_DP);
	}

	@Override
	public Drawable getDrawable(final Resources res, final int resId) {
		final ActivityIconsInterceptor.IconSpec spec = sIconMap.get(resId, null);
		if (spec == null) return null;
		final IconicFontDrawable drawable = new IconicFontDrawable(mContext, spec.icon);
		drawable.setIconPadding(Math.round(mIconSize * (1 - spec.contentFactor)) / 2);
		drawable.setIntrinsicWidth(mIconSize);
		drawable.setIntrinsicHeight(mIconSize);
		drawable.setIconColor(mIconColor);
		return drawable;
	}

	private static class IconSpec {
		private final Icon icon;
		private final float contentFactor;

		IconSpec(final Icon icon, final float contentFactor) {
			this.icon = icon;
			this.contentFactor = contentFactor;
		}
	}

}