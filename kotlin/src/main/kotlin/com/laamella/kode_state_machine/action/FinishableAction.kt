package com.laamella.kode_state_machine.action

import com.laamella.kode_state_machine.Action
import com.laamella.kode_state_machine.Condition

/**
 * An action which finishes at some time in the future. A transition can wait
 * for the action to be finished by using the isFinished condition.
 */
interface FinishableAction<E> : Action {
    val isFinished: Condition<E>
}
