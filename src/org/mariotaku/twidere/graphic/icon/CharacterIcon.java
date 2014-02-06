package org.mariotaku.twidere.graphic.icon;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.atermenji.android.iconicdroid.icon.Icon;
import com.atermenji.android.iconicdroid.util.TypefaceManager.IconicTypeface;

public class CharacterIcon implements Icon {

	private static final long serialVersionUID = 6749903097624346937L;
	private final int mIconUtfValue;

	public CharacterIcon(final int codePoint) {
		mIconUtfValue = codePoint;
	}

	public CharacterIcon(final String string) {
		this(getCodePoint(string));
	}

	@Override
	public IconicTypeface getIconicTypeface() {
		return CharacterIconTypeface.SINGLETON;
	}

	@Override
	public int getIconUtfValue() {
		return mIconUtfValue;
	}

	@Override
	public String toString() {
		return "CharacterIcon{" + new String(Character.toChars(mIconUtfValue)) + "}";
	}

	private static int getCodePoint(final String string) {
		if (TextUtils.isEmpty(string)) throw new NullPointerException();
		if (string.length() > 1 && Character.isSurrogatePair(string.charAt(0), string.charAt(1)))
			return string.codePointBefore(2);
		else
			return string.codePointAt(0);
	}

	private static final class CharacterIconTypeface implements IconicTypeface {

		static final CharacterIconTypeface SINGLETON = new CharacterIconTypeface();

		private Typeface mTypeface;

		@Override
		public Typeface getTypeface(final Context context) {
			if (mTypeface == null) {
				mTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/AndroidEmoji.ttf");
			}
			return mTypeface;
		}
	}
}
