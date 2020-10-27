package com.laamella.code_state_machine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

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
    public Chain(T... items) {
        this.items = new ArrayList<>(asList(items));
    }

    public void add(T additionalItem) {
        requireNonNull(additionalItem);
        items.add(additionalItem);
    }

    @SafeVarargs
    public final void add(T... additionalItems) {
        items.addAll(asList(additionalItems));
    }

    public void add(Chain<T> additionalItems) {
        Objects.requireNonNull(additionalItems);
        items.addAll(additionalItems.items);
    }

    public void remove(T item) {
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
