/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.handmark.pulltorefresh.library.extras;

import static org.mariotaku.twidere.util.Utils.announceForAccessibilityCompat;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;

public class AccessibilityPullEventListener<V extends View> implements PullToRefreshBase.OnPullEventListener<V> {

	private final Context mContext;

	/**
	 * Constructor
	 * 
	 * @param context - Context
	 */
	public AccessibilityPullEventListener(final Context context) {
		mContext = context;
	}

	@Override
	public final void onPullEvent(final PullToRefreshBase<V> refreshView, final State state, final Mode mode) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) return;
		final CharSequence text = getAccessibilityText(refreshView, state, mode);
		if (TextUtils.isEmpty(text)) return;
		announceForAccessibilityCompat(mContext, refreshView, text, getClass());
	}

	private static <V extends View> CharSequence getAccessibilityText(final PullToRefreshBase<V> refreshView,
			final State state, final Mode mode) {
		if (state == State.PULL_TO_REFRESH)
			return refreshView.getPullLabel(mode);
		else if (state == State.RELEASE_TO_REFRESH)
			return refreshView.getReleaseLabel(mode);
		else if (state == State.REFRESHING) return refreshView.getRefreshingLabel(mode);
		return null;
	}
}
