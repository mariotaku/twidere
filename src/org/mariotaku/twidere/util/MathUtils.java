package org.mariotaku.twidere.util;

public class MathUtils {
	public static float clamp(final float num, final float max, final float min) {
		return Math.max(Math.min(num, max), min);
	}

	public static int clamp(final int num, final int max, final int min) {
		return Math.max(Math.min(num, max), min);
	}

}
