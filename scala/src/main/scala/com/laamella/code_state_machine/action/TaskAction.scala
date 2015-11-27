package com.laamella.code_state_machine.action

import java.lang.Thread.State

import com.laamella.code_state_machine.NonEventBasedCondition

/**
 * This action starts a separate work thread with user code. A transition can
 * wait for this work to be finished by using the isFinished condition.
 *
 * @tparam Event event type.
 */
// TODO test
abstract class TaskAction[Event] extends Runnable with FinishableAction[Event] {
  private var taskThread: Thread = _
  private var finishedCondition: NonEventBasedCondition[Event] = _

  override def apply() {
    taskThread = new Thread(this)
    finishedCondition = new NonEventBasedCondition[Event]() {
      override def evaluate = taskThread.getState == State.TERMINATED
    }
    taskThread.start()
  }

  override def finished = finishedCondition
}
