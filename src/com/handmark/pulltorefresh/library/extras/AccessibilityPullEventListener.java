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

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.State;
import java.util.HashMap;
import org.mariotaku.twidere.R;
import android.text.TextUtils;

public class AccessibilityPullEventListener<V extends View> implements PullToRefreshBase.OnPullEventListener<V> {

	private final Context mContext;

	/**
	 * Constructor
	 * 
	 * @param context - Context
	 */
	public AccessibilityPullEventListener(Context context) {
		mContext = context;
	}

	@Override
	public final void onPullEvent(PullToRefreshBase<V> refreshView, State state, Mode mode) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.DONUT) return;
		final AccessibilityManager accessibilityManager = (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
		if (!accessibilityManager.isEnabled()) return;
		final CharSequence text = getAccessibilityText(refreshView, state, mode);
		if (TextUtils.isEmpty(text)) return;
		// Prior to SDK 16, announcements could only be made through FOCUSED
		// events. Jelly Bean (SDK 16) added support for speaking text verbatim
		// using the ANNOUNCEMENT event type.
		final int eventType;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;
		} else {
			eventType = AccessibilityEventCompat.TYPE_ANNOUNCEMENT;
		}

		// Construct an accessibility event with the minimum recommended
		// attributes. An event without a class name or package may be dropped.
		final AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
		event.getText().add(text);
		event.setClassName(getClass().getName());
		event.setPackageName(mContext.getPackageName());
		event.setSource(refreshView);

		// Sends the event directly through the accessibility manager. If your
		// application only targets SDK 14+, you should just call
		// getParent().requestSendAccessibilityEvent(this, event);
		accessibilityManager.sendAccessibilityEvent(event);
	}

	private CharSequence getAccessibilityText(PullToRefreshBase<V> refreshView, State state, Mode mode) {
		if (state == State.PULL_TO_REFRESH) {
			return refreshView.getPullLabel(mode);
		} else if (state == State.RELEASE_TO_REFRESH) {
			return refreshView.getReleaseLabel(mode);
		} else if (state == State.REFRESHING) {
			return refreshView.getRefreshingLabel(mode);
		}
		return null;
	}
}
