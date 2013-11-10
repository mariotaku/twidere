package org.mariotaku.internal.menu;

import android.view.MenuItem;

import java.lang.reflect.Method;

public class MenuItemUtils {

	public static int getShowAsActionFlags(MenuItem item) {
		if (item == null) throw new NullPointerException();
		if (item instanceof MenuItemImpl) {
			return ((MenuItemImpl) item).getShowAsActionFlags();
		}
		try {
			final Method m = item.getClass().getMethod("getShowAsAction");
			return (Integer) m.invoke(item);
		} catch (Exception e) {
			return MenuItem.SHOW_AS_ACTION_NEVER;
		}
	}

}
