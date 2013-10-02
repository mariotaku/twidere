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

package org.mariotaku.twidere.view.iface;

public interface IColorLabelView {

    public static final float LABEL_WIDTH = 3.5f;

    public void drawBackground(final int color);

    public void drawEnd(final int color);

    public void drawLabel(final int left, final int right, final int background);

    public void drawStart(final int color);

    public boolean isPaddingsIgnored();

    public void setIgnorePaddings(final boolean ignorePaddings);

    public void setVisibility(int visibility);
}
