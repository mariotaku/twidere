package org.mariotaku.twidere.util;

import java.util.Collection;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class NoDuplicatesList<E> extends LinkedList<E> {
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
		final Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(copy);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> collection) {
		final Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(index, copy);
	}
}