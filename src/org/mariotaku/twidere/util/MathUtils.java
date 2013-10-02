
package org.mariotaku.twidere.util;

public class MathUtils {

    public static int clamp(final int num, final int max, final int min) {
        return Math.max(Math.min(num, max), min);
    }

}
