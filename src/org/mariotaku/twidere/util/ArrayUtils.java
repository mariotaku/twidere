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

public final class ArrayUtils {

	private ArrayUtils() {
		throw new IllegalArgumentException("You are trying to create an instance for this utility class!");
	}

	public static String buildString(long[] array, char token, boolean include_space) {
		final StringBuilder builder = new StringBuilder();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			final String id_string = String.valueOf(array[i]);
			if (id_string != null) {
				if (i > 0) {
					builder.append(include_space ? token + " " : token);
				}
				builder.append(id_string);
			}
		}
		return builder.toString();
	}

	public static String buildString(Object[] array, char token, boolean include_space) {
		final StringBuilder builder = new StringBuilder();
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			final String id_string = String.valueOf(array[i]);
			if (id_string != null) {
				if (i > 0) {
					builder.append(include_space ? token + " " : token);
				}
				builder.append(id_string);
			}
		}
		return builder.toString();
	}

	public static boolean contains(long[] array, long value) {
		for (final long item : array) {
			if (item == value) return true;
		}
		return false;
	}

	public static boolean contains(Object[] array, Object value) {
		for (final Object item : array) {
			if (item == null || value == null) {
				if (item == value) return true;
				continue;
			}
			if (item.equals(value)) return true;
		}
		return false;
	}

	public static boolean contentMatch(Object[] array1, Object[] array2) {
		if (array1 == null || array2 == null) return array1 == array2;
		if (array1.length != array2.length) return false;
		final int length = array1.length;
		for (int i = 0; i < length; i++) {
			if (!contains(array2, array1[i])) return false;
		}
		return true;
	}

	public static int indexOf(long[] array, long value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}

	public static int indexOf(Object[] array, Object value) {
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (array[i].equals(value)) return i;
		}
		return -1;
	}
}
