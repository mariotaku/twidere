package org.mariotaku.twidere.util;

import android.support.v4.util.LongSparseArray;

public class LongSparseArrayUtils {
	/**
	 * @return A copy of all keys contained in the sparse array.
	 */
	public static <E> long[] getKeys(final LongSparseArray<E> array) {
		final int length = array.size();
		final long[] result = new long[length];
		for (int i = 0, j = length; i < j; i++) {
			result[i] = array.keyAt(i);
		}
		return result;
	}

	public static <E> boolean hasKey(final LongSparseArray<E> array, final long key) {
		return array.indexOfKey(key) >= 0;
	}

	/**
	 * Sets all supplied keys to the given unique value.
	 * 
	 * @param keys Keys to set
	 * @param uniqueValue Value to set all supplied keys to
	 */
	public static <E> void setValues(final LongSparseArray<E> array, final long[] keys, final E uniqueValue) {
		final int length = keys.length;
		for (int i = 0; i < length; i++) {
			array.put(keys[i], uniqueValue);
		}
	}
}
