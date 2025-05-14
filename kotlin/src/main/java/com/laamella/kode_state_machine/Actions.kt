package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.util.Chain

/**
 * A simple wrapper around a list of actions.
 */
class Actions(vararg actions: Action) : Chain<Action>(*actions) {
    fun execute() {
        for (action in getItems()) {
            action.execute()
        }
    }
}
