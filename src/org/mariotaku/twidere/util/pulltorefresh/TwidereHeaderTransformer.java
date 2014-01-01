/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
