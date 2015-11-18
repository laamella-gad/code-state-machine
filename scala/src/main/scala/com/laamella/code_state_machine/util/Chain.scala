package com.laamella.code_state_machine.util

import scala.collection.mutable.ListBuffer

/**
 * Simple base class for chaining instances of some type.
 */
// TODO fix these constructors
class Chain[T <: Object](val items: ListBuffer[T]) {
  def this(items: Seq[T]) = this(new ListBuffer ++= items.toTraversable)

  def add(additionalItems: T*) {
    items ++= additionalItems
  }

  def add(additionalItems: Chain[T]) {
    items ++= additionalItems.items
  }

  def remove(item: T) {
    items -= item
  }

  protected def getItems = items.toList

  override def toString: String = {
    if (items.isEmpty) {
      return "always"
    }
    if (items.size == 1) {
      return items.head.toString
    }
    val buffer = new StringBuilder("[")

    items.foreach { i =>
      buffer.append(i.toString)
      buffer.append(", ")
    }
    buffer.setLength(buffer.length - 2)
    buffer.append("]").toString()
  }
}
