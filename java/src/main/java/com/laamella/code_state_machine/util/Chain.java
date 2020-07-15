package com.laamella.code_state_machine.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple base class for chaining instances of some type.
 */
public class Chain<T> {
	private final List<T> items;

	// This method exists only to suppress warnings about varargs.
	public Chain() {
		this.items = new ArrayList<>();
	}

	@SafeVarargs
	public Chain(final T... items) {
		this.items = new ArrayList<>(Arrays.asList(items));
	}

	public void add(final T additionalItem) {
		assert additionalItem != null;
		items.add(additionalItem);
	}

	@SafeVarargs
	public final void add(final T... additionalItems) {
		items.addAll(Arrays.asList(additionalItems));
	}

	public void add(final Chain<T> additionalItems) {
		assert additionalItems != null;
		items.addAll(additionalItems.items);
	}

	public void remove(final T item) {
		items.remove(item);
	}

	protected Iterable<T> getItems() {
		return items;
	}

	@Override
	public String toString() {
		if (items.size() == 0) {
			return "always";
		}
		if (items.size() == 1) {
			return items.get(0).toString();
		}
		final StringBuilder buffer = new StringBuilder("[");
		for (final T item : items) {
			buffer.append(item.toString());
			buffer.append(", ");
		}
		buffer.setLength(buffer.length() - 2);
		return buffer.append("]").toString();
	}

}
