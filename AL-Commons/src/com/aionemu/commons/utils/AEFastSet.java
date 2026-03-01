package com.aionemu.commons.utils;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Set implementation backed by ConcurrentHashMap.newKeySet().
 * Replaces the former Javolution FastMap-based implementation.
 *
 * @author NB4L1
 * @param <E> the type of elements in this set
 */
public class AEFastSet<E> extends AEFastCollection<E> implements Set<E> {

	private final Set<E> set;

	public AEFastSet() {
		set = ConcurrentHashMap.newKeySet();
	}

	public AEFastSet(int capacity) {
		set = ConcurrentHashMap.newKeySet(capacity);
	}

	public AEFastSet(Set<? extends E> elements) {
		set = ConcurrentHashMap.newKeySet(elements.size());
		set.addAll(elements);
	}

	@Override
	public boolean add(E value) {
		return set.add(value);
	}

	@Override
	public void clear() {
		set.clear();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return set.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return set.remove(o);
	}

	@Override
	public int size() {
		return set.size();
	}

	@Override
	public String toString() {
		return set.toString();
	}
}
