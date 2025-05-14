package com.laamella.kode_state_machine.priority

/**
 * By using Integer as the priority type, and assigning priorities by using this
 * class, the state machine will become deterministic since transitions that
 * have been defined earlier always get priority over transitions that have been
 * defined later.
 */
// TODO this is a rather odd solution.
object PriorityDeterminizer {
    private var nextPriority = Int.MAX_VALUE

    fun nextPriority(): Int {
        return nextPriority--
    }
}
