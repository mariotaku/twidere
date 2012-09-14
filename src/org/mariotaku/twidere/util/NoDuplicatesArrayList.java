package org.mariotaku.twidere.util;

import java.util.ArrayList;
import java.util.Collection;

public class NoDuplicatesArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = -7277301117508689125L;

	@Override
	public boolean add(E e) {
		if (contains(e))
			return false;
		else
			return super.add(e);
	}

	@Override
	public void add(int index, E element) {
		if (contains(element))
			return;
		else {
			super.add(index, element);
		}
	}

	@Override
	public boolean addAll(Collection<? extends E> collection) {
		final Collection<E> copy = new ArrayList<E>(collection);
		copy.removeAll(this);
		return super.addAll(copy);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> collection) {
		final Collection<E> copy = new ArrayList<E>(collection);
		copy.removeAll(this);
		return super.addAll(index, copy);
	}
}