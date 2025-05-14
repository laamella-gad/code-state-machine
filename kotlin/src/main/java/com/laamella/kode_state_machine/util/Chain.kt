package com.laamella.kode_state_machine.util

import java.util.*

/**
 * Simple base class for chaining instances of some type.
 */
open class Chain<T> {
    val items: MutableList<T>

    constructor(vararg items: T) {
        this.items = ArrayList<T>(Arrays.asList<T>(*items))
    }

    fun add(additionalItem: T) {
        items.add(additionalItem)
    }

    @SafeVarargs
    fun add(vararg additionalItems: T) {
        items.addAll(Arrays.asList<T>(*additionalItems))
    }

    fun add(additionalItems: Chain<T>) {
        items.addAll(additionalItems.items)
    }

    fun remove(item: T) {
        items.remove(item)
    }

    protected fun getItems(): Iterable<T> {
        return items
    }

    override fun toString(): String {
        if (items.isEmpty()) {
            return "always"
        }
        if (items.size == 1) {
            return items.get(0).toString()
        }
        val buffer = StringBuilder("[")
        for (item in items) {
            buffer.append(item.toString())
            buffer.append(", ")
        }
        buffer.setLength(buffer.length - 2)
        return buffer.append("]").toString()
    }
}
