package org.mariotaku.twidere.util;
import java.util.Arrays;

public final class ArrayUtils {

	private ArrayUtils() {
		throw new IllegalArgumentException("You are trying to create an instance for this utility class!");
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

	public static<T extends Object> boolean contains(T[] array, T value) {
		for (final T item : array) {
			if (item == null || value == null) {
				if (item == value) return true;
				continue;
			}
			if (item.equals(value)) return true;
		}
		return false;
	}
	
	public static int indexOf(long[] array, long value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return i;
		}
		return -1;
	}
	
	public static<T> boolean contentMatch(T[] array1, T[] array2) {
		if (array1 == null || array2 == null) {
			return array1 == array2;
		}
		if (array1.length != array2.length) return false;
		for (int i = 0; i < array1.length; i++) {
			if (!contains(array2, array1[i])) {
				return false;
			}
		}
		return true;
	}
}
