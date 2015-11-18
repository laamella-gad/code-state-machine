package com.laamella.code_state_machine.priority

/**
 * A generic priority type.
 */
object LeveledPriority extends Enumeration {
  type LeveledPriority = Value
  val LOWEST, LOW, NORMAL, HIGH, HIGHEST = Value

  override def toString() = getClass.getSimpleName

}
