package org.mariotaku.twidere.util;

import java.lang.reflect.Field;

import android.support.v4.widget.ViewDragHelper;

public class ViewDragHelperAccessor {

	private ViewDragHelperAccessor() {
		throw new AssertionError();
	}

	public static boolean setEdgeSize(final ViewDragHelper helper, final int edgeSize) {
		try {
			final Field f = helper.getClass().getField("mEdgeSize");
			f.setAccessible(true);
			f.setInt(helper, edgeSize);
			return true;
		} catch (final Exception e) {
			return false;
		}
	}

}
