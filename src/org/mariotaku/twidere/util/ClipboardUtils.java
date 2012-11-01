package org.mariotaku.twidere.util;

import android.content.Context;
import android.text.ClipboardManager;

@SuppressWarnings("deprecation")
public final class ClipboardUtils {

	public static CharSequence getText(final Context context) {
		if (context == null) return null;
		return ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).getText();
	}

	public static void setText(final Context context, final CharSequence text) {
		if (context == null) return;
		((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE)).setText(text);
	}
}
