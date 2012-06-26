package org.mariotaku.twidere.view;

import org.mariotaku.twidere.adapter.UserAutoCompleteAdapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

public class StatusComposeEditText extends MultiAutoCompleteTextView {

	private UserAutoCompleteAdapter mAdapter;

	public StatusComposeEditText(Context context) {
		this(context, null);
	}

	public StatusComposeEditText(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
	}

	public StatusComposeEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mAdapter = new UserAutoCompleteAdapter(context);
		setTokenizer(new ScreenNameTokenizer());
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (mAdapter == null || mAdapter.isCursorClosed()) {
			mAdapter = new UserAutoCompleteAdapter(getContext());
		}
		setAdapter(mAdapter);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAdapter != null) {
			mAdapter.closeCursor();
			mAdapter = null;
		}
		setAdapter(null);
	}

	private static class ScreenNameTokenizer implements Tokenizer {

		@Override
		public int findTokenEnd(CharSequence text, int cursor) {
			int i = cursor;
			final int len = text.length();

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
			int start = cursor;

			while (start > 0 && text.charAt(start - 1) != ' ') {
				start--;
			}

			while (start < cursor && text.charAt(start) == ' ') {
				start++;
			}

			if (start < cursor && text.charAt(start) == '@') {
				start++;
			} else {
				start = cursor;
			}

			return start;
		}

		@Override
		public CharSequence terminateToken(CharSequence text) {
			int i = text.length();

			while (i > 0 && text.charAt(i - 1) == '@') {
				i--;
			}

			if (i > 0 && text.charAt(i - 1) == ' ')
				return text;
			else {
				if (text instanceof Spanned) {
					final SpannableString sp = new SpannableString(text);
					TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
					return sp;
				} else
					return text;
			}
		}
	}
}
