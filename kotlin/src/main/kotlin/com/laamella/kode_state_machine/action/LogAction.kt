package com.laamella.kode_state_machine.action

import com.laamella.kode_state_machine.Action
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

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