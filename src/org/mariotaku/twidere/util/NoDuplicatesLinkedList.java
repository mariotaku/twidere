package org.mariotaku.twidere.util;

import java.util.Collection;
import java.util.LinkedList;

public class NoDuplicatesLinkedList<E> extends LinkedList<E> {
	private static final long serialVersionUID = 4295241536597752001L;

	@Override
	public boolean add(final E e) {
		if (contains(e))
			return false;
		else
			return super.add(e);
	}

	@Override
	public void add(final int index, final E element) {
		if (contains(element))
			return;
		else {
			super.add(index, element);
		}
	}

	@Override
	public boolean addAll(final Collection<? extends E> collection) {
		final Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(copy);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends E> collection) {
		final Collection<E> copy = new LinkedList<E>(collection);
		copy.removeAll(this);
		return super.addAll(index, copy);
	}
}