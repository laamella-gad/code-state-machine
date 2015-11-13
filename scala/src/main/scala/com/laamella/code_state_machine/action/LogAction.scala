package com.laamella.code_state_machine.action

import com.laamella.code_state_machine.Action
import org.slf4j.LoggerFactory

/**
 * This action logs a line.
 */
final class LogAction(logText: String) extends Action {
  val log = LoggerFactory.getLogger("LogAction")

  override def execute() = log.trace(logText)

  override def toString = s"log ($logText)"
}