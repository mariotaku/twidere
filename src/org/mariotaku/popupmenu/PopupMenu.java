package org.mariotaku.popupmenu;

import android.content.Context;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * QuickAction dialog, shows action list as icon and text like the one in
 * Gallery3D app. Currently supports vertical and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 *         Contributors: - Kevin Peck <kevinwpeck@gmail.com>
 */
public abstract class PopupMenu {

	/**
	 * Dismiss the popup window.
	 */
	public abstract void dismiss();

	public abstract Menu getMenu();

	public abstract void inflate(int menuRes);

	public abstract void setMenu(Menu menu);

	/**
	 * Set listener for window dismissed. This listener will only be fired if
	 * the quickaction dialog is dismissed by clicking outside the dialog or
	 * clicking on sticky item.
	 */
	public abstract void setOnDismissListener(PopupMenu.OnDismissListener listener);

	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public abstract void setOnMenuItemClickListener(OnMenuItemClickListener listener);

	public abstract void show();

	public static PopupMenu getInstance(Context context, View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return new PopupMenuNative(context, view);
		else
			return new PopupMenuCompat(context, view);
	}

	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public void onDismiss(PopupMenu PopupMenu);
	}

	/**
	 * Listener for item click
	 * 
	 */
	public interface OnMenuItemClickListener {
		public boolean onMenuItemClick(MenuItem item);
	}

}
