package org.mariotaku.twidere.util.pulltorefresh;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.mariotaku.twidere.util.ThemeUtils;

import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;

public class TwidereHeaderTransformer extends DefaultHeaderTransformer {

	@Override
	protected Drawable getActionBarBackground(final Context context) {
		return ThemeUtils.getActionBarBackground(context, false);
	}
}
