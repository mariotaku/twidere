package org.mariotaku.twidere.content.res.iface;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.mariotaku.twidere.graphic.icon.TwidereIcon;
import org.mariotaku.twidere.util.ThemeUtils;

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
			addDrawableInterceptor(new ActionIconInterceptor(context, res.getDisplayMetrics()));
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

		private static class ActionIconInterceptor implements DrawableInterceptor {

			private static final SparseArray<IconSpec> sIconMap = new SparseArray<IconSpec>();

			static {
				sIconMap.put(R.drawable.ic_iconic_action_twidere, new IconSpec(TwidereIcon.TWIDERE, 0.875f));
				sIconMap.put(R.drawable.ic_iconic_action_web, new IconSpec(TwidereIcon.WEB, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_compose, new IconSpec(TwidereIcon.COMPOSE, 0.875f));
				sIconMap.put(R.drawable.ic_iconic_action_color_palette, new IconSpec(TwidereIcon.COLOR_PALETTE, 0.875f));
				sIconMap.put(R.drawable.ic_iconic_action_camera, new IconSpec(TwidereIcon.CAMERA, 0.9375f));
				sIconMap.put(R.drawable.ic_iconic_action_new_message, new IconSpec(TwidereIcon.NEW_MESSAGE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_server, new IconSpec(TwidereIcon.SERVER, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_gallery, new IconSpec(TwidereIcon.GALLERY, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_save, new IconSpec(TwidereIcon.SAVE, 0.6875f));
				sIconMap.put(R.drawable.ic_iconic_action_star, new IconSpec(TwidereIcon.STAR, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_search, new IconSpec(TwidereIcon.SEARCH, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_retweet, new IconSpec(TwidereIcon.RETWEET, 0.875f));
				sIconMap.put(R.drawable.ic_iconic_action_reply, new IconSpec(TwidereIcon.REPLY, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_delete, new IconSpec(TwidereIcon.DELETE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_add, new IconSpec(TwidereIcon.ADD, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_share, new IconSpec(TwidereIcon.SHARE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_inbox, new IconSpec(TwidereIcon.INBOX, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_outbox, new IconSpec(TwidereIcon.OUTBOX, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_copy, new IconSpec(TwidereIcon.COPY, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_translate, new IconSpec(TwidereIcon.TRANSLATE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_profile, new IconSpec(TwidereIcon.USER, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_accounts, new IconSpec(TwidereIcon.USERS, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_send, new IconSpec(TwidereIcon.SEND, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_edit, new IconSpec(TwidereIcon.EDIT, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_accept, new IconSpec(TwidereIcon.ACCEPT, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_cancel, new IconSpec(TwidereIcon.CANCEL, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_preferences, new IconSpec(TwidereIcon.SETTINGS, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_mylocation, new IconSpec(TwidereIcon.LOCATION, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_mute, new IconSpec(TwidereIcon.MUTE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_quote, new IconSpec(TwidereIcon.QUOTE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_message, new IconSpec(TwidereIcon.MESSAGE, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_twitter, new IconSpec(TwidereIcon.TWITTER, 0.75f));
				sIconMap.put(R.drawable.ic_iconic_action_home, new IconSpec(TwidereIcon.HOME, 0.9375f));
				sIconMap.put(R.drawable.ic_iconic_action_mention, new IconSpec(TwidereIcon.AT, 0.75f));

				sIconMap.put(R.drawable.ic_iconic_action_mark, new IconSpec(TwidereIcon.ACCEPT, 0.75f));
			}

			private static int MENU_ICON_SIZE_DP = 32;
			private final Context mContext;
			private final int mIconSize;
			private final int mIconColor;
			private final float mDensity;

			ActionIconInterceptor(final Context context, final DisplayMetrics dm) {
				mContext = context;
				if (context instanceof TwidereContextThemeWrapper) {
					final int resId = ((TwidereContextThemeWrapper) context).getThemeResourceId();
					mIconColor = ThemeUtils.getActionIconColor(resId);
				} else {
					mIconColor = ThemeUtils.getActionIconColor(context);
				}
				mDensity = dm.density;
				mIconSize = Math.round(mDensity * MENU_ICON_SIZE_DP);
			}

			@Override
			public Drawable getDrawable(final int id) {
				final IconSpec spec = sIconMap.get(id, null);
				if (spec == null) return null;
				final IconicFontDrawable drawable = new IconicFontDrawable(mContext, spec.icon);
				drawable.setIconPadding(Math.round(mIconSize * (1 - spec.contentFactor)) / 2);
				drawable.setIntrinsicWidth(mIconSize);
				drawable.setIntrinsicHeight(mIconSize);
				drawable.setIconColor(mIconColor);
				return drawable;
			}

			@Override
			public boolean shouldIntercept(final int id) {
				return sIconMap.indexOfKey(id) >= 0;
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
	}

}
