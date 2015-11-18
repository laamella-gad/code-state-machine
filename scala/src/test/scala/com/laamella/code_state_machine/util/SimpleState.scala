package com.laamella.code_state_machine.util

sealed trait SimpleState {
  override def toString = getClass.getSimpleName
}

object A extends SimpleState

object B extends SimpleState

object C extends SimpleState

object D extends SimpleState

object E extends SimpleState