package com.laamella.code_state_machine.priority

/**
 * A generic priority type.
 */
object LeveledPriority extends Enumeration {
  type LeveledPriority = Value
	val HIGHEST, HIGH, NORMAL, LOW, LOWEST= Value
}
