package org.mariotaku.twidere.text;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

import org.mariotaku.twidere.Constants;

public class TwidereHighLightStyle extends CharacterStyle implements Constants {

	private final int option;

	public TwidereHighLightStyle(final int option) {
		this.option = option;
	}

	@Override
	public void updateDrawState(final TextPaint ds) {
		if ((option & LINK_HIGHLIGHT_OPTION_CODE_UNDERLINE) != 0) {
			ds.setUnderlineText(true);
		}
		if ((option & LINK_HIGHLIGHT_OPTION_CODE_HIGHLIGHT) != 0) {
			ds.setColor(ds.linkColor);
		}
	}
}