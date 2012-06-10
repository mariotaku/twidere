package org.mariotaku.twidere.util;

import org.mariotaku.twidere.view.StatusComposeEditText;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView.Tokenizer;

/**
 * This simple Tokenizer can be used for lists where the items are separated by
 * a comma and one or more spaces.
 */
public class ScreenNameTokenizer implements Tokenizer {

	private StatusComposeEditText mEditText;

	public ScreenNameTokenizer(StatusComposeEditText edittext) {
		mEditText = edittext;
	}

	@Override
	public int findTokenEnd(CharSequence text, int cursor) {
		int i = cursor;
		int len = text.length();

		while (i < len) {
			if (text.charAt(i) == ' ')
				return i;
			else {
				i++;
			}
		}

		return len;
	}

	@Override
	public int findTokenStart(CharSequence text, int cursor) {
		int i = cursor;

		while (i > 0 && text.charAt(i - 1) != ' ') {
			i--;
		}
		while (i < cursor && text.charAt(i) == '@') {
			i++;
		}

		return i;
	}

	@Override
	public CharSequence terminateToken(CharSequence text) {
		int i = text.length();

		while (i > 0 && text.charAt(i - 1) == '@') {
			i--;
		}

		boolean at_sign_entered = mEditText.isAtSignEntered();

		if (i > 0 && text.charAt(i - 1) == ' ')
			return at_sign_entered ? text : "@" + text;
		else {
			if (text instanceof Spanned) {
				SpannableString sp = new SpannableString(at_sign_entered ? text : "@" + text);
				TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
				return sp;
			} else
				return at_sign_entered ? text : "@" + text;
		}
	}
}