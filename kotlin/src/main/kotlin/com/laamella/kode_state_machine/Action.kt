package com.laamella.kode_state_machine

/**
 * Any kind of user defined code that is executed when a certain event is
 * received.
 */
fun interface Action {
    /**
     * User code.
     */
    fun execute()
}
