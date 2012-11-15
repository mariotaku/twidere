/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SynchronizedStateSavedList<E, State extends Serializable> implements Serializable, List<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6648620731104270012L;
	private State state;
	private final List<E> list;

	public SynchronizedStateSavedList() {
		list = Collections.synchronizedList(new NoDuplicatesArrayList<E>());
	}

	public SynchronizedStateSavedList(final int capacity) {
		list = Collections.synchronizedList(new NoDuplicatesArrayList<E>(capacity));
	}

	public SynchronizedStateSavedList(final java.util.Collection<? extends E> collection) {
		list = Collections.synchronizedList(new NoDuplicatesArrayList<E>(collection));
	}

	@Override
	public boolean add(final E e) {
		return list.add(e);
	}

	@Override
	public void add(final int index, final E e) {
		list.add(index, e);
	}

	@Override
	public boolean addAll(final Collection<? extends E> collection) {
		return list.addAll(collection);
	}

	@Override
	public boolean addAll(final int location, final Collection<? extends E> collection) {
		return list.addAll(location, collection);
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public boolean contains(final Object object) {
		return list.contains(object);
	}

	@Override
	public boolean containsAll(final Collection<?> collection) {
		return list.containsAll(collection);
	}

	@Override
	public E get(final int location) {
		return list.get(location);
	}

	public State getState() {
		return state;
	}

	@Override
	public int indexOf(final Object object) {
		return list.indexOf(object);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return list.iterator();
	}

	@Override
	public int lastIndexOf(final Object object) {
		return list.lastIndexOf(object);
	}

	@Override
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int location) {
		return list.listIterator(location);
	}

	@Override
	public E remove(final int location) {
		return list.remove(location);
	}

	@Override
	public boolean remove(final Object object) {
		return list.remove(object);
	}

	@Override
	public boolean removeAll(final Collection<?> collection) {
		return list.removeAll(collection);
	}

	@Override
	public boolean retainAll(final Collection<?> collection) {
		return list.retainAll(collection);
	}

	@Override
	public E set(final int location, final E object) {
		return list.set(location, object);
	}

	public void setState(final State state) {
		this.state = state;
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public List<E> subList(final int start, final int end) {
		return list.subList(start, end);
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@Override
	public <T extends Object> T[] toArray(final T[] array) {
		return list.toArray(array);
	}

}
