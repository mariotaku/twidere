package org.mariotaku.twidere.theme;

import android.content.Context;
import android.view.Window;

import com.negusoft.holoaccent.AccentHelper;
import com.negusoft.holoaccent.dialog.DividerPainter;

public class TwidereAccentHelper extends AccentHelper {

	private DividerPainter mDividerPainter;
	private final int mAccentColor;

	public TwidereAccentHelper() {
		mAccentColor = 0;
	}

	public TwidereAccentHelper(final int color) {
		super(color);
		mAccentColor = color;
	}

	@Override
	public void prepareDialog(final Context c, final Window window) {
		if (mDividerPainter == null) {
			if (mAccentColor != 0) {
				mDividerPainter = new DividerPainter(mAccentColor);
			} else {
				mDividerPainter = new DividerPainter(c);
			}
		}
		mDividerPainter.paint(window);
	}

}
