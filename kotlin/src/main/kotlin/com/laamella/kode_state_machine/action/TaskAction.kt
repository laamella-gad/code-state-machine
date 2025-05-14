package com.laamella.kode_state_machine.action

import com.laamella.kode_state_machine.Condition
import com.laamella.kode_state_machine.condition.NonEventBasedCondition

/**
 * This action starts a separate work thread with user code. A transition can
 * wait for this work to be finished by using the isFinished condition.
 *
 * @param <E> event type.
 */
// TODO test
abstract class TaskAction<E> : Runnable, FinishableAction<E> {
    private var taskThread: Thread? = null
    private var finishedCondition: NonEventBasedCondition<E>? = null

    override fun execute() {
        taskThread = Thread(this)
        finishedCondition = object : NonEventBasedCondition<E>() {
            override val isMet: Boolean
                get() = taskThread!!.getState() == Thread.State.TERMINATED
        }
        taskThread!!.start()
    }

    override val isFinished: Condition<E>
        get() = finishedCondition!!
}
