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

package org.mariotaku.twidere.util.pulltorefresh.viewdelegates;

import android.view.View;

import com.huewu.pla.lib.internal.PLAAbsListView;

import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

/**
 * FIXME
 */
public class PLAAbsListViewDelegate implements ViewDelegate {

	public static final Class<?>[] SUPPORTED_VIEW_CLASSES = { PLAAbsListView.class };

	@Override
	public boolean isReadyForPull(final View view, final float x, final float y) {
		boolean ready = false;

		// First we check whether we're scrolled to the top
		final PLAAbsListView absListView = (PLAAbsListView) view;
		if (absListView.getCount() == 0) {
			ready = true;
		} else if (absListView.getFirstVisiblePosition() == 0) {
			final View firstVisibleChild = absListView.getChildAt(0);
			ready = firstVisibleChild != null && firstVisibleChild.getTop() >= 0;
		}

		return ready;
	}

}
