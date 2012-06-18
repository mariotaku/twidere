package org.mariotaku.twidere.util;

public final class ArrayUtils {

	private ArrayUtils() {
	}

	public static boolean contains(long[] array, long value) {
		for (long item : array) {
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
