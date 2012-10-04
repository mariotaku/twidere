package org.mariotaku.actionbarcompat;

import android.app.Activity;
import android.os.Build;
import android.view.MenuInflater;

abstract class ActionBarCompat {

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
	MenuInflater getMenuInflater(final MenuInflater inflater) {
		return inflater;
	}

	static ActionBarCompat getInstance(final Activity activity) {
		if (activity == null) return null;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return new ActionBarCompatNative(activity);
		else
			return new ActionBarCompatBase(activity);
	}

}
