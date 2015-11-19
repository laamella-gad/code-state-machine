package com.laamella.code_state_machine

/**
 * Any kind of user defined code that is executed when a certain event is
 * received.
 */
trait Action extends (() => Unit) {}
