package com.laamella.code_state_machine

import org.junit.Assert
import scala.collection.mutable

object StateMachineAssert {
  def assertActive[T, E, P <: Ordered[P]](machine: StateMachine[T, E, P], expectedStates: T*) {
    for (expectedState <- expectedStates) {
      if (!machine.isActive(expectedState)) {
        Assert.fail("Expected " + expectedState + " to be active.")
      }
    }
    val expectedStatesSet = mutable.HashSet[T](expectedStates: _*)
    for (actualState <- machine.getActiveStates) {
      if (!expectedStatesSet.contains(actualState)) {
        Assert.fail(s"$actualState was active, but not expected.")
      }
    }
  }
}
