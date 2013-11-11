package org.mariotaku.internal.menu;

import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("serial")
final class MenusList extends ArrayList<MenuItem> {

	private final MenuAdapter mAdapter;

	public MenusList() {
		this(null, null);
	}

	public MenusList(final Collection<MenuItem> items) {
		this(null, items);
	}

	public MenusList(final MenuAdapter adapter) {
		this(adapter, null);
	}

	public MenusList(final MenuAdapter adapter, final Collection<MenuItem> items) {
		mAdapter = adapter;
		if (items != null) {
			addAll(items);
		}
	}

	@Override
	public void add(final int index, final MenuItem object) {
		super.add(index, object);
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
	}

	@Override
	public boolean add(final MenuItem object) {
		add(findInsertIndex(object.getOrder()), object);
		return true;
	}

	@Override
	public boolean addAll(final Collection<? extends MenuItem> collection) {
		final boolean result = super.addAll(collection);
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
		return result;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends MenuItem> collection) {
		final boolean result = super.addAll(index, collection);
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
		return result;
	}

	@Override
	public void clear() {
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
		super.clear();
	}

	@Override
	public MenuItem remove(final int index) {
		final MenuItem result = super.remove(index);
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
		return result;
	}

	@Override
	public boolean remove(final Object object) {
		final boolean result = super.remove(object);
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
		return result;
	}

	@Override
	public MenuItem set(final int index, final MenuItem object) {
		final MenuItem result = super.set(index, object);
		if (mAdapter != null) {
			mAdapter.setMenuItems();
		}
		return result;
	}

	private int findInsertIndex(final int order) {
		for (int i = size() - 1; i >= 0; i--) {
			final MenuItem item = get(i);
			if (item.getOrder() <= order) return i + 1;
		}
		return 0;
	}

}
