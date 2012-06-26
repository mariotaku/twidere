package org.mariotaku.twidere.util;

public final class ArrayUtils {

	private ArrayUtils() {
	}

	public static String buildString(long[] array, char token, boolean include_space) {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
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

	public static int indexOf(long[] array, long value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}
}
