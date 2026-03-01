package com.aionemu.commons.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

/**
 * Abstract base class for high-performance collection implementations.
 * Replaces the former Javolution-based implementation with standard Java collections.
 *
 * @author NB4L1
 * @param <E> the type of elements in this collection
 */
@SuppressWarnings("unchecked")
public abstract class AEFastCollection<E> implements Collection<E> {

	public E getFirst() {
		Iterator<E> it = iterator();
		return it.hasNext() ? it.next() : null;
	}

	public E getLast() {
		E last = null;
		for (E e : this) {
			last = e;
		}
		return last;
	}

	public E removeFirst() {
		Iterator<E> it = iterator();
		if (it.hasNext()) {
			E value = it.next();
			it.remove();
			return value;
		}
		return null;
	}

	public E removeLast() {
		Iterator<E> it = iterator();
		E last = null;
		while (it.hasNext()) {
			last = it.next();
		}
		if (last != null) {
			remove(last);
		}
		return last;
	}

	public boolean addAll(E[] c) {
		boolean modified = false;
		for (E e : c) {
			if (add(e)) {
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		for (E e : c) {
			if (add(e)) {
				modified = true;
			}
		}
		return modified;
	}

	public boolean containsAll(Object[] c) {
		for (Object obj : c) {
			if (!contains(obj)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object obj : c) {
			if (!contains(obj)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			if (c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean modified = false;
		Iterator<E> it = iterator();
		while (it.hasNext()) {
			if (!c.contains(it.next())) {
				it.remove();
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public Object[] toArray() {
		return toArray(new Object[size()]);
	}

	@Override
	public <T> T[] toArray(T[] array) {
		int size = size();
		if (array.length < size) {
			array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
		}
		int i = 0;
		for (E e : this) {
			array[i++] = (T) e;
		}
		if (array.length > size) {
			array[size] = null;
		}
		return array;
	}
}
