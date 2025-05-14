package com.laamella.kode_state_machine

/**
 * Action that appends some text to a stringbuffer, so the stringbuffer contains
 * a log of actions.
 */
class TraceAction(private val trace: StringBuffer, private val signature: String) : Action {
    override fun execute() {
        trace.append(signature)
    }
}
