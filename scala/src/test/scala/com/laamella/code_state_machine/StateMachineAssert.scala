package com.laamella.code_state_machine

import org.scalatest.Assertions._

import scala.collection.mutable

object StateMachineAssert {
  def assertActive[T, E, P <: Ordered[P]](machine: StateMachine[T, E, P], expectedStates: T*) {
    for (expectedState <- expectedStates) {
      if (!machine.active(expectedState)) {
        fail(s"Expected $expectedState to be active.")
      }
    }
    val expectedStatesSet = mutable.HashSet[T](expectedStates: _*)
    for (actualState <- machine.activeStates) {
      if (!expectedStatesSet.contains(actualState)) {
        fail(s"$actualState was active, but not expected.")
      }
    }
  }
}
