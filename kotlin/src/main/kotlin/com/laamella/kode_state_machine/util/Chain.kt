package com.laamella.kode_state_machine.util

/**
 * Simple base class for chaining instances of some type.
 */
open class Chain<T>(vararg items: T) {
    val items: MutableList<T> = mutableListOf(*items)

    fun add(additionalItem: T) {
        items.add(additionalItem)
    }

    fun add(vararg additionalItems: T) {
        items.addAll(additionalItems)
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
            return items[0].toString()
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
