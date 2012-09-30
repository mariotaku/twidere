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

package org.mariotaku.twidere.util;

import java.util.List;

public class ListUtils {

	public static long min(final List<Long> list) {
		long min = -1;
		for (final long item : list) {
			if (min == -1) {
				min = item;
			}
			if (min > item) {
				min = item;
			}
		}
		return min;
	}

	public static <T> String toString(final List<T> list, final char token, final boolean include_space) {
		final StringBuilder builder = new StringBuilder();
		final int size = list.size();
		for (int i = 0; i < size; i++) {
			final String item_string = String.valueOf(list.get(i));
			if (item_string != null) {
				if (i > 0) {
					builder.append(include_space ? token + " " : token);
				}
				builder.append(item_string);
			}
		}
		return builder.toString();
	}

	public static String toStringForSQL(final List<String> list) {
		final StringBuilder builder = new StringBuilder();
		final int size = list.size();
		for (int i = 0; i < size; i++) {
			final String string = list.get(i);
			if (string != null) {
				if (i > 0) {
					builder.append(',');
				}
				builder.append('\'' + string + '\'');
			}
		}
		return builder.toString();
	}
}
