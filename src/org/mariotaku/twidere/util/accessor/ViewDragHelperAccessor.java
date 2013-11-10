package org.mariotaku.twidere.util.accessor;

import android.support.v4.widget.ViewDragHelper;

import java.lang.reflect.Field;

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
