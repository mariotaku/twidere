/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

public class StatusImagePreviewPager extends SquareViewPager {

	private final GestureDetector mGestureDetector;

	public StatusImagePreviewPager(final Context context) {
		this(context, null);
	}

	public StatusImagePreviewPager(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(context, new YScrollDetector());
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev) {
		final int action = ev.getAction();
		if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL && mGestureDetector.onTouchEvent(ev)) {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		return super.onInterceptTouchEvent(ev);
	}

	// Return false if we're scrolling in the x direction
	private static class YScrollDetector extends SimpleOnGestureListener {
		@Override
		public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
			return Math.abs(distanceX) > Math.abs(distanceY);
		}
	}
}
