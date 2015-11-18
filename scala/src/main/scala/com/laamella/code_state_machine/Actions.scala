package com.laamella.code_state_machine

import com.laamella.code_state_machine.util.Chain

import scala.collection.mutable.ListBuffer

/**
 * A simple wrapper around a list of actions.
 */
class Actions(items: ListBuffer[Action]) extends Chain[Action](items) {
  def this(actions: Action*) = this(new ListBuffer ++= actions.toTraversable)

  def execute() {
    getItems.foreach {
      _.execute()
    }
  }
}

object Actions {
  val None = new Actions()
}