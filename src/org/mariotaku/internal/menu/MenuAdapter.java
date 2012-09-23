package org.mariotaku.internal.menu;

import java.util.List;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public final class MenuAdapter extends ArrayAdapter<MenuItem> {

	private Menu mMenu;

	public MenuAdapter(final Context context) {
		super(context, R.layout.menu_list_item);
	}

	@Override
	public long getItemId(final int index) {
		return getItem(index).getItemId();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final TextView view = (TextView) super.getView(position, convertView, parent);
		final MenuItem item = getItem(position);
		view.setEnabled(item.isEnabled());
		view.setVisibility(item.isVisible() ? View.VISIBLE : View.GONE);
		view.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
		return view;
	}

	public void setMenu(final Menu menu) {
		mMenu = menu;
		setMenuItems();
	}

	public void setMenuItems() {
		clear();
		final List<MenuItem> items = mMenu == null ? null : ((MenuImpl) mMenu).getMenuItems();
		if (items == null) return;
		for (final MenuItem item : items) {
			if (item.isVisible()) {
				add(item);
			}
		}
	}

}
