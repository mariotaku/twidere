/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ExtendedViewPager extends ViewPager {

	private boolean mPagingEnabled = true;

	public ExtendedViewPager(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mPagingEnabled = true;
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		if (!mPagingEnabled) return false;
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (!mPagingEnabled) return false;
		return super.onTouchEvent(event);
	}

	public void setPagingEnabled(final boolean enabled) {
		mPagingEnabled = enabled;
	}
}
