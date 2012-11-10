package org.mariotaku.twidere.util;

import java.io.Serializable;
import java.util.List;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;

public class SynchronizedStateSavedList<E, State extends Serializable> implements Serializable, List<E> {

	private State state;
	private final List<E> list;
	
	public SynchronizedStateSavedList(int capacity) {
		list = Collections.synchronizedList(new NoDuplicatesArrayList<E>(capacity));
	}

    public SynchronizedStateSavedList() {
		list = Collections.synchronizedList(new NoDuplicatesArrayList<E>());
	}

    public SynchronizedStateSavedList(java.util.Collection<? extends E> collection) {
		list = Collections.synchronizedList(new NoDuplicatesArrayList<E>(collection));
	}
	
	public State getState() {
		return state;
	}

	public void setState(final State state) {
		this.state = state;
	}
	
	public void add(int index, E e) {
		list.add(index, e);
	}

	public boolean add(E e) {
		return list.add(e);
	}

	public boolean addAll(int location, Collection<? extends E> collection) {
		return list.addAll(location, collection);
	}

	public boolean addAll(Collection<? extends E> collection) {
		return list.addAll(collection);
	}

	public void clear() {
		list.clear();
	}

	public boolean contains(Object object) {
		return list.contains(object);
	}

	public boolean containsAll(Collection<?> collection) {
		return list.containsAll(collection);
	}

	public E get(int location) {
		return list.get(location);
	}

	public int indexOf(Object object) {
		return list.indexOf(object);
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public Iterator<E> iterator() {
		return list.iterator();
	}

	public int lastIndexOf(Object object) {
		return list.lastIndexOf(object);
	}

	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	public ListIterator<E> listIterator(int location) {
		return list.listIterator(location);
	}

	public E remove(int location) {
		return list.remove(location);
	}

	public boolean remove(Object object) {
		return list.remove(object);
	}

	public boolean removeAll(Collection<?> collection) {
		return list.removeAll(collection);
	}

	public boolean retainAll(Collection<?> collection) {
		return list.retainAll(collection);
	}

	public E set(int location, E object) {
		return list.set(location, object);
	}

	public int size() {
		return list.size();
	}

	public List<E> subList(int start, int end) {
		return list.subList(start, end);
	}

	public Object[] toArray() {
		return list.toArray();
	}

	public <T extends Object> T[] toArray(T[] array) {
		return list.toArray(array);
	}
	
}
