package org.mariotaku.actionbarcompat;

import android.app.Activity;
import android.os.Build;
import android.view.MenuInflater;

public abstract class ActionBarCompat {

	ActionBar getActionBar() {
		if (this instanceof ActionBar) return (ActionBar) this;
		return null;
	}

	/**
	 * Returns a {@link MenuInflater} for use when inflating menus. The
	 * implementation of this method in {@link ActionBarHelperBase} returns a
	 * wrapped menu inflater that can read action bar metadata from a menu
	 * resource pre-ICS.
	 */
	MenuInflater getMenuInflater(MenuInflater inflater) {
		return inflater;
	}

	public static ActionBarCompat getInstance(Activity activity) {
		if (activity == null) return null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return new ActionBarCompatNative(activity);
		else
			return new ActionBarCompatBase(activity);
	}

}
