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

package org.mariotaku.twidere.model;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public final class ExtensionsViewHolder {

	public final ImageView icon;
	public final TextView text1, text2;

	public ExtensionsViewHolder(final View view) {

		icon = (ImageView) view.findViewById(android.R.id.icon);
		text1 = (TextView) view.findViewById(android.R.id.text1);
		text2 = (TextView) view.findViewById(android.R.id.text2);
	}
}