package org.mariotaku.twidere.graphic.icon;

import android.content.Context;
import android.graphics.Typeface;

import com.atermenji.android.iconicdroid.icon.Icon;
import com.atermenji.android.iconicdroid.util.TypefaceManager.IconicTypeface;

public enum TwidereIcon implements Icon {
	TWIDERE(0xF000), WEB(0xF001), COMPOSE(0xF002), COLOR_PALETTE(0xF003), CAMERA(0xF004), NEW_MESSAGE(0xF005), SERVER(
			0xF006), GALLERY(0xF007), SAVE(0xF008), STAR(0xF009), SEARCH(0xF00A), RETWEET(0xF00B), REPLY(0xF00C), DELETE(
			0xF00D), ADD(0xF00E), SHARE(0xF00F), INBOX(0xF010), OUTBOX(0xF011), COPY(0xF012), TRANSLATE(0xF013), USER(
			0xF014), USERS(0xF015), CONVERSATION(0xF016), SEND(0xF017), EDIT(0xF018), ACCEPT(0xF019), CANCEL(0xF01A), PREFERENCES(
			0xF01B), LOCATION(0xF01C), MUTE(0xF01D), QUOTE(0xF01E), MESSAGE(0xF01F), TWITTER(0xF020), HOME(0xF021), AT(
			0xF022), HASHTAG(0xF023), TRENDS(0xF024), LIST(0xF025), STAGGERED(0xF026), NEKO(0xF027), TAB(0xF028), EXTENSION(
			0xF029), CARD(0xF02A), REFRESH(0xF02B), GRID(0xF02C), ABOUT(0xF02D), MORE(0xF02E), OPEN_SOURCE(0xF02F), NOTIFICATION(
			0xF030), INTERFACE(0xF031);

	private final int mIconUtfValue;

	private TwidereIcon(final int iconUtfValue) {
		mIconUtfValue = iconUtfValue;
	}

	@Override
	public IconicTypeface getIconicTypeface() {
		return TwidereIconTypefaces.SINGLETON;
	}

	@Override
	public int getIconUtfValue() {
		return mIconUtfValue;
	}

	private static final class TwidereIconTypefaces implements IconicTypeface {

		static final TwidereIconTypefaces SINGLETON = new TwidereIconTypefaces();

		private Typeface mTypeface;

		@Override
		public Typeface getTypeface(final Context context) {
			if (mTypeface == null) {
				mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/TwidereIconic.ttf");
			}

			return mTypeface;
		}
	}
}
