package com.laamella.code_state_machine.util

sealed trait SimpleEvent {
  override def toString = getClass.getSimpleName
}

object X extends SimpleEvent

object Y extends SimpleEvent

object Z extends SimpleEvent
