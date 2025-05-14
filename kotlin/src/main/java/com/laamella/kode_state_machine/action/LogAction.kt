package com.laamella.kode_state_machine.action

import com.laamella.kode_state_machine.Action
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This action logs a line.
 */
class LogAction(private val logText: String) : Action {
    override fun execute() {
        log.trace(logText)
    }

    override fun toString(): String {
        return "log ($logText)"
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(LogAction::class.java)
    }
}