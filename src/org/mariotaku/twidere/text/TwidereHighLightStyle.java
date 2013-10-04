package org.mariotaku.twidere.text;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

import org.mariotaku.twidere.preference.LinkHighlightPreference;

public class TwidereHighLightStyle extends CharacterStyle {

	private final int option;

	public TwidereHighLightStyle(final int option) {
		this.option = option;
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		switch (option) {
			case LinkHighlightPreference.LINK_HIGHLIGHT_OPTION_CODE_BOTH:
				ds.setUnderlineText(true);
				ds.setColor(ds.linkColor);
				break;
			case LinkHighlightPreference.LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE:
				ds.setUnderlineText(true);
				break;
			case LinkHighlightPreference.LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT:
				ds.setColor(ds.linkColor);
				break;
			default:
				break;
		}
	}
}