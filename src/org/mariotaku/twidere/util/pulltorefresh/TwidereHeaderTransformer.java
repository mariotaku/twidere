package org.mariotaku.twidere.util.pulltorefresh;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.mariotaku.twidere.util.ThemeUtils;

import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;

public class TwidereHeaderTransformer extends DefaultHeaderTransformer {

	@Override
	public void onViewCreated(final Activity activity, final View headerView) {
		super.onViewCreated(activity, headerView);
		setProgressBarStyle(PROGRESS_BAR_STYLE_INSIDE);
		setProgressBarHeight(Math.round(activity.getResources().getDisplayMetrics().density * 2));
		setProgressBarColor(Color.WHITE);
		// if (ThemeUtils.isColoredActionBar(activity)) {
		// setProgressBarColor(ThemeUtils.getUserThemeColor(activity));
		// } else {
		// setProgressBarColor(Color.WHITE);
		// }
	}

	@Override
	protected Drawable getActionBarBackground(final Context context) {
		return ThemeUtils.getActionBarBackground(context, false);
	}
}
