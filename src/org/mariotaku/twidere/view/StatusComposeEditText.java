package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

public class StatusComposeEditText extends MultiAutoCompleteTextView {

	private boolean mIsAtSignEntered = false;

	public StatusComposeEditText(Context context) {
		super(context);
	}

	public StatusComposeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public StatusComposeEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isAtSignEntered() {
		return mIsAtSignEntered;
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
		String string = text.toString();
		if (!mIsAtSignEntered) {
			mIsAtSignEntered = string.endsWith("@");
		} else {
			if (start == text.length() - 1) {
				mIsAtSignEntered = !string.endsWith(" ");
			} else {
				mIsAtSignEntered = true;
			}
		}
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}

}
