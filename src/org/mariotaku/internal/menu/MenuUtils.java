package org.mariotaku.internal.menu;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import java.lang.reflect.Method;
import java.util.Collection;

public class MenuUtils {

	public static Menu createMenu(final Context context) {
		return new MenuImpl(context, null, null);
	}

	public static Menu createMenu(final Context context, final Collection<MenuItem> items) {
		return new MenuImpl(context, null, items);
	}

	public static Menu createMenu(final Context context, final MenuAdapter adapter) {
		return new MenuImpl(context, adapter, null);
	}

	public static Menu createMenu(final Context context, final MenuAdapter adapter, final Collection<MenuItem> items) {
		return new MenuImpl(context, adapter, items);
	}

	public static int getShowAsActionFlags(final MenuItem item) {
		if (item == null) throw new NullPointerException();
		if (item instanceof MenuItemImpl) return ((MenuItemImpl) item).getShowAsActionFlags();
		try {
			final Method m = item.getClass().getMethod("getShowAsAction");
			return (Integer) m.invoke(item);
		} catch (final Exception e) {
			return MenuItem.SHOW_AS_ACTION_NEVER;
		}
	}
}
