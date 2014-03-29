package org.mariotaku.twidere.util.theme;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.atermenji.android.iconicdroid.IconicFontDrawable;
import com.atermenji.android.iconicdroid.icon.Icon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.iface.ITwidereContextWrapper;
import org.mariotaku.twidere.content.res.iface.IThemedResources.DrawableInterceptor;
import org.mariotaku.twidere.graphic.icon.TwidereIcon;
import org.mariotaku.twidere.util.ThemeUtils;

public class ActionIconsInterceptor implements DrawableInterceptor {

	private static final SparseArray<ActionIconsInterceptor.IconSpec> sIconMap = new SparseArray<ActionIconsInterceptor.IconSpec>();

	static {
		sIconMap.put(R.drawable.ic_iconic_action_twidere, new IconSpec(TwidereIcon.TWIDERE, 0.875f));
		sIconMap.put(R.drawable.ic_iconic_action_twidere_square, new IconSpec(TwidereIcon.TWIDERE_SQUARE, 0.875f));
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
		sIconMap.put(R.drawable.ic_iconic_action_user, new IconSpec(TwidereIcon.USER, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_accounts, new IconSpec(TwidereIcon.USER_GROUP, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_send, new IconSpec(TwidereIcon.SEND, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_edit, new IconSpec(TwidereIcon.EDIT, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_ok, new IconSpec(TwidereIcon.OK, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_cancel, new IconSpec(TwidereIcon.CANCEL, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_preferences, new IconSpec(TwidereIcon.PREFERENCES, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_mylocation, new IconSpec(TwidereIcon.LOCATION_FOUND, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_speaker_muted, new IconSpec(TwidereIcon.SPEAKER_MUTED, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_quote, new IconSpec(TwidereIcon.QUOTE, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_message, new IconSpec(TwidereIcon.MESSAGE, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_twitter, new IconSpec(TwidereIcon.TWITTER, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_home, new IconSpec(TwidereIcon.HOME, 0.9375f));
		sIconMap.put(R.drawable.ic_iconic_action_mention, new IconSpec(TwidereIcon.AT, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_hashtag, new IconSpec(TwidereIcon.HASHTAG, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_trends, new IconSpec(TwidereIcon.TRENDS, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_list, new IconSpec(TwidereIcon.LIST, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_staggered, new IconSpec(TwidereIcon.STAGGERED, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_tab, new IconSpec(TwidereIcon.TAB, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_extension, new IconSpec(TwidereIcon.EXTENSION, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_card, new IconSpec(TwidereIcon.CARD, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_refresh, new IconSpec(TwidereIcon.REFRESH, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_grid, new IconSpec(TwidereIcon.GRID, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_about, new IconSpec(TwidereIcon.INFO, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_more, new IconSpec(TwidereIcon.MORE, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_open_source, new IconSpec(TwidereIcon.OPEN_SOURCE, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_notification, new IconSpec(TwidereIcon.NOTIFICATION, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_interface, new IconSpec(TwidereIcon.INTERFACE, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_block, new IconSpec(TwidereIcon.BLOCK, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_warning, new IconSpec(TwidereIcon.WARNING, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_heart, new IconSpec(TwidereIcon.HEART, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_checked, new IconSpec(TwidereIcon.CHECKED, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_drafts, new IconSpec(TwidereIcon.DRAFTS, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_import, new IconSpec(TwidereIcon.IMPORT, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_export, new IconSpec(TwidereIcon.EXPORT, 0.75f));
		sIconMap.put(R.drawable.ic_iconic_action_storage, new IconSpec(TwidereIcon.STORAGE, 0.75f));
	}

	private static int MENU_ICON_SIZE_DP = 32;
	private final Context mContext;
	private final int mIconSize;
	private final int mIconColor;
	private final float mDensity;

	public ActionIconsInterceptor(final Context context, final DisplayMetrics dm, final int overrideThemeRes) {
		mContext = context;
		if (overrideThemeRes != 0) {
			mIconColor = ThemeUtils.getActionIconColor(overrideThemeRes);
		} else if (context instanceof ITwidereContextWrapper) {
			final int resId = ((ITwidereContextWrapper) context).getThemeResourceId();
			mIconColor = ThemeUtils.getActionIconColor(resId);
		} else {
			mIconColor = ThemeUtils.getActionIconColor(context);
		}
		mDensity = dm.density;
		mIconSize = Math.round(mDensity * MENU_ICON_SIZE_DP);
	}

	@Override
	public Drawable getDrawable(final Resources res, final int resId) {
		final ActionIconsInterceptor.IconSpec spec = sIconMap.get(resId, null);
		if (spec == null) return null;
		// final TextDrawable drawable = new TextDrawable(mContext);
		// final Icon icon = spec.icon;
		// drawable.setText(new
		// String(Character.toChars(icon.getIconUtfValue())));
		// drawable.setTextAlign(Alignment.ALIGN_CENTER);
		// drawable.setTypeface(icon.getIconicTypeface().getTypeface(mContext));
		// drawable.setTextColor(mIconColor);
		// drawable.setTextSize(TypedValue.COMPLEX_UNIT_PX, mIconSize);
		// drawable.setBounds(0, 0, mIconSize, mIconSize);
		final IconicFontDrawable drawable = new IconicFontDrawable(mContext, spec.icon);
		drawable.setIntrinsicWidth(mIconSize);
		drawable.setIntrinsicHeight(mIconSize);
		drawable.setIconColor(mIconColor);
		drawable.setBounds(0, 0, mIconSize, mIconSize);
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