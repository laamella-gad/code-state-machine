package com.laamella.code_state_machine

import org.scalatest.Assertions._

import scala.collection.mutable

object StateMachineAssert {
  def assertActive[State, Event, Priority <: Ordered[Priority]](machine: StateMachine[State, Event, Priority], expectedStates: State*) {
    for (expectedState <- expectedStates) {
      if (!machine.active(expectedState)) {
        fail(s"Expected $expectedState to be active.")
      }
    }
    val expectedStatesSet = mutable.HashSet[State](expectedStates: _*)
    for (actualState <- machine.activeStates) {
      if (!expectedStatesSet.contains(actualState)) {
        fail(s"$actualState was active, but not expected.")
      }
    }
  }
}
