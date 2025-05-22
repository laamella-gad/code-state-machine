package com.laamella.kode_state_machine

import com.laamella.kode_state_machine.condition.NonEventBasedCondition
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.Thread.State.TERMINATED

private val logger = KotlinLogging.logger {}

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

/**
 * An action which finishes at some time in the future. A transition can wait
 * for the action to be finished by using the isFinished condition.
 */
interface FinishableAction<E> : Action {
    val isFinished: Condition<E>
}

class NoAction: Action {
    override fun execute() {
    }
}
fun log(logText: String): Action {
    return LogAction(logText)
}

/**
 * This action logs a line.
 */
class LogAction(private val logText: String) : Action {
    override fun execute() {
        logger.trace { logText }
    }

    override fun toString(): String {
        return "log ($logText)"
    }
}

/**
 * This action starts a separate work thread with user code. A transition can
 * wait for this work to be finished by using the isFinished condition.
 *
 * @param E event type.
 */
// TODO test
abstract class TaskAction<E> : Runnable, FinishableAction<E> {
    private var finishedCondition: NonEventBasedCondition<E>? = null

    override fun execute() {
        // TODO use coroutines
        val taskThread = Thread(this)
        finishedCondition = object : NonEventBasedCondition<E>() {
            override val isMet: Boolean
                get() = taskThread.state == TERMINATED
        }
        taskThread.start()
    }

    override val isFinished: Condition<E>
        get() = finishedCondition!!
}
