package com.aionemu.commons.callbacks.util;

import java.util.Comparator;

import com.aionemu.commons.callbacks.Callback;

/**
 * Callback priority comparator. Delegates to {@link CallbackPriorityComparator}.
 */
public class CallbackPriorityFastComparator implements Comparator<Callback<?>> {

	private static final CallbackPriorityComparator cpc = new CallbackPriorityComparator();

	@Override
	public int compare(Callback<?> o1, Callback<?> o2) {
		return cpc.compare(o1, o2);
	}
}
